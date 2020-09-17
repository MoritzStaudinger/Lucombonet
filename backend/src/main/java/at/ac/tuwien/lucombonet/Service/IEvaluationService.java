package at.ac.tuwien.lucombonet.Service;

import at.ac.tuwien.lucombonet.Entity.BatchEvaluation;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

public interface IEvaluationService {

    void createRandomWords() throws IOException;

    void evaluate() throws IOException, ParseException;
}
