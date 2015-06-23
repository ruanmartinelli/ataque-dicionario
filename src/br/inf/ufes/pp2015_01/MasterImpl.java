package br.inf.ufes.pp2015_01;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class MasterImpl implements Master {

	private Map<String, Slave> escravos;
	
	
	public MasterImpl(){
		escravos = new HashMap<String, Slave>();
	}
	
	
	@Override
	public int addSlave(Slave s, String slavename) throws RemoteException {
		
		escravos.put(slavename,s);
		System.out.println("Escravo adicionado.");
		
		return 0;
	}

	@Override
	public void removeSlave(int slaveKey) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundGuess(long currentindex, Guess currentguess)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkpoint(long currentindex) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
		// TODO Auto-generated method stub
		return null;
	}

}
