/**
 * Slave.java
 */
package br.inf.ufes.pp2015_01;

import java.rmi.Remote;

public interface Slave extends Remote {
	
	/**
	 * Solicita a um escravo que inicie sua parte do ataque.
	 * @param ciphertext mensagem critografada
	 * @param knowntext trecho conhecido da mensagem decriptografada
	 * @param initialwordindex �ndice inicial do trecho do dicion�rio 
	 * a ser considerado no sub-ataque.
	 * @param finalwordindex �ndice final do trecho do dicion�rio 
	 * a ser considerado no sub-ataque.
	 * @param callbackinterface interface do mestre para chamada de 
	 * checkpoint e foundGuess
	 * @throws java.rmi.RemoteException
	 */
	public void startSubAttack(
			byte[] ciphertext, 
			byte[] knowntext,
			long initialwordindex, 
			long finalwordindex, 
			SlaveManager callbackinterface)
	throws java.rmi.RemoteException;
	
}
