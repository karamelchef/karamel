/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.cookbookmeta;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.util.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;

/**
 *
 * @author kamal
 */
public class BerksfileTest {

  @Test
  public void testLinePatterns() throws CookbookUrlException {
    Settings.CB_CLASSPATH_MODE = true;
    List<String> lines = new ArrayList<>();
    lines.add("cookbook 'kagent', github: 'karamelchef/kagent-chef'");
    lines.add("cookbook 'ark', github: 'burtlo/ark', tag: 'v0.8.2'");
    lines.add("cookbook 'ark2', github: 'burtlo/ark2', branch: 'kitchen'");
    lines.add("cookbook 'ark3', github: 'burtlo/ark3', version: 'v0.4.0'");
    Berksfile berksfile = new Berksfile(lines);
    Assert.assertTrue(berksfile.getDeps().containsKey("kagent"));
    Assert.assertEquals("karamelchef/kagent-chef", berksfile.getDeps().get("kagent"));
    Assert.assertEquals("master", berksfile.getBranches().get("kagent"));
    Assert.assertTrue(berksfile.getDeps().containsKey("ark"));
    Assert.assertEquals("burtlo/ark", berksfile.getDeps().get("ark"));
    Assert.assertEquals("v0.8.2", berksfile.getBranches().get("ark"));
    Assert.assertTrue(berksfile.getDeps().containsKey("ark2"));
    Assert.assertEquals("burtlo/ark2", berksfile.getDeps().get("ark2"));
    Assert.assertEquals("kitchen", berksfile.getBranches().get("ark2"));
    Assert.assertTrue(berksfile.getDeps().containsKey("ark3"));
    Assert.assertEquals("burtlo/ark3", berksfile.getDeps().get("ark3"));
     Assert.assertEquals("v0.4.0", berksfile.getBranches().get("ark3"));
  }
}
