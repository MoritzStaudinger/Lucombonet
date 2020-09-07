package at.ac.tuwien.lucombonet.Repository;

import at.ac.tuwien.lucombonet.Entity.QueryTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QueryRepository  {

    @Query(value="SELECT id from query_table where query like :query AND version_id = :version LIMIT 1", nativeQuery = true)
    Long existsQueryTableByQuery(@Param("query") String query, @Param("version") Long version);

    @Query(value="SELECT * from query_table where version_id = :version", nativeQuery = true)
    List<QueryTable> getQueryByVersion(@Param("version") Long version);
}
