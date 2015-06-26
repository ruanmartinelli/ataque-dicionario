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

	//private Map<String, Slave> escravos;
	//private Map<Integer, String> controleEscravos;
	//private List<ThreadDTO> workers = new ArrayList<ThreadDTO>();
	private Map<Integer, ThreadDTO> workers = new HashMap<Integer, ThreadDTO>();
	static List<String> dicionario = new ArrayList<String>();
	static List<Guess> listaguess = new ArrayList<Guess>();
	static Map<Integer,SlaveData> escravos;
	static int indiceEscravo;
	
	private static List<Long> checkpoints;
	
	
	public MasterImpl(){
		escravos = new HashMap<Integer, SlaveData>();
		//controleEscravos = new HashMap<Integer, String>();
		indiceEscravo = 0;
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
		int posicao;
		for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			if(entry.getValue().getNome().equals(slavename)){
				System.out.println("Escravo Verificado.");
				return entry.getValue().getId();
			}
		}
		SlaveData novo = new SlaveData();
		novo.setNome(slavename);
		novo.setSlave(s);
			synchronized(this){
				novo.setId(indiceEscravo);
				escravos.put(indiceEscravo, novo);
				posicao=indiceEscravo;
				indiceEscravo++;
			}
			System.out.println("Escravo Adicionado.");
		return posicao;
	}
	
	
	private long findCheckpointRemovedSlave(SlaveData escravoRemovido){
		long maiorCheckpoint = -1;
		for(Long cp : checkpoints){
			if(cp <= escravoRemovido.getFim() && cp >= escravoRemovido.getInicio()){
				maiorCheckpoint = cp;
			}
		}
		return maiorCheckpoint;
	}
	
	//TODO redistribui a tarefa
	@Override
	public void removeSlave(int slaveKey) throws RemoteException {
		SlaveData escravoRemovido = escravos.get(slaveKey);
		ThreadDTO threadInterrompida = workers.get(slaveKey);
		Long ultimoIndiceEscravo = findCheckpointRemovedSlave(escravoRemovido );
		
		
		//distribui
		escravos.remove(escravoRemovido);
		threadInterrompida.interrupt();
		resdistribuirAttack(threadInterrompida.cipher, threadInterrompida.known, ultimoIndiceEscravo , escravoRemovido.getFim(),threadInterrompida.sm);
		
		
/*		for(ThreadDTO w : workers){
		}
		String slavename = controleEscravos.get(slaveKey);
		for(int i=0;i<workers.size();i++){
			if(workers.get(i).nome.equals(slavename)){
				workers.get(i).escravo.
				escravos.remove(slaveKey);
				//resdistribuirAttack();
				
				//nova função
				workers.get(i).interrupt();
				
			}
		}
*/	
		}
	public void resdistribuirAttack(byte[] ciphertext, byte[] knowntext,long initialwordindex, long finalwordindex,SlaveManager callbackinterface){
		//TODO Redistribuir o ataque
		//Funcao ja está sendo chamada
	}

	@Override
	public void foundGuess(long currentindex, Guess currentguess) throws RemoteException {
		listaguess.add(currentguess);
		System.out.println("FoundGuess :\n Index: "+ currentindex+"\n Chave Candidata:"+currentguess.getKey());
		
	}

	@Override
	public void checkpoint(long currentindex) throws RemoteException {
		checkpoints.add(currentindex);
		System.out.println("Checkpoint :"+dicionario.get((int)currentindex));
	}

	@Override
	public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
		
		long tamanho = dicionario.size();
		long pedaco = tamanho / escravos.size();
		long from = 0, to = pedaco-1;
		
		for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			
			if (from + to > tamanho) {
				to = tamanho-1;
				ThreadDTO exec = new ThreadDTO(entry.getValue().getNome(),entry.getValue().getSlave(),ciphertext,knowntext,from,to);
				entry.getValue().setInicio(from);
				entry.getValue().setFim(to);
				workers.put(entry.getKey(), exec);

				exec.start();
				
			} else {
				ThreadDTO exec = new ThreadDTO(entry.getValue().getNome(),entry.getValue().getSlave(),ciphertext,knowntext,from,to);
				entry.getValue().setInicio(from);
				entry.getValue().setFim(to);
				workers.put(entry.getKey(), exec);
				
				exec.start();
				from = to+1;
				to += pedaco;
			}

		}
		for (Map.Entry<Integer, ThreadDTO> entry : workers.entrySet()){
			try {
				entry.getValue().join();
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
		public String nome;
		public SlaveManager sm;
		public byte[] cipher;
		public byte[] known;

		public ThreadDTO(String nome,Slave es,byte[] ciphertext, byte[] knowntext,long initialwordindex,long finalwordindex) {
			this.nome = nome;
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
