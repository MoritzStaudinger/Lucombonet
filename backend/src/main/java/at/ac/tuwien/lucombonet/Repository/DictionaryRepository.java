package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

    @Query(value = "SELECT * FROM Dictionary WHERE term like :name LIMIT 1", nativeQuery = true)
    Dictionary findByTerm(@Param("name") String term);
}
