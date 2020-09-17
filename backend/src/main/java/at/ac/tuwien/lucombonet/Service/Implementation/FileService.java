package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.*;
import at.ac.tuwien.lucombonet.Entity.Dictionary;
import at.ac.tuwien.lucombonet.Entity.XML.Page;
import at.ac.tuwien.lucombonet.Entity.XML.Wiki;
import at.ac.tuwien.lucombonet.Persistence.IDictionaryDao;
import at.ac.tuwien.lucombonet.Persistence.IDocTermDao;
import at.ac.tuwien.lucombonet.Persistence.IDocumentDao;
import at.ac.tuwien.lucombonet.Persistence.IVersionDao;
import at.ac.tuwien.lucombonet.Service.IFileService;
import at.ac.tuwien.lucombonet.Service.ISearchService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class FileService implements IFileService {

    SmallFloat smallFloat;
    ISearchService searchService;
    LuceneConfig luceneConfig;
    IVersionDao versionDao;
    IDictionaryDao dictionaryDao;
    IDocumentDao documentDao;
    IDocTermDao docTermDao;
    private static final int batchSize = 20000;
    private int batchcounter=0;
    private Timestamp readingStart;
    private Timestamp luceneIndexingStart;
    private Timestamp luceneIndexingEnd;
    private Timestamp MonetDBIndexingEnd;

    HashSet<Dictionary> terms = new HashSet<>();
    List<DocTermTemp> docTermsTemp = new ArrayList<>();

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
        readingStart = new Timestamp(System.currentTimeMillis());
        File f = new File(docname);
        HashSet<String> newHashes = new HashSet<>();
        if(f.exists()) {
            XmlMapper xmlMapper = new XmlMapper();
            luceneConfig.open();
            String readContent = Files.readString(Paths.get(docname));
            Wiki wiki = xmlMapper.readValue(readContent.toString(), Wiki.class);
            System.out.println("number of pages: "+wiki.getPages().size());
            luceneIndexingStart = new Timestamp((System.currentTimeMillis()));
            for(Page page: wiki.getPages()) {
                newHashes.add(indexPageLucene(page));
            }
            luceneConfig.close();
            System.out.println("Index Lucene finished");
            luceneIndexingEnd = new Timestamp(System.currentTimeMillis());
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
            //System.out.println("trying to delete " + page.getTitle() + " - " + page.getTitle().hashCode());

            QueryParser q = new QueryParser("hash", luceneConfig.getAnalyzer());
            luceneConfig.getWriter().deleteDocuments(q.parse(QueryParser.escape(page.getTitle().hashCode()+"")));

            //List<SearchResultInt> results = searchService.searchLuceneTitleHash(page.getTitle().hashCode()+"");
        }
        Document document = getDocumentLucene(page);
        //System.out.println("Add " + document.getField("title").stringValue());
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
    private void indexMariaDB(HashSet<String> hashes) throws IOException {
        luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
        luceneConfig.setSearcher(new IndexSearcher(luceneConfig.getReader()));
        Version v = versionDao.save(Version.builder().timestamp(new Timestamp(System.currentTimeMillis())).build());
        BufferedWriter writer = new BufferedWriter(new FileWriter("doc.txt"));
        Long index = documentDao.getMaxId();
        int count = 0;
        for(int i = 0; i < luceneConfig.getReader().maxDoc(); i++) {
            Document doc = luceneConfig.getReader().document(i);
            if(hashes.contains(doc.getField("hash").stringValue())) {
                Terms termVector = luceneConfig.getSearcher().getIndexReader().getTermVector(i, "content");
                Long length = 0L;
                if(termVector != null) {
                    length = termVector.getSumTotalTermFreq();
                }
                Long approxLength = (long) smallFloat.byte4ToInt(smallFloat.intToByte4(Integer.parseInt(length.toString().trim())));
                String title = doc.getField("title").stringValue();
                String hash = doc.getField("hash").stringValue();
                Doc d = documentDao.findByHash(hash);
                if (d != null) {
                    documentDao.markAsDeleted(d, v);
                }
                Doc dc = Doc.builder().id(++index).name(title).length(length).approximatedLength(approxLength).added(v).hash(hash).build();
                writer.write((index) + "|"+dc.getApproximatedLength() + "|"+ dc.getHash() +"|"+ dc.getLength() +"|"+ dc.getName() +"|" +dc.getAdded().getId() + "|" +null +"\n");
                if (termVector != null) {
                    addToBatch(dc, termVector);
                }
                count++;
                if(count == batchSize) {
                    writer.close();
                    File f = new File("doc.txt");
                    String filename = f.getAbsolutePath().replace("\\", "\\\\");
                    documentDao.saveAll("\'"+filename+"\'");
                    f.delete();
                    addTermsToDB();
                    count = 0;
                    writer = new BufferedWriter(new FileWriter("doc.txt"));
                }
            }
        }
        //add the last elements;
        writer.close();
        File f = new File("doc.txt");
        String filename = f.getAbsolutePath().replace("\\", "\\\\");
        documentDao.saveAll("\'"+filename+"\'");
        f.delete();
        addTermsToDB();
        MonetDBIndexingEnd = new Timestamp(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder();
        sb.append("reading start: " + readingStart.toLocalDateTime().toString() +"\n");
        sb.append("lucene start: " + luceneIndexingStart.toLocalDateTime().toString()+"\n");
        sb.append("lucene end: " + luceneIndexingEnd.toLocalDateTime().toString()+"\n");
        sb.append("lucene time: " + (luceneIndexingEnd.getTime() - luceneIndexingStart.getTime())+"\n");
        sb.append("monetdb end: " + MonetDBIndexingEnd.toLocalDateTime().toString()+"\n");
        sb.append("monetdb time: " + (MonetDBIndexingEnd.getTime() - luceneIndexingEnd.getTime())+"\n\n");
        Files.write(Paths.get("results/indexing.txt"), sb.toString().getBytes(), StandardOpenOption.APPEND);
    }

    private void addToBatch(Doc dc, Terms termVector) throws IOException {
        BytesRef term = null;
        TermsEnum  itr = termVector.iterator();
        while((term = itr.next()) != null) {
            terms.add(Dictionary.builder().term(term.utf8ToString()).build());
            docTermsTemp.add(DocTermTemp.builder().term(term.utf8ToString()).document(dc).termFrequency(itr.totalTermFreq()).build());
        }
    }

    private void addTermsToDB() throws IOException {
        Set<String> dics = dictionaryDao.getAll().parallelStream().map(dictionary -> dictionary.getTerm()).collect(Collectors.toSet());
        List<Dictionary> dicUpdated = terms.parallelStream()
                .filter(d ->!dics.contains(d.getTerm()))
                .collect(Collectors.toList());
        if(dicUpdated.size() > 1) {
            BufferedWriter writer = new BufferedWriter(new FileWriter("dictionaries.txt"));
            Long i = dictionaryDao.getMaxId();

            for(Dictionary dictionary : dicUpdated) {
                try {
                    writer.write((++i) + "|"+dictionary.getTerm()+"\n");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writer.close();
            File f = new File("dictionaries.txt");
            String filename = f.getAbsolutePath().replace("\\", "\\\\");
            dictionaryDao.saveAll("\'"+filename+"\'");
            f.delete();
        }
        HashMap<String, Dictionary> dicMap = dictionaryDao.getAllMap();

        BufferedWriter writer = new BufferedWriter(new FileWriter("docterms.txt"));
        docTermsTemp.parallelStream().forEach(d -> {
            if(dicMap.get(d.getTerm()) != null ) {
                try {
                    writer.write(d.getTermFrequency()  +"|"+d.getDocument().getId()+ "|" +dicMap.get(d.getTerm()).getId() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        writer.close();
        File f = new File("docterms.txt");
        String filename = f.getAbsolutePath().replace("\\", "\\\\");
        docTermDao.saveAll("\'"+filename+"\'");
        f.delete();
        terms.clear();
        docTermsTemp.clear();
    }



}
