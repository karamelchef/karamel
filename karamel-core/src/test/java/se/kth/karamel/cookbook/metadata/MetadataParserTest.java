/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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
  public void testNdbRecognizedLines() throws IOException, MetadataParseException {
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
    assertEquals(45, attributes.size());
    
    assertEquals("ndb/ports", attributes.get(0).getName());
    assertEquals("Dummy ports", attributes.get(0).getDescription());
    assertEquals("array", attributes.get(0).getType());
    assertEquals("required", attributes.get(0).getRequired());
    assertEquals("['123', '134', '145']", attributes.get(0).getDefault());

    assertEquals("ndb/DataMemory", attributes.get(1).getName());
    assertEquals("Data memory for each MySQL Cluster Data Node", attributes.get(1).getDescription());
    assertEquals("string", attributes.get(1).getType());
    assertEquals("required", attributes.get(1).getRequired());
    assertEquals("80", attributes.get(1).getDefault());

    assertEquals("kagent/enabled", attributes.get(44).getName());
    assertEquals("Install kagent", attributes.get(44).getDescription());
    assertEquals("string", attributes.get(44).getType());
    assertEquals("optional", attributes.get(44).getRequired());
    assertEquals("false", attributes.get(44).getDefault());
  }
  
    @Test
  public void testLinks() throws MetadataParseException, IOException {
    String content = IoUtils.readContentFromClasspath("se/kth/karamel/cookbook/metadata/metadata.rb");
    MetadataRb metadatarb = MetadataParser.parse(content);
    List<Recipe> recipes = metadatarb.getRecipes();
    assertEquals(recipes.size(), 2);
    Recipe r1 = recipes.get(0);
    Recipe r2 = recipes.get(1);
    assertEquals(r1.getName(), "hopshub::install");
    Set<String> l1 = r1.getLinks();
    assertEquals(l1.size(), 0);
    assertEquals(r2.getName(), "hopshub::default");
    Set<String> l2 = r2.getLinks();
    assertEquals(l2.size(), 2);
    assertEquals(l2.toArray()[0], "Click {here,https://%host%:8181/hop-dashboard} to launch hopshub in your browser");
    assertEquals(l2.toArray()[1], "Visit Karamel {here,www.karamel.io}");
  }
}
