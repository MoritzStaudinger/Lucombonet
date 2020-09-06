package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Service.IVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class VersionEndpoint {

    IVersionService versionService;

    @Autowired
    public VersionEndpoint(IVersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping("/version")
    public List<Version> search() {
        System.out.println("Version");
        try {
            return versionService.getAllVersions();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
