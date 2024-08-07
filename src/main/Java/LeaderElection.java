import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.zookeeper.*;
ter
public class LeaderElection implements Watcher {
    // Address where the Zookeeper server is stored
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final String ELECTION_NAMESPACE = "/election";
    // Timeout to check communication with its client and Zookeeper
    private static final int SESSION_TIMEOUT = 3000;
    private String currentZnodeName;
    private ZooKeeper zooKeeper;

    //Multiple LeaderElection instances created for each zNode connecting to zookeeper and having its own main
    public static void main(String[] args) {
        LeaderElection leaderElection = new LeaderElection();
        try {
            leaderElection.connectToZookeeper();
            //Volunteer to election
            leaderElection.volunteerForLeadership();
            leaderElection.electLeader();
            leaderElection.run();
            //Once main thread wakes up by notifyAll
            leaderElection.close(); //close resources
            System.out.println("Disconnected from zookeeper");
        } catch (Exception e) {
            System.out.println("e");
        }
    }

    //Methods to volunteer leadership
    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String znodePrefix = ELECTION_NAMESPACE + "/c_";
        //empty byte data, access control list, ephemeral sequential (once disconnected, zNode deleted, sequential order insertion)
        String zNodeFullPath = zooKeeper.create(znodePrefix, new byte[] {}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("zNode name" + zNodeFullPath);
        //Remove /election part and keep /c_numberGiven
        //This is done to give an id to the current instance (in this case /c_0 or /c_1
        //This instance will be checked among the election children and each instance will execute a for loop to identify whether it matches the smallest number from sorting.
        this.currentZnodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    //Get the zNode children from the /election and elect a leader based on 2 cases:
    public void electLeader() throws KeeperException, InterruptedException{
        //whether we need to watch node (second parameter). Array in unsorted order
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
        Collections.sort(children);
        String smallestChild = children.get(0);
        //Each instance has a unique id from zNode creation. Use that to identify whether is a leader or not. Smallest one is leader. (Only one instance will execute this code)
        if (smallestChild.equals(this.currentZnodeName)) {
            System.out.println("I am the leader");
            return;
        }
        System.out.println("I am not the leader. This is the leader: " + smallestChild);

    }


    //In case, zookeeper disconnects we want to wake up the main thread to close resources and exit
    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    //Allow to wait for zookeeper. Main thread put to a wait state for zookeeper response
    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }
    public void connectToZookeeper() throws IOException {
        // Zookeeper is async and event-driven. To keep track of connections, register an event handler and pass it to Zookeeper as a watcher object implemented
        // from the Watcher interface and the process method
        //Passing the currrent instance of LeaderElection to zookeeper
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    // Called by Zookeeper on a separate thread (one of those 2) when a new event comes from the Zookeeper server
    // Identify event
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                // General Zookeeper events have None type
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    // Successful connection
                    System.out.println("Successful connection to server");
                } else {
                    //This is run when an execution event runs (in this case disconnected is run
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from zookeeper event");
                        //To wake up the main thread
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
