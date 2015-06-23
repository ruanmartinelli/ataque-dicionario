package br.inf.ufes.pp2015_01;

import java.rmi.RemoteException;


/*Para testar as funções localmente*/
public class MainTeste {
	
	private static Master master;
	//private static SlaveManager slaveManager;
	private static Slave s;
	
	private static String nome = "Jorge";
	
	public static void main(String[] args) {
		
		master = new MasterImpl();
		
		try {
			master.addSlave(s, nome);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
			
	}
	
	
}
