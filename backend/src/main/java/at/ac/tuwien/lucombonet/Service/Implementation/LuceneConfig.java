package at.ac.tuwien.lucombonet.Service.Implementation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;

@Service
public class LuceneConfig {

    IndexWriter writer;
    IndexReader reader;
    Directory indexDirectory;
    Analyzer analyzer;
    IndexSearcher searcher;
    BM25Similarity bm;

    public LuceneConfig() throws IOException {
        indexDirectory = FSDirectory.open(Paths.get("")); //Path to directory
        analyzer = new GermanAnalyzer();
        open();
    }

    public void open() throws IOException {
        if(writer == null || !writer.isOpen()) {
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            bm = new BM25Similarity(1.2f, 0.75f);
            bm.setDiscountOverlaps(false);
            iwc.setSimilarity(bm);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(indexDirectory, iwc);
        }
    }

    public void close() throws IOException {
        if(writer.isOpen())
            writer.close();
    }

    public IndexWriter getWriter() {
        return writer;
    }


    public IndexReader getReader() {
        return reader;
    }

    public void setReader(IndexReader reader) {
        this.reader = reader;
    }

    public Directory getIndexDirectory() {
        return indexDirectory;
    }

    public void setIndexDirectory(Directory indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    public void setSearcher(IndexSearcher searcher) {
        this.searcher = searcher;
    }

    public BM25Similarity getBm() {
        return bm;
    }

    public void setBm(BM25Similarity bm) {
        this.bm = bm;
    }
}
