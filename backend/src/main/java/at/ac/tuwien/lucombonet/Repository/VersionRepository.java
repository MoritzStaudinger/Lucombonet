package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VersionRepository  extends JpaRepository<Version, Long> {

    @Query(value= "SELECT * FROM version ORDER BY id desc LIMIT 1", nativeQuery = true)
    Version getMax();

    @Query(value = "SELECT * FROM version ORDER BY id desc", nativeQuery = true)
    List<Version> getAll();
}
