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
  private FileDescription[] commitElements;
  private int id;
  private FileDescription sentFile;

  /**
   * Constructor for a message to send a file update
   * @param type
   * @param id
   * @param sentFile 
   */
  public Message(EnumMessageType type, int id, FileDescription sentFile) {
    this.type = type;
    this.id = id;
    this.sentFile = sentFile;
  }

  /**
   * Constructor for a message containing a commit
   * @param type
   * @param commitElements 
   */
  public Message(EnumMessageType type, FileDescription[] commitElements) {
    this.type = type;
    this.commitElements = commitElements;
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
}
