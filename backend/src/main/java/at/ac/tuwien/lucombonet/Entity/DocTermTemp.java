package at.ac.tuwien.lucombonet.Entity;

import lombok.*;


@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocTermTemp {

    String term;

    Long termFrequency;
}
