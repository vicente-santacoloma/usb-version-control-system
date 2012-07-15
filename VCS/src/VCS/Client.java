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

  public void clientCommit(String [] files) {

    if(files.length == 0) {
      System.out.println("No files to commit");
      System.exit(1);  
    }

    for(int i = 0; i < files.length; i++) {
      File file = new File(files[i]);
      if(!file.exists()) {
        System.out.println("File " + files[i] + "not found");
        System.exit(1);
      }
    }

    HashMap filesDescriptions = this.loadFilesDescriptions();
    FileDescription [] filesDescriptionsArray = new FileDescription [files.length];

    for(int i = 0; i < files.length; i++) {      
      FileDescription fileDescription = (FileDescription) filesDescriptions.get(files[i]);
      if(fileDescription == null) {
        fileDescription = new FileDescription(files[i], 1, null,userName);
        filesDescriptions.put(files[i], fileDescription);
      }
      fileDescription.loadData();
      fileDescription.setTimestamp(new Date(Calendar.getInstance().getTime().getTime()));
      filesDescriptionsArray[i] = fileDescription;
    }
    // server.commit(filesDescriptionsArray);

    for(int i = 0; i < filesDescriptionsArray.length; i++)
      filesDescriptionsArray[i].setVersion(filesDescriptionsArray[i].getVersion() + 1);

    this.writeFilesDescriptions(filesDescriptions);

  }

  public void clientCheckout() {


  }

  public void clientUpdate() {


  }

  public void initialize() {
    BufferedWriter out = null;
    try {
      File file = new File(".vcs");
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
      System.out.println("The repository has not been initialized");
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

  public static void main(String args[]) {

    Client c = new Client();
    c.initialize();
    String[] files = new String [3];
    files[0] = "file1";
    files[1] = "file2";
    files[2] = "file3";

    c.clientCommit(files);
    System.exit(0);

  }

}
