/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.*;
import java.sql.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guille
 */
public class FileDescription implements Serializable {
  
  private String fileName;
  private int version;
  private Date timestamp;
  private String userName;
  private byte[] data;

  /**
   * Constructor with all attributes
   * 
   * @param fileName
   * @param version
   * @param timestamp
   * @param userName
   * @param data 
   */
  public FileDescription(String fileName, int version, Date timestamp, String userName, byte[] data) {
    this.fileName = fileName;
    this.version = version;
    this.timestamp = timestamp;
    this.userName = userName;
    this.data = data;
  }

  /**
   * Constructor that sets all attributes but the file data, which is set to null
   * 
   * @param version
   * @param timestamp
   * @param userName 
   */
  public FileDescription(String fileName, int version, Date timestamp, String userName) {
    this.fileName = fileName;
    this.version = version;
    this.timestamp = timestamp;
    this.userName = userName;
    this.data = null;
  }
  
  public void loadData() {
    
    File file = new File(fileName);
    Scanner scanner = null;
    try {
      scanner = new Scanner(file);
    } catch (FileNotFoundException ex) {
      System.out.println("The " + fileName + " to commit does not exist");
      Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
    }
    data = new byte[(int)file.length()];
    try {
      InputStream in = new FileInputStream(file);
      in.read(data);        
    } catch (FileNotFoundException ex) {
      Logger.getLogger(FileDescription.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(FileDescription.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public void writeData() {
    
    try {
      
      createDirectories(this.fileName);
      
      FileOutputStream out = new FileOutputStream(this.fileName);
      out.write(this.data);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(FileDescription.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        Logger.getLogger(FileDescription.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
  
  public void updateFileDescriptionFile(String fileName, int version, Date timestamp, String userName, byte[] data) {
    this.fileName = fileName;
    this.version = version;
    this.timestamp = timestamp;
    this.userName = userName;
    this.data = data;
  }

  // Getters and Setters

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }
  
  private void createDirectories(String path){
  
    String[] tokens = path.split("/");
    File f;
    String currentPath = ".";
    
    for(int i = 0; i < tokens.length -1; ++i){
      
      currentPath += "/" + tokens[i];
      f = new File(currentPath);
      if (!f.exists()) f.mkdir();
    }
     
  }

}
