package se.kth.karamel.webservicemodel;

/**
 * JSON representing the karamel main board.
 *
 */
public class KaramelBoardJSON {
  // Create setter and getter properties. Should be a JSON Object.

  private String json;

  public KaramelBoardJSON() {
  }

  public KaramelBoardJSON(String json) {
    this.json = json;
  }

  public String getJson() {
    return json;
  }

  public void setJson(String json) {
    this.json = json;
  }

}
