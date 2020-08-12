package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.QueryTable;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Repository.*;
import at.ac.tuwien.lucombonet.Service.ISearchService;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService implements ISearchService {

    DocumentRepository documentRepository;
    DictionaryRepository dictionaryRepository;
    DocTermRepository docTermRepository;
    QueryRepository queryRepository;
    VersionRepository versionRepository;

    LuceneConfig luceneConfig;

    @Autowired
    public SearchService(DocumentRepository documentRepository,
                         DictionaryRepository dictionaryRepository,
                         DocTermRepository docTermRepository,
                         VersionRepository versionRepository,
                         QueryRepository queryRepository,
                         LuceneConfig luceneConfig) {
        this.docTermRepository = docTermRepository;
        this.documentRepository = documentRepository;
        this.dictionaryRepository = dictionaryRepository;
        this.versionRepository = versionRepository;
        this.queryRepository = queryRepository;
        this.luceneConfig = luceneConfig;
    }

    public List<SearchResultInt> searchLuceneContent(String query, int resultnumber) throws IOException, ParseException {
        luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
        luceneConfig.setSearcher(new IndexSearcher(luceneConfig.getReader()));
        //MultiFieldQueryParser q = new MultiFieldQueryParser(new String[] {"title","content"}, analyzer);
        QueryParser q = new QueryParser("content", luceneConfig.getAnalyzer()); // only on content for reproducibility
        int hitsPerPage = resultnumber > 0 ? resultnumber : 10;
        BM25Similarity bm = new BM25Similarity(1.2f, 0.75f);
        luceneConfig.getSearcher().setSimilarity(bm);

        TopDocs docs = luceneConfig.getSearcher().search(q.parse(query), hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        List<SearchResultInt> results = new ArrayList<>();
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = luceneConfig.getSearcher().doc(docId);
            System.out.println(luceneConfig.getSearcher().explain(q.parse(query), i));
            results.add(SearchResult.builder().name(d.get("title")).score((double)hits[i].score).build());
        }
        return results;
    }

    @Override
    public List<SearchResultInt> searchLuceneTitleHash(String query) throws IOException, ParseException {
        luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
        luceneConfig.setSearcher(new IndexSearcher(luceneConfig.getReader()));

        QueryParser q = new QueryParser("title", luceneConfig.getAnalyzer()); // only on content for reproducibility
        BM25Similarity bm = new BM25Similarity(1.2f, 0.75f);
        luceneConfig.getSearcher().setSimilarity(bm);

        TopDocs docs = luceneConfig.getSearcher().search(q.parse(QueryParser.escape(query)), 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<SearchResultInt> results = new ArrayList<>();
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = luceneConfig.getSearcher().doc(docId);
            results.add(SearchResult.builder().name(d.get("title")).score((double)hits[i].score).build());
        }
        return results;
    }

    @Override
    public List<SearchResultInt> searchMariaDB(String query, int resultnumber) throws ParseException {
        QueryParser q = new QueryParser("", luceneConfig.getAnalyzer());
        List<String> strings = Arrays.stream(q.parse(query).toString().split(" ")).sorted().collect(Collectors.toList());
        List<SearchResultInt> a = documentRepository.findByTermsBM25(strings);
        return a;
    }

    @Override
    public List<SearchResultInt> search(String searchstring, Integer resultnumber, Version version) throws ParseException, IOException {
        QueryParser q = new QueryParser("", luceneConfig.getAnalyzer());
        List<String> strings = Arrays.stream(q.parse(searchstring).toString().split(" ")).sorted().collect(Collectors.toList());

        if(queryRepository.existsQueryTableByQuery(strings.toString())) {
            System.out.println("mariaDB");
            return searchMariaDB(searchstring, resultnumber);
        } else {
            queryRepository.save(QueryTable.builder().query(strings.toString()).build());
            System.out.println("Lucene");
            return searchLuceneContent(searchstring, resultnumber);
        }
    }
}
