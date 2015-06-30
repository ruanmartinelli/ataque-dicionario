/**
 * Attacker.java
 */
package br.inf.ufes.pp2015_01;
import java.rmi.Remote;

public interface Attacker extends Remote {
	
	/**
	 * Operacaoo oferecida pelo mestre para iniciar um ataque.
	 * @param ciphertext mensagem critografada
	 * @param knowntext trecho conhecido da mensagem decriptografada
	 * @return vetor de chutes: chaves candidatas e mensagem 
	 * decritografada com chaves candidatas
	 */
	public Guess[] attack(byte[] ciphertext, 
			byte[] knowntext) throws java.rmi.RemoteException;
}
