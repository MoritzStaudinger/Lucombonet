package at.ac.tuwien.lucombonet.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {

    @JacksonXmlProperty(localName = "title")
    private String title;
    @JacksonXmlProperty(localName = "ns")
    private String ns;
    @JacksonXmlProperty(localName = "id")
    private Integer id;
    @JacksonXmlProperty(localName = "timestamp")
    private Timestamp timestamp;
    @JacksonXmlProperty(localName = "contributor")
    private Contributor contributor;
    @JacksonXmlProperty(localName = "comment")
    private String comment;
    @JacksonXmlProperty(localName = "text")
    private String content;
}
