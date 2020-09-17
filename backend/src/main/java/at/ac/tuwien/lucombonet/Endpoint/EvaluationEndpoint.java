package at.ac.tuwien.lucombonet.Endpoint;

import at.ac.tuwien.lucombonet.Entity.BatchEvaluation;
import at.ac.tuwien.lucombonet.Service.IEvaluationService;
import at.ac.tuwien.lucombonet.Service.IFileService;
import at.ac.tuwien.lucombonet.Service.ISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
public class EvaluationEndpoint {

    IEvaluationService evaluationService;

    @Autowired
    public EvaluationEndpoint(IEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/evaluate")
    public void searchEvaluation() {
        try {
            evaluationService.evaluate();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/createRandomWords")
    public void createRandomWords() {
        try {
            evaluationService.createRandomWords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
