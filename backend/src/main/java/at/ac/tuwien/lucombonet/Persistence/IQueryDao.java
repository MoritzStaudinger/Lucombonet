package at.ac.tuwien.lucombonet.Persistence;

import at.ac.tuwien.lucombonet.Entity.QueryTable;

import java.util.List;

public interface IQueryDao {

    Long existsQueryTableByQuery(String query, Long version);

    List<QueryTable> getQueryByVersion(Long version);

    QueryTable getOneById(Long id);

    QueryTable save(QueryTable query);
}
