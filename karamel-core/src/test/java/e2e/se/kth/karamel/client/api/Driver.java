/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package e2e.se.kth.karamel.client.api;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import se.kth.karamel.client.api.KaramelApi;
import se.kth.karamel.client.api.KaramelApiImpl;
import se.kth.karamel.common.Ec2Credentials;
import se.kth.karamel.common.exception.KaramelException;

/**
 * 
 * @author kamal
 */
public class Driver {

  public static void main(String[] args) throws IOException, KaramelException, InterruptedException {
    
    //Karamel will read your ssh-key pair from the default location (~/.ssh/id_rsa and ~/.ssh/id_rsa/id_rsa.pub) in 
    //the unix-based systems. It uses your key-pair for ec2, so please make sure you have them provided before start 
    //running this program
    
    KaramelApi api = new KaramelApiImpl();
    //give your own yaml file
    String ymlString = Resources.toString(Resources.getResource("se/kth/hop/model/hiway.yml"), Charsets.UTF_8);
    //since api works with json you will need to convert your yaml to json
    String json = api.yamlToJson(ymlString);

    //pass in your ec2 credentials here, 
    Ec2Credentials credentials = new Ec2Credentials();
    credentials.setAccountId("<ec2-accoun-id>");
    credentials.setAccessKey("<ec2-access-key>");
    if (!api.updateEc2CredentialsIfValid(credentials))
      throw new IllegalThreadStateException("Ec2 credentials is not valid");
    
    //this is an async call, you will need to pull status of your cluster periodically with the forthcoming call
    api.startCluster(json);
    
    long ms1 = System.currentTimeMillis();
    while (ms1 + 6000000 > System.currentTimeMillis()) {
      //the name of the cluster should be equal to the one you specified in your yaml file
      String clusterStatus = api.getClusterStatus("hiway");
      System.out.println(clusterStatus);
      Thread.currentThread().sleep(60000);
    }
  }
}
