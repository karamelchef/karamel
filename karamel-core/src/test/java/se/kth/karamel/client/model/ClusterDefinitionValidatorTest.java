/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.client.model;

import java.io.IOException;
import org.junit.Test;
import se.kth.karamel.backend.ClusterDefinitionService;
import se.kth.karamel.common.IoUtils;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.exception.ValidationException;

/**
 *
 * @author kamal
 */
public class ClusterDefinitionValidatorTest {

  @Test(expected = ValidationException.class)
  public void testInvalidGroupSizeForBaremetal() throws IOException, KaramelException {
    Settings.CB_CLASSPATH_MODE = true;
    String yaml = IoUtils.readContentFromClasspath("se/kth/hop/model/validations.yml");
    ClusterDefinitionService.yamlToJson(yaml);
  }
}
