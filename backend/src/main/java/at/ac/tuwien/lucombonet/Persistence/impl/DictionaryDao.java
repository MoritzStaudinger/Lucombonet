package at.ac.tuwien.lucombonet.Persistence.impl;

import at.ac.tuwien.lucombonet.Entity.Dictionary;
import at.ac.tuwien.lucombonet.Persistence.IDictionaryDao;
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
public class DictionaryDao implements IDictionaryDao {

    private final DBConnectionManager dbConnectionManager;

    @Autowired
    public DictionaryDao(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    private static Dictionary dbResultToDictionary(ResultSet result) throws SQLException {
        return new Dictionary(
                result.getLong("id"),
                result.getString("term"));
    }

    @Override
    public Dictionary findByTerm(String term) {
        String sql = "SELECT * FROM dictionary WHERE term like ? LIMIT 1";
        PreparedStatement statement = null;
        Dictionary dictionary = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setString(1, term);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                dictionary = dbResultToDictionary(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    @Override
    public Dictionary getOneById(Long id) {
        String sql = "SELECT * FROM dictionary WHERE id = ?";
        PreparedStatement statement = null;
        Dictionary dictionary = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                dictionary = dbResultToDictionary(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    @Override
    public Dictionary save(Dictionary d) {
        String sql = "INSERT INTO dictionary(term) VALUES (?)" ;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, d.getTerm());
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
    public void saveAll(List<Dictionary> dictionaries) {
        //TODO
    }

    @Override
    public List<Dictionary> getAll() {
        String sql = "SELECT * FROM dictionary";
        PreparedStatement statement = null;
        List<Dictionary> dictionaries = new ArrayList<>();
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                dictionaries.add(dbResultToDictionary(result));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return dictionaries;
    }


}
