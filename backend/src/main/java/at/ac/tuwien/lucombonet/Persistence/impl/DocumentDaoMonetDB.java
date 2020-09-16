package at.ac.tuwien.lucombonet.Persistence.impl;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResult;
import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.Version;
import at.ac.tuwien.lucombonet.Persistence.IDocumentDao;
import at.ac.tuwien.lucombonet.Persistence.IVersionDao;
import at.ac.tuwien.lucombonet.Persistence.util.DBConnectionManager;
import at.ac.tuwien.lucombonet.Persistence.util.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Profile("monet")
@Repository
public class DocumentDaoMonetDB implements IDocumentDao {

    private final DBConnectionManager dbConnectionManager;
    private IVersionDao versionDao;

    @Autowired
    public DocumentDaoMonetDB(DBConnectionManager dbConnectionManager, IVersionDao versionDao) {
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
        String sql = "UPDATE doc SET removed_id = ? WHERE added_id = ? AND hash = ?" ;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, v.getId());
            statement.setLong(2, d.getAdded().getId());
            statement.setString(3, d.getHash());
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
        String sql = String.format("SELECT scoring.name, sum(scoring.bm25) as score \n" +
                "             FROM (\n" +
                " SELECT d.name, di.term, (log(1 + ( ? - n.freq + 0.5)/(n.freq +0.5))* \n" +
                "                                      dt.term_frequency / (dt.term_frequency + 1.2 * (1-0.75 + 0.75 " +
                "* (\n" +
                "                              d.approximated_length \n" +
                "                              /(SELECT avg(length) as avlength from doc where added_id <= ? AND (removed_id is null OR removed_id > ?)))))) as bm25  \n" +
                " FROM \n" +
                " (SELECT id, name, added_id, removed_id, approximated_length FROM doc WHERE added_id <= ? AND " +
                "(removed_id >? OR removed_id is null)) AS d\n" +
                " INNER JOIN doc_terms dt ON d.id = dt.document_id\n" +
                " INNER JOIN (SELECT * FROM dictionary di WHERE di.term in (%s)) as di ON di.id = dt" +
                ".dictionary_id\n" +
                " INNER JOIN (SELECT dictionary_id, count(dt.dictionary_id) as freq FROM doc d INNER JOIN doc_terms " +
                "dt ON (d.id = dt.document_id AND added_id <= ? AND (removed_id is null OR removed_id > ?)) GROUP BY " +
                "dictionary_id) as n \n" +
                " ON n.dictionary_id = di.id \n" +
                " GROUP BY d.name, di.term, bm25\n" +
                " ORDER BY bm25 desc ) as scoring\n" +
                " GROUP BY scoring.name\n" +
                " ORDER BY score desc, scoring.name desc LIMIT ?;\n" +
                " ", inSql );

        System.out.println(sql);
        PreparedStatement statement = null;
        List<SearchResultInt> results = new ArrayList<>();
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, getDocumentNumber(version)); //DocNumber
            //statement.setDouble(2, getAverageLength(version)); //AvgLength
            statement.setLong(2, version);
            statement.setLong(3, version);
            statement.setLong(4, version);
            statement.setLong(5, version);
            statement.setLong(6, version);
            statement.setLong(7, version);
            statement.setLong(8, resultnumber);
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

    @Override
    public void saveAll(String filename) {
        String sql = "COPY INTO doc FROM " +filename;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.execute();

        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long getMaxId() {
        String sql = "select max(id) from doc;";
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                return result.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (PersistenceException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public Long getDocumentNumber(Long v) {
        String sql = "(SELECT count(id) as number From doc WHERE added_id <= ? AND (removed_id is null OR removed_id > ?))";
        PreparedStatement statement = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, v);
            statement.setLong(2, v);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                return result.getLong("number");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public Double getAverageLength(Long v) {
        String sql = "(SELECT avg(length) as avlength from doc where added_id <= ? AND (removed_id is null OR removed_id > ?) )";
        PreparedStatement statement = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, v);
            statement.setLong(2, v);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                return result.getDouble("avlength");
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
