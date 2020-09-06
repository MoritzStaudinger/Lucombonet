package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.QueryTable;
import at.ac.tuwien.lucombonet.Repository.QueryRepository;
import at.ac.tuwien.lucombonet.Service.IQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    QueryRepository queryRepository;

    @Autowired
    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Override
    public List<QueryTable> getQueries(Long version) {
        List<QueryTable> results = queryRepository.getQueryByVersion(version);
        for(QueryTable q : results) {
            q.setQuery(q.getQuery().substring(1, q.getQuery().length()-1));
            q.setQuery(q.getQuery().replaceAll(",", ""));
        }
        return results;
    }
}
