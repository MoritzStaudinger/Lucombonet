package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Entity.Dictionary;
import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.DocTerms;
import at.ac.tuwien.lucombonet.Entity.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.XML.Page;
import at.ac.tuwien.lucombonet.Entity.XML.Wiki;
import at.ac.tuwien.lucombonet.Repository.DictionaryRepository;
import at.ac.tuwien.lucombonet.Repository.DocTermRepository;
import at.ac.tuwien.lucombonet.Repository.DocumentRepository;
import at.ac.tuwien.lucombonet.Service.IFileInputService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.QueryCachingPolicy;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.UsageTrackingQueryCachingPolicy;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.client.solrj.io.stream.ParallelStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Service
public class FileInputService implements IFileInputService {

    DocumentRepository documentRepository;
    DictionaryRepository dictionaryRepository;
    DocTermRepository docTermRepository;
    SmallFloat smallFloat;

    IndexWriter writer;
    IndexReader reader;
    Directory indexDirectory;
    Analyzer analyzer;
    IndexSearcher searcher;
    BM25Similarity bm;

    @Autowired
    public FileInputService(DocumentRepository documentRepository, DictionaryRepository dictionaryRepository, DocTermRepository docTermRepository, SmallFloat smallFloat) throws IOException {
        this.docTermRepository = docTermRepository;
        this.documentRepository = documentRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.smallFloat = smallFloat;

        indexDirectory = FSDirectory.open(Paths.get("")); //Path to directory
        analyzer = new GermanAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        bm = new BM25Similarity(1.2f, 0.75f);
        bm.setDiscountOverlaps(false);
        iwc.setSimilarity(bm);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(indexDirectory, iwc);
    }

    @Override
    public String createIndex() throws IOException {

        File f = new File("30xml.xml");
        if(f.exists()) {
            XmlMapper xmlMapper = new XmlMapper();
            String readContent = new String(Files.readAllBytes(Paths.get("30xml.xml")));
            Wiki wiki = xmlMapper.readValue(readContent, Wiki.class);
            System.out.println("number of pages: "+wiki.getPages().size());
            for(Page page: wiki.getPages())    {
                indexPageLucene(page);
            }
            close();
            indexMariaDB();
            return "successful";
        }
        System.out.println("error");
        throw new FileNotFoundException();
    }

    public void close() throws IOException {
        writer.close();
    }

    private void indexPageLucene(Page page) throws IOException {
        //System.out.println("Indexing: "+page.getTitle());
        Document document = getDocumentLucene(page);
        writer.addDocument(document);
    }

    private Document getDocumentLucene(Page page) {
        Document document = new Document();
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setStoreTermVectors(true);
        ft.setStored(true);
        document.add(new Field("content", page.getRevision().getContent(), ft));
        document.add(new Field("title", page.getTitle(), ft));
        return document;
    }

    public List<SearchResult> searchLucene(String query, int resultnumber) throws IOException, ParseException {
        reader = DirectoryReader.open(indexDirectory);
        searcher = new IndexSearcher(reader);
        //MultiFieldQueryParser q = new MultiFieldQueryParser(new String[] {"title","content"}, analyzer);
        QueryParser q = new QueryParser("content", analyzer); // only on content for reproducibility
        int hitsPerPage = resultnumber > 0 ? resultnumber : 10;
        BM25Similarity bm = new BM25Similarity(1.2f, 0.75f);
        searcher.setSimilarity(bm);

        TopDocs docs = searcher.search(q.parse(query), hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        for(int i = 0; i < reader.maxDoc(); i++) {
            Explanation e = searcher.explain(q.parse(query), i);
            System.out.println(e.toString());
            System.out.println("Real Length: "+ searcher.getIndexReader().getTermVector(i, "content").getSumTotalTermFreq());

        }
        List<SearchResult> results = new ArrayList<>();
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            results.add(SearchResult.builder().name(d.get("title")).score(hits[i].score).build());
        }
        return results;
    }

    @Override
    public List<SearchResult> searchMariaDB(String query, int resultnumber) {
        String[] terms = query.split(" ");
        List<SearchResult> a = documentRepository.findBySingleTermBM25(query, query);
        return null;
    }

    private void indexMariaDB() throws IOException {
        reader = DirectoryReader.open(indexDirectory);
        searcher = new IndexSearcher(reader);
        for(int i = 0; i < reader.maxDoc(); i++) {
            Document doc = reader.document(i);
            System.out.println("Processing file number : "+ i + " von "+ reader.maxDoc() + ", docId: "+doc.get("id") + ", " + doc.getField("title").toString());
            Terms termVector = searcher.getIndexReader().getTermVector(i, "content");
            Long length = termVector.getSumTotalTermFreq();
            Long approxLength = (long)smallFloat.byte4ToInt(smallFloat.intToByte4(Integer.parseInt(length.toString())));
            String title = doc.getField("title").name();
            Doc dc = Doc.builder().name(title).length(length).approximatedLength(approxLength).build();
            dc = documentRepository.save(dc);
            if(termVector != null) {
                TermsEnum itr = termVector.iterator();
                BytesRef term = null;

                while((term = itr.next()) != null) {
                    String termText = term.utf8ToString();
                    Term termInstance = new Term("content", term);
                    Dictionary d = null;
                    if( (d = dictionaryRepository.findByTerm(termText)) == null) {
                        Dictionary dic = Dictionary.builder().term(termText).build();
                        d = dictionaryRepository.save(dic);
                        // System.out.println(d.toString());
                    }
                    DocTerms dt = DocTerms.builder().id(DocTerms.DocTermsKey.builder().dictionary(d).document(dc).build()).termFrequency(itr.totalTermFreq()).build();
                    docTermRepository.save(dt);
                }
            }
        }
    }



}
