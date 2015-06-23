package br.inf.ufes.pp2015_01;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class SlaveImpl implements Slave{
	
	private String id;

	@Override
	public void startSubAttack(byte[] ciphertext, byte[] knowntext,
			long initialwordindex, long finalwordindex,
			SlaveManager callbackinterface) throws RemoteException {
	}
	
	/* Procura Master no Registry e retorna a interface. */
	private static Master getMaster(String host){
		
		Master mestre = new MasterImpl();
		
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(host);
			mestre = (Master) registry.lookup("mestre");
			
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
		return mestre;
	}
	
	private static void registerSlave(Master mestre){
		SlaveImpl escravo = new SlaveImpl();
		try {
			escravo.setId(UUID.randomUUID().toString());
			
			//Slave stub = (Slave) UnicastRemoteObject.exportObject(escravo, 2001);
			Slave stub = (Slave) UnicastRemoteObject.exportObject(escravo, 0);
			 
			mestre.addSlave(stub, escravo.getId());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		registerSlave(getMaster(""));
		
	}
	
	
	//Getters and Setters
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
