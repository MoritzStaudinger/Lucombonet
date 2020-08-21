CREATE VIEW IF NOT EXISTS idf AS SELECT di.id, di.term, log(1 + ((SELECT count(id) from doc) - count(dt.dictionary_id) + 0.5)/(count(dt.dictionary_id) + 0.5)) as score  FROM doc d
    INNER JOIN doc_terms dt ON d.id = dt.document_id
    INNER JOIN dictionary di ON di.id = dt.dictionary_id
    GROUP by di.id, di.term;

CREATE VIEW IF NOT EXISTS versioned_idf AS SELECT v.id as version, di.id, di.term, log(1 + ( (SELECT count(id) From doc WHERE added_id <= v.id AND (removed_id is null OR removed_id > v.id)
    ) - (SELECT count(dt.dictionary_id) FROM doc d INNER JOIN doc_terms dt ON d.id = dt.document_id WHERE dt.dictionary_id = dto.dictionary_id AND d.added_id <= v.id AND (removed_id is null OR removed_id > v.id)) +0.5)
    /
    ((SELECT count(dt.dictionary_id) FROM doc d INNER JOIN doc_terms dt ON d.id = dt.document_id WHERE dt.dictionary_id = dto.dictionary_id AND added_id <= v.id AND (removed_id is null OR removed_id > v.id)) +0.5)) as score
    FROM version v, doc d
    INNER JOIN doc_terms dto ON d.id = dto.document_id
    INNER JOIN dictionary di ON di.id = dto.dictionary_id
    GROUP by v.id, di.id, di.term;