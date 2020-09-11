package at.ac.tuwien.lucombonet.Persistence;

import at.ac.tuwien.lucombonet.Entity.Dictionary;

import java.util.List;

public interface IDictionaryDao {

    Dictionary findByTerm(String term);

    Dictionary getOneById(Long id);

    Dictionary save(Dictionary d);

    List<Dictionary> saveAll(List<Dictionary> dictionaries);

    List<Dictionary> getAll();
}
