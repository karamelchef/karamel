/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.common.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * Widely used collection transformations functions, using Guava underneath
 *
 * @author kamal
 */
public class CollectionsUtil {

  public static List<String> asStringList(List<Object> list) {
    List<String> newList = Lists.transform(list, new Function<Object, String>() {
      @Override
      public String apply(Object input) {
        return input.toString();
      }
    });
    return newList;
  }
}
