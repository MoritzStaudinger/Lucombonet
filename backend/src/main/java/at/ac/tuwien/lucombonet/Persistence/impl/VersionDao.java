package at.ac.tuwien.lucombonet.Persistence.impl;

import at.ac.tuwien.lucombonet.Entity.Doc;
import at.ac.tuwien.lucombonet.Entity.Version;
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
public class VersionDao implements IVersionDao {

    private final DBConnectionManager dbConnectionManager;

    @Autowired
    public VersionDao(DBConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    private static Version dbResultToVersion(ResultSet result) throws SQLException {
        return new Version(
                result.getLong("id"),
                result.getTimestamp("timestamp"));
    }

    @Override
    public Version getMax() {
        String sql = "SELECT * FROM version ORDER BY id desc LIMIT 1";
        PreparedStatement statement = null;
        Version version = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                version = dbResultToVersion(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return version;
    }

    @Override
    public Version getOneById(Long id) {
        String sql = "SELECT * FROM version WHERE id = ?";
        PreparedStatement statement = null;
        Version version = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                version = dbResultToVersion(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return version;
    }

    @Override
    public List<Version> getAll() {
        String sql = "SELECT * FROM version ORDER BY id desc";
        PreparedStatement statement = null;
        List<Version> versions = new ArrayList<>();
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                versions.add(dbResultToVersion(result));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return versions;
    }

    @Override
    public Version save(Version v) {
        String sql = "INSERT INTO version(timestamp) VALUES (?)" ;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setTimestamp(1, v.getTimestamp());
            statement.execute();
            return getOneByTimestamp(v);
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Version getOneByTimestamp(Version v) {
        String sql = "SELECT * FROM version WHERE timestamp = ?";
        PreparedStatement statement = null;
        Version version = null;
        try{
            statement = dbConnectionManager.getConnection().prepareStatement(sql);
            statement.setTimestamp(1, v.getTimestamp());
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                version = dbResultToVersion(result);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
        return version;
    }
}
