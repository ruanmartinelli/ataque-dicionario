/**
 * SlaveManager.java
 */
package br.inf.ufes.pp2015_01;
import java.rmi.Remote;

public interface SlaveManager extends Remote {
	/**
	 * Registra escravo no mestre. Deve ser chamada a cada 30s 
	 * por um escravo para se re-registrar.
	 * @param s refer�ncia para o escravo
	 * @param slavename identificador para o escravo
	 * @return chave que identifica o escravo para posterior remo��o
	 * @throws java.rmi.RemoteException
	 */
	public int addSlave(Slave s, String slavename)
		throws java.rmi.RemoteException;

	/**
	 * Desegistra escravo no mestre. 
	 * @param slaveKey chave que identifica o escravo
	 * @throws java.rmi.RemoteException
	 */
	public void removeSlave(int slaveKey)
		throws java.rmi.RemoteException;

	/**
	 * Indica para o mestre que o escravo achou uma chave candidata.
	 * @param currentindex �ndice da chave candidata no dicion�rio
	 * @param currentguess chute que inclui chave candidata e 
	 * mensagem decriptografada com a chave candidata
	 * @throws java.rmi.RemoteException
	 */
	public void foundGuess(long currentindex, 
				Guess currentguess)
		throws java.rmi.RemoteException;

	/**
	 * Chamado por cada escravo a cada 10s durante ataque para indicar 
	 * progresso no ataque, e ao final do ataque.
	 * @param currentindex �ndice da chave j� verificada
	 * @throws java.rmi.RemoteException
	 */
	public void checkpoint(long currentindex)
		throws java.rmi.RemoteException;
}
