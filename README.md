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

## Service Registry Implementation

1. **Update Zookeepeer Tree Format**
   - New tree structure in Zookeeper will have /registry to store available node addresses:
     ```
     /              (znode parent global)
     └── /registry  (children)
         └── /registry/c_1, /registry_2, etc.
     └── /election  (children)
         └── /election/c_1, /election/c_2, etc.
     ```
2. **Register nodes to registry**
     - Create znode under /registry to keep track of registry. This will be triggered when znode created and volunteers to leadership
   
3. **How to update registry nodes**
     - To update list of nodes I will call via a synchronized updateAddresses() method (atomic operation behavior) which will retrieve a list of nodes using /registry getChildren()
     - An instance variable will be declared to keep track of this List<String> allServicesAddresses. A local List<String> will be created and appended the znode value items every time this function is called, but znodes appended will only be appended if exists(node) is true.
     - In case znode not exists just skip it from being added.
     - Instance variable then is updated.
     - This function will be run for every single change to the zookeeper cluster, but could be specified to some specific ones  (like creation, deletion, etc.)

3. **How to retrieve registry nodes**
      - Create syncrhonized function getAllServicesAddresses() which will execute updateAddresses() first and then return instance variable from ServiceRegistryClass

4. **How to connect with reelection leader implementation**
      - Create class onClassElection which will instantiate with service registry (from my ServiceRegistry) and port (from nodes created in LeaderElection)
      - Two methods defined:
           - 1. onElectedToBeLeader(): I don't want the leader to be registered since he is the only one to have the addresses for all other nodes
                  -> unregisterFromCluster() which removes it from /registry children zNodes if exists there
                  -> Trigger manual update to /registry children zNodes
                
             3. onElectedToBeWorker(): I want worker to registerItself to /registry children if he doesn't exist there, then call function defined in ServiceRegistry for this via registerToCluster and pass proper formatted currentServerAddress.
      - Call onElectedToBeLeader() in the reelectLeader(), specifically when condition matches the current node as the smallest node.
      - Otherwise, onElectedToBeWoker() should be executed for the rest of nodes.
      - Not best performance, but allows room for improvement.

5. **Possible Improvements**
   - Define specific cases where methods should be triggered (like createNode, deleteNode) for serviceRegistry.
   - In case of node failure, recover node automatically by creating new node when event thread of Zookeeper is nodeDeleted type 
   
 Next steps to take: Incorporate Service Registry and Cluster Auto Healer with Zookeeper.

 Afterwards, I will be implementing my own Distributed Document Search
   - Apply Load Balancing theory to use with HAProxy.
   - Building Distributive Banking System to retrieve account balances and find fraudalent operatonis with Apache Kafka, Apache Flink/Streams, Apache Ignite, MongoDB/PostgreSQL
