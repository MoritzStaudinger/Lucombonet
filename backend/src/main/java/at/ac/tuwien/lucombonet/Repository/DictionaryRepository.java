package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface DictionaryRepository {

    @Query(value = "SELECT * FROM dictionary WHERE term like :name LIMIT 1", nativeQuery = true)
    Dictionary findByTerm(@Param("name") String term);

}
