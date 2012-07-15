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
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.HashMap;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;

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

  
  private boolean checkConfigFile( Document document ,FileDescription[] files)
  {
    List<Element> servers = FileParser.serverList(document);
    
    for(Element server : servers)
    {
      List<Element> file4Server = FileParser.getDataElements(server);
      
      for(Element fil: file4Server)
      {
        for (int i = 0; i < files.length; i++)
        {
          String idFile = FileParser.getValueOfFile(fil, "name");
          
          if (idFile.equals(files[i].getFileName()))
          {
            String versionFile = FileParser.getValueOfFile(fil, "version");
            int actualVersion = Integer.parseInt(versionFile);
            
            if (actualVersion == files[i].getVersion())
            {
              //actualizar document
        
            }else if(actualVersion < files[i].getVersion())
            {
              //El archivo esta corrupto, checkout
              
            }else
            {
              //revisar timestamp y usuario si son iguales es retransmision
              
              //Hay q avisar que el usuario tiene q hacer update
            }
          }
        }
      }
      
    }
    
    return false;
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
    return this.checkout();
  }

  @Override
  public FileDescription[] updateServer(int id)
    throws RemoteException{

    return null;
  }

  @Override
  public boolean requestEntry(int id, InetAddress ip) 
    throws RemoteException {
    boolean exists = false;
    Document config = FileParser.parserFile("location.xml");
    Message msg;
    ByteArrayOutputStream bout;
    ObjectOutputStream oos;
    byte[] confB, bSend;
    DatagramPacket pack;
    
    /*Check if the server already exists on the list*/
    for(Element s : FileParser.serverList(config)){
      if(Integer.parseInt(FileParser.getValueOfServer(s, "id")) == id){
        exists = true;
        break;
      }
    }
    
    /*The element does not exist, add it and send the commit*/
    if(!exists){
      /*Add the element to the loaded document*/
      try{
        bout = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bout);
        oos.writeObject(config);
        confB = bout.toByteArray();

        msg = new Message(confB, null);

        bout = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bout);
        oos.writeObject(msg);
        bSend = bout.toByteArray();
        pack = new DatagramPacket(bSend, bSend.length);
        _messages.send(pack);
        
        return true;
      }catch(IOException ioe){
        System.out.println(ioe.getMessage());
      }
    }
    return true;
  }
  
}
