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
  private Message msg;
  private MulticastSocket listener;
  private VersionControlServer father;
  private DatagramPacket pack;
  private ByteArrayInputStream bais;
  private ObjectInputStream ois;
  private ObjectOutputStream oos;
  Element servConf;
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
        
        /*Load this servers configuration*/
        for(Element s : FileParser.serverList(FileParser.parserFile("location.xml"))){
          if(Integer.parseInt(FileParser.getValueOfServer(s, "id")) != father.getId()){
            servConf = s;
            break;
          }
        }
        
        if(msg.getType() == EnumMessageType.FILE_S){
          /*The coordinator is requesting to receive all files contained in
           this server*/
          coord = new Socket(father.getDns().get(father.getCoordId()), 10704);
          oos = new ObjectOutputStream(coord.getOutputStream());
            
          for(Element file : FileParser.getDataElements(servConf)){
            fName = FileParser.getValueOfFile(file, "name");

            /*Get the files data*/
            in = new File(fName);
            data = new byte[(int) in.length()];
            fis = new FileInputStream(in);
            fis.read(data);

            /*Create the FileDescription to be sent*/
            fSend = new FileDescription(
                      fName, 
                      Integer.parseInt(FileParser.getValueOfFile(file, "version")), 
                      Date.valueOf(FileParser.getValueOfFile(file, "timestamp")),
                      FileParser.getValueOfFile(file, "user"),
                      data
                    );

            oos.writeObject(fSend);
          }
            
        }else if(msg.getType() == EnumMessageType.COMMIT){
          /*An update has been made and this server must be updated*/
          for(FileDescription fe : msg.getCommitElements()){
           for(Element file : FileParser.getDataElements(servConf)){
             if(!fe.getFileName().equals(FileParser.getValueOfFile(file, "name")))
               continue;
             
           }
          }
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
