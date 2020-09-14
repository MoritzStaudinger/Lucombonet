package at.ac.tuwien.lucombonet.Persistence.impl;

import at.ac.tuwien.lucombonet.Entity.DocTerms;
import at.ac.tuwien.lucombonet.Persistence.IDictionaryDao;
import at.ac.tuwien.lucombonet.Persistence.IDocTermDao;
import at.ac.tuwien.lucombonet.Persistence.IDocumentDao;
import at.ac.tuwien.lucombonet.Persistence.util.DBConnectionManager;
import at.ac.tuwien.lucombonet.Persistence.util.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class DocTermDao implements IDocTermDao {

    private final DBConnectionManager dbConnectionManager;
    private final IDocumentDao documentDao;
    private final IDictionaryDao dictionaryDao;

    @Autowired
    public DocTermDao(DBConnectionManager dbConnectionManager, IDictionaryDao dictionaryDao, IDocumentDao documentDao ) {
        this.dbConnectionManager = dbConnectionManager;
        this.dictionaryDao = dictionaryDao;
        this.documentDao = documentDao;
    }

    private DocTerms dbResultToDocTerms(ResultSet result) throws SQLException {
        return new DocTerms(
                result.getLong("term_frequency"),
                dictionaryDao.getOneById(result.getLong("dictionary_id")),
                documentDao.getOneById(result.getLong("document_id"))
                );
    }

    @Override
    public void saveAll(List<DocTerms> terms) {
        String sql = "INSERT INTO doc_terms(document_id, dictionary_id, term_frequency) VALUES (?,?,?)" ;
        PreparedStatement statement = null;
        try {
            statement = dbConnectionManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for(DocTerms t: terms) {
                statement.clearParameters();
                statement.setLong(1, t.getDocument().getId());
                statement.setLong(2, t.getDictionary().getId());
                statement.setLong(3, t.getTermFrequency());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch(SQLException e) {
            e.printStackTrace();
        } catch(PersistenceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAll(String filename) {
        String sql = "COPY INTO doc_terms FROM " +filename;
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
}
