package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Service.IFileInputService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class InputEndpoint {

    IFileInputService fileInputService;

    @Autowired
    public InputEndpoint(IFileInputService fileInputService) {
        this.fileInputService = fileInputService;
    }

    @PostMapping("/createIndex")
    public String createIndex() {
        try {
            return fileInputService.createIndex();
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/searchLucene")
    public List<SearchResultInt> searchLucene(@RequestParam String searchstring, @RequestParam Integer resultnumber) {
        try {
            return fileInputService.searchLuceneContent(searchstring, resultnumber);
        } catch(IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(ParseException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/searchMariaDB")
    public List<SearchResultInt> searchMariaDB(@RequestParam String searchstring, @RequestParam Integer resultnumber) {
        try {
            return fileInputService.searchMariaDB(searchstring, resultnumber);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    /**
     * Combined Search with MariaDB and Lucene, if query was already sent use MariaDB otherwise Lucene
     * @param searchstring
     * @param resultnumber
     * @return
     */
    @GetMapping("/search")
    public List<SearchResultInt> search(@RequestParam String searchstring, @RequestParam Integer resultnumber) {
        try {
            return fileInputService.search(searchstring, resultnumber, new Version());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }


}
