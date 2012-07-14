/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.*;
import java.net.*;

/**
 *
 * @author Guille
 */
public class ServerElection extends Thread{
  private MulticastSocket elections;
  private ServerSocket waiter;
  private VersionControlServer father;
  private Socket coord;
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
   * @param elections
   * @param father 
   */
  public ServerElection(MulticastSocket elections, VersionControlServer father) {
    this.elections = elections;
    this.father = father;
  }
  
  @Override
  public void run(){
    //Buffers for reading and writing on packets

    /*Listen to sockets all the time and take the necessary actions*/
    try{
      for(;;){

        listenMulticast();
       /*Check if coordinator is alive*/
        coordA = father.getDns().get(father.getCoordId());
        coordSocket = new Socket(coordA, 41651);
        bufIn = new BufferedReader(new InputStreamReader(coordSocket.getInputStream()));
        coordSocket.setSoTimeout(3000);
        coordAlive = bufIn.readLine();

        /*Coordinator is alive, close socket and continue to listen to multicast*/
        if(coordAlive != null){
            bufIn.close();
            continue;
        }

        coordSocket.close();
        /*If got here, assume coordinator is dead and start election*/
        if(startElections()){
          return;
        }
      }
    }catch(SocketException se){
      System.out.println(se.getMessage());
    }catch(IOException ioe){
      System.out.println(ioe.getMessage());
    }
  }
  
  /**
   * Function that handles elections
   * 
   * @return True if father has been elected coordinator, false otherwise
   */
  private boolean startElections(){
    try{
      msg = new Message(father.getId(), EnumMessageType.ELECTION_S);
      oos = new ObjectOutputStream(bout);
      oos.writeObject(msg);
      bSend = bout.toByteArray();
      pack = new DatagramPacket(bSend, bSend.length);
      elections.send(pack);
      if(!resp.isClosed())
        resp.close();
      resp = new ServerSocket(11149);
      resp.setSoTimeout(5000);
      respSock = resp.accept();

      /*I'm the new coordinator, notify all*/
      if(respSock == null){
        /*I'm the new coordinator, notify and return*/
        msg = new Message(father.getId(), EnumMessageType.COORDINATOR);
        bout = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bout);
        oos.writeObject(msg);
        bSend = bout.toByteArray();
        pack = new DatagramPacket(bSend, bSend.length);
        elections.send(pack);
        return true;
      }
      
      /*If here, a response was received, wait on multicast for coordinator*/
      int mcRet = listenMulticast();
      while((mcRet == 1) || (mcRet == -1))
        mcRet = listenMulticast();
      
      if(mcRet == 0){
        startElections();
      }else{
        return false;
      }
    }catch(SocketException se){
      System.out.println(se.getMessage());
    }catch(IOException ioe){
      System.out.println(ioe.getMessage());
    }
    return false;
  }
  
  /**
   * Function that defines the operations to be done when an election package 
   * is received
   * 
   * @return 0 if no package was received, 1 if the package processed was ELECTION_S
   *         2 if package processed was COORDINATOR, -1 for invalid type
   */
  private int listenMulticast(){
    try{
      bRecv = new byte[65535];
      pack = new DatagramPacket(bRecv, bRecv.length);
      elections.setSoTimeout(1000);
      elections.receive(pack);
      if(pack.getLength() == 0)
        return 0;
      
      /*Received something, analize*/
      bin = new ByteArrayInputStream(pack.getData());
      ois = new ObjectInputStream(bin);
      msg = (Message) ois.readObject();
      
      if(msg.getType() == EnumMessageType.COORDINATOR){
        /*A new coordinator has been elected*/
        father.setCoordId(msg.getId());
        return 2;
      }else if(msg.getType() == EnumMessageType.ELECTION_S){
        if(msg.getId() > father.getId()){
          /*Respond to the process that sent the message and start elections*/
          respSock = new Socket(father.getDns().get(msg.getId()), 11149);
          pwout = new PrintWriter(respSock.getOutputStream());
          pwout.println("Sending response to election from " + father.getId());
          startElections();
        }
        return 1;
      }else{
        System.out.println("Message received on multicast port: 10602 "
                + "that is not of a valid type");
        return -1;
      }
    }catch(SocketException se){
      System.out.println(se.getMessage());
    }catch(IOException ioe){
      System.out.println(ioe.getMessage());
    }catch(ClassNotFoundException cne){
      System.out.println(cne.getMessage());
    }
    return 0;
  }
}
