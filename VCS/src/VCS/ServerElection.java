/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.*;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 *
 * @author Guille
 */
public class ServerElection extends Thread{
  private MulticastSocket elections;
  private ServerSocket waiter;
  private VersionControlServer father;
  private Socket coord;

  /**
   * Constructor to build the election control thread
   * @param elections
   * @param father 
   */
  public ServerElection(MulticastSocket elections, VersionControlServer father) {
    this.elections = elections;
    this.father = father;
  }

  ServerElection() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void run(){
    //Buffers for reading and writing on packets
    byte[] bRecv = new byte[65535];
    byte[] bSend;
    ByteArrayInputStream bin;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectInputStream ois;
    ObjectOutputStream oos;
    try{
      elections = new MulticastSocket(810602);
      elections.joinGroup(father.getMulticastAddress());
      
    }catch(SocketException se){
      System.out.println(se.getMessage());
    }catch(IOException ioe){
      System.out.println(ioe.getMessage());
    }
  }
}
