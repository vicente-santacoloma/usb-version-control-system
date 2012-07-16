/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guille
 */
public class ServerElection extends Thread {

  private MulticastSocket elections;
  private VersionControlServer father;
  private byte[] bRecv;
  private byte[] bSend;
  private ByteArrayInputStream bin;
  private ByteArrayOutputStream bout;
  private ObjectInputStream ois;
  private ObjectOutputStream oos;
  private DatagramPacket pack;
  private InetAddress coordA;
  private BufferedReader bufIn;
  private Socket coordSocket, respSock;
  private ServerSocket resp;
  private String coordAlive;
  private Message msg;
  private PrintWriter pwout;

  /**
   * Constructor to build the election control thread
   *
   * @param elections
   * @param father
   */
  public ServerElection(MulticastSocket elections, VersionControlServer father) {
    this.elections = elections;
    this.father = father;
  }

  @Override
  public void run() {
    /*
     * Listen to sockets all the time and take the necessary actions
     */
    try {
      /*
       * Call elections to get the coordinators id
       */
      startElections();
      for (;;) {
        listenMulticast();
        /*
         * Check if coordinator is alive
         */
        if(father.getDns() == null) continue;
        coordA = father.getDns().get(father.getCoordId());
        coordSocket = new Socket(coordA, 41651);
        bufIn = new BufferedReader(new InputStreamReader(coordSocket.getInputStream()));
        coordSocket.setSoTimeout(3000);
        coordAlive = bufIn.readLine();

        coordSocket.close();
        /*
         * Coordinator is alive, close socket and continue to listen to
         * multicast
         */
        if (coordAlive != null) {
          bufIn.close();
          continue;
        }

        /*
         * If got here, assume coordinator is dead and start election
         */
        startElections();
      }
    } catch (SocketException se) {
      System.out.println(se.getMessage());
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    }
  }

  /**
   * Function that handles elections
   *
   * @return True if father has been elected coordinator, false otherwise
   */
  private boolean startElections() {
    try {
      System.out.println(father.getId() + ": Starting election process");

      msg = new Message(father.getId(), EnumMessageType.ELECTION_S);
      bout = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(bout);
      oos.writeObject(msg);
      bSend = bout.toByteArray();
      pack = new DatagramPacket(bSend, bSend.length, InetAddress.getByName("255.0.0.5"), 10602);

      if (resp != null && !resp.isClosed()) {
        resp.close();
      }
      resp = new ServerSocket(11149);
      resp.setSoTimeout(5000);
      elections.send(pack);
      respSock = null;
      try {
        respSock = resp.accept();
      } catch (IOException ioe) {
        try {
          System.out.println(father.getId() + ": No one responded, I'm coordinator");
          /*
           * I'm the new coordinator, notify all
           */

          msg = new Message(father.getId(), EnumMessageType.COORDINATOR);
          bout = new ByteArrayOutputStream();
          oos = new ObjectOutputStream(bout);
          oos.writeObject(msg);
          bSend = bout.toByteArray();
          pack = new DatagramPacket(bSend, bSend.length, InetAddress.getByName("255.0.0.5"), 10602);
          elections.send(pack);
          father.setCoordId(father.getId());
          CoordinatorFunctions cf = new CoordinatorFunctions(father);
          cf.start();
          return true;
        } catch (IOException ex) {
          System.out.println(ex.getMessage());
        }
      }


      /*
       * If here, a response was received, wait for coordinator
       */
      System.out.println(father.getId() + ": A response was received, waiting "
              + "for coordinator");
      int mcRet = listenMulticast();
      while ((mcRet == 1) || (mcRet == -1)) {
        mcRet = listenMulticast();
      }

      if (mcRet == 0) {
        System.out.println(father.getId() + ": Socket timed out, restarting "
                + "elections");
        return startElections();
      } else {
        System.out.println(father.getId() + ": Coordinator elected and saved");
        return false;
      }
    } catch (SocketException se) {
      System.out.println("Error Socket1: " + se.getMessage());
    } catch (IOException ioe) {
      System.out.println("Error Socket2: " + ioe.getMessage());
    }
    return false;
  }

  /**
   * Function that defines the operations to be done when an election package is
   * received
   *
   * @return 0 if no package was received, 1 if the package processed was
   * ELECTION_S 2 if package processed was COORDINATOR, -1 for invalid type
   */
  private int listenMulticast() {
    try {
      bRecv = new byte[65535];
      pack = new DatagramPacket(bRecv, bRecv.length, InetAddress.getByName("255.0.0.5"), 10602);
      elections.setSoTimeout(5000);
      System.out.println(father.getId() + ": Listening to the elections "
              + "multicast socket");

      try {
        elections.receive(pack);
      } catch (IOException ioe) {
        System.out.println(father.getId() + ": No package received");
        return 0;
      }


      /*
       * Received something, analize
       */
      bin = new ByteArrayInputStream(pack.getData());
      ois = new ObjectInputStream(bin);
      msg = (Message) ois.readObject();

      if (msg.getType() == EnumMessageType.COORDINATOR) {
        /*
         * A new coordinator has been elected
         */
        System.out.println(father.getId() + ": Received a COORDINATOR message"
                + " from: " + msg.getId());
        father.setCoordId(msg.getId());
        return 2;
      } else if (msg.getType() == EnumMessageType.ELECTION_S) {
        System.out.println(father.getId() + ": Received an ELECTION_S message "
                + "from " + msg.getId());
        if (msg.getId() <= father.getId()) {
          /*
           * Respond to the process that sent the message and start elections
           */
          respSock = new Socket(father.getDns().get(msg.getId()), 11149);
          pwout = new PrintWriter(respSock.getOutputStream());
          respSock.setSoTimeout(50);
          
          pwout.println("Sending response to election from " + father.getId());
          startElections();
        }
        return 1;
      } else {
        System.out.println("Message received on elections multicast "
                + "that is not of invalid type: " + msg.getType());
        return -1;
      }
    } catch (SocketException se) {
      System.out.println(se.getMessage());
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    } catch (ClassNotFoundException cne) {
      System.out.println(cne.getMessage());
    }
    return 0;
  }
}
