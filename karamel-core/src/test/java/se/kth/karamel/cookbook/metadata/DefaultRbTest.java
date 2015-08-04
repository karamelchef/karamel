/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.Settings;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public class DefaultRbTest {

  @Test
  public void testLoadAttributes() throws CookbookUrlException, MetadataParseException {
    Settings.CB_CLASSPATH_MODE = true;
    KaramelizedCookbook cb = new KaramelizedCookbook("biobankcloud/hiway-chef");
    DefaultRb defaultRb = cb.getDefaultRb();
    
    Object value = defaultRb.getValue("hiway/variantcall/reads/run_ids");
    Assert.assertEquals(Lists.newArrayList("SRR359188", "SRR359195"), value);

    value = defaultRb.getValue("hiway/variantcall/reference/chromosomes");
    Assert.assertEquals(Lists.newArrayList("chr22", "chrY"), value);
  }
}
