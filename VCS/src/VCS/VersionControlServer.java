/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashMap;
/**
 *
 * @author Guille
 */
public class VersionControlServer{
  
  private MulticastSocket elections;
  private MulticastSocket messages;
  private int coordId;
  private HashMap<Integer, InetAddress> dns;
  private InetAddress multicastAddress;
  private int id;

  /**
   * Constructor to build a new version control server
   * 
   * @param elections
   * @param messages 
   */ 
  public VersionControlServer(MulticastSocket elections, MulticastSocket messages){
    this.elections = elections;
    this.messages = messages;
    
    try {
      this.multicastAddress = InetAddress.getByName("225.0.0.5");
    } catch( UnknownHostException e ) {
      System.out.println( e );
    }
  }
  
  //Getters
  public int getId() {
    return id;
  }

  public int getCoordId() {
    return coordId;
  }

  public HashMap<Integer, InetAddress> getDns() {
    return dns;
  }

  public MulticastSocket getElections() {
    return elections;
  }

  public MulticastSocket getMessages() {
    return messages;
  }

  public InetAddress getMulticastAddress() {
    return multicastAddress;
  }
  
  //Setters
  public void setCoordId(int coordId) {
    this.coordId = coordId;
  }

   public static void main(String[] args) throws InterruptedException, RemoteException, IOException{
     
    //parametro de entrada ip rmiregistry
     
    InetAddress group = InetAddress.getByName("225.0.0.5");
     
    MulticastSocket s = new MulticastSocket(10602);
    
    MulticastSocket p = new MulticastSocket(41895);
     
    s.joinGroup(group);
    p.joinGroup(group);
     
    VersionControlServer v = new VersionControlServer(s, p);
    VersionControlImpl vci = new VersionControlImpl(p,v.dns);
    
    /*
     llamo a eleccion y hago join con este hilo
     si soy electo, hago cosas de coordinador hasta q me muera
     * 
     * hilos: servercommunitation
     * serverelection
     * falta uno
     */
     
    /*updateclient = actualizar los archivos*/
    
    Thread election = new ServerElection(s, v);
    Thread listenMessages = new ServerCommunication();
    
    election.start();
    listenMessages.start();
    
    election.join();
    
    /* coordinador se inscribe en rmi y resuelve peticiones del cliente */
    
    Naming.rebind("VCS", vci);
   
    // escuchar por un socket particular
    
    Socket clientSocket = null;
    
    ServerSocket acceptS = null;
    
    try {
      acceptS = new ServerSocket(41651);
    } catch (IOException e) {
      System.err.println("Could not listen on port.");
      System.exit(1);
    }
    
    PrintWriter out; 
    String inputLine = "coordinator alive";
    
    while(true){
    
      clientSocket = acceptS.accept();
      
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      
      out.println(inputLine);
      
    }
    
  }
}