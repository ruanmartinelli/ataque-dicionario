/**
 * SlaveManager.java
 */
package br.inf.ufes.pp2015_01;
import java.rmi.Remote;

public interface SlaveManager extends Remote {
	
	public int addSlave(Slave s, String slavename)
		throws java.rmi.RemoteException;

	
	public void removeSlave(int slaveKey)
		throws java.rmi.RemoteException;

	
	public void foundGuess(long currentindex, 
				Guess currentguess)
		throws java.rmi.RemoteException;

	
	public void checkpoint(long currentindex)
		throws java.rmi.RemoteException;
}
