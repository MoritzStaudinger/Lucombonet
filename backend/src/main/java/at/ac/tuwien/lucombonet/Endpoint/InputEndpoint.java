package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Service.IFileInputService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

@RestController
public class InputEndpoint {

    IFileInputService fileInputService;

    @Autowired
    public InputEndpoint(IFileInputService fileInputService) {
        this.fileInputService = fileInputService;
    }

    @PostMapping("/createIndex")
    public String test() {
        try {
            return fileInputService.createIndex();
        } catch(IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/searchLucene")
    public List<SearchResult> searchLucene(@RequestParam String searchstring, @RequestParam Integer resultnumber) {
        try {
            return fileInputService.searchLucene(searchstring, resultnumber);
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/searchMariaDB")
    public List<SearchResult> searchMariaDB(@RequestParam String searchstring, @RequestParam Integer resultnumber) {
        try {
            return fileInputService.searchMariaDB(searchstring, resultnumber);
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }


}
