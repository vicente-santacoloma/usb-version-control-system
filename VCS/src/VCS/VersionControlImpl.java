/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import com.sun.org.apache.xml.internal.serializer.utils.Messages;
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
import org.dom4j.Document;

/**
 *
 * @author Guille
 */
public class VersionControlImpl extends RemoteObject implements VersionControl {

  private MulticastSocket _messages;
  private HashMap<Integer, InetAddress> _dns;
  private String _configFile;
  public VersionControlImpl(MulticastSocket messages, HashMap dns,String configFile) {
    super();

    _messages = messages;
    _dns = dns;
    _configFile = configFile;
  }

  @Override
  public String commit(FileDescription[] files)
          throws RemoteException {
        try {

             Document document = FileParser.parserFile(_configFile);
             //Actualizar Config File
             //Revisar lo de la versiones??
             
             ByteArrayOutputStream bs= new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream (bs);
             os.writeObject(document);
             os.close();
             
             byte[] configData =  bs.toByteArray();
  
            Message mensaje = new Message(configData, files);
            ByteArrayOutputStream bs2 = new ByteArrayOutputStream();
            ObjectOutputStream os2 = new ObjectOutputStream (bs2);
            os2.writeObject(mensaje); 
            os2.close();
            byte[] bytes =  bs2.toByteArray();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
             
            _messages.send(packet);
            return null;
        } catch (IOException ex) {
            System.err.println("No se pudo realizar el commit.");
            //Hay q retornar el mensaje q no se realizo el commit
            return null;
        }
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
