/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.karamel.backend.dag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import se.kth.karamel.common.exception.DagConstructionException;

/**
 *
 * @author kamal
 */
public class DagTest {

  @Test
  public void testCycle() throws DagConstructionException {
    Dag dag = new Dag("");
    dag.addNode("11");
    dag.addDependency("12", "121");
    dag.addDependency("13", "131");
    try {
      dag.detectCycles();
    } catch (DagConstructionException e) {
      Assert.fail();
    }
    dag.addDependency("131", "132");
    dag.addDependency("132", "133");
    dag.addDependency("133", "13");
    try {
      dag.detectCycles();
    } catch (DagConstructionException e) {
    }
  }

  @Test
  public void testPrint() throws DagConstructionException {
    Dag dag = new Dag("");
    dag.addNode("task1");
    dag.addDependency("task2", "task21");
    dag.addDependency("task3", "task31");
    dag.addDependency("task31", "task32");
    dag.addDependency("task32", "task33");
    dag.addDependency("task21", "task31");
    System.out.println(dag.print());
  }

  @Test
  public void testCycleOnRealData() throws DagConstructionException {
    Dag dag = new Dag("");
    dag.addNode("apt-get essentials on hopsworks1");
    dag.addNode("apt-get essentials on ndb1");
    dag.addNode("apt-get essentials on ndb2");
    dag.addNode("apt-get essentials on datanodes1");
    dag.addDependency("apt-get essentials on hopsworks1", "install berkshelf on hopsworks1");
    dag.addDependency("install berkshelf on hopsworks1", "make solo.rb on hopsworks1");
    dag.addDependency("make solo.rb on hopsworks1", "clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on hopsworks1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on hopsworks1", "hadoop::install on hopsworks1");
    dag.addDependency("hadoop::nn on hopsworks1", "hadoop::rm on hopsworks1");
    dag.addDependency("hadoop::install on hopsworks1", "hadoop::rm on hopsworks1");
    dag.addDependency("hadoop::install on hopsworks1", "hadoop::nn on hopsworks1");
    dag.addDependency("hadoop::nn on hopsworks1", "hadoop::jhs on hopsworks1");
    dag.addDependency("hadoop::nn on hopsworks1", "hadoop::jhs on hopsworks1");
    dag.addDependency("hadoop::install on hopsworks1", "hadoop::jhs on hopsworks1");
    dag.addDependency("hadoop::dn on datanodes1", "hadoop::jhs on hopsworks1");
    dag.addDependency("make solo.rb on hopsworks1", "clone and vendor cookbooks/hopshadoop/ndb-chef on hopsworks1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/ndb-chef on hopsworks1", "ndb::install on hopsworks1");
    dag.addDependency("ndb::ndbd on ndb1", "ndb::mysqld on hopsworks1");
    dag.addDependency("ndb::install on hopsworks1", "ndb::mysqld on hopsworks1");
    dag.addDependency("ndb::mgmd on hopsworks1", "ndb::mysqld on hopsworks1");
    dag.addDependency("ndb::ndbd on ndb2", "ndb::mysqld on hopsworks1");
    dag.addDependency("ndb::install on hopsworks1", "ndb::mgmd on hopsworks1");
    dag.addDependency("make solo.rb on hopsworks1", "clone and vendor cookbooks/hopshadoop/hopsworks-chef on hopsworks1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/hopsworks-chef on hopsworks1", "hopsworks::install on hopsworks1");
    dag.addDependency("hadoop::nn on hopsworks1", "hopsworks::default on hopsworks1");
    dag.addDependency("hopsworks::install on hopsworks1", "hopsworks::default on hopsworks1");
    dag.addDependency("ndb::mysqld on hopsworks1", "hopsworks::default on hopsworks1");
    dag.addDependency("hadoop::dn on datanodes1", "hopsworks::default on hopsworks1");
    dag.addDependency("apt-get essentials on ndb1", "install berkshelf on ndb1");
    dag.addDependency("install berkshelf on ndb1", "make solo.rb on ndb1");
    dag.addDependency("make solo.rb on ndb1", "clone and vendor cookbooks/hopshadoop/ndb-chef on ndb1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/ndb-chef on ndb1", "ndb::install on ndb1");
    dag.addDependency("ndb::install on ndb1", "ndb::ndbd on ndb1");
    dag.addDependency("ndb::mgmd on hopsworks1", "ndb::ndbd on ndb1");
    dag.addDependency("apt-get essentials on ndb2", "install berkshelf on ndb2");
    dag.addDependency("install berkshelf on ndb2", "make solo.rb on ndb2");
    dag.addDependency("make solo.rb on ndb2", "clone and vendor cookbooks/hopshadoop/ndb-chef on ndb2");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/ndb-chef on ndb2", "ndb::install on ndb2");
    dag.addDependency("ndb::mgmd on hopsworks1", "ndb::ndbd on ndb2");
    dag.addDependency("ndb::install on ndb2", "ndb::ndbd on ndb2");
    dag.addDependency("apt-get essentials on datanodes1", "install berkshelf on datanodes1");
    dag.addDependency("install berkshelf on datanodes1", "make solo.rb on datanodes1");
    dag.addDependency("make solo.rb on datanodes1", "clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on datanodes1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on datanodes1", "hadoop::install on datanodes1");
    dag.addDependency("hadoop::rm on hopsworks1", "hadoop::nm on datanodes1");
    dag.addDependency("hadoop::install on datanodes1", "hadoop::nm on datanodes1");
    dag.addDependency("hadoop::nn on hopsworks1", "hadoop::dn on datanodes1");
    dag.addDependency("hadoop::install on datanodes1", "hadoop::dn on datanodes1");
    dag.detectCycles();
    System.out.println(dag.print());
  }

  @Test(expected = DagConstructionException.class)
  public void testValidation() throws DagConstructionException {
    class DummyTask implements DagTask {

      String id;

      public DummyTask(String id) {
        this.id = id;
      }

      @Override
      public String dagNodeId() {
        return id;
      }

      @Override
      public void submit(DagTaskCallback callback) {
        callback.succeed();
      }

      @Override
      public Set<String> dagDependencies() {
        return Collections.EMPTY_SET;
      }

      @Override
      public void prepareToStart() {
      }

      @Override
      public String asJson() {
        throw new UnsupportedOperationException("Not supported yet."); 
      }

      @Override
      public void terminate() {
      }

    }

    Dag dag = new Dag("");
    dag.addNode("task1");
    dag.addDependency("task2", "task21");
    dag.addDependency("task3", "task31");
    dag.addDependency("task31", "task32");
    dag.addDependency("task32", "task33");
    dag.addDependency("task21", "task31");
    dag.addTask(new DummyTask("task1"));
    dag.addTask(new DummyTask("task2"));
    dag.addTask(new DummyTask("task3"));
    dag.addTask(new DummyTask("task21"));
    dag.addTask(new DummyTask("task31"));
    dag.addTask(new DummyTask("task32"));
    //task33 doesnt have any task, expecting exception here
    dag.start();
  }

  @Test
  public void testOrder() throws DagConstructionException {
    final List<String> expectedOrder = new ArrayList<>();
    expectedOrder.add("task1");
    expectedOrder.add("task2");
    expectedOrder.add("task21");
    expectedOrder.add("task3");
    expectedOrder.add("task31");
    expectedOrder.add("task32");
    expectedOrder.add("task33");

    class DummyTask implements DagTask {

      String id;

      public DummyTask(String id) {
        this.id = id;
      }

      @Override
      public String dagNodeId() {
        return id;
      }

      @Override
      public void submit(DagTaskCallback callback) {
        synchronized (expectedOrder) {
          String id = ((DagNode) callback).getId();
          System.out.println(id + " is ready to go");
          String expectedId = expectedOrder.get(0);
          Assert.assertEquals(expectedId, id);
          expectedOrder.remove(0);
          callback.succeed();
        }
      }

      @Override
      public Set<String> dagDependencies() {
        return Collections.EMPTY_SET;
      }

      @Override
      public void prepareToStart() {
      }

      @Override
      public String asJson() {
        throw new UnsupportedOperationException("Not supported yet."); 
      }

      @Override
      public void terminate() {
      }
    }

    Dag dag = new Dag("");
    dag.addNode("task1");
    dag.addDependency("task2", "task21");
    dag.addDependency("task3", "task31");
    dag.addDependency("task31", "task32");
    dag.addDependency("task32", "task33");
    dag.addDependency("task21", "task31");
    dag.addTask(new DummyTask("task1"));
    dag.addTask(new DummyTask("task2"));
    dag.addTask(new DummyTask("task3"));
    dag.addTask(new DummyTask("task21"));
    dag.addTask(new DummyTask("task31"));
    dag.addTask(new DummyTask("task32"));
    dag.addTask(new DummyTask("task33"));
    dag.start();
  }

  @Test
  public void testToJson() throws DagConstructionException {
    final List<String> expectedOrder = new ArrayList<>();
    expectedOrder.add("task1");
    expectedOrder.add("task2");
    expectedOrder.add("task21");
    expectedOrder.add("task3");
    expectedOrder.add("task31");
    expectedOrder.add("task32");
    expectedOrder.add("task33");

    class DummyTask implements DagTask {

      String id;

      public DummyTask(String id) {
        this.id = id;
      }

      @Override
      public String dagNodeId() {
        return id;
      }

      @Override
      public void submit(DagTaskCallback callback) {
        synchronized (expectedOrder) {
          String id = ((DagNode) callback).getId();
          String expectedId = expectedOrder.get(0);
          Assert.assertEquals(expectedId, id);
          expectedOrder.remove(0);
          callback.succeed();
        }
      }

      @Override
      public Set<String> dagDependencies() {
        return Collections.EMPTY_SET;
      }

      @Override
      public void prepareToStart() {
      }

      @Override
      public String asJson() {
        return String.format("\"name\": \"%s\", \"machin\": \"%s\"", id, "192.168.0.1");
      }

      @Override
      public String toString() {
        return id;
      }

      @Override
      public void terminate() {
      }
    }

    Dag dag = new Dag("");
    dag.addNode("task1");
    dag.addDependency("task2", "task21");
    dag.addDependency("task3", "task31");
    dag.addDependency("task31", "task32");
    dag.addDependency("task32", "task33");
    dag.addDependency("task21", "task31");
    dag.addTask(new DummyTask("task1"));
    dag.addTask(new DummyTask("task2"));
    dag.addTask(new DummyTask("task3"));
    dag.addTask(new DummyTask("task21"));
    dag.addTask(new DummyTask("task31"));
    dag.addTask(new DummyTask("task32"));
    dag.addTask(new DummyTask("task33"));
    String asJson = dag.asJson();
    String expectedResult = "["
        + "{id: 'task1', name: 'task1', machine: '192.168.0.1', status: 'WAITING', preds: '[]'},"
        + "{id: 'task2', name: 'task2', machine: '192.168.0.1', status: 'WAITING', preds: '[]'},"
        + "{id: 'task3', name: 'task3', machine: '192.168.0.1', status: 'WAITING', preds: '[]'},"
        + "{id: 'task33', name: 'task33', machine: '192.168.0.1', status: 'WAITING', preds: '[task32]'},"
        + "{id: 'task32', name: 'task32', machine: '192.168.0.1', status: 'WAITING', preds: '[task31]'},"
        + "{id: 'task21', name: 'task21', machine: '192.168.0.1', status: 'WAITING', preds: '[task2]'},"
        + "{id: 'task31', name: 'task31', machine: '192.168.0.1', status: 'WAITING', preds: '[task3,task21]'}]";
//    Assert.assertEquals(expectedResult, asJson);
    System.out.println(asJson);
  }
}
