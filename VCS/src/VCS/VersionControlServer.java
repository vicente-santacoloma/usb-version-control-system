/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
  public VersionControlServer(MulticastSocket elections, MulticastSocket messages, int id){
    this.elections = elections;
    this.messages = messages;
    this.id = id;
    
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
   * @param args Parámetros recibidos por consola: host de rmi, puerto de rmi, 
   * ID servidor, IP servidor
   * @throws InterruptedException
   * @throws RemoteException
   * @throws IOException 
   */
   public static void main(String[] args) 
           throws InterruptedException, RemoteException, IOException, NotBoundException{

    
    String hostrmi = null;
    int portrmi = 0;
    int id = 0;
    String ip = null;
     
    if (!((0 < args.length) && (args.length < 5))) {
	    System.err.print("Parametros incorrectos: ");
	    System.err.println("VersionControlServer <hostNamermi> <portrmi> <ID> <IP>");
	    System.exit(1);
    }

    try {
      
	    hostrmi = args[0];
	    portrmi = Integer.parseInt(args[1]);
      id = Integer.parseInt(args[2]);
      ip = args[3];
      
    }
    catch (Exception e) {
	    System.out.println("java.lang.Exception");
	    System.out.println(e);
      System.exit(1);
    }
     
    InetAddress group = InetAddress.getByName("225.0.0.5");
     
    MulticastSocket s = new MulticastSocket(10602);
    
    MulticastSocket p = new MulticastSocket(41895);
    
    System.out.println("Uniendome al grupo");
     
    s.joinGroup(group);
    p.joinGroup(group);
    
    System.out.println("Me uni al grupo");
    
    /* mando un mensaje con mi id diciendo q me uno a la red */
     
    VersionControlServer v = new VersionControlServer(s, p, id);
    VersionControlImpl vci = new VersionControlImpl(p,v.dns,"vcsinfo.xml");
    
    VersionControl c = (VersionControl) Naming.lookup("rmi://" + hostrmi + ":" + portrmi 
            + "/VCS");
     
    /* updateServer = actualizar los archivos */
    
    System.out.println("actualizando los archivos");
    
    c.updateServer(v.getId());
    
    Thread election = new ServerElection(s, v);
    Thread listenMessages = new ServerCommunication(p,v);
    
    election.start();
    listenMessages.start();
    
    System.out.println("se crearon los hilos, espero por el join");
    
    election.join();
    
    System.out.println("soy coord: me conecto al rmi");
    
    /* coordinador se inscribe en rmi y resuelve peticiones del cliente */
    
    try {
      Naming.rebind("rmi://" + hostrmi + ":" + portrmi + "/VCS", vci);
    } catch (IOException e) {
      System.err.println("Could not connect to RMI.");
      System.exit(1);
    }
    
    System.out.println("se unio al grupo, espero peticiones de vida");
    
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
      
      System.out.println("alguien me pregunto si vivo");
      
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      
      out.println(inputLine);
      
    }
    
  }
}