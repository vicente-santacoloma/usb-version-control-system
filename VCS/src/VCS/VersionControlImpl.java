/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  public FileDescription[] updateClient(int id)
          throws RemoteException {

    return null;
  }
}
