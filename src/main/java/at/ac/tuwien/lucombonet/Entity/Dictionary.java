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
import javax.persistence.SequenceGenerator;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@EqualsAndHashCode @ToString @Builder @NoArgsConstructor @AllArgsConstructor
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_dic_id")
    @SequenceGenerator(name = "seq_dic_id", sequenceName = "seq_dic_id")
    private Long id;

    @NotNull
    @Column(unique=true)
    private String term;
}
