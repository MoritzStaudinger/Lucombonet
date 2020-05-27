package at.ac.tuwien.lucombonet.Entity.XML;

import at.ac.tuwien.lucombonet.Entity.XML.Page;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "mediawiki")
public class Wiki {

    @JacksonXmlElementWrapper(localName = "pages")
    private List<Page> pages;
}
