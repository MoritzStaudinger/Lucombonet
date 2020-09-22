package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Persistence.IVersionDao;
import at.ac.tuwien.lucombonet.Service.IVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionService implements IVersionService {

    IVersionDao versionDao;

    @Autowired
    public VersionService(IVersionDao versionDao) {
        this.versionDao = versionDao;
    }

    @Override
    public List<Version> getAllVersions() {
        return versionDao.getAll();
    }
}
