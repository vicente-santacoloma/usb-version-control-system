/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.*;
import java.net.*;
import java.sql.Date;
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
    FileOutputStream fout;
    File f;
    Document conf;
    byte[] data;
    try{
      for(;;){
        /* 
         * Can receive messages to perform a commit, to send files
         */
        bRecv = new byte[65535];
        pack = new DatagramPacket(bRecv, bRecv.length);
        listener.receive(pack);
        bais = new ByteArrayInputStream(pack.getData());
        ois = new ObjectInputStream(bais);
        System.out.println(father.getId() + ": Waiting to receive a server message...");
        msg = (Message) ois.readObject();
        System.out.println("Server message received");
        
        /*Load this servers configuration*/
        System.out.println(father.getId() + ": Loading server configuration...");
        for(Element s : FileParser.serverList(FileParser.parserFile("location.xml"))){
          if(Integer.parseInt(FileParser.getValueOfServer(s, "id")) != father.getId()){
            servConf = s;
            break;
          }
        }
        System.out.println("Done loading server configuration");
        
        if(msg.getType() == EnumMessageType.FILE_R){
          System.out.println(father.getId() + ": Received a request to send the files");
          /*The coordinator is requesting to receive all files contained in
           this server*/
          coord = new Socket(father.getDns().get(father.getCoordId()), 10704);
          oos = new ObjectOutputStream(coord.getOutputStream());
            
          for(Element file : FileParser.getDataElements(servConf)){
            fName = FileParser.getValueOfFile(file, "name");

            /*Get the files data*/
            f = new File(fName);
            data = new byte[(int) f.length()];
            fis = new FileInputStream(f);
            fis.read(data);

            /*Create the FileDescription to be sent*/
            fSend = new FileDescription(
                      fName, 
                      Integer.parseInt(FileParser.getValueOfFile(file, "version")), 
                      Date.valueOf(FileParser.getValueOfFile(file, "timestamp")),
                      FileParser.getValueOfFile(file, "user"),
                      data
                    );
            System.out.println(father.getId() + ": Sending file " + fName + "...");
            oos.writeObject(fSend);
            System.out.println("Done sending " + fName);
          }
            
        }else if(msg.getType() == EnumMessageType.COMMIT){
          /*An update has been made and this server must be updated*/
          /*First update the configuration file*/
          System.out.println(father.getId() + ": Received a new commit");
          bais = new ByteArrayInputStream(msg.getConfigFile());
          ois = new ObjectInputStream(bais);
          conf = (Document) ois.readObject();
          
          System.out.println(father.getId() + ": Updating configuration file...");
          FileParser.updateXMLFile("location.xml", conf);
          System.out.println("Done updating configuration file");
          
          /*Update the appropriate files*/
          if(msg.getCommitElements() != null){
            for(FileDescription fe : msg.getCommitElements()){
              for(Element file : FileParser.getDataElements(servConf)){
                if(!fe.getFileName().equals(FileParser.getValueOfFile(file, "name")))
                  continue;

                System.out.println(father.getId() + ": Updating file" 
                        + FileParser.getValueOfFile(file, "name")
                        + "...");
                fout = new FileOutputStream(FileParser.getValueOfFile(file, "name"));
                fout.write(fe.getData());
                System.out.println("Done updating file " 
                        + FileParser.getValueOfFile(file, "name"));
              }
            }
          }
        }else{
          System.out.println(father.getId() + ": Received a message through "
                  + "server communication of invalid type: " + msg.getType());
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
