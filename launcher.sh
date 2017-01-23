#!/bin/bash

netid=rxm163730

# Root directory of your project
PROJDIR=$HOME/CS6378/RXM163730_Project1

CONFIG=$PROJDIR/config.txt
# CONFIG=$PROJDIR/config.txt

# Directory your java classes are in
BINDIR=$PROJDIR/bin

# Your main project class
PROG=Start

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "\s*[0-9]\+\s*\w\+.*" |
(
    while read line 
    do

	n=$( echo $line | awk '{ print $1 }' )
	host=$( echo $line | awk '{ print $2 }' )
		
        ssh -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG $n $CONFIG &
    done
)
