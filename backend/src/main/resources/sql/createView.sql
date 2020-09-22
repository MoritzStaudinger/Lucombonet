CREATE TABLE IF NOT EXISTS dictionary (
  id bigint AUTO_INCREMENT ,
  term varchar(500),
  unique(term)
);

CREATE TABLE IF NOT EXISTS version (
  id bigint AUTO_INCREMENT,
  timestamp timestamp
);

CREATE TABLE IF NOT EXISTS query_table (
  id bigint AUTO_INCREMENT,
  query varchar(255),
  version_id bigint
);

CREATE TABLE IF NOT EXISTS doc (
  id bigint AUTO_INCREMENT,
  approximated_length bigint ,
  wiki_id varchar(255),
  length bigint ,
  name varchar(200),
  added_id bigint,
  removed_id bigint
);

CREATE TABLE IF NOT EXISTS doc_terms (
  term_frequency bigint DEFAULT NULL,
  document_id bigint NOT NULL,
  dictionary_id bigint NOT NULL
);
