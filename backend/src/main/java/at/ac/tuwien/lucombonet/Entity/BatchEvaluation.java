package at.ac.tuwien.lucombonet.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchEvaluation {

    boolean correct;
    double avgDiff;
    double maxDiff;
    long avgQueryTimeLucene;
    long avgQueryTimeMonet;
    public List<QueryEvaluation> queryEvaluations;
}
