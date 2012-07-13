/**
 *
 * @author ninina31
 */
package VCS;

import java.rmi.server.RemoteObject;
import java.net.MulticastSocket;

public class versionControlServer extends RemoteObject{
  
  private MulticastSocket elections;
  private MulticastSocket messages;
  
  /*
   *Operaciones
	commit
	checkout
	updateNode
   * 
   * 
   * hilo: revisar sockets multicast para mensajes
hilo: revisar sockets multicast para elecciones
   
   */
  
  
}
