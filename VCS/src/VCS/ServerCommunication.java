/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

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
