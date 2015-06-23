package br.inf.ufes.pp2015_01;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterImpl implements Master {

	private Map<String, Slave> escravos;
	private List<Thread> threads = new ArrayList<Thread>();
	
	
	public MasterImpl(){
		escravos = new HashMap<String, Slave>();
	}
	
	public static void main(String[] args) {
		//String host = null; //(args.length < 1) ? "" : args[0];
		//if (args.length > 0) {
			//System.setProperty("java.rmi.server.hostname", null);//args[0]);
		//}

		//System.out.println("Connection try at host: " + host);

		try {
			MasterImpl obj = new MasterImpl();

			Master ref = (Master) UnicastRemoteObject.exportObject(obj,2001);

			Registry registry = LocateRegistry.getRegistry();
			//registry.rebind("mestre", ref);
			System.out.println("Master registered!");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	
	
	@Override
	public int addSlave(Slave s, String slavename) throws RemoteException {
		synchronized(this){
		escravos.put(slavename,s);
		}
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
		List<ThreadDTO> workers = new ArrayList<ThreadDTO>();
		
		for (Map.Entry<String, Slave> entry : escravos.entrySet()) {
			List<Integer> subpalavras = new ArrayList<Integer>();

			
			/* Criacao de threads. */
			ThreadDTO exec = new ThreadDTO(entry.getValue(),subpalavras,ciphertext,knowntext);
			workers.add(exec);
			Thread t = new Thread(exec);
			threads.add(t);

			t.start();

		}
		for (Thread t : threads) {
			try {
				t.join();
				//Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	/* Inner class para auxiliar*/
	public class ThreadDTO extends Thread {

		public final Slave escravo;
		public List<Integer> lista;
		public byte[] cipher;
		public byte[] known;

		public ThreadDTO(Slave es, List<Integer> lista,byte[] ciphertext, byte[] knowntext) {
			this.cipher = ciphertext;
			this.known = knowntext;
			this.escravo = es;
			this.lista = lista;
		}

		/* Executa quando a thread eh iniciacada. */
		@Override
		public void run() {

			try {
				
				System.out.println("");
				lista = escravo.startSubAttack(cipher,known,/*long com o indice inicial*/,/*long com o indice final*/,/*função de callback*/);
				
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

}
