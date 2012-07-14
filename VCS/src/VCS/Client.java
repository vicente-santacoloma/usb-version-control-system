/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guille
 */
public class Client {
  
    private String userName;
    private VersionControl server;
    
    public Client(String host, int port) {
      userName = null;
      try {
        server = (VersionControl) Naming.lookup("rmi://" + host + ":" + port + "/VCS");
      } catch (NotBoundException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        System.exit(1);
      } catch (MalformedURLException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        System.exit(1);
      } catch (RemoteException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        System.exit(1);
      }
    }
    
    public void clientCommit(String [] files) {
      
   
    }
    
    public void clientCheckout() {
      
  
    }
    
    public void clientUpdate() {
      
  
    }
    
    public void initialize() {
      
      File file = new File(".vcs");
      ArrayList<String> files = listFiles(".");
      
      
      
    }
    
    private ArrayList<String> listFiles(String path) {
      
      File folder = new File(path);
      File [] listOfFiles = folder.listFiles();
      ArrayList<String> files = new ArrayList<String>();
      
      for(int i = 0; i < listOfFiles.length; i++) {
        
        if(listOfFiles[i].isFile())  
          files.add(listOfFiles[i].getPath());
        else if (listOfFiles[i].isDirectory()) {
          ArrayList<String> directoryFiles = this.listFiles(listOfFiles[i].getPath());
          
          for(int j = 0; j < directoryFiles.size(); j++)
            files.add(directoryFiles.get(j));
        } 
      }
      return files;
    }
    
    private HashMap loadFilesDescriptions() {
      
      File file = new File(".vcs");
    
      Scanner scanner = null;
      try {
        scanner = new Scanner(file);
      } catch (FileNotFoundException ex) {
        // Colocar mensaje avisando que el repositorio no esta inicializado
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        System.exit(1);
      }

      userName = scanner.next();
         
      HashMap filesDescriptions = new HashMap();
      
      while(scanner.hasNext()) {
        
        String fileName = scanner.next();
        int version = Integer.parseInt(scanner.next());
        FileDescription fileDescription = 
                new FileDescription(fileName, version, null,userName);
        filesDescriptions.put(fileName, fileDescription);
        
      }
      
      return filesDescriptions;
    }
    
    private void writeFilesDescriptions(HashMap filesDescriptions) {
      
      try {

        BufferedWriter out = new BufferedWriter(new FileWriter(new File(".vcs")));
        
        out.write(userName);
        out.newLine();
        
        Set set = filesDescriptions.entrySet();
        Iterator i = set.iterator();
        
        while(i.hasNext()) {
          Map.Entry me = (Map.Entry)i.next();
          FileDescription fileDescription = (FileDescription) me.getValue();
          out.write(fileDescription.getFileName() + " " + fileDescription.getVersion());
          out.newLine(); 
        }

      } catch (IOException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        System.exit(1);
      }
 
    }
    
}
