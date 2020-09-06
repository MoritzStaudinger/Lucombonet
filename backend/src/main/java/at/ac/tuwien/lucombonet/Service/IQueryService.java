package at.ac.tuwien.lucombonet.Service;

import at.ac.tuwien.lucombonet.Entity.QueryTable;

import java.util.List;

public interface IQueryService {
    List<QueryTable> getQueries(Long version);
}
