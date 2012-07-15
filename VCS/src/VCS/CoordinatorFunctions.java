/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package VCS;

/**
 *
 * @author Alberto
 */
public class CoordinatorFunctions extends Thread{
  private VersionControlServer vcs;

  public CoordinatorFunctions(VersionControlServer vcs) {
    this.vcs = vcs;
  }
  
  @Override
  public void run(){
    vcs.coordFunctions();
  }
}
