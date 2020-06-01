package at.ac.tuwien.lucombonet.Unittests;

import at.ac.tuwien.lucombonet.Entity.XML.Page;
import at.ac.tuwien.lucombonet.Entity.XML.Wiki;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class XmlParserTests {

    @Test
    public void whenPageGotFromXmlStr_thenCorrect() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        Page value
                = xmlMapper.readValue("<page>\n" +
                "    <title>Linsenbohne</title>\n" +
                "    <ns>0</ns>\n" +
                "    <id>3029</id>\n" +
                "    <redirect title=\"Urdbohne\" />\n" +
                "    <revision>\n" +
                "      <id>6748949</id>\n" +
                "      <parentid>8221</parentid>\n" +
                "      <timestamp>2002-08-28T10:51:21Z</timestamp>\n" +
                "      <contributor>\n" +
                "        <username>Conversion script</username>\n" +
                "        <id>0</id>\n" +
                "      </contributor>\n" +
                "      <minor />\n" +
                "      <comment>Automated conversion</comment>\n" +
                "      <model>wikitext</model>\n" +
                "      <format>text/x-wiki</format>\n" +
                "      <text bytes=\"22\" xml:space=\"preserve\">#REDIRECT [[Urdbohne]]\n" +
                "</text>\n" +
                "      <sha1>kdax9bic03a99dutelk05ivrhgertqw</sha1>\n" +
                "    </revision>\n" +
                "  </page>", Page.class);
        System.out.println(value.toString());
        //TODO ASSERT EQUALS
    }

    @Test
    public void whenMediawikiGotFromXmlStr_thenCorrect() throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        Wiki value
                = xmlMapper.readValue("<mediawiki><pages><page>\n" +
                "    <title>Linsenbohne</title>\n" +
                "    <ns>0</ns>\n" +
                "    <id>3029</id>\n" +
                "    <redirect title=\"Urdbohne\" />\n" +
                "    <revision>\n" +
                "      <id>6748949</id>\n" +
                "      <parentid>8221</parentid>\n" +
                "      <timestamp>2002-08-28T10:51:21Z</timestamp>\n" +
                "      <contributor>\n" +
                "        <username>Conversion script</username>\n" +
                "        <id>0</id>\n" +
                "      </contributor>\n" +
                "      <minor />\n" +
                "      <comment>Automated conversion</comment>\n" +
                "      <model>wikitext</model>\n" +
                "      <format>text/x-wiki</format>\n" +
                "      <text bytes=\"22\" xml:space=\"preserve\">#REDIRECT [[Urdbohne]]\n" +
                "</text>\n" +
                "      <sha1>kdax9bic03a99dutelk05ivrhgertqw</sha1>\n" +
                "    </revision>\n" +
                "  </page></pages></mediawiki>", Wiki.class);
        System.out.println(value.toString());
        //TODO ASSERT EQUALS
    }

}
