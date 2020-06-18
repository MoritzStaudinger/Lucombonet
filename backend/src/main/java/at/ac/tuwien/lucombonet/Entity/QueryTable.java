package at.ac.tuwien.lucombonet.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryTable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_query_id")
    @SequenceGenerator(name = "seq_query_id", sequenceName = "seq_query_id")
    private Long id;

    @NotNull
    @Column(unique=true)
    private String query;

    @NotNull
    @ManyToOne
    private Version version;
}