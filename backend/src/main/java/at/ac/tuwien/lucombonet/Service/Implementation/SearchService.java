package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.BatchEvaluation;
import at.ac.tuwien.lucombonet.Entity.QueryEvaluation;
import at.ac.tuwien.lucombonet.Entity.QueryTable;
import at.ac.tuwien.lucombonet.Persistence.IDocumentDao;
import at.ac.tuwien.lucombonet.Persistence.IQueryDao;
import at.ac.tuwien.lucombonet.Persistence.IVersionDao;
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

import javax.validation.ValidationException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService implements ISearchService {

    IDocumentDao documentDao;
    IQueryDao queryDao;
    IVersionDao versionDao;

    LuceneConfig luceneConfig;

    @Autowired
    public SearchService(
            LuceneConfig luceneConfig, IQueryDao queryDao, IVersionDao versionDao, IDocumentDao documentDao) {
        this.queryDao = queryDao;
        this.versionDao = versionDao;
        this.luceneConfig = luceneConfig;
        this.documentDao = documentDao;
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
            //System.out.println(luceneConfig.getSearcher().explain(q.parse(query), i));
            results.add(SearchResult.builder().name(d.get("title")).score((double)hits[i].score).engine("Lucene").build());
        }
        results.sort(Comparator.comparing(SearchResultInt::getScore).reversed().thenComparing(SearchResultInt::getName));
        return results;
    }

    @Deprecated
    @Override
    public List<SearchResultInt> searchLuceneWikiId(String query) throws IOException, ParseException {
        luceneConfig.setReader(DirectoryReader.open(luceneConfig.getIndexDirectory()));
        luceneConfig.setSearcher(new IndexSearcher(luceneConfig.getReader()));

        QueryParser q = new QueryParser("id", luceneConfig.getAnalyzer()); // only on content for reproducibility
        BM25Similarity bm = new BM25Similarity(1.2f, 0.75f);
        luceneConfig.getSearcher().setSimilarity(bm);

        TopDocs docs = luceneConfig.getSearcher().search(q.parse(QueryParser.escape(query)), 10);
        ScoreDoc[] hits = docs.scoreDocs;
        List<SearchResultInt> results = new ArrayList<>();
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = luceneConfig.getSearcher().doc(docId);
            luceneConfig.getWriter().tryDeleteDocument(luceneConfig.getReader(),docId);
            results.add(SearchResult.builder().name(d.get("title")).score((double)hits[i].score).build());
        }
        return results;
    }

    @Override
    public List<SearchResultInt> searchMonetDB(String query, int resultnumber) throws ParseException {
        return searchMonetDBVersioned(query, versionDao.getMax().getId(), resultnumber);
    }

    @Override
    public List<SearchResultInt> searchMonetDBVersioned(String query, long version, int resultnumber) throws ParseException {
        QueryParser q = new QueryParser("", luceneConfig.getAnalyzer());
        List<String> strings = Arrays.stream(q.parse(query).toString().split(" ")).map(x -> "\'"+x+"\'").sorted().collect(Collectors.toList());
        List<SearchResultInt> results = documentDao.findByTermsBM25Version(strings, version, resultnumber);
        List<SearchResultInt> resultsWithEngine = new ArrayList<>();
        for(SearchResultInt result : results) {
            resultsWithEngine.add(SearchResult.builder().name(result.getName()).score(result.getScore()).engine("MonetDB").build());
        }
        return resultsWithEngine;
    }


    @Override
    public List<SearchResultInt> search(String searchstring, Integer resultnumber) throws ParseException, IOException {
        if(searchstring.length() < 1) {
            System.out.println("Empty String");
            throw new ValidationException("Dont enter an empty string, Lucene does not like that");
        }
        QueryParser q = new QueryParser("", luceneConfig.getAnalyzer());
        List<String> strings = Arrays.stream(q.parse(searchstring).toString().split(" ")).sorted().collect(Collectors.toList());
        if(queryDao.existsQueryTableByQuery(strings.toString(), versionDao.getMax().getId()) != null) {
            return searchMonetDB(searchstring, resultnumber);
        } else {
            queryDao.save(QueryTable.builder().query(strings.toString()).version(versionDao.getMax()).build());
            return searchLuceneContent(searchstring, resultnumber);
        }
    }

    @Override
    public BatchEvaluation batchEvaluations(String file, int batchnumber) throws IOException, ParseException {
        String words = Files.readString(Paths.get(file));
        String[] wordList = words.split("\n");
        List<QueryEvaluation> queries = new ArrayList<>();
        for(String w: wordList) {
           queries.add(queryEvaluation(w));
        }

        boolean correct = true;
        double maxDifference = 0;
        long elements = 0;
        double sumdifferences = 0;
        double sumTimeLucene = 0;
        double sumTimeMonet = 0;
        for(QueryEvaluation q: queries){
            correct = correct & q.isCorrect();
            sumTimeLucene += q.getQueryTimeLucene();
            sumTimeMonet += q.getQueryTimeMonet();
            for (int i = 0; i < q.getLuceneResults().size() ; i++) {
                double differenceScore = Math.abs(q.getLuceneResults().get(0).getScore() - q.getMonetResults().get(0).getScore());
                if(maxDifference < differenceScore ) {
                    maxDifference = differenceScore;
                }
                sumdifferences +=differenceScore;
                elements++;
            }
        }

        BatchEvaluation bs = BatchEvaluation.builder()
                .correct(correct)
                .queryEvaluations(queries)
                .maxDiff(maxDifference)
                .avgDiff(sumdifferences/elements)
                .avgQueryTimeLucene((long)sumTimeLucene/queries.size())
                .avgQueryTimeMonet((long)sumTimeMonet/queries.size())
                .build();
        FileWriter fw = new FileWriter("results/batch_"+batchnumber+ "_"+file.split("/")[1]);
        fw.write(bs.toString());
        fw.close();
        return bs;
    }

    private QueryEvaluation queryEvaluation(String term) throws IOException, ParseException {
        long luceneSearchStarted = System.currentTimeMillis();
        List<SearchResultInt> luceneSearch = searchLuceneContent(term, 10);
        long luceneSearchFinished = System.currentTimeMillis();
        List<SearchResultInt> monetdbSearch = searchMonetDB(term, 10);
        long monetDBSearchFinished = System.currentTimeMillis();
        boolean correct = true;
        for (int i = 0; i < luceneSearch.size(); i++) {
            if(!luceneSearch.get(0).getName().equals(monetdbSearch.get(0).getName())) {
                correct = false;
            }
        }
        QueryEvaluation qs = QueryEvaluation.builder()
                .query(term)
                .luceneResults(luceneSearch)
                .monetResults(monetdbSearch)
                .queryTimeLucene(luceneSearchFinished-luceneSearchStarted)
                .queryTimeMonet(monetDBSearchFinished-luceneSearchFinished)
                .correct(correct)
                .build();
        return qs;
    }
}
