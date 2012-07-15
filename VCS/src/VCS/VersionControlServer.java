/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
/**
 *
 * @author Guille
 */
public class VersionControlServer{
  
  private MulticastSocket elections;
  private MulticastSocket messages;
  private int coordId;
  private HashMap<Integer, InetAddress> dns;
  private InetAddress multicastAddress;
  private int id;
  private VersionControl coord;

  /**
   * Constructor to build a new version control server
   * 
   * @param elections
   * @param messages 
   */ 
  public VersionControlServer(MulticastSocket elections, MulticastSocket messages){
    this.elections = elections;
    this.messages = messages;
    
    try {
      this.multicastAddress = InetAddress.getByName("225.0.0.5");
    } catch( UnknownHostException e ) {
      System.out.println( e );
    }
  }
  
  //Getters
  public int getId() {
    return id;
  }

  public int getCoordId() {
    return coordId;
  }

  public HashMap<Integer, InetAddress> getDns() {
    return dns;
  }

  public MulticastSocket getElections() {
    return elections;
  }

  public MulticastSocket getMessages() {
    return messages;
  }

  public InetAddress getMulticastAddress() {
    return multicastAddress;
  }
  
  //Setters
  public void setCoordId(int coordId) {
    this.coordId = coordId;
  }

  
  /**
   * 
   * @param args Parámetros recibidos por consola: ID, IP
   * @throws InterruptedException
   * @throws RemoteException
   * @throws IOException 
   */
   public static void main(String[] args) throws InterruptedException, RemoteException, IOException, NotBoundException{

  
     //parametro de entrada ip rmiregistry
    // recibir por linea de comando mi ip y mi id
     
    String host = null;
    int port =0;
     
    if (!((0 < args.length) && (args.length < 3))) {
	    System.err.print("Parametros incorrectos: ");
	    System.err.println("VersionControlServer <hostName> <port>");
	    System.exit(1);
    }

    try {
      
	    host = args[0];
	    port = Integer.parseInt(args[1]);
      
    }
    catch (Exception e) {
	    System.out.println();
	    System.out.println("java.lang.Exception");
	    System.out.println(e);
    }
     
    InetAddress group = InetAddress.getByName("225.0.0.5");
     
    MulticastSocket s = new MulticastSocket(10602);
    
    MulticastSocket p = new MulticastSocket(41895);
     
    s.joinGroup(group);
    p.joinGroup(group);
    
    /* mando un mensaje con mi id diciendo q me uno a la red */
     
    VersionControlServer v = new VersionControlServer(s, p);
    //cambiar el 10
    VersionControlImpl vci = new VersionControlImpl(p,v.dns,"vcsinfo.xml",10);
    
    
    /*
    Message m = new Message(v.getId(),EnumMessageType.ENTRY);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bout);
    oos.writeObject(m);
    byte[] bSend = bout.toByteArray();
    DatagramPacket pack = new DatagramPacket(bSend, bSend.length);
    //p.send(pack); */
    
    VersionControl c = (VersionControl) Naming.lookup("rmi://" + host + ":" + port + "/VCS");
    

     
    /* updateServer = actualizar los archivos */
    
    
    
    
    
    Thread election = new ServerElection(s, v);
    Thread listenMessages = new ServerCommunication(p,v);
    
    election.start();
    listenMessages.start();
    
    election.join();
    
    /* coordinador se inscribe en rmi y resuelve peticiones del cliente */
    
    Naming.rebind("VCS", vci);
   
    // escuchar por un socket particular
    
    Socket clientSocket = null;
    
    ServerSocket acceptS = null;
    
    try {
      acceptS = new ServerSocket(41651);
    } catch (IOException e) {
      System.err.println("Could not listen on port.");
      System.exit(1);
    }
    
    PrintWriter out; 
    String inputLine = "coordinator alive";
    
    while(true){
    
      clientSocket = acceptS.accept();
      
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      
      out.println(inputLine);
      
    }
    
  }
}