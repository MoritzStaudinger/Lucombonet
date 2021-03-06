package at.ac.tuwien.lucombonet.Persistence;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.Version;

import java.util.List;

public interface IDocumentDao {


    Doc findByWikiId(String id);

    Doc getOneById(Long id);

    Doc getByIdAndVersion(Doc d);

    Doc save(Doc d);

    Doc markAsDeleted(Doc d, Version v);

    List<SearchResultInt> findByTermsBM25Version(List<String> terms,Long version, Integer resultnumber);

    void saveAll(String filename);

    Long getMaxId();
}
