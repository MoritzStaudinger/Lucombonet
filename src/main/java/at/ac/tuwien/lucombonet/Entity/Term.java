package at.ac.tuwien.lucombonet.Entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@EqualsAndHashCode @ToString @Builder @NoArgsConstructor @AllArgsConstructor
public class Term {

    @Id
    private Long id;
}
