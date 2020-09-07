package at.ac.tuwien.lucombonet.Persistence;

import at.ac.tuwien.lucombonet.Entity.Version;

import java.util.List;

public interface IVersionDao {

    Version getMax();

    List<Version> getAll();

    Version getOneById(Long id);

    Version save(Version v);

    Version getOneByTimestamp(Version v);
}
