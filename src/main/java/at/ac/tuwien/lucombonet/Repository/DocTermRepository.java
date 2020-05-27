package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.DocTerms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocTermRepository extends JpaRepository<DocTerms, Long> {
}
