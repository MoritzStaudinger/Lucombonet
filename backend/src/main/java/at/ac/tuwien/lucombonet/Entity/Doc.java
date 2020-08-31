package at.ac.tuwien.lucombonet.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
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
    @Column(length = 200, columnDefinition = "VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL")
    private String name;

    private String hash;

    private Long approximatedLength;

    private Long length;

    @ManyToOne
    private Version added;

    @ManyToOne
    private Version removed;
}
