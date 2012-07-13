/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Dictionary;

/**
 *
 * @author Guille
 */
public class VersionControlServer extends RemoteObject implements VersionControl{
  private MulticastSocket elections;
  private MulticastSocket messages;
  private int coordId;
  private Dictionary<Integer, InetAddress> dns;
  private InetAddress multicastAddres = 225.0.0.5;

  /**
   * Constructor to build a new version control server
   * 
   * @param elections
   * @param messages 
   */ 
  public VersionControlServer(MulticastSocket elections, MulticastSocket messages) {
    this.elections = elections;
    this.messages = messages;
  }
    
  @Override
  public String commit(FileDescription[] files)
    throws RemoteException{

    return null;
  }

  @Override
  public FileDescription[] checkout()
    throws RemoteException{

    return null;
  }

  @Override
  public FileDescription[] update()
    throws RemoteException{

    return null;
  }

  @Override
  public FileDescription[] updateClient(int id)
    throws RemoteException{

    return null;
  }


  //Getters
  public int getCoordId() {
    return coordId;
  }


  public Dictionary<Integer, InetAddress> getDns() {
    return dns;
  }

  public MulticastSocket getElections() {
    return elections;
  }

  public MulticastSocket getMessages() {
    return messages;
  }
  
   public static void main(String[] args) throws InterruptedException{
     
     //parametro de entrada ip rmiregistry
     
     
    
    /*
     llamo a eleccion y hago join con este hilo
     si soy electo, hago cosas de coordinador hasta q me muera
     * 
     * hilos: servercommunitation
     * serverelection
     * falta uno
     */
     
    /*updateclient = actualizar los archivos*/
    
    Thread election = new ServerElection();
    Thread listenMessages = new ServerCommunication();
    
    election.start();
    listenMessages.start();
    
    election.join();
    
    /* coordinador se inscribe en rmi y resuelve peticiones del cliente */
    
    

  }

}