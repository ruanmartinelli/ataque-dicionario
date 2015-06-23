/**
 * Master.java
 */
package br.inf.ufes.pp2015_01;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master extends Remote, SlaveManager, Attacker{
	// o mestre é um SlaveManager e um Attacker
	
	public  int addSlave(Slave s, String slavename) throws RemoteException;
	public void removeSlave(int slaveKey) throws RemoteException;
	public void foundGuess(long currentindex, Guess currentguess)throws RemoteException;
	public void checkpoint(long currentindex) throws RemoteException;
	public Guess[] attack(byte[] ciphertext, byte[] knowntext);
	
}
