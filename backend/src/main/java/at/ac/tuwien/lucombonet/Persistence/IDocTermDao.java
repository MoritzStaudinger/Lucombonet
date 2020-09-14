package at.ac.tuwien.lucombonet.Persistence;

import at.ac.tuwien.lucombonet.Entity.DocTerms;

import java.util.List;

public interface IDocTermDao {

    void saveAll(List<DocTerms> terms);

    void saveAll(String filename);
}
