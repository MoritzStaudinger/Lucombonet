package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
}
