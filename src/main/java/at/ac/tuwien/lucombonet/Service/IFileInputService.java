package at.ac.tuwien.lucombonet.Service;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public interface IFileInputService {

    String createIndex() throws XMLStreamException, IOException;
}
