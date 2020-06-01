package at.ac.tuwien.lucombonet.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import java.io.Serializable;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocTerms {

    @EmbeddedId
    DocTermsKey id;

    Long termFrequency;

    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocTermsKey implements Serializable {
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "dictionary_id")
        private Dictionary dictionary;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "document_id")
        private Doc document;
    }
}
