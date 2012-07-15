/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Guille
 */
public class FileDescription implements Serializable {
  
  private String fileName;
  private int version;
  private Date timestamp;
  private String userName;
  private Byte[] data;

  /**
   * Constructor with all attributes
   * 
   * @param fileName
   * @param version
   * @param timestamp
   * @param userName
   * @param data 
   */
  public FileDescription(String fileName, int version, Date timestamp, String userName, Byte[] data) {
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

  //Getters
  public Byte[] getData() {
    return data;
  }

  public Date getTimestamp() {
    return timestamp;
  }
  
  public String getFileName() {
    return fileName;
  }

  public String getUserName() {
    return userName;
  }

  public int getVersion() {
    return version;
  }
  
  
}
