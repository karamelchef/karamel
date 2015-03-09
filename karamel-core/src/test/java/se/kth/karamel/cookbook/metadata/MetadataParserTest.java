/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package se.kth.karamel.cookbook.metadata;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public class MetadataParserTest {
  
  @Test
  public void testMetadata() throws MetadataParseException, IOException {
    String file = Resources.toString(Resources.getResource("se/kth/karamel/cookbook/metadata/metadata.rb"), Charsets.UTF_8);
    StringReader reader = new StringReader(file);
    MetadataRb metadatarb = MetadataParser.parse("testurl", reader);
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
