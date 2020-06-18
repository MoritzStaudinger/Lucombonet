package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Entity.Dictionary;
import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.DocTerms;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.QueryTable;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Entity.XML.Page;
import at.ac.tuwien.lucombonet.Entity.XML.Wiki;
import at.ac.tuwien.lucombonet.Repository.DictionaryRepository;
import at.ac.tuwien.lucombonet.Repository.DocTermRepository;
import at.ac.tuwien.lucombonet.Repository.DocumentRepository;
import at.ac.tuwien.lucombonet.Repository.QueryRepository;
import at.ac.tuwien.lucombonet.Repository.VersionRepository;
import at.ac.tuwien.lucombonet.Service.IFileInputService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class FileInputService implements IFileInputService {

    //private final String DOCNAME = "testxml.xml";
    private final String DOCNAME = "30xml.xml";

    DocumentRepository documentRepository;
    DictionaryRepository dictionaryRepository;
    DocTermRepository docTermRepository;
    QueryRepository queryRepository;
    VersionRepository versionRepository;
    SmallFloat smallFloat;

    IndexWriter writer;
    IndexReader reader;
    Directory indexDirectory;
    Analyzer analyzer;
    IndexSearcher searcher;
    BM25Similarity bm;

    @Autowired
    public FileInputService(DocumentRepository documentRepository,
                            DictionaryRepository dictionaryRepository,
                            DocTermRepository docTermRepository,
                            VersionRepository versionRepository,
                            QueryRepository queryRepository,
                            SmallFloat smallFloat) throws IOException {
        this.docTermRepository = docTermRepository;
        this.documentRepository = documentRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.versionRepository = versionRepository;
        this.queryRepository = queryRepository;
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
    public String createIndex() throws IOException, ParseException {

        File f = new File(DOCNAME);
        if(f.exists()) {
            XmlMapper xmlMapper = new XmlMapper();
            String readContent = new String(Files.readAllBytes(Paths.get(DOCNAME)));
            Wiki wiki = xmlMapper.readValue(readContent, Wiki.class);
            System.out.println("number of pages: "+wiki.getPages().size());
            for(Page page: wiki.getPages())    {
                indexPageLucene(page);
            }
            close();
            //indexMariaDB();
            return "successful";
        }
        System.out.println("error");
        throw new FileNotFoundException();
    }

    public void close() throws IOException {
        writer.close();
    }


    private void indexPageLucene(Page page) throws IOException, ParseException {
        //Check if Page is already in the Index, then only flag as delete and save new document
        if(DirectoryReader.indexExists(indexDirectory)) {
            reader = DirectoryReader.open(indexDirectory);
            System.out.println("trying to delete " + page.getTitle());
            QueryParser q = new QueryParser("title", analyzer);
            writer.deleteDocuments(q.parse(QueryParser.escape(page.getTitle()))); //???
        }
        Document document = getDocumentLucene(page);
        System.out.println("Add " + document.getField("title").stringValue());
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

    public List<SearchResultInt> searchLucene(String query, int resultnumber) throws IOException, ParseException {
        reader = DirectoryReader.open(indexDirectory);
        searcher = new IndexSearcher(reader);
        //MultiFieldQueryParser q = new MultiFieldQueryParser(new String[] {"title","content"}, analyzer);
        QueryParser q = new QueryParser("content", analyzer); // only on content for reproducibility
        int hitsPerPage = resultnumber > 0 ? resultnumber : 10;
        BM25Similarity bm = new BM25Similarity(1.2f, 0.75f);
        searcher.setSimilarity(bm);

        TopDocs docs = searcher.search(q.parse(query), hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        List<SearchResultInt> results = new ArrayList<>();
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(searcher.explain(q.parse(query), i));
            results.add(SearchResult.builder().name(d.get("title")).score((double)hits[i].score).build());
        }
        return results;
    }

    @Override
    public List<SearchResultInt> searchMariaDB(String query, int resultnumber) throws ParseException {
        QueryParser q = new QueryParser("", analyzer);
        List<String> strings = Arrays.stream(q.parse(query).toString().split(" ")).sorted().collect(Collectors.toList());
        List<SearchResultInt> a = documentRepository.findByTermsBM25(strings);
        return a;
    }

    @Override
    public List<SearchResultInt> search(String searchstring, Integer resultnumber, Version version) throws ParseException, IOException {
        QueryParser q = new QueryParser("", analyzer);
        List<String> strings = Arrays.stream(q.parse(searchstring).toString().split(" ")).sorted().collect(Collectors.toList());

        if(queryRepository.existsQueryTableByQuery(strings.toString())) {
            System.out.println("mariaDB");
            return searchMariaDB(searchstring, resultnumber);
        } else {
            queryRepository.save(QueryTable.builder().query(strings.toString()).build());
            System.out.println("Lucene");
            return searchLucene(searchstring, resultnumber);
        }
    }

    /**
     * Initialize the index at the first start
     * @throws IOException
     */
    private void indexMariaDB() throws IOException {
        reader = DirectoryReader.open(indexDirectory);
        searcher = new IndexSearcher(reader);
        Version v = versionRepository.save(Version.builder().timestamp(new Timestamp(System.currentTimeMillis())).build());
        for(int i = 0; i < reader.maxDoc(); i++) {
            Document doc = reader.document(i);
            System.out.println("Processing file number : "+ i + " von "+ reader.maxDoc() + ", docId: "+doc.get("id") + ", " + doc.getField("title").stringValue());
            Terms termVector = searcher.getIndexReader().getTermVector(i, "content");
            Long length = termVector.getSumTotalTermFreq();
            Long approxLength = (long)smallFloat.byte4ToInt(smallFloat.intToByte4(Integer.parseInt(length.toString())));
            String title = doc.getField("title").stringValue();
            Doc dc = Doc.builder().name(title).length(length).approximatedLength(approxLength).version(v).build();
            dc = documentRepository.save(dc);
            if(termVector != null) {
                addTermsToDB(termVector,dc);
            }
        }
    }

    private void addTermsToDB(Terms terms, Doc dc) throws IOException {
        TermsEnum itr = terms.iterator();
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

    private void removeMariaDBDocument(Long docID) {
       Doc d =  documentRepository.getOne(docID);
       d.setRemoved(true);
       documentRepository.save(d);
    }

    private void insertDocument(Document doc, Terms terms, Version version) {

    }



}
