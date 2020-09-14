package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Service.IFileService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
public class InputEndpoint {

    IFileService fileInputService;

    @Autowired
    public InputEndpoint(IFileService fileInputService) {
        this.fileInputService = fileInputService;
    }

    @PostMapping("/createIndexTest")
    public String createIndexTest() {
        System.out.println("Test");
        try {
            return fileInputService.createIndex("testxml.xml");
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/createIndex30")
    public String createIndex30() {
        try {
            return fileInputService.createIndex("30xml.xml");
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/createIndex2000")
    public String createIndex2000() {
        try {
            return fileInputService.createIndex("2000xml.xml");
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/createIndexWiki")
    public String createIndexWiki() {
        try {
            return fileInputService.createIndex("dewiki_changed.xml");
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

}
