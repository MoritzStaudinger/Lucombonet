package at.ac.tuwien.lucombonet.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {

    @JsonProperty("title")
    private String title;
    @JsonProperty("ns")
    private String ns;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("revision")
    private Revision revision;
}
