/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model.yaml;

import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Test;
import se.kth.karamel.common.exception.MetadataParseException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class YamlScopeTest {

  @Test
  public void foldOutAttrTest() throws MetadataParseException {
    YamlScope yamlScope = new YamlScope() {

      @Override
      public void validate() throws ValidationException {
        throw new UnsupportedOperationException("Not supported yet."); 
      }
    };
    Map<String, Object> attrs = new HashMap<>();
    yamlScope.foldOutAttr("mysql/user", "admin", attrs);
    assertFalse(attrs.isEmpty());
    assertTrue(attrs.size() == 1);
    assertTrue(attrs.get("mysql") instanceof Map);
    Map<String, Object> mysql = (Map<String, Object>) attrs.get("mysql");
    assertTrue(mysql.size() == 1);
    assertTrue(mysql.get("user") instanceof String);
    String user = (String) mysql.get("user");
    assertEquals("admin", user);
  }
}
