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

  
  private EnumVCS checkConfigFile( Document document ,FileDescription[] files)
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
           
              
              FileParser.setValueOfFile(fil,"version", Integer.toString( actualVersion+1) );
              FileParser.setValueOfFile(fil,"timestamp",files[i].getTimestamp().toString());
              FileParser.setValueOfFile(fil,"user",files[i].getUserName());
              FileParser.setValueOfFile(fil, "size", Integer.toString(files[i].getData().length));

            return EnumVCS.OK;
            }else if(actualVersion < files[i].getVersion())
            {
              return EnumVCS.CHECKOUT;
            }else
            {
              return EnumVCS.UPDATE;
            }
          }
        }
      }
      
    }
    
    return EnumVCS.ERROR;
  }
  
  @Override
  public synchronized EnumVCS commit(FileDescription[] files)
          throws RemoteException {
        try {

            Document document = FileParser.parserFile(_configFile);
             //Actualizar Config File
             //Revisar lo de la versiones??
            EnumVCS result= checkConfigFile(document, files);
            
            if(result == EnumVCS.OK) 
            {
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
            return EnumVCS.OK;
              
            }else if(result == EnumVCS.CHECKOUT) 
            {
              return result;
            }else if(result == EnumVCS.UPDATE) 
            {
              return result;
            }else
            {   //Retorno Error
              return result;
           
            }
            
        } catch (IOException ex) {
            System.err.println("No se pudo realizar el commit.");
            //Hay q retornar el mensaje q no se realizo el commit
            return EnumVCS.ERROR;
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
  public synchronized boolean requestEntry(int id, InetAddress ip) 
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
      FileParser.addElementServer(config, id, ip, null);
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
