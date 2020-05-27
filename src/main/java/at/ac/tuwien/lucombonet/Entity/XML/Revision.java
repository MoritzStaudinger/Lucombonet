package at.ac.tuwien.lucombonet.Entity.XML;

import at.ac.tuwien.lucombonet.Entity.XML.Contributor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Revision {
    @JsonProperty("timestamp")
    private Timestamp timestamp;
    @JsonProperty("contributor")
    private Contributor contributor;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("text")
    private String content;

}
