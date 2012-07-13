/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

/**
 *
 * @author Guille
 */
public class VersionControlServer extends RemoteObject implements VersionControl{
  private MulticastSocket elections;
  private MulticastSocket messages;

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

  public static void main(String[] args){

  }
}
