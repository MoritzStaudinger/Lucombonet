package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.Version;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VersionRepository  extends JpaRepository<Version, Long> {
}
