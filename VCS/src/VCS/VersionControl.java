/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Guille
 */
public interface VersionControl extends Remote {
  
    public String commit(FileDescription[] files)
      throws RemoteException;
    
    public FileDescription[] checkout()
      throws RemoteException;
    
    public FileDescription[] update()
      throws RemoteException;
    
    public FileDescription[] updateServer(int id)
      throws RemoteException;
    
    public boolean requestEntry(int id, InetAddress ip)
      throws RemoteException;
}
