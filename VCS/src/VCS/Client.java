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
import java.sql.Date;
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

  public Client() {

  }

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
  
  public void run(String args[]) {
    
    String [] files = null;
    if(args[0].equals("init")) {
      if(args.length == 1)
        this.clientInit();       
      System.out.println("Invalid operation");
      System.exit(1);
    } 
    else if(args[0].equals("commit")) {
      if(args.length > 1) {
        files = this.getFiles(args);
        this.clientCommit(files);
      }
      System.out.println("Invalid operation");
      System.exit(1);
    } else if(args[0].equals("checkout")) {
      if(args.length == 1)
        this.clientCheckout();
      System.out.println("Invalid operation");
      System.exit(1);
    } else if(args[0].equals("update")) {
      if(args.length > 1) {
        files = this.getFiles(args);
        this.clientUpdate(files);
      }
      System.out.println("Invalid operation");
      System.exit(1);
    }
  }

  private void clientCommit(String [] files) {

    if(files.length == 0) {
      System.out.println("No files to commit");
      System.exit(0);  
    }

    for(int i = 0; i < files.length; ++i) {
      File file = new File(files[i]);
      if(!file.exists()) {
        System.out.println("File " + files[i] + "not found");
        System.exit(0);
      }
    }

    HashMap<String,FileDescription> filesDescriptions = this.loadFilesDescriptions();
    FileDescription [] filesDescriptionsArray = new FileDescription [files.length];

    for(int i = 0; i < files.length; ++i) {      
      FileDescription fileDescription = (FileDescription) filesDescriptions.get(files[i]);
      if(fileDescription == null) {
        fileDescription = new FileDescription(files[i], 1, null,userName);
        filesDescriptions.put(files[i], fileDescription);
      }
      fileDescription.loadData();
      fileDescription.setTimestamp(new Date(Calendar.getInstance().getTime().getTime()));
      filesDescriptionsArray[i] = fileDescription;
    }
    
    EnumVCS enumVCS = null;
    try {
      enumVCS = server.commit(filesDescriptionsArray);
    } catch (RemoteException ex) {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }

    if(this.checkEnumVCS(enumVCS)) {
      for(int i = 0; i < filesDescriptionsArray.length; ++i)
        filesDescriptionsArray[i].setVersion(filesDescriptionsArray[i].getVersion() + 1);

      this.writeFilesDescriptions(filesDescriptions);
    }
    
    
    for(int i = 0; i < filesDescriptionsArray.length; ++i)
      filesDescriptionsArray[i].setVersion(filesDescriptionsArray[i].getVersion() + 1);

    this.writeFilesDescriptions(filesDescriptions);

  }
  
  private boolean checkEnumVCS(EnumVCS enumVCS) {
    
    if(enumVCS == enumVCS.OK) {
      System.out.println("The file(s) has been commited");
      return true;
    } else if(enumVCS == enumVCS.CHECKOUT) {
      System.out.println("You must checkout");
    } else if(enumVCS == enumVCS.UPDATE) {
      System.out.println("You must update");
    } else if(enumVCS == enumVCS.ERROR) {
      System.out.println("operation failed");
    }
    
    return false;
  }

  private void clientCheckout() {
    
    FileDescription [] checkoutFilesDescriptions = null;
    try {
      checkoutFilesDescriptions = server.checkout();
    } catch (RemoteException ex) {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    if(checkoutFilesDescriptions.length == 0) {
      System.out.println("No files to checkout");
      System.exit(0);
    }
    
    HashMap<String,FileDescription> filesDescriptions = this.loadFilesDescriptions();
    
    for(int i = 0; i < checkoutFilesDescriptions.length; ++i) {
      filesDescriptions.put(checkoutFilesDescriptions[i].getFileName(), checkoutFilesDescriptions[i]);
      checkoutFilesDescriptions[i].writeData();
    }
        
    this.writeFilesDescriptions(filesDescriptions);

  }

  private void clientUpdate(String [] files) {
    
    if(files.length == 0) {
      System.out.println("No files to update");
      System.exit(1);  
    }
    
    for(int i = 0; i < files.length; ++i) {
      File file = new File(files[i]);
      if(!file.exists()) {
        System.out.println("File " + files[i] + "not found");
        System.exit(0);
      }
    }
    
    FileDescription [] updateFilesDescriptions = null;
    
    try {
      updateFilesDescriptions = server.update();
    } catch (RemoteException ex) {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
    if(updateFilesDescriptions.length == 0) {
      System.out.println("No files to update");
      System.exit(0);
    }
    
    HashMap<String,FileDescription> filesDescriptions = this.loadFilesDescriptions();
    
    for(int i = 0; i < files.length; ++i) {
      FileDescription fileDescription = this.getFileDescription(files[i], updateFilesDescriptions);
      fileDescription.writeData();
      filesDescriptions.put(files[i], fileDescription);
    }

    this.writeFilesDescriptions(filesDescriptions);

  }
  
  private FileDescription getFileDescription(String file, FileDescription[] filesDescriptions) {
    
    for(int i = 0; i < filesDescriptions.length; ++i) {
      if(file.equals(filesDescriptions[i].getFileName()))
        return filesDescriptions[i];
    }
    return null;
  }

  public void clientInit() {
    BufferedWriter out = null;
    try {
      out = new BufferedWriter(new FileWriter(new File(".vcs")));
      System.out.println("Insert username: ");
      BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
      userName = read.readLine();
      out.write(userName);
    } catch (IOException ex) {
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      try {
        out.close();
      } catch (IOException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private ArrayList<String> listFiles(String path) {

    File folder = new File(path);
    File [] listOfFiles = folder.listFiles();
    ArrayList<String> files = new ArrayList<String>();

    for(int i = 0; i < listOfFiles.length; ++i) {

      if(listOfFiles[i].isFile())  
        files.add(listOfFiles[i].getPath());
      else if (listOfFiles[i].isDirectory()) {
        ArrayList<String> directoryFiles = this.listFiles(listOfFiles[i].getPath());

        for(int j = 0; j < directoryFiles.size(); ++j)
          files.add(directoryFiles.get(j));
      } 
    }
    return files;
  }

  private HashMap<String,FileDescription> loadFilesDescriptions() {

    File file = new File(".vcs");

    Scanner scanner = null;
    try {
      scanner = new Scanner(file);
    } catch (FileNotFoundException ex) {
      System.out.println("The repository has not been initialized");
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      System.exit(1);
    }

    userName = scanner.next();

    HashMap<String,FileDescription> filesDescriptions = new HashMap<String,FileDescription>();

    while(scanner.hasNext()) {

      String fileName = scanner.next();
      int version = Integer.parseInt(scanner.next());
      FileDescription fileDescription = 
              new FileDescription(fileName, version, null,userName);
      filesDescriptions.put(fileName, fileDescription);

    }

    return filesDescriptions;
  }

  private void writeFilesDescriptions(HashMap<String,FileDescription> filesDescriptions) {

    BufferedWriter out = null;
    try {
      out = new BufferedWriter(new FileWriter(new File(".vcs")));

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
    } finally {
      try {
        out.close();
      } catch (IOException ex) {
        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

  }
   
  private String [] getFiles(String args []) {
    
    File file = null;
    ArrayList<String> files = new ArrayList<String>();
    
    for(int i = 1; i < args.length; ++i) {
      
      if(args[i].equals("."))
        return (String[]) this.listFiles(".").toArray();
      file = new File(args[i]);
      if(file.isFile())
        files.add(args[i]);
      else if(file.isDirectory())
        files.add(args[i]);
    }
   
    return (String[]) files.toArray();
  }

  public static void main(String args[]) {

    Client c = new Client();
    // Client c = new Client(,);
    c.run(args);
    System.exit(0);
  }

}
