package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.Dictionary;
import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.DocTerms;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Entity.XML.Page;
import at.ac.tuwien.lucombonet.Entity.XML.Wiki;
import at.ac.tuwien.lucombonet.Repository.DictionaryRepository;
import at.ac.tuwien.lucombonet.Repository.DocTermRepository;
import at.ac.tuwien.lucombonet.Repository.DocumentRepository;
import at.ac.tuwien.lucombonet.Repository.QueryRepository;
import at.ac.tuwien.lucombonet.Repository.VersionRepository;
import at.ac.tuwien.lucombonet.Service.IFileService;
import at.ac.tuwien.lucombonet.Service.ISearchService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;


@Service
public class FileService implements IFileService {

    private final String DOCNAME = "testxml.xml";
    //private final String DOCNAME = "30xml.xml";

    DocumentRepository documentRepository;
    DictionaryRepository dictionaryRepository;
    DocTermRepository docTermRepository;
    QueryRepository queryRepository;
    VersionRepository versionRepository;
    SmallFloat smallFloat;
    ISearchService searchService;
    LuceneConfig luceneConfig;

    @Autowired
    public FileService(DocumentRepository documentRepository,
                       DictionaryRepository dictionaryRepository,
                       DocTermRepository docTermRepository,
                       VersionRepository versionRepository,
                       QueryRepository queryRepository,
                       SmallFloat smallFloat,
                       ISearchService searchService,
                       LuceneConfig luceneConfig) {
        this.docTermRepository = docTermRepository;
        this.documentRepository = documentRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.versionRepository = versionRepository;
        this.queryRepository = queryRepository;
        this.smallFloat = smallFloat;
        this.searchService = searchService;
        this.luceneConfig = luceneConfig;
    }

    @Override
    public String createIndex() throws IOException, ParseException {
        luceneConfig.open();
        File f = new File(DOCNAME);
        if(f.exists()) {
            XmlMapper xmlMapper = new XmlMapper();
            String readContent = new String(Files.readAllBytes(Paths.get(DOCNAME)));
            Wiki wiki = xmlMapper.readValue(readContent, Wiki.class);
            System.out.println("number of pages: "+wiki.getPages().size());
            for(Page page: wiki.getPages())    {
                indexPageLucene(page);
            }
            luceneConfig.close();
            indexMariaDB();
            return "successful";
        }
        System.out.println("error");
        throw new FileNotFoundException();
    }



    private void indexPageLucene(Page page) throws IOException, ParseException {
        //Check if Page is already in the Index, then only flag as delete and save new document
        if(DirectoryReader.indexExists(luceneConfig.getIndexDirectory())) {
            luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
            System.out.println("trying to delete " + page.getTitle());
            Query query = new TermQuery(new Term(""+(page.getTitle().hashCode())));
            BooleanQuery q = new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).build();
            System.out.println(searchService.searchLuceneTitleHash(page.getTitle()).size());
            luceneConfig.getWriter().deleteDocuments(q); //???
        }
        Document document = getDocumentLucene(page);
        System.out.println("Add " + document.getField("title").stringValue());
        luceneConfig.getWriter().addDocument(document);
    }

    private Document getDocumentLucene(Page page) {
        Document document = new Document();
        FieldType ft = new FieldType(TextField.TYPE_STORED);
        ft.setStoreTermVectors(true);
        ft.setStored(true);
        document.add(new Field("content", page.getRevision().getContent(), ft));
        document.add(new Field("title", page.getTitle(), ft));
        document.add(new Field("hash", ""+page.getTitle().hashCode(), ft));
        return document;
    }

    /**
     * Initialize the index at the first start
     * @throws IOException
     */
    private void indexMariaDB() throws IOException {
        luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
        luceneConfig.setSearcher(new IndexSearcher(luceneConfig.getReader()));
        Version v = versionRepository.save(Version.builder().timestamp(new Timestamp(System.currentTimeMillis())).build());
        for(int i = 0; i < luceneConfig.getReader().maxDoc(); i++) {
            Document doc = luceneConfig.getReader().document(i);
            System.out.println("DB - Processing file number : "+ (i+1) + " von "+ luceneConfig.getReader().maxDoc() + ", docId: "+doc.get("id") + ", " + doc.getField("title").stringValue());
            Terms termVector = luceneConfig.getSearcher().getIndexReader().getTermVector(i, "content");
            Long length = termVector.getSumTotalTermFreq();
            Long approxLength = (long)smallFloat.byte4ToInt(smallFloat.intToByte4(Integer.parseInt(length.toString())));
            String title = doc.getField("title").stringValue();
            String hash = doc.getField("hash").stringValue();
            Doc d = documentRepository.findByHash(hash);
            if(d != null) {
                System.out.println("marked as deleted");
                d.setRemoved(v);
                documentRepository.save(d);
            }
            Doc dc = Doc.builder().name(title).length(length).approximatedLength(approxLength).added(v).hash(hash).build();
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
       //d.setRemoved(true);
       documentRepository.save(d);
    }

    private void insertDocument(Document doc, Terms terms, Version version) {

    }

    private boolean docDBexists(String title) {
        return false;
    }

    private boolean docLucExists(String title) {
        return false;
    }



}
