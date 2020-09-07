package at.ac.tuwien.lucombonet.Persistence.impl;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.Dictionary;
import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Persistence.IDocumentDao;
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
import java.util.Collections;
import java.util.List;

@Repository
public class DocumentDao implements IDocumentDao {

    private final DBConnectionManager dbConnectionManager;
    private IVersionDao versionDao;

    @Autowired
    public DocumentDao(DBConnectionManager dbConnectionManager, IVersionDao versionDao) {
        this.dbConnectionManager = dbConnectionManager;
        this.versionDao = versionDao;
    }

    private Doc dbResultToDoc(ResultSet result) throws SQLException {
        return new Doc(
                result.getLong("id"),
                result.getString("name"),
                result.getString("hash"),
                result.getLong("approximated_length"),
                result.getLong("length"),
                versionDao.getOneById(result.getLong("added_id")),
                versionDao.getOneById(result.getLong("removed_id")));
    }

    private SearchResultInt dbResultToSearchResultInt(ResultSet result) throws SQLException {
        return new SearchResult(
                result.getString("name"),
                result.getDouble("score"),
                "MariaDB");
    }

    @Override
    public Doc findByHash(String hash) {
        String sql = "SELECT * from doc where hash = ? AND removed_id is null";
        PreparedStatement statement = null;
        Doc doc = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setString(1, hash);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                doc = dbResultToDoc(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return doc;
    }

    @Override
    public Doc getOneById(Long id) {
        String sql = "SELECT * FROM doc WHERE id = ?";
        PreparedStatement statement = null;
        Doc doc = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                doc = this.dbResultToDoc(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return doc;
    }

    @Override
    public Doc getByHashAndVersion(Doc d) {
        String sql = "SELECT * FROM doc WHERE hash = ? AND added_id = ?";
        PreparedStatement statement = null;
        Doc doc = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setString(1, d.getHash());
            statement.setLong(2, d.getAdded().getId());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                doc = this.dbResultToDoc(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return doc;
    }

    @Override
    public Doc save(Doc d) {
        String sql = "INSERT INTO doc(approximated_length, hash, length, name, added_id) VALUES (?,?,?,?,?)" ;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, d.getApproximatedLength());
            statement.setString(2, d.getHash());
            statement.setLong(3, d.getLength());
            statement.setString(4,d.getName());
            statement.setLong(5,d.getAdded().getId());
            statement.execute();
            return getByHashAndVersion(d);
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Doc markAsDeleted(Doc d, Version v) {
        String sql = "UPDATE doc SET removed_id = ? WHERE id = ?" ;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, d.getApproximatedLength());
            statement.setLong(2, v.getId());
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

    @Override
    public List<SearchResultInt> findByTermsBM25Version(List<String> terms, Long version, Integer resultnumber) {
        String inSql = String.join(",", terms);
        String sql = String.format("SELECT scoring.name, sum(scoring.bm25) as score " +
                "FROM (" +
                "         SELECT d.name, (versioned_idf.score * " +
                "                         dt.term_frequency / (dt.term_frequency + 1.2 * (1-0.75 + 0.75 * (" +
                "                 d.approximated_length " +
                "                 /(SELECT avg(length) from doc where added_id <= ? AND (removed_id is null OR removed_id > ?) ))))) as bm25 " +
                "         FROM doc d " +
                "                  INNER JOIN (SELECT * FROM version where id = ?) as v ON added_id <= v.id AND (removed_id is null OR removed_id > v.id) " +
                "                  INNER JOIN doc_terms dt ON d.id = dt.document_id " +
                "                  INNER JOIN (SELECT * FROM dictionary di WHERE di.term IN (%s)) as di ON di.id = dt.dictionary_id " +
                "                  INNER JOIN (SELECT * from versioned_idf where version = ?) as versioned_idf ON versioned_idf.id = di.id " +
                "         GROUP BY d.name, di.term " +
                "         ORDER BY bm25 desc) AS scoring " +
                "GROUP BY scoring.name " +
                "ORDER BY sum(scoring.bm25) desc, scoring.name LIMIT ? ;", inSql );

        System.out.println(sql);
        PreparedStatement statement = null;
        List<SearchResultInt> results = new ArrayList<>();
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, version);
            statement.setLong(2, version);
            statement.setLong(3, version);
            statement.setLong(4, version);
            statement.setLong(5, resultnumber);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                results.add(this.dbResultToSearchResultInt(result));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return results;
    }
}