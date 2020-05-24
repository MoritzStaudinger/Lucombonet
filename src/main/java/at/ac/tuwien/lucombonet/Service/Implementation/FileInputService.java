package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.Page;
import at.ac.tuwien.lucombonet.Entity.Term;
import at.ac.tuwien.lucombonet.Entity.Wiki;
import at.ac.tuwien.lucombonet.Repository.TermRepository;
import at.ac.tuwien.lucombonet.Service.IFileInputService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@Service
public class FileInputService implements IFileInputService {

    private final TermRepository termRepository;
    IndexWriter writer;
    Directory indexDirectory;
    StandardAnalyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

    @Autowired
    public FileInputService(TermRepository termRepository) throws IOException {
        this.termRepository = termRepository;
        indexDirectory = FSDirectory.open(Paths.get("")); //Path to directory
        writer = new IndexWriter(indexDirectory, iwc);
    }

    @Override
    public String createIndex() throws IOException {

        File f = new File("2000xml.xml");
        if(f.exists()) {
            System.out.println("true");
            XmlMapper xmlMapper = new XmlMapper();
            try {
                String readContent = new String(Files.readAllBytes(Paths.get("2000xml.xml")));
                Wiki p = xmlMapper.readValue(readContent, Wiki.class);
                System.out.println(p.toString());
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        indexFile(f);
        return "ok";
    }

    public void close() throws IOException {
        writer.close();
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    private Document getDocument(File file) throws IOException {
        Document document = new Document();

        TextField contentField = new TextField("content", new FileReader(file));
        TextField fileNameField = new TextField("filename",
                file.getName(), TextField.Store.YES);
        TextField filePathField = new TextField("path",
                file.getCanonicalPath(),TextField.Store.YES);

        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);

        return document;
    }



}
