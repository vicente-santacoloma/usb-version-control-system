/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

import java.io.Serializable;

/**
 *
 * @author Guille
 */
public class Message implements Serializable{
  private EnumMessageType type;
  private Byte[] configFile;
  private FileDescription[] commitElements;
  private int id;
  private FileDescription sentFile;


  /**
   * Constructor for a message to send a file update
   * @param id
   * @param sentFile 
   */
  public Message(int id, FileDescription sentFile) {
    this.id = id;
    this.sentFile = sentFile;
    this.type = EnumMessageType.FILE_S;
  }

  /**
   * Constructor for a message containing a commit
   * @param configFile
   * @param commitElements 
   */
  public Message(Byte[] configFile, FileDescription[] commitElements) {
    this.commitElements = commitElements;
    this.configFile = configFile;
    this.type = EnumMessageType.COMMIT;
  }

  /**
   * Constructor for an election message, either election start or coordinator
   * @param id 
   * @param type 
   */
  public Message(int id, EnumMessageType type) {
    this.id = id;
    this.type = type;
  }

  //Getters
  public FileDescription[] getCommitElements() {
    return commitElements;
  }

  public int getId() {
    return id;
  }

  public FileDescription getSentFile() {
    return sentFile;
  }

  public EnumMessageType getType() {
    return type;
  }

  public Byte[] getConfigFile() {
    return configFile;
  }
  
}
