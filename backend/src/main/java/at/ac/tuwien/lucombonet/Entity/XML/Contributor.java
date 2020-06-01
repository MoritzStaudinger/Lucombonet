package at.ac.tuwien.lucombonet.Entity.XML;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contributor {
    @JsonProperty("username")
    private String username;
    @JsonProperty("id")
    private Integer id;
}
