package br.inf.ufes.pp2015_01;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	private List<ThreadDTO> workers = new ArrayList<ThreadDTO>();
	static List<String> dicionario = new ArrayList<String>();
	static List<Guess> listaguess = new ArrayList<Guess>(); 
	
	
	public MasterImpl(){
		escravos = new HashMap<String, Slave>();
	}
	
	public static void main(String[] args) {
		//String host = null; //(args.length < 1) ? "" : args[0];
		//if (args.length > 0) {
			//System.setProperty("java.rmi.server.hostname", "localhost");//args[0]);
		//}

		//System.out.println("Connection try at host: " + host);
		
		BufferedReader br = null;
		String sCurrentLine;
		try {
			br = new BufferedReader(new FileReader("dictionary.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				dicionario.add(sCurrentLine);
			}
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
			
		
		try {
			MasterImpl obj = new MasterImpl();

			Master ref = (Master) UnicastRemoteObject.exportObject(obj,2002);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("mestre", ref);
			System.out.println("Master registered!");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	
	
	@Override
	public int addSlave(Slave s, String slavename) throws RemoteException {
		for (Map.Entry<String, Slave> entry : escravos.entrySet()) {
			if(entry.getKey().equals(slavename)){
				System.out.println("Escravo Verificado.");
				return 0;
			}
		}
			synchronized(this){
				escravos.put(slavename,s);
			}
			System.out.println("Escravo Adicionado.");
		return 0;
	}
	//TODO redistribui a tarefa
	@Override
	public void removeSlave(int slaveKey) throws RemoteException {
		for(int i=0;i<workers.size();i++){
			if(workers.get(i).id.equals(slaveKey)){
				escravos.remove(slaveKey);
				//redistribui a tarefa dele com ATTACK;
				workers.get(i).interrupt();
				
			}
		}
	}

	@Override
	public void foundGuess(long currentindex, Guess currentguess) throws RemoteException {
		listaguess.add(currentguess);
		System.out.println("FoundGuess :\n Index: "+ currentindex+"\n Chave Candidata:"+currentguess.getKey());
		
	}

	@Override
	public void checkpoint(long currentindex) throws RemoteException {
		System.out.println("Checkpoint :"+dicionario.get((int)currentindex));
	}

	@Override
	public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
		
		long tamanho = dicionario.size();
		long pedaco = tamanho / escravos.size();
		long from = 0, to = pedaco-1;
		for (Map.Entry<String, Slave> entry : escravos.entrySet()) {
			
			if (from + to > tamanho) {
				to = tamanho-1;
				ThreadDTO exec = new ThreadDTO(entry.getKey(),entry.getValue(),ciphertext,knowntext,from,to);
				workers.add(exec);
				exec.start();
				
			} else {
				ThreadDTO exec = new ThreadDTO(entry.getKey(),entry.getValue(),ciphertext,knowntext,from,to);
				workers.add(exec);
				exec.start();
				from = to+1;
				to += pedaco;
			}

		}
		for (ThreadDTO t : workers) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Guess[] resultados = new Guess[listaguess.size()];
		for (Guess g : listaguess){
			int cont=0;
			resultados[cont] = g;
			cont++;
		}
		
		return resultados;
	}
	/* Inner class para auxiliar*/
	public class ThreadDTO extends Thread {
		public long inicio;
		public long fim;
		public final Slave escravo;
		public String id;
		public SlaveManager sm;
		public byte[] cipher;
		public byte[] known;

		public ThreadDTO(String id,Slave es,byte[] ciphertext, byte[] knowntext,long initialwordindex,long finalwordindex) {
			this.id = id;
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
					escravo.startSubAttack(cipher,known,inicio,fim,sm);
					
				} catch (RemoteException e) {
					e.printStackTrace();
				}
		}
	}

}
