package at.ac.tuwien.lucombonet.Entity;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Immutable
@Subselect(
        "SELECT di.id, di.term, log(1 + ((SELECT count(id) from doc) - count(dt.dictionary_id) + 0.5)/(count(dt" +
                ".dictionary_id) + 0.5)) as score  FROM doc d\n" +
                "    INNER JOIN doc_terms dt ON d.id = dt.document_id\n" +
                "    INNER JOIN dictionary di ON di.id = dt.dictionary_id\n" +
                "    GROUP by di.id, di.term;"
)
public class Idf {

    @Id
    private Long id;
    @Column
    private String term;
    @Column
    private Double score;
}
