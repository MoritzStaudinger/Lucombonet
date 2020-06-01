package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.Doc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Doc, Long> {
}
