/**
 * @author  Rammurthy Mudimadugula
 * @netid   rxm163730
 * @version 1.0
 * @since   2016-09-22
 *
 * CS6370 - Advanced Operating Systems - Project #1
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.atomic.AtomicInteger;

// import com.sun.nio.sctp.*;

/**
 * Node class implements methods to run node as server and as client.
 * Server starts listening to the incoming sockets. Accepts a socket when arrives
 * and starts a separate thread to process it.
 * Client computes the label sum and forwards the socket to the next destination.
 */

public class Node implements Runnable {
    public static AtomicInteger numOfCompleteMsgs = new AtomicInteger();
    /**
     * Private variables related to Node class.
     * @variable label        Label value of the node.
     * @variable identifier   ID of the node.
     * @variable configPath   Path of the config file.
     * @variable port         Port on which the server starts listening.
     * @variable hostname     Address of the node in network.
     * @variable token        Token which is forwarded to this node.
     * @variable type         Type of the node, either server or client.
     * @variable numOfNodes   Number of nodes in the network.
     * @variable serverSocket Server Socket object to bind port address.
     * @variable allNodes     Details of the each node in the network.
     * @variable tokenPaths   Paths for each node token to be traversed.
     */

    private int identifier;
    private int port = 0;
    private final int label;
    private String hostname = null;
    private Token token;
    private String configPath;
    private String type;
    private int numOfNodes;
    private ServerSocket serverSocket = null;
    private HashMap<Integer, String> allNodes   = new LinkedHashMap<Integer, String>();
    private HashMap<Integer, String> tokenPaths = new LinkedHashMap<Integer, String>();
    private boolean isStopped = false;

    // private SocketAddress serverSocket;
    // private SctpServerChannel sctpServerChannel;

    /**
     * Class method to instantiate the Node class.
     * @param  id    ID of the node.
     * @param  path  Path of the config file.
     * @param  label Randomly generated number.
     * @param  type  Type of the node, either server or client.
     * @return null Nothing.
     */
    public Node(int id, int label, String path, String type) {
        this.identifier = id;
        this.label      = label;
        this.configPath = path;
        this.type       = type;
    }

    public static void update() {
        this.numOfCompleteMsgs += 1;
    }

    public static int get() {
        return Integer.parseInt(this.numOfCompleteMsgs);
    }

    /**
     * 
     */
    public synchronized boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Method to run node as server. Server starts listening on the port 
     * specified and accepts the socket as soon as it is received it and
     * starts a separate to process the socket and starts listening for incoming
     * sockets again.
     *
     * @exception Throws IOException+.
     * @return null Nothing.
     */
    private void runAsServer() throws IOException {
        readFile(configPath);
        if ((this.hostname != null) && (this.port != 0)) {
            try {
                this.serverSocket = new ServerSocket(this.port);
                while(!isStopped()) {
                    MultiThreadedServer client = new MultiThreadedServer(this.serverSocket.accept(), this.label, this.identifier, this.allNodes);
                    new Thread(client).start(); 
                }

                // this.serverSocket = new InetSocketAddress(this.port); 
                // this.sctpServerChannel =  SctpServerChannel.open().bind(serverSocketAddress);

                // while ((sctpChannel = sctpServerChannel.accept()) != null) { 
                //     MultiThreadedServer client = new MultiThreadedServer(this.sctpServerChannel.accept(), this.label, this.identifier, this.allNodes);
                //     new Thread(Client).start();
                // }
            } catch (IOException e) {
                throw new RuntimeException("Node: runAsServer: Cannot open port on " + this.port, e);
            }
        } else {
            System.out.println("Node: runAsServer: Error: Hostname and port are not set.");
            System.out.println("Node: runAsServer: Exiting program...");
            System.exit(1);
        }
    }

    /**
     * Method to run node as client. Client instantiates a token to be piggy
     * backed to the socket. Adds it's own label value and forwards to the next
     * node.
     *
     * @exception Throws IOException+.
     * @return null Nothing.
     */
    private void runAsClient() throws IOException {
        readFile(configPath);
        String path = this.tokenPaths.get(this.identifier);

        if (path != null) {
            path = path + "," + this.identifier;
            this.token = new Token(this.identifier, path, this.label, path);
            Client client = new Client(this.token, this.allNodes);
            new Thread(client).start();
        } else {
            createFile();
            // TO-DO
            // Broadcast complete message
            sendCompleteMsg();
        }
    }

    /**
     * Method to broadcast  COMPLETE message once traversal is complete.
     * 
     * @return Nothing
     */
    private void sendCompleteMsg() {
        for (Integer key : this.allNodes.keySet()) {
            if (this.identifier != key) {
                String[] clientDetails = this.allNodes.get(key).split(",");
                Token statusToken = new Token(this.identifier, Integer.toString(key), this.label, Integer.toString(key));
                statusToken.setStatus("COMPLETE");
                Client newClient = new Client(statusToken, this.allNodes);
                new Thread(newClient).start();
            }
        }
    }

    /**
     * Method to create file and write ID and Label values to it.
     *
     * @return Nothing
     */
    private void createFile() throws IOException {
        File newFile = new File("rxm163730:" + this.identifier + ".out");
        File file = new File("rxm163730:" + this.identifier + ".out");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("Label: " + this.label + "\n");
        fileWriter.write("Sum of labels: " + this.label + "\n");
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * Method to open for the file if exists or throw an IOException.
     * If the file exists, open it and read the input file line by line.
     * Set appropriate variable values.
     *
     * @param path Path of the configuration file.
     * @exception Throws IOException+.
     * @return null Nothing.
     */
    public void readFile(String path) throws IOException {
        try {
            FileReader configFile = new FileReader(path);
            BufferedReader bufRead = new BufferedReader(configFile);
            List<String> list = new ArrayList<String>();

            String currentLine = null;
            while ((currentLine = bufRead.readLine()) != null) {
                currentLine = currentLine.trim();
                if (!currentLine.startsWith("#") && !currentLine.isEmpty()) {
                    list.add(currentLine);
                }
            }

            this.numOfNodes = Integer.parseInt(list.get(0));

            for (int i = 1; i <= this.numOfNodes; i++) {
                String[] temp1 = list.get(i).split("\\s+");
                this.allNodes.put(Integer.parseInt(temp1[0]), temp1[1] + "," + temp1[2]);
                if (Integer.parseInt(temp1[0]) == this.identifier) {
                    this.hostname = temp1[1];
                    this.port     = Integer.parseInt(temp1[2]);
                }
            } 

            for (int i = this.numOfNodes + 1; i < list.size(); i++) {
                String[] temp2 = list.get(i).split("\\s+");
                Matcher m = Pattern.compile("\\((.*?)\\)").matcher(list.get(i));
                m.find();
                this.tokenPaths.put(Integer.parseInt(temp2[0]), m.group(1).replaceAll("\\s+",""));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Node: readFile: Unable to open the file: " + path);
        }
    }

    /**
     * Run method starts when a start method is called on a Node class thread.
     * Stars the node as either server or client.
     *
     * @exception Throws IOException+.
     * @return null Nothing.
     */
    @Override
    public void run() {
        if (type == "server") {
            try {
                runAsServer();
            } catch (IOException e) {
                System.out.println("Node: run: Error: Config file not found.");
                System.out.println("Node: run: Exiting program...");
                System.exit(1);
            }
        } else {
            try {
                runAsClient();
            } catch (IOException e) {
                System.out.println("Node: run: Error: Config file not found.");
                System.out.println("Node: run: Exiting program...");
                System.exit(1);
            }
        }
    }
}
