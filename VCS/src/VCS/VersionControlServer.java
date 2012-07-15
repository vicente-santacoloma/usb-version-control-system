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
import org.dom4j.Element;
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
  private String hostrmi;
  private int portrmi;
  private VersionControl vci;

  /**
   * Constructor for class VersionControlServer
   * @param dns
   * @param id
   * @param hostrmi
   * @param portrmi
   * @param vci 
   */
  public VersionControlServer(HashMap<Integer, InetAddress> dns, int id,
          String hostrmi, int portrmi, VersionControl vci) {
    this.dns = dns;
    this.id = id;
    this.hostrmi = hostrmi;
    this.portrmi = portrmi;
    this.vci = vci;
    this.coordId = -1;
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
    InetAddress ip = null;
  
     //parametro de entrada ip rmiregistry
    // recibir por linea de comando mi ip y mi id
     
    if (!((0 < args.length) && (args.length < 4))) {
	    System.out.print("Parametros incorrectos: ");
	    System.out.println("VersionControlServer <hostNamermi> <ID> <IP>");
	    return;
    }

    try {
      
	    hostrmi = args[0];
	    portrmi = 40619;
      id = Integer.parseInt(args[1]);
      ip = InetAddress.getByName(args[2]);
      
    }
    catch (Exception e) {
	    System.out.println("java.lang.Exception");
	    System.out.println(e);
      //System.exit(1);
    }
     
    InetAddress group = InetAddress.getByName("225.0.0.5");
     
    MulticastSocket s = new MulticastSocket(10602);
    
    MulticastSocket p = new MulticastSocket(41895);
    
    System.out.println("Uniendome al grupo");
     
    s.joinGroup(group);
    p.joinGroup(group);
    
    System.out.println("Me uni al grupo");
    
    VersionControlServer v = new VersionControlServer(null, id, hostrmi, portrmi,
            new VersionControlImpl(p, "location.xml", 3));
   
    Thread election = new ServerElection(s, v);
    election.start();
    
    while(v.coordId == -1)
      Thread.sleep(6000);
    
    VersionControl vci = (VersionControl) Naming.lookup("rmi://" + hostrmi + ":" + portrmi
            + "/VCS");
     
    /* mando un mensaje con mi id diciendo q me uno a la red */
    vci.requestEntry(id, ip);
    
    System.out.println("Actualizando los archivos");
    
    FileDescription[] files = vci.updateServer(id);
    
    for(FileDescription fd: files)
      fd.writeData();
    
    HashMap<Integer, InetAddress> dns = new HashMap<Integer, InetAddress>();
    
    for(Element serv : FileParser.serverList(FileParser.parserFile("location.xml")))
      dns.put(Integer.parseInt(FileParser.getValueOfServer(serv, "id")),
              InetAddress.getByName(FileParser.getValueOfServer(serv, "ip"))
              );

    v.setDns(dns);
    System.out.println("Ya tengo DNS!");
    Thread listenMessages = new ServerCommunication(p, v);
    
    listenMessages.start();
    
    System.out.println("se crearon los hilos, espero por el join");
    election.join();
    listenMessages.join();
   }

  public void setDns(HashMap<Integer, InetAddress> dns) {
    this.dns = dns;
  }
   
   public void coordFunctions(){
    System.out.println("soy coord: me conecto al rmi");
    
    /* coordinador se inscribe en rmi y resuelve peticiones del cliente */
    
    try {
      Naming.rebind("rmi://" + this.hostrmi + ":" + this.portrmi + "/VCS", this.vci);
    
      System.out.println("se unio al grupo, espero peticiones de vida");

      // escuchar por un socket particular

      Socket clientSocket;

      ServerSocket acceptS = null;

      try {
        acceptS = new ServerSocket(41651);
      } catch (IOException e) {
        System.err.println("Could not listen on port.");
        //System.exit(1);
      }

      PrintWriter out; 
      String inputLine = "Coordinator alive";

      while(this.id == this.coordId){

        acceptS.setSoTimeout(8000);
        clientSocket = acceptS.accept();

        if(null == clientSocket || clientSocket.isClosed())
          continue;
        System.out.println("Alguien me pregunto si vivo");

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        clientSocket.setSoTimeout(5);
        out.println(inputLine);
        clientSocket.close();
      }
      acceptS.close();
    } catch (IOException e) {
      System.out.println("Could not connect to RMI.");
      //System.exit(1);
    }
    
  }
}
