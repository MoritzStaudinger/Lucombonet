package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Repository.VersionRepository;
import at.ac.tuwien.lucombonet.Service.IVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionService implements IVersionService {

    VersionRepository versionRepository;

    @Autowired
    public VersionService(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }

    @Override
    public List<Version> getAllVersions() {
        return versionRepository.getAll();
    }
}
