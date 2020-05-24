package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Service.IFileInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

@RestController
public class InputEndpoint {

    IFileInputService fileInputService;

    @Autowired
    public InputEndpoint(IFileInputService fileInputService) {
        this.fileInputService = fileInputService;
    }

    @PostMapping("/read")
    public String test() {
        try {
            return fileInputService.createIndex();
        } catch(XMLStreamException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
