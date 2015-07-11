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
import se.kth.karamel.common.exception.MetadataParseException;

/**
 *
 * @author kamal
 */
public class MetadataParser {

  public static Pattern NAME = singleValueEntry("name");
  public static Pattern DESC = singleValueEntry("description");
  public static Pattern VERSION = singleValueEntry("version");
  public static Pattern RESULTS_DIR = singleValueEntry("results_dir");
  public static Pattern URL_BINARY = singleValueEntry("url_binary");
  public static Pattern URL_GITCLONE = singleValueEntry("url_gitclone");
  public static Pattern BUILD_COMMAND = singleValueEntry("build_command");
  public static Pattern RECIPE = Pattern.compile(
      "recipe\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*(\\,\\s*[\\\"|\\'](.+)[\\\"|\\'])+");
  public static Pattern ATTR = Pattern.compile("attribute\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*(,)?\\s*");
  public static Pattern ATTR_DISP_NAME = Pattern.compile(":display_name\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_DESC = Pattern.compile(":description\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_TYPE = Pattern.compile(":type\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_DEFAULT = Pattern.compile(":default\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static Pattern ATTR_REQUIRED = Pattern.compile(":required\\s*=>\\s*[\\\"|\\'](.+)[\\\"|\\']s*(,)?\\s*");
  public static String COMMA_CLOSING_LINE = ".*,\\s*$";

  private static Pattern singleValueEntry(String name) {
    return Pattern.compile(name + "\\s*[\\\"|\\'](.+)[\\\"|\\']\\s*");
  }

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
      String line = scanner.nextLine();
      if (line.startsWith("#")) {
        comments.add(line);
      } else {
        Matcher mName = NAME.matcher(line);
        if (mName.matches()) {
          metadata.setName(mName.group(1));
        } else {
          Matcher mDesc = DESC.matcher(line);
          if (mDesc.matches()) {
            metadata.setDescription(mDesc.group(1));
          } else {
            Matcher mVer = VERSION.matcher(line);
            if (mVer.matches()) {
              metadata.setVersion(mVer.group(1));
            } else {
              Matcher mRecipe = RECIPE.matcher(line);
              if (mRecipe.matches()) {
                Recipe r = new Recipe();
                r.setName(mRecipe.group(1));
                r.setDescription(mRecipe.group(3));
                r.parseComments(comments);
                metadata.getRecipes().add(r);
              } else {
                Matcher mRes = RESULTS_DIR.matcher(line);
                if (mRes.matches()) {
                  metadata.setResultsDir(mRes.group(1));
                } else {
                  Matcher mBuild = BUILD_COMMAND.matcher(line);
                  if (mBuild.matches()) {
                    metadata.setBuildCommand(mBuild.group(1));
                  } else {
                    Matcher mUrlBinary = URL_BINARY.matcher(line);
                    if (mUrlBinary.matches()) {
                      metadata.setUrlBinary(mUrlBinary.group(1));
                    } else {
                      Matcher mUrlGitclone = URL_GITCLONE.matcher(line);
                      if (mUrlGitclone.matches()) {
                        metadata.setBuildCommand(mUrlGitclone.group(1));
                      } else {
                        Matcher mAttr = ATTR.matcher(line);
                        if (mAttr.matches()) {
                          Attribute attr = new Attribute();
                          attr.setName(mAttr.group(1));
                          while (line.matches(COMMA_CLOSING_LINE) && scanner.hasNext()) {
                            line = scanner.nextLine();
                            Matcher mGroup = ATTR_DISP_NAME.matcher(line);
                            if (mGroup.matches()) {
                              attr.setDisplayName(mGroup.group(1));
                            } else {
                              Matcher mAttrType = ATTR_TYPE.matcher(line);
                              if (mAttrType.matches()) {
                                attr.setType(mAttrType.group(1));
                              } else {
                                Matcher mAttrDesc = ATTR_DESC.matcher(line);
                                if (mAttrDesc.matches()) {
                                  attr.setDescription(mAttrDesc.group(1));
                                } else {
                                  Matcher mAttrDef = ATTR_DEFAULT.matcher(line);
                                  if (mAttrDef.matches()) {
                                    attr.setDefault(mAttrDef.group(1));
                                  } else {
                                    Matcher mAreqd = ATTR_REQUIRED.matcher(line);
                                    if (mAreqd.matches()) {
                                      attr.setRequired(mAreqd.group(1));
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
