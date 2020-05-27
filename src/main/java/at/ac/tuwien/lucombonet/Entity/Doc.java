package at.ac.tuwien.lucombonet.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doc {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_doc_id")
    @SequenceGenerator(name = "seq_doc_id", sequenceName = "seq_doc_id")
    private Long id;

    @NotNull
    private String name;

    private Long length;

    private Timestamp timestamp;
}
