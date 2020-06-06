package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.QueryTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueryRepository extends JpaRepository<QueryTable, Long> {

    //@Query(value="SELECT id from queryTable where query like :name")
    Boolean existsQueryTableByQuery(String query);
}
