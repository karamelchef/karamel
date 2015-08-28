/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public class MetadataParser {

  private static final Logger logger = Logger.getLogger(MetadataParser.class);

  public static Pattern NAME = Pattern.compile("name\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*");
  public static Pattern DESC = Pattern.compile("description\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*");
  public static Pattern VERSION = Pattern.compile("version\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*");
  public static Pattern RECIPE = Pattern.compile(
      "recipe\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*(\\,\\s*[\\\"|\\'](.+)[\\\"|\\'])+");
  public static Pattern ATTR = Pattern.compile("attribute\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*(,)?\\s*");
  public static Pattern ATTR_DISP_NAME
      = Pattern.compile("\\s*:display_name\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_DESC = Pattern.compile("\\s*:description\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_TYPE = Pattern.compile("\\s*:type\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_DEFAULT_SIMPLE
      = Pattern.compile("\\s*:default\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_DEFAULT_ARRAY = Pattern.compile("\\s*:default\\s*=>\\s*\\[(.*)\\]s*(,)?\\s*");
  public static Pattern ATTR_DEFAULT_ARRAY_ITEMS = Pattern.compile("[\\'|\\\"]([^\\'|\\\"]*)[\\'|\\\"]");
  public static Pattern ATTR_REQUIRED = Pattern.compile("\\s*:required\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static String COMMA_CLOSING_LINE = ".*,\\s*$";

  /**
   *
   * @param content
   * @return
   * @throws se.kth.karamel.common.exception.MetadataParseException
   */
  public static MetadataRb parse(String content) throws MetadataParseException {
    MetadataRb metadata = new MetadataRb();
    StringReader reader = new StringReader(content);
    Scanner scanner = new Scanner(reader);
    List<String> comments = new ArrayList<>();
    while (scanner.hasNextLine()) {
      boolean found = false;
      String line = scanner.nextLine();
      line = line.trim();
      if (!line.isEmpty()) {
        if (line.startsWith("#")) {
          //It assumes that each comment block belongs to the next definition, it records comments until it finds out 
          //the next item
          comments.add(line);
          found = true;
        }

        //name of the cookbook
        if (!found) {
          Matcher m1 = NAME.matcher(line);
          if (m1.matches()) {
            metadata.setName(m1.group(1));
            found = true;
          }
        }

        //description of the cookbook
        if (!found) {
          Matcher m2 = DESC.matcher(line);
          if (m2.matches()) {
            metadata.setDescription(m2.group(1));
            found = true;
          }
        }

        //version of the cookbook
        if (!found) {
          Matcher m3 = VERSION.matcher(line);
          if (m3.matches()) {
            metadata.setVersion(m3.group(1));
            found = true;
          }
        }

        //recipe is a single line definition
        if (!found) {
          Matcher m4 = RECIPE.matcher(line);
          if (m4.matches()) {
            Recipe r = new Recipe();
            r.setName(m4.group(1));
            r.setDescription(m4.group(3));
            r.parseComments(comments);
            metadata.getRecipes().add(r);
            found = true;
            comments.clear();
          }
        }

        //attribute is multiple line definition
        if (!found) {
          Matcher m5 = ATTR.matcher(line);
          if (m5.matches()) {
            Attribute attr = new Attribute();
            attr.setName(m5.group(1));
            while (line.matches(COMMA_CLOSING_LINE) && scanner.hasNext()) {
              boolean found2 = false;
              line = scanner.nextLine();

              Matcher m6 = ATTR_DISP_NAME.matcher(line);
              if (m6.matches()) {
                attr.setDisplayName(m6.group(1));
                found2 = true;
              }

              if (!found2) {
                Matcher m7 = ATTR_TYPE.matcher(line);
                if (m7.matches()) {
                  attr.setType(m7.group(1));
                  found2 = true;
                }
              }

              if (!found2) {
                Matcher m8 = ATTR_DESC.matcher(line);
                if (m8.matches()) {
                  attr.setDescription(m8.group(1));
                  found2 = true;
                }

              }

              if (!found2) {
                Matcher m92 = ATTR_DEFAULT_ARRAY.matcher(line);
                if (m92.matches()) {
                  String sarr = m92.group(1);
                  Matcher m921 = ATTR_DEFAULT_ARRAY_ITEMS.matcher(sarr);
                  List<String> deflist = new ArrayList<>();
                  while (m921.find()) {
                    String item = m921.group(1);
                    deflist.add(item);
                  }
                  attr.setDefault(deflist);
                  found2 = true;
                }
              }
              
              if (!found2) {
                Matcher m91 = ATTR_DEFAULT_SIMPLE.matcher(line);
                if (m91.matches()) {
                  attr.setDefault(m91.group(1));
                  found2 = true;
                }
              }
              
              if (!found2) {
                Matcher m10 = ATTR_REQUIRED.matcher(line);
                if (m10.matches()) {
                  attr.setRequired(m10.group(1));
                  found2 = true;
                }
              }

              if (!found2) {
                logger.warn(String.format("Urecognized line for attribute in the metadata.rb '%s'", line));
              }
            }
            metadata.getAttributes().add(attr);
            found = true;
          }
        }

        if (!found) {
          logger.debug(String.format("Urecognized line in the metadata.rb '%s'", line));
        }
      } else {
        comments.clear();
      }
    }
    return metadata;
  }

}