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

			Master ref = (Master) UnicastRemoteObject.exportObject(obj,0);

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
		escravos.remove(slaveKey);
		
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
		long tamanho = ciphertext.length;
		long pedaco = tamanho / escravos.size();
		long from = 0, to = pedaco-1;
		for (Map.Entry<String, Slave> entry : escravos.entrySet()) {
			
			if (from + to > tamanho) {
				to = tamanho-1;
				ThreadDTO exec = new ThreadDTO(entry.getValue(),ciphertext,knowntext,from,to);
				workers.add(exec);
				Thread t = new Thread(exec);
				threads.add(t);
				t.start();
				
			} else {
				ThreadDTO exec = new ThreadDTO(entry.getValue(),ciphertext,knowntext,from,to);
				workers.add(exec);
				Thread t = new Thread(exec);
				threads.add(t);
				t.start();
				from = to+1;
				to += pedaco;
			}
			

			
			/* Criacao de threads. */
			/*ThreadDTO exec = new ThreadDTO(entry.getValue(),ciphertext,knowntext);
			workers.add(exec);
			Thread t = new Thread(exec);
			threads.add(t);

			t.start();*/

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
		public long inicio;
		public long fim;
		public final Slave escravo;
		public SlaveManager sm;
		public byte[] cipher;
		public byte[] known;

		public ThreadDTO(Slave es,byte[] ciphertext, byte[] knowntext,long initialwordindex,long finalwordindex) {
			this.inicio = initialwordindex;
			this.fim = finalwordindex;
			this.cipher = ciphertext;
			this.known = knowntext;
			this.escravo = es;
		}

		/* Executa quando a thread eh iniciacada. */
		@Override
		public void run() {

			try {
				
				System.out.println("");
				escravo.startSubAttack(cipher,known,inicio,fim,sm);
				
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

}
