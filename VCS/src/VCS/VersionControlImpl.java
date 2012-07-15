/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashMap;

/**
 *
 * @author Guille
 */
public class VersionControlImpl extends RemoteObject implements VersionControl {

  private MulticastSocket _messages;
  private HashMap<Integer, InetAddress> _dns;

  public VersionControlImpl(MulticastSocket messages, HashMap dns) {
    super();

    _messages = messages;
    _dns = dns;
  }

  @Override
  public String commit(FileDescription[] files)
          throws RemoteException {
    
    Message mensaje = new Message();
    
    ByteArrayOutputStream bs= new ByteArrayOutputStream();
    ObjectOutputStream os = new ObjectOutputStream (bs);
    os.writeObject(mensaje);  // this es de tipo DatoUdp
    os.close();
    byte[] bytes =  bs.toByteArray();
    
    DatagramPacket paquete = new DatagramPacket(bytes, bytes.length);
    _messages.send(paquete);
    
   
    for (int i = 0; i < files.length; i++) {
      
      
    }

    return null;
  }

  @Override
  public FileDescription[] checkout()
          throws RemoteException {

    return null;
  }

  @Override
  public FileDescription[] update()
          throws RemoteException {

    return null;
  }

  @Override
  public FileDescription[] updateServer(int id)
    throws RemoteException{

    return null;
  }

  @Override
  public boolean requestEntry(int id, InetAddress ip) 
    throws RemoteException {
    
    return false;
  }
  
}
