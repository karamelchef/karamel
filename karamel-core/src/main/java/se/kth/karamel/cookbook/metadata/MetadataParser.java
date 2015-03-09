/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.cookbook.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.kth.karamel.common.exception.CookbookUrlException;
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public class MetadataParser {

  public static Pattern NAME = Pattern.compile("name\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*");
  public static Pattern DESC = Pattern.compile("description\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*");
  public static Pattern VERSION = Pattern.compile("version\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*");
  public static Pattern RECIPE = Pattern.compile("recipe\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*(\\,\\s*[\\\"|\\'](.+)[\\\"|\\'])+");
  public static Pattern ATTR = Pattern.compile("attribute\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*(,)?\\s*");
  public static Pattern ATTR_DISP_NAME = Pattern.compile(":display_name\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_DESC = Pattern.compile(":description\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_TYPE = Pattern.compile(":type\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_DEFAULT = Pattern.compile(":default\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_REQUIRED = Pattern.compile(":required\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static String COMMA_CLOSING_LINE = ".*,\\s*$";

  /**
   *
   * @param cookbookPath
   * @param metedataPath
   * @return
   * @throws se.kth.karamel.common.exception.CookbookUrlException
   * @throws se.kth.karamel.common.exception.MetadataParseException
   */
  public static MetadataRb parse(String cookbookPath, String metedataPath) throws CookbookUrlException, MetadataParseException {
    URL url;
    try {
      url = new URL(metedataPath);
      return parse(cookbookPath, url);
    } catch (MalformedURLException ex) {
      throw new CookbookUrlException("metadata.rb url is malford " + metedataPath, ex);
    }

  }

  /**
   * @param cookbookPath
   * @param url
   *
   * @return
   * @throws se.kth.karamel.common.exception.MetadataParseException
   */
  public static MetadataRb parse(String cookbookPath, URL url) throws MetadataParseException {
    InputStream in;
    try {
      in = url.openStream();
      Reader reader = new InputStreamReader(in, "UTF-8");
      return parse(cookbookPath, reader);
    } catch (IOException ex) {
      throw new MetadataParseException("Exception occured during parsing metadata.rb", ex);
    }

  }

  /**
   *
   * @param cookbookPath
   * @param reader
   * @return
   * @throws se.kth.karamel.common.exception.MetadataParseException
   */
  public static MetadataRb parse(String cookbookPath, Reader reader) throws MetadataParseException {
    MetadataRb metadata = new MetadataRb();
    metadata.setUrl(cookbookPath);
    Scanner scanner = new Scanner(reader);
    List<String> comments = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (line.startsWith("#")) {
        comments.add(line);
      } else {
        Matcher m1 = NAME.matcher(line);
        if (m1.matches()) {
          metadata.setName(m1.group(1));
        } else {
          Matcher m2 = DESC.matcher(line);
          if (m2.matches()) {
            metadata.setDescription(m2.group(1));
          } else {
            Matcher m3 = VERSION.matcher(line);
            if (m3.matches()) {
              metadata.setVersion(m3.group(1));
            } else {
              Matcher m4 = RECIPE.matcher(line);
              if (m4.matches()) {
                Recipe r = new Recipe();
                r.setName(m4.group(1));
                r.setDescription(m4.group(3));
                r.parseComments(comments);
                metadata.getRecipes().add(r);
              } else {
                Matcher m5 = ATTR.matcher(line);
                if (m5.matches()) {
                  Attribute attr = new Attribute();
                  attr.setName(m5.group(1));
                  while (line.matches(COMMA_CLOSING_LINE) && scanner.hasNext()) {
                    line = scanner.nextLine();
                    Matcher m6 = ATTR_DISP_NAME.matcher(line);
                    if (m6.matches()) {
                      attr.setDisplayName(m6.group(1));
                    } else {
                      Matcher m7 = ATTR_TYPE.matcher(line);
                      if (m7.matches()) {
                        attr.setType(m7.group(1));
                      } else {
                        Matcher m8 = ATTR_DESC.matcher(line);
                        if (m8.matches()) {
                          attr.setDescription(m8.group(1));
                        } else {
                          Matcher m9 = ATTR_DEFAULT.matcher(line);
                          if (m9.matches()) {
                            attr.setDefault(m9.group(1));
                          } else {
                            Matcher m10 = ATTR_REQUIRED.matcher(line);
                            if (m10.matches()) {
                              attr.setRequired(m10.group(1));
                            }
                          }
                        }
                      }

                    }
                  }
                  metadata.getAttributes().add(attr);
                }
              }
            }
          }
        }
        comments.clear();
      }
    }
    return metadata;
  }

}
