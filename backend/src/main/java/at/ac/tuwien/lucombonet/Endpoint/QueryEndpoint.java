package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Entity.QueryTable;
import at.ac.tuwien.lucombonet.Service.IQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class QueryEndpoint {

    IQueryService queryService;

    @Autowired
    public QueryEndpoint(IQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/queries")
    public List<QueryTable> getQueries(@RequestParam Long version) {
        try {
            return queryService.getQueries(version);
        } catch(Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
