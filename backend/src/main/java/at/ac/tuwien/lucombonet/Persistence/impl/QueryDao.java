package at.ac.tuwien.lucombonet.Persistence.impl;

import at.ac.tuwien.lucombonet.Entity.QueryTable;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Persistence.IQueryDao;
import at.ac.tuwien.lucombonet.Persistence.IVersionDao;
import at.ac.tuwien.lucombonet.Persistence.util.DBConnectionManager;
import at.ac.tuwien.lucombonet.Persistence.util.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class QueryDao implements IQueryDao {

    private final DBConnectionManager dbConnectionManager;
    private IVersionDao versionDao;

    @Autowired
    public QueryDao(DBConnectionManager dbConnectionManager, IVersionDao versionDao) {
        this.dbConnectionManager = dbConnectionManager;
        this.versionDao = versionDao;
    }

    private  QueryTable dbResultToQuery(ResultSet result) throws SQLException {
        return new QueryTable(
                result.getLong("id"),
                result.getString("query"),
                versionDao.getOneById(result.getLong("version_id")));
    }

    @Override
    public Long existsQueryTableByQuery(String query, Long version) {
        String sql = "SELECT * from query_table where query like ? AND version_id = ? LIMIT 1";
        PreparedStatement statement = null;
        QueryTable res = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setString(1,query);
            statement.setLong(2, version);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                res = this.dbResultToQuery(result);
                return res.getId();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<QueryTable> getQueryByVersion(Long version) {
        String sql = "SELECT * FROM query_table WHERE version_id = ? ORDER BY id desc";
        PreparedStatement statement = null;
        List<QueryTable> queries = new ArrayList<>();
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, version);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                queries.add(dbResultToQuery(result));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return queries;
    }

    @Override
    public QueryTable getOneById(Long id) {
        String sql = "SELECT * FROM query_table WHERE id = ?";
        PreparedStatement statement = null;
        QueryTable query = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                query = this.dbResultToQuery(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return query;
    }

    @Override
    public QueryTable save(QueryTable query) {
        String sql = "INSERT INTO query_table(query, version_id) VALUES (?,?)" ;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, query.getQuery());
            statement.setLong(2, query.getVersion().getId());
            statement.execute();
            ResultSet result = statement.getGeneratedKeys();
            long i = 0;
            while (result.next())
            {
                i = result.getLong(1);
            }
            return getOneById(i);
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }

        return null;
    }
}
