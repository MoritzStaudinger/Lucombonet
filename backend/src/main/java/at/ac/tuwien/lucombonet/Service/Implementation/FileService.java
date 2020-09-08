package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.*;
import at.ac.tuwien.lucombonet.Entity.XML.Page;
import at.ac.tuwien.lucombonet.Entity.XML.Wiki;
import at.ac.tuwien.lucombonet.Persistence.IDictionaryDao;
import at.ac.tuwien.lucombonet.Persistence.IDocTermDao;
import at.ac.tuwien.lucombonet.Persistence.IDocumentDao;
import at.ac.tuwien.lucombonet.Persistence.IVersionDao;
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
import org.apache.lucene.queryparser.classic.QueryParser;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;


@Service
public class FileService implements IFileService {

    SmallFloat smallFloat;
    ISearchService searchService;
    LuceneConfig luceneConfig;
    IVersionDao versionDao;
    IDictionaryDao dictionaryDao;
    IDocumentDao documentDao;
    IDocTermDao docTermDao;

    @Autowired
    public FileService(
                       SmallFloat smallFloat,
                       ISearchService searchService,
                       LuceneConfig luceneConfig,
                       IVersionDao versionDao,
                       IDictionaryDao dictionaryDao,
                       IDocumentDao documentDao,
                       IDocTermDao docTermDao) {
        this.smallFloat = smallFloat;
        this.searchService = searchService;
        this.luceneConfig = luceneConfig;
        this.versionDao = versionDao;
        this.dictionaryDao = dictionaryDao;
        this.documentDao = documentDao;
        this.docTermDao = docTermDao;
    }

    @Override
    public String createIndex(String docname) throws IOException, ParseException {
        luceneConfig.open();
        File f = new File(docname);
        if(f.exists()) {
            XmlMapper xmlMapper = new XmlMapper();
            String readContent = Files.readString(Paths.get(docname));
            Wiki wiki = xmlMapper.readValue(readContent, Wiki.class);
            System.out.println("number of pages: "+wiki.getPages().size());
            List<String> newHashes = new ArrayList<>();
            for(Page page: wiki.getPages())    {
                newHashes.add(indexPageLucene(page));
            }
            luceneConfig.close();
            indexMariaDB(newHashes);
            return "successful";
        }
        System.out.println("error");
        throw new FileNotFoundException();
    }



    private String indexPageLucene(Page page) throws IOException, ParseException {
        //Check if Page is already in the Index, then only flag as delete and save new document
        if(DirectoryReader.indexExists(luceneConfig.getIndexDirectory())) {
            luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
            System.out.println("trying to delete " + page.getTitle() + " - " + page.getTitle().hashCode());

            QueryParser q = new QueryParser("hash", luceneConfig.getAnalyzer()); // only on content for reproducibility
            luceneConfig.getWriter().deleteDocuments(q.parse(QueryParser.escape(page.getTitle().hashCode()+"")));

            //List<SearchResultInt> results = searchService.searchLuceneTitleHash(page.getTitle().hashCode()+"");
            //???
        }
        Document document = getDocumentLucene(page);
        System.out.println("Add " + document.getField("title").stringValue());
        luceneConfig.getWriter().addDocument(document);
        return document.getField("hash").stringValue();
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
    private void indexMariaDB(List<String> hashes) throws IOException {
        luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
        luceneConfig.setSearcher(new IndexSearcher(luceneConfig.getReader()));
        Version v = versionDao.save(Version.builder().timestamp(new Timestamp(System.currentTimeMillis())).build());
        for(int i = 0; i < luceneConfig.getReader().maxDoc(); i++) {
            Document doc = luceneConfig.getReader().document(i);
            if(hashes.contains(doc.getField("hash").stringValue())) {
                System.out.println("DB - Processing file " + doc.getField("title").stringValue());
                Terms termVector = luceneConfig.getSearcher().getIndexReader().getTermVector(i, "content");
                Long length = termVector.getSumTotalTermFreq();
                Long approxLength = (long) smallFloat.byte4ToInt(smallFloat.intToByte4(Integer.parseInt(length.toString().trim())));
                String title = doc.getField("title").stringValue();
                String hash = doc.getField("hash").stringValue();
                Doc d = documentDao.findByHash(hash);
                if (d != null) {
                    System.out.println("marked as deleted");
                    documentDao.markAsDeleted(d, v);
                }
                Doc dc = Doc.builder().name(title).length(length).approximatedLength(approxLength).added(v).hash(hash).build();
                dc = documentDao.save(dc);
                if (termVector != null) {
                    addTermsToDB(termVector, dc);
                }
            }
        }
    }

    private void addTermsToDB(Terms terms, Doc dc) throws IOException {
        TermsEnum itr = terms.iterator();
        BytesRef term = null;

        List<Dictionary> dicTerms = new ArrayList<>();
        List<DocTermTemp> docTermTemps = new ArrayList<>();
        while((term = itr.next()) != null) {
            docTermTemps.add(DocTermTemp.builder().term(term.utf8ToString()).termFrequency(itr.totalTermFreq()).build());
            dicTerms.add(Dictionary.builder().term(term.utf8ToString()).build());
        }
        List<Dictionary> dics = dictionaryDao.getAll();
        List<Dictionary> finalDics = dics;
        List<Dictionary> dicUpdated = dicTerms.stream()
                .filter(d ->finalDics.stream().noneMatch(x ->x.getTerm().equals(d.getTerm())))
                .collect(Collectors.toList());
        dictionaryDao.saveAll(dicUpdated);
        dics = dictionaryDao.getAll();

        List<DocTerms> docterms = new ArrayList<>();
        for (DocTermTemp d : docTermTemps) {
            Dictionary dic = dics.stream().filter(di -> di.getTerm().equals(d.getTerm())).findFirst().get();
            docterms.add(DocTerms.builder().dictionary(dic).document(dc).termFrequency(d.getTermFrequency()).build());
        }
        docTermDao.saveAll(docterms);
    }



}
