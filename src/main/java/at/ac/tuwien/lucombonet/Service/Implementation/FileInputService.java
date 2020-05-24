package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.Page;
import at.ac.tuwien.lucombonet.Entity.Wiki;
import at.ac.tuwien.lucombonet.Service.IFileInputService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@Service
public class FileInputService implements IFileInputService {

    IndexWriter writer;
    Directory indexDirectory;
    Analyzer analyzer;

    public FileInputService() throws IOException {
        indexDirectory = FSDirectory.open(Paths.get("")); //Path to directory
        analyzer = new GermanAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setSimilarity(new BM25Similarity(1.2f, 0.75f));
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(indexDirectory, iwc);
    }

    @Override
    public String createIndex() throws IOException {

        File f = new File("2000xml.xml");
        if(f.exists()) {
            XmlMapper xmlMapper = new XmlMapper();
            String readContent = new String(Files.readAllBytes(Paths.get("2000xml.xml")));
            Wiki wiki = xmlMapper.readValue(readContent, Wiki.class);
            System.out.println("number of pages: "+wiki.getPages().size());
            for(Page page: wiki.getPages())    {
                indexPageLucene(page);
            }
            close();
            return "successful";
        }
        throw new FileNotFoundException();
    }

    public void close() throws IOException {
        writer.close();
    }

    private void indexPageLucene(Page page) throws IOException {
        System.out.println("Indexing: "+page.getTitle());
        Document document = getDocumentLucene(page);
        writer.addDocument(document);
    }

    private Document getDocumentLucene(Page page) throws IOException {
        Document document = new Document();
        document.add(new TextField("content", page.getRevision().getContent(), Field.Store.YES));
        document.add(new TextField("title", page.getTitle(), TextField.Store.YES));
        return document;
    }

    public String searchLucene(String query, int resultnumber) throws IOException, ParseException {
        MultiFieldQueryParser q = new MultiFieldQueryParser(new String[] {"title","content"}, analyzer);


        int hitsPerPage = resultnumber > 0 ? resultnumber : 10;
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1.2f, 0.75f));
        TopDocs docs = searcher.search(q.parse(query), hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        String result = "";
        result += ("Found " + hits.length + " hits." + "\n");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            result += ((i + 1) + ". " + "\t" + d.get("title")+ " :"+ "score" /*TODO*/+ "\n");
        }
        return result;
    }

    private void indexPageMonet(Page page) {

    }





}
