/**
 * This program implements a distributed system of n nodes.
 * The value of n and the location of each of the node is specified in a 
 * config file.
 * Every nodes selects a label value, an integer between 0-10, uniformly at
 * random. Every node then circulates a token through the system that visits
 * each node in the system in certain order (specified in the config file)
 * and computes the sum of all the label values along the way.
 * The nodes communicate with each via server and client implemented in
 * TCP protocol.
 *
 * @author  Rammurthy Mudimadugula
 * @netid   rxm163730
 * @version 1.0
 * @since   2016-09-22
 *
 * CS6370 - Advanced Operating Systems - Project #1
 */

import java.util.Random;

/**
 * Start class implements Runnable interface which starts individual thread for
 * server and client.
 * This class also computes random number between and 0-10 and assign the label
 * value.
 * The main function takes two arguments, nodeid and config file path from
 * commandline.
 */

public class Start implements Runnable {
    /**
     * Private variables related to Start class.
     * @variable label      Label value of the node.
     * @variable identifier ID of the node.
     * @variable configPath Path of the config file.
     */

    private int identifier;
    private int label;
    private String configPath;

    /**
     * Class method to instantiate the Start class.
     * @param  id   ID of the node.
     * @param  path Path of the config file.
     * @return null Nothing.
     */
    public Start(int id, String path) {
        this.identifier = id;
        this.configPath = path;
        this.label      = randomNumberGenerator(1, 10);
    }

    /**
     * Method to generate a uniformly random value between min and max integers.
     * @param  min Minimum value to generate random number.
     * @param  max Maximum value to generate random number.
     * @return int random number between min and max integers.
     */
    private static int randomNumberGenerator(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt(max-min) + min;
        return randomNum;
    }
    
    /**
     * Method to start node thread as either server or client
     * @param  type Specifies whether to start node as server or client.
     * @return null Nothing.
     */
    private void startNode(String type) {
        Node node = new Node(this.identifier, this.label, this.configPath, type);
        Thread t = new Thread(node);
        t.start();
    }

    /**
     * Run method is called when start method is called on a Start class thread.
     * This method starts node as server and client 2 seconds apart in separate
     * thread.
     * @exception Throws InterruptedException.
     * @return null Nothing.
     */
    @Override
    public void run() {
        startNode("server");
        try {
            Thread.sleep(2000); // Sleep for 2 seconds before starting node as client
            startNode("client");
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Main method takes two arguments from commandline and instantiates Start
     * class.
     * @param  args first  ID of the node.
     * @param  args second Path of the config file.
     * @return null Nothing.
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            int id = Integer.parseInt(args[0]);
            String path = System.getProperty("user.dir") + "/" + args[1];
            Start start = new Start(id, path);
            Thread t = new Thread(start);
            t.start();
        } else {
            System.out.println("USAGE: java Start 0 /config/file/path/config.txt");
        }
    }
}
