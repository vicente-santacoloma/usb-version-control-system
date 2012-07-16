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
  
    public EnumVCS commit(FileDescription[] files)
      throws RemoteException;
    
    public Object[] checkout()
      throws RemoteException;
    
    public Object[] update()
      throws RemoteException;
    
    public Object[] updateServer(int id)
      throws RemoteException;
    
    public boolean requestEntry(int id, InetAddress ip)
      throws RemoteException;
}
