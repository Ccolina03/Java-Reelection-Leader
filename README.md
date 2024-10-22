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
   - To avoid making all nodes watch the leader and avoid lots of unnecessary events and increased load to be handled (i.e. nodeDeleted type should ideally only be prompted once) we can specify a node to watch only one at all times.
   - In this case, the strategy would look like a linked list where n represents node and what what each node is watching for is the previous node:
        n1(leader) <- n2 <- n3 <- n4 <- n5
   - watchers are trigger once per event so we have to run the reelection function if node deleted to make sure they are synchronized



 Next steps to take: Finalize algorithm and incorporate Service Registry and Cluster Auto Healer with Zookeeper.

 Afterwards, I will be implementing my own Distributed Document Search
   - Apply Load Balancing theory to use with HAProxy.
   - Building Distributive Banking System with Apache Kafka
   - Dynamix Sharding, Database Replication and Quorum Consensus in MongoDB
   - MultiRegion Deployed and global load balancing
