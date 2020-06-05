package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.SearchResultInt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Doc, Long> {

    @Query(value = "SELECT t.name AS name, sum(bm25) AS score \n" +
            "FROM (\n" +
            "         SELECT d.name, ((SELECT log(1 + ((SELECT count(id) from doc) - count(dt.dictionary_id) + 0.5)/" +
            "(count(dt.dictionary_id) + 0.5))\n" +
            "                          FROM doc d\n" +
            "                                   INNER JOIN doc_terms dt ON d.id = dt.document_id\n" +
            "                                   INNER JOIN dictionary di ON di.id = dt.dictionary_id\n" +
            "                          WHERE di.term like :term2) *\n" +
            "                         dt.term_frequency / (dt.term_frequency + 1.2 * (1-0.75 + 0.75 * (\n" +
            "                 d.approximated_length\n" +
            "                 /(SELECT avg(length) from doc))))) as bm25\n" +
            "         FROM doc d\n" +
            "                  INNER JOIN doc_terms dt ON d.id = dt.document_id\n" +
            "                  INNER JOIN dictionary di ON di.id = dt.dictionary_id\n" +
            "         WHERE di.term IN (:term)\n" +
            "         GROUP BY d.name, di.term\n" +
            "         ORDER BY bm25 desc) AS t\n" +
            "GROUP BY t.name\n" +
            "ORDER BY sum(bm25) desc", nativeQuery = true)
    List<SearchResult> findBySingleTermBM25(@Param("term") String term, @Param("term2") String term2);

    @Query(value = "SELECT scoring.name, sum(scoring.bm25)\n" +
            "FROM (\n" +
            "         SELECT d.name, (idf.score *\n" +
            "                         dt.term_frequency / (dt.term_frequency + 1.2 * (1-0.75 + 0.75 * (\n" +
            "                 d.approximated_length\n" +
            "                 /(SELECT avg(length) from doc))))) as bm25\n" +
            "         FROM doc d\n" +
            "                  INNER JOIN doc_terms dt ON d.id = dt.document_id\n" +
            "                  INNER JOIN (SELECT * FROM dictionary di WHERE di.term IN :terms) as" +
            " di ON di.id = dt.dictionary_id\n" +
            "                  INNER JOIN idf ON idf.id = di.id\n" +
            "         GROUP BY d.name, di.term\n" +
            "         ORDER BY bm25 desc) AS scoring\n" +
            "GROUP BY scoring.name\n" +
            "ORDER BY sum(scoring.bm25) desc;", nativeQuery = true)
    List<SearchResult> findByTermsBM25(@Param("terms") List<String> terms);
}
