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
    Dag dag = new Dag();
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
    Dag dag = new Dag();
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
    Dag dag = new Dag();
    dag.addNode("apt-get essentials on hopshub1");
    dag.addNode("apt-get essentials on ndb1");
    dag.addNode("apt-get essentials on ndb2");
    dag.addNode("apt-get essentials on datanodes1");
    dag.addDependency("apt-get essentials on hopshub1", "install berkshelf on hopshub1");
    dag.addDependency("install berkshelf on hopshub1", "make solo.rb on hopshub1");
    dag.addDependency("make solo.rb on hopshub1", "clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on hopshub1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on hopshub1", "hadoop::install on hopshub1");
    dag.addDependency("hadoop::nn on hopshub1", "hadoop::rm on hopshub1");
    dag.addDependency("hadoop::install on hopshub1", "hadoop::rm on hopshub1");
    dag.addDependency("hadoop::install on hopshub1", "hadoop::nn on hopshub1");
    dag.addDependency("hadoop::nn on hopshub1", "hadoop::jhs on hopshub1");
    dag.addDependency("hadoop::nn on hopshub1", "hadoop::jhs on hopshub1");
    dag.addDependency("hadoop::install on hopshub1", "hadoop::jhs on hopshub1");
    dag.addDependency("hadoop::dn on datanodes1", "hadoop::jhs on hopshub1");
    dag.addDependency("make solo.rb on hopshub1", "clone and vendor cookbooks/hopshadoop/ndb-chef on hopshub1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/ndb-chef on hopshub1", "ndb::install on hopshub1");
    dag.addDependency("ndb::ndbd on ndb1", "ndb::mysqld on hopshub1");
    dag.addDependency("ndb::install on hopshub1", "ndb::mysqld on hopshub1");
    dag.addDependency("ndb::mgmd on hopshub1", "ndb::mysqld on hopshub1");
    dag.addDependency("ndb::ndbd on ndb2", "ndb::mysqld on hopshub1");
    dag.addDependency("ndb::install on hopshub1", "ndb::mgmd on hopshub1");
    dag.addDependency("make solo.rb on hopshub1", "clone and vendor cookbooks/hopshadoop/hopshub-chef on hopshub1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/hopshub-chef on hopshub1", "hopshub::install on hopshub1");
    dag.addDependency("hadoop::nn on hopshub1", "hopshub::default on hopshub1");
    dag.addDependency("hopshub::install on hopshub1", "hopshub::default on hopshub1");
    dag.addDependency("ndb::mysqld on hopshub1", "hopshub::default on hopshub1");
    dag.addDependency("hadoop::dn on datanodes1", "hopshub::default on hopshub1");
    dag.addDependency("apt-get essentials on ndb1", "install berkshelf on ndb1");
    dag.addDependency("install berkshelf on ndb1", "make solo.rb on ndb1");
    dag.addDependency("make solo.rb on ndb1", "clone and vendor cookbooks/hopshadoop/ndb-chef on ndb1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/ndb-chef on ndb1", "ndb::install on ndb1");
    dag.addDependency("ndb::install on ndb1", "ndb::ndbd on ndb1");
    dag.addDependency("ndb::mgmd on hopshub1", "ndb::ndbd on ndb1");
    dag.addDependency("apt-get essentials on ndb2", "install berkshelf on ndb2");
    dag.addDependency("install berkshelf on ndb2", "make solo.rb on ndb2");
    dag.addDependency("make solo.rb on ndb2", "clone and vendor cookbooks/hopshadoop/ndb-chef on ndb2");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/ndb-chef on ndb2", "ndb::install on ndb2");
    dag.addDependency("ndb::mgmd on hopshub1", "ndb::ndbd on ndb2");
    dag.addDependency("ndb::install on ndb2", "ndb::ndbd on ndb2");
    dag.addDependency("apt-get essentials on datanodes1", "install berkshelf on datanodes1");
    dag.addDependency("install berkshelf on datanodes1", "make solo.rb on datanodes1");
    dag.addDependency("make solo.rb on datanodes1", "clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on datanodes1");
    dag.addDependency("clone and vendor cookbooks/hopshadoop/apache-hadoop-chef on datanodes1", "hadoop::install on datanodes1");
    dag.addDependency("hadoop::rm on hopshub1", "hadoop::nm on datanodes1");
    dag.addDependency("hadoop::install on datanodes1", "hadoop::nm on datanodes1");
    dag.addDependency("hadoop::nn on hopshub1", "hadoop::dn on datanodes1");
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

    }

    Dag dag = new Dag();
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
    }

    Dag dag = new Dag();
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
}
