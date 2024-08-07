﻿# Java-Reelection-Leader

The following is my first ditributive system algorithm: Leader Election using Apache Zookeeper and Java. 

First Draft of Design looks like this:
  -> 1. Create Zookeeper object --> 2 ADDITIONAL threads are created (event and IO thread)
  -> 2. Using zkCli to simulate having multiple nodes
	-> 3. Every nodes connected to Zookeper volunteers to become a leader in main,
			-> This is done by submitting its candidacy by adding a znode that represents itself under the election znode parents
			-> Tree format in Zookeeper: 
				-> / (/ zNode parent global)/ -> One children (/election) / -> lots of chidren / (/election/c_1), etc /
			-> Zookeeper mantains gloabl order of addition (names according to addition) - Using this for my advantage :)
	-> 4. After all znodes copies added to /election, it would query the current children of the election parent ( which is in order of addition)
	-> 5. If first zNode in election is the smallest number then, this is the leader. Otherwise, we know it is not the the leader and now is waiting from instructions from elected leader

 Next steps to take: Finalize algorithm and incorporate Service Registry and Cluster Auto Healer with Zookeeper.

 Afterwards, I will be implementing my own Distributed Document Search
   - Apply Load Balancing theory to use with HAProxy.
   - Building Distributive Banking System with Apache Kafka
   - Dynamix Sharding, Database Replication and Quorum Consensus in MongoDB
   - MultiRegion Deployed and global load balancing
