package at.ac.tuwien.lucombonet.Service.Implementation;

import at.ac.tuwien.lucombonet.Endpoint.EvaluationEndpoint;
import at.ac.tuwien.lucombonet.Service.IEvaluationService;
import at.ac.tuwien.lucombonet.Service.IFileService;
import at.ac.tuwien.lucombonet.Service.ISearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class EvaluationService implements IEvaluationService {

    ISearchService searchService;
    IFileService fileService;

    @Autowired
    public EvaluationService(ISearchService searchService, IFileService fileService) {
        this.searchService = searchService;
        this.fileService = fileService;
    }

    @Override
    public void createRandomWords() throws IOException {
        String wordString = Files.readString(Paths.get("Wordlists/OfficialScrabbleWordListGerman.txt"));
        String[] words = wordString.split("\n");
        FileWriter fw = new FileWriter("Wordlists/word_1.txt");
        for (int i = 0; i < 100; i++) {
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + "\n");
        }
        fw.close();
        fw = new FileWriter("Wordlists/word_2.txt");
        for (int i = 0; i < 100; i++) {
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + "\n");
        }
        fw.close();

        fw = new FileWriter("Wordlists/word_5.txt");
        for (int i = 0; i < 100; i++) {
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + "\n");
        }
        fw.close();

        fw = new FileWriter("Wordlists/word_10.txt");
        for (int i = 0; i < 100; i++) {
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + " ");
            fw.write(words[ThreadLocalRandom.current().nextInt(0, words.length)] + "\n");
        }
        fw.close();
    }



    @Override
    public void evaluate() throws IOException, ParseException {
        //createRandomWords();
        for (int i = 0; i < 26; i++) {
            fileService.createIndex("snippet_" + i);
                searchService.batchEvaluations("Wordlists/word_1.txt", i);
                searchService.batchEvaluations("Wordlists/word_2.txt", i);
                searchService.batchEvaluations("Wordlists/word_5.txt", i);
                searchService.batchEvaluations("Wordlists/word_10.txt", i);
            System.out.println("Batch nr.:" + i+" completed");
        }
    }
}
