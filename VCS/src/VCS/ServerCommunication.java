/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.*;
import java.net.*;
import java.sql.Date;
import java.util.List;
import org.dom4j.*;

/**
 *
 * @author Guille
 */
public class ServerCommunication extends Thread{

  private byte[] bRecv;
  private byte[] bSend;
  private Message msg;
  private MulticastSocket listener;
  private VersionControlServer father;
  private DatagramPacket pack;
  private ByteArrayInputStream bais;
  private ObjectInputStream ois;
  private ByteArrayOutputStream baos;
  private ObjectOutputStream oos;
  List<Element> servs;
  /**
   * Constructor to build a server communication listener
   * @param listener
   * @param father 
   */
  public ServerCommunication(MulticastSocket listener, VersionControlServer father) {
    this.listener = listener;
    this.father = father;
  }
  
  @Override
  public void run(){
    String fName;
    Socket coord;
    FileDescription fSend;
    FileInputStream fis;
    File in;
    byte[] data;
    try{
      for(;;){
        /* 
         * Can receive messages to perform a commit, to send files and net entry
         */
        bRecv = new byte[65535];
        pack = new DatagramPacket(bRecv, bRecv.length);
        listener.receive(pack);
        bais = new ByteArrayInputStream(pack.getData());
        ois = new ObjectInputStream(bais);
        msg = (Message) ois.readObject();
        
        if(msg.getType() == EnumMessageType.FILE_S){
          /*The coordinator is requesting to receive all files contained in
           this server*/
          coord = new Socket(father.getDns().get(father.getCoordId()), 10704);
          oos = new ObjectOutputStream(coord.getOutputStream());
          
          servs = FileParser.serverList(FileParser.parserFile("location.xml"));
          for(Element s : servs){
            if(Integer.parseInt(FileParser.getValueOfServer(s, "id")) != father.getId())
              continue;
            
            for(Element file : FileParser.getDataElements(s)){
              fName = FileParser.getValueOfFile(file, "name");
              
              /*Get the files data*/
              in = new File(fName);
              data = new byte[(int) in.length()];
              fis = new FileInputStream(in);
              fis.read(data);
              
              fSend = new FileDescription(fName, 
                      Integer.parseInt(FileParser.getValueOfFile(file, "version")), 
                      Date.valueOf(FileParser.getValueOfFile(file, "timestamp")),
                      FileParser.getValueOfFile(file, "user"),
                      data
                      );
            }
            
            break;
          }
        }else if(msg.getType() == EnumMessageType.COMMIT){
          /*An update has been made and this server must be updated*/
        }
      }
    }catch(SocketException se){
      System.out.println(se.getMessage());
    }catch(IOException ioe){
      System.out.println(ioe.getMessage());
    }catch(ClassNotFoundException cnfe){
      System.out.println(cnfe.getMessage());
    }
  }
}
