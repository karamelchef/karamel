package se.kth.karamel.common.clusterdef.json;

import se.kth.karamel.common.exception.KaramelException;
import se.kth.karamel.common.clusterdef.Scope;
import se.kth.karamel.common.cookbookmeta.KaramelizedCookbook;
import se.kth.karamel.common.clusterdef.yaml.YamlScope;
import se.kth.karamel.common.exception.ValidationException;
import se.kth.karamel.common.cookbookmeta.CookbookCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonScope extends Scope {

  protected final List<KaramelizedCookbook> cookbooks = new ArrayList<>();

  public Map<String, Object> attributes;

  public static CookbookCache CACHE;

  public JsonScope() {
  }

  public JsonScope(YamlScope scope) throws KaramelException {
    super(scope);
  }

  public List<KaramelizedCookbook> getCookbooks() {
    return cookbooks;
  }

  public void setCookbooks(List<KaramelizedCookbook> cookbooks) {
    this.cookbooks.addAll(cookbooks);
  }

  @Override
  public Object getAttr(String key) {
    return null;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public void validate() throws ValidationException {
    super.validate();
  }
}
