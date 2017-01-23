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

// import com.sun.nio.sctp.*;

/**
 * Client class implements runnable interface which starts when start method is
 * called on a Client class thread.
 * This class contains methods to forward token to the next node in the path.
 */
public class Client implements Runnable {
    /**
     * Private variables related to Client class
     * @variable clientSocket Socket to be forwarded to the next node in the path.
     * @variable clientToken  Socket forwarded by the previous node in the path.
     * @variable allNodes     Hashmap with all the nodes details.
     */
    private Socket clientSocket;
    private Token clientToken;
    private HashMap<Integer, String> allNodes = new LinkedHashMap<Integer, String>();

    /**
     * Class method to instantiate the Client class.
     * @param  token    Token forwarded by the previous node in the path.
     * @param  allNodes Hashmap with all the nodes details.
     * @return null Nothing.
     */
    public Client(Token token, HashMap<Integer, String> allNodes) {
        this.clientToken = token;
        this.allNodes = allNodes;
    }

    /**
     * 
     */
    public Token getToken () {
        return this.clientToken;
    }

    /**
     * Method to open a socket to connect to the next node in the path.
     * 
     * @param hostname Hostname of the next node in the path.
     * @param port     Port on which the server is listening.
     * @exception IOException
     * @return Nothing.
     */
    public void sendToken(String hostname, int port) {
        try {
            System.out.println("Client: sendToken: Trying to send socket on " + hostname + ":" + port);
            this.clientSocket = new Socket(hostname, port);
            ObjectOutputStream outputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());

            outputStream.writeObject(this.clientToken);
            this.clientSocket.close();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.out.println("Client: sendToken: Server " + hostname + " on port " + port + " seems to be offline");
            System.out.println("Client: sendToken: Trying again after 2 seconds...");
            try {
                Thread.sleep(2000);//2 seconds
                this.sendToken(hostname, port);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    /**
     * Run method starts when start method is called a Client class thread.
     * Processes token to be forwarded
     * 
     * @return Nothing.
     */
    @Override
    public void run() {
        String path = this.clientToken.getPath();
        int clientId = Integer.parseInt(path.substring(0,1));
        String[] clientDetails = this.allNodes.get(clientId).split(",");
        String hostname = clientDetails[0];
        int port = Integer.parseInt(clientDetails[1]);
        sendToken(hostname, port);
    }
}
