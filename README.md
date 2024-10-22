# Java-Reelection-Leader

The following is my first ditributive system algorithm: Leader Election using Apache Zookeeper and Java. 

## First Draft of Design

1. **Create Zookeeper Object**
   - Two additional threads are created: event thread and IO thread.

2. **Using zkCli to Simulate Multiple Nodes**

3. **Node Leadership Election**
   - Every node connected to Zookeeper volunteers to become a leader.
   - This is done by submitting its candidacy by adding a znode that represents itself under the election znode parent.

4. **Zookeeper Tree Format**
   - Tree structure in Zookeeper:
     ```
     /              (znode parent global)
     └── /election  (children)
         └── /election/c_1, /election/c_2, etc.
     ```
   - Zookeeper maintains a global order of addition (names according to addition), which is used to determine leadership.

5. **Leader Election Process**
   - After all znodes are added to `/election`, query the current children of the election parent (which are in order of addition).
   - The first znode in the election is the leader (smallest number).
   - If a node is not the leader, it waits for instructions from the elected leader.

5. **Leader Reelection Process**
      - To reduce the load and avoid unnecessary events (such as nodeDeleted events being triggered multiple times), I implemented a strategy where each node watches only one other node,          rather than all          nodes watching the leader. This minimizes the number of events the system has to handle.
         In this strategy, the nodes are arranged in a similar structure to linked list, where each node watches the node directly preceding it in the sequence. For example:
         n1(leader) <- n2 <- n3 <- n4 <- n5
         n1 is the leader, and n2 is watching n1. n3 is watching n2, and so on. This ensures that when a node fails or is deleted, only the immediate follower node is notified.
      - Since watchers are triggered only once per event, when a node is deleted, the system must:
            Run the reelection process, where the next available node in the sequence becomes the leader.
            Attach a new watcher to ensure the chain is re-established correctly, ensuring continuous synchronization.



 Next steps to take: Finalize algorithm and incorporate Service Registry and Cluster Auto Healer with Zookeeper.

 Afterwards, I will be implementing my own Distributed Document Search
   - Apply Load Balancing theory to use with HAProxy.
   - Building Distributive Banking System with Apache Kafka
   - Dynamix Sharding, Database Replication and Quorum Consensus in MongoDB
   - MultiRegion Deployed and global load balancing
