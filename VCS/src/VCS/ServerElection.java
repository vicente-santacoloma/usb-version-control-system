/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

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

  @Override
  public void run(){
    try{
      elections.setSoTimeout(1000);
    }catch(SocketException se){
      System.out.println(se.getMessage());
    }
  }
}
