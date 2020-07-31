CREATE VIEW IF NOT EXISTS idf AS SELECT di.id, di.term, log(1 + ((SELECT count(id) from doc) - count(dt.dictionary_id) + 0.5)/(count(dt.dictionary_id) + 0.5)) as score  FROM doc d
    INNER JOIN doc_terms dt ON d.id = dt.document_id
    INNER JOIN dictionary di ON di.id = dt.dictionary_id
    GROUP by di.id, di.term;