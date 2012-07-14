/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

/**
 *
 * @author Guille
 */
public class VersionControlImpl extends RemoteObject implements VersionControl{
    
  
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
  
}
