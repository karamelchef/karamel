package se.kth.karamel.webservicemodel;

/**
 * Created by babbarshaer on 2014-11-13.
 */
public class CookbookJSON {

    String url;
    boolean refresh;

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
