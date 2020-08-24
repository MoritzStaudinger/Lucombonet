package at.ac.tuwien.lucombonet.Service;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.Version;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.List;

public interface ISearchService {

    List<SearchResultInt> searchLuceneContent(String query, int resultnumber) throws IOException, ParseException;
    List<SearchResultInt> searchLuceneTitleHash(String query) throws IOException, ParseException;

    List<SearchResultInt> searchMariaDB(String query, int resultnumber) throws ParseException;

    List<SearchResultInt> searchMariaDBVersioned(String query, long version, int resultnumber) throws ParseException;

    List<SearchResultInt> search(String searchstring, Integer resultnumber, Version version) throws ParseException, IOException;
}
