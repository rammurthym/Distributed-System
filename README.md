# Distributed-System

### Description:
	Implemented a distributed system consisting of n nodes. The value of n and the location of each of
	the n node is specified in a configuration file. Every node selects a label value (basically an integer)
	uniformly at random from the interval [1, 10] in the beginning. Every node then circulates a token
	through the system that visits each node in the system in certain order and computes the sum of
	all the label values along the way. The path taken by the token of each node is again specified in
	the configuration file. This path is piggybacked on the token by the node that generated the token.
	At the end, each node prints its label value and the sum of all the label values computed by its
	token.

	Implemented using both TCP and SCTP sockets (implementation using SCTP sockets is commented out).

###### Execution:
	> ./launcher.sh

	Output:
    	$PROJDIR/rxm163730:{nodeid}.out

	Output Format:
	    Label: {label_value}
	    Sum of labels: {sum}
