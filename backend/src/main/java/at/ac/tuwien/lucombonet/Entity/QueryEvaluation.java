package at.ac.tuwien.lucombonet.Entity;

import at.ac.tuwien.lucombonet.Endpoint.DTO.SearchResultInt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryEvaluation {
    String query;
    boolean correct;
    long readingTime;
    List<SearchResultInt> luceneResults;
    long queryTimeLucene;
    public List<SearchResultInt> monetResults;
    long queryTimeMonet;

}
