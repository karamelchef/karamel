/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.common.IoUtils;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public class MetadataParserTest {

  @Test
  public void regexTest() {
    Pattern ATTR_DEFAULT = Pattern.compile("\\s*:default\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*,?\\s*");
    Pattern ATTR_REQUIRED = Pattern.compile("\\s*:required\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*,?\\s*");

    String line = "          :required => \"required\",";
    Matcher matcher = ATTR_REQUIRED.matcher(line);
    assertTrue(matcher.matches());

    line = "          :default => \"80\"";
    matcher = ATTR_DEFAULT.matcher(line);
    assertTrue(matcher.matches());

  }

  @Test
  public void testRecognizedLines() throws IOException, MetadataParseException {
    String content = IoUtils.readContentFromClasspath("cookbooks/hopshadoop/ndb-chef/master/metadata.rb");
    MetadataRb metadatarb = MetadataParser.parse(content);
    assertEquals("ndb", metadatarb.getName());
    assertEquals("Installs/Configures NDB (MySQL Cluster)", metadatarb.getDescription());
    assertEquals("1.0", metadatarb.getVersion());
    List<Recipe> recipes = metadatarb.getRecipes();
    assertEquals(11, recipes.size());
    assertEquals("ndb::install", recipes.get(0).getName());
    assertEquals("Installs MySQL Cluster binaries", recipes.get(0).getDescription());
    assertEquals("ndb::purge", recipes.get(10).getName());
    assertEquals("Removes all data and all binaries related to a MySQL Cluster installation",
        recipes.get(10).getDescription());
    
    List<Attribute> attributes = metadatarb.getAttributes();
    assertEquals(44, attributes.size());
    
    assertEquals("ndb/DataMemory", attributes.get(0).getName());
    assertEquals("Data memory for each MySQL Cluster Data Node", attributes.get(0).getDescription());
    assertEquals("string", attributes.get(0).getType());
    assertEquals("required", attributes.get(0).getRequired());
    assertEquals("80", attributes.get(0).getDefault());

    assertEquals("kagent/enabled", attributes.get(43).getName());
    assertEquals("Install kagent", attributes.get(43).getDescription());
    assertEquals("string", attributes.get(43).getType());
    assertEquals("optional", attributes.get(43).getRequired());
    assertEquals("false", attributes.get(43).getDefault());
  }
}
