package se.kth.karamel.webservicemodel;

/**
 * JSON equivalent of the Yaml
 *
 * Created by babbarshaer on 2014-11-07.
 */
public class KaramelBoardYaml {

    private String yml;

    public KaramelBoardYaml(){}

    public KaramelBoardYaml(String yml){
        this.yml = yml;
    }

    public String getYml() {
        return yml;
    }

    public void setYml(String yml) {
        this.yml = yml;
    }

}
