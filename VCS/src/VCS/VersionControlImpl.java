/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.*;
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
  private int _kTolerance;
  public VersionControlImpl(MulticastSocket messages, HashMap dns,String configFile, int kTolerance) {
    super();

    _messages = messages;
    _dns = dns;
    _configFile = configFile;
    _kTolerance = kTolerance;
  }


  
  private ArrayList<String> serverTolerance(HashMap<String,Integer> servers)
  {
    
    //Si no funcion ausar .compound(Ordering.natural())
    ValueComparator.MapStringIntegerComparator bvc = new ValueComparator.MapStringIntegerComparator(servers);
    TreeMap<String,Integer> sorted_map = new TreeMap(bvc);
    sorted_map.putAll(servers);
    
    ArrayList<String> tolerance = new ArrayList<String>(_kTolerance);
    int count = 0;
    for (String key : sorted_map.keySet()) {
      tolerance.add(key);
      count++;
      if(count ==_kTolerance )
      {
        break;
      }
    }
    return tolerance;
  }
  
  private EnumVCS checkConfigFile(Document document, FileDescription[] files)
  {
    HashMap<String,Integer> totalSizeInServer = new HashMap<String,Integer>();
    int count = 0;
    
    List<Element> servers = FileParser.serverList(document);
    
    for(Element server : servers)
    {
      count = 0;
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
           
              
              FileParser.setValueOfFile(fil,"version", Integer.toString( files[i].getVersion()+1) );
              FileParser.setValueOfFile(fil,"timestamp",files[i].getTimestamp().toString());
              FileParser.setValueOfFile(fil,"user",files[i].getUserName());
              FileParser.setValueOfFile(fil, "size", Integer.toString(files[i].getData().length));

            }else if(actualVersion < files[i].getVersion())
            {
              return EnumVCS.CHECKOUT;
            } else
            {
              return EnumVCS.UPDATE;
            }
          }
        }
        count += Integer.parseInt(FileParser.getValueOfFile(fil, "size"));
      }
      totalSizeInServer.put(FileParser.getValueOfServer(server, "id"), Integer.valueOf(count));
    }
    
    HashSet<String> totalFiles = FileParser.getTotalFiles(document);
    ArrayList<String> serverOrdenados = serverTolerance( totalSizeInServer);
    
    for(int i = 0; i< files.length; i++)
    {
      if(!totalFiles.contains(files[i].getFileName()))
      {
        for(int k= 0; k <_kTolerance;k++)
        {
          FileDescription[] descrip = new FileDescription[1];
          descrip[0] = new FileDescription(files[i].getFileName(),files[i].getVersion(),files[i].getTimestamp()
                  , files[i].getUserName(),files[i].getData());
          FileParser.addFileInServer(document, serverOrdenados.get(k), descrip);
        }
      }
    }
    
    return EnumVCS.OK;
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
  public synchronized FileDescription[] checkout()
          throws RemoteException {
    DatagramPacket pack;
    Socket recv;
    ServerSocket sock = null;
    byte[] bSend;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos;
    ObjectInputStream ois;
    Message msg = new Message();
    ArrayList<FileDescription> toRet = new ArrayList<FileDescription>();
    HashSet<String> files;
    FileDescription fileR;
    
    System.out.println("RMI: Received a checkout request");
    try{
      oos = new ObjectOutputStream(baos);
      oos.writeObject(msg);
      bSend = baos.toByteArray();
      pack = new DatagramPacket(bSend, bSend.length);
      
      System.out.println("RMI: Getting the files set");
      files = FileParser.getTotalFiles(FileParser.parserFile("location.xml"));
      
      if(sock != null && !sock.isClosed())
        sock.close();
      
      sock = new ServerSocket(10704);
      System.out.println("RMI: Sending request for files");
      _messages.send(pack);
      
      while(!files.isEmpty()){
        System.out.println("RMI: Waiting to receive another file");
        recv = sock.accept();
        ois = new ObjectInputStream(recv.getInputStream());
        fileR = (FileDescription) ois.readObject();
        System.out.println("RMI: Received file " + fileR.getFileName());
        if(files.remove(fileR.getFileName()))
          toRet.add(fileR);
      }
    }catch(IOException ioe){
      System.out.println(ioe.getMessage());
    }catch(ClassNotFoundException cnf){
      System.out.println(cnf.getMessage());
    }
    
    System.out.println("RMI: Checkout built, returning");
    return (FileDescription [])toRet.toArray();
  }

  @Override
  public FileDescription[] update()
          throws RemoteException {
    System.out.println("RMI: Received an update request");
    return this.checkout();
  }

  @Override
  public FileDescription[] updateServer(int id)
    throws RemoteException{
    
    FileDescription[] files = this.checkout();
    ArrayList<FileDescription> update = new ArrayList<FileDescription>();
    Document doc = FileParser.parserFile(_configFile);
    Element server = null;
    
    
    ByteArrayOutputStream bs= new ByteArrayOutputStream();
    try{
      ObjectOutputStream os = new ObjectOutputStream (bs);
      os.writeObject(doc);
      os.close();
    } catch(Exception e){
        System.out.println(e.getMessage());
    }
    
    update.add(new FileDescription("location.xml", -1, null, null, bs.toByteArray()));
    
    for(Element s: FileParser.serverList(doc)){
      
      if (Integer.parseInt(FileParser.getValueOfServer(s, "id")) == id){
      
        server = s;
        break;
      }
    }
    
    for(Element file: FileParser.getDataElements(server)){
      
      for(FileDescription f: files){
      
        if (f.getFileName().equals(FileParser.getValueOfFile(file, "name"))){
          update.add(f);
          break;
        }
      }
    }
    
    return (FileDescription[]) update.toArray();
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
        
      }catch(IOException ioe){
        System.out.println(ioe.getMessage());
      }
    }
    return true;
  }
  
  
  
}
