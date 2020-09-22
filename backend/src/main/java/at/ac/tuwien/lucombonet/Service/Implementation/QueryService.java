package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Entity.QueryTable;
import at.ac.tuwien.lucombonet.Persistence.IQueryDao;
import at.ac.tuwien.lucombonet.Service.IQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService implements IQueryService {

    IQueryDao queryDao;

    @Autowired
    public QueryService(IQueryDao queryDao) {
        this.queryDao = queryDao;
    }

    @Override
    public List<QueryTable> getQueries(Long version) {
        List<QueryTable> results = queryDao.getQueryByVersion(version);
        for(QueryTable q : results) {
            q.setQuery(q.getQuery().substring(1, q.getQuery().length()-1));
            q.setQuery(q.getQuery().replaceAll(",", ""));
        }
        return results;
    }
}
