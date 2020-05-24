package at.ac.tuwien.lucombonet.Service;

import org.apache.lucene.queryparser.classic.ParseException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public interface IFileInputService {

    String createIndex() throws IOException;

    String searchLucene(String query, int resultnumber) throws IOException, ParseException;
}
