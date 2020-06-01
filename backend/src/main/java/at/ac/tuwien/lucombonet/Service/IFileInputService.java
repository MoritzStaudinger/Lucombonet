package at.ac.tuwien.lucombonet.Service;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

public interface IFileInputService {

    String createIndex() throws IOException;

    List<SearchResult> searchLucene(String query, int resultnumber) throws IOException, ParseException;
}
