package br.inf.ufes.pp2015_01;
import java.io.BufferedReader;
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

	private Map<Integer, ThreadDTO> workers = new HashMap<Integer, ThreadDTO>();
	private static List<String> dicionario 	= new ArrayList<String>();
	private static List<Guess> listaGuess 	= new ArrayList<Guess>();
	private static Map<Long,Long> listaFaltosos 	= new HashMap<Long,Long>();
	private static Map<Integer,SlaveData> escravos 	= new HashMap<Integer, SlaveData>();
	private static int 		indiceEscravo;
	private static Master	masterRef; 
	
	private static List<Long> checkpoints = new ArrayList<Long>();
	
	public MasterImpl(){
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
		
		MasterImpl obj = new MasterImpl();
		try {

			masterRef = (Master) UnicastRemoteObject.exportObject(obj,2002);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("mestre", masterRef);
			obj.attachShutDownHook();

			System.out.println("[INFO]: Master registered!");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public int addSlave(Slave s, String slavename) throws RemoteException {
		int posicao;
		for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			if(entry.getValue().getNome().equals(slavename)){
				System.out.println("[INFO]: Escravo verificado.");
				return entry.getValue().getId();
			}
		}
		SlaveData novo = new SlaveData();
		novo.setNome(slavename);
		novo.setSlave(s);
			synchronized(this){
				novo.setId(indiceEscravo);
				escravos.put(indiceEscravo, novo);
				posicao = indiceEscravo;
				indiceEscravo++;
			}
			System.out.println("[INFO]: Escravo adicionado.");
		return posicao;
	}
	
	private long findCheckpointRemovedSlave(SlaveData escravoRemovido){
		long maiorCheckpoint = -1;
		for(Long cp : checkpoints){
			if(cp <= escravoRemovido.getFim() && cp >= escravoRemovido.getInicio()){
				maiorCheckpoint = cp;
			}
		}
		System.out.println("[INFO]: Checkpoint Final");
		System.out.println("[INFO]: 	Nome do Escravo: " + escravoRemovido.getNome());		
		System.out.println("[INFO]:		Palavra:     " + dicionario.get((int)maiorCheckpoint));
		System.out.println("[INFO]: 	Posicao:     " + maiorCheckpoint);
		System.out.println("[INFO]: 	Tempo Atual: " + System.nanoTime() / 1000000000.0);
		
		return maiorCheckpoint;
	}
	
	//remove escravo e guarda o pedaco faltoso para distribuir
	@Override
	public void removeSlave(int slaveKey) throws RemoteException {
		SlaveData escravoRemovido = escravos.get(slaveKey);
		ThreadDTO threadInterrompida = workers.get(slaveKey);
		//pega ultimo checkpoint para saber oque falta fazer
		long ultimoIndiceEscravo = findCheckpointRemovedSlave(escravoRemovido );
		if(ultimoIndiceEscravo > 0){
			guardaPedaco(ultimoIndiceEscravo,escravoRemovido.getFim());
		}
		escravos.remove(escravoRemovido.getId());
		System.out.println("[INFO]: Escravo " + escravoRemovido.getNome() + " removido!");
		threadInterrompida.interrupt();

		}
	
	public void guardaPedaco(long inicial,long fim){
		listaFaltosos.put(inicial,fim);
	}
	
	public void redistribuirAttack(byte[] ciphertext, byte[] knowntext,long initialwordindex, long finalwordindex){
		for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			ThreadDTO exec = new ThreadDTO(entry.getValue().getSlave(),ciphertext,knowntext,initialwordindex,finalwordindex);
			entry.getValue().setInicio(initialwordindex);
			entry.getValue().setFim(finalwordindex);
			workers.put(entry.getKey(), exec);
			exec.start();
			return;
		}
	}

	@Override
	public void foundGuess(long currentindex, Guess currentguess) throws RemoteException {
		listaGuess.add(currentguess);
		System.out.println("[INFO]: FoundGuess! Chave candidata encontrada:");
		System.out.println("[INFO]: \tPosicao: " + currentindex);
		System.out.println("[INFO]: \tChave: "+ currentguess.getKey());
	}

	@Override
	public void checkpoint(long currentindex) throws RemoteException {
		Double tempo = new Double(0);
		for(Map.Entry<Integer, SlaveData> entry : escravos.entrySet()){
			if (currentindex >= entry.getValue().getInicio() && currentindex <= entry.getValue().getFim()){
				System.out.println("[INFO]: Checkpoint");
				System.out.println("[INFO]: 	Nome do Escravo: " + entry.getValue().getNome());		
				tempo = entry.getValue().getTempo();
			}
		}
		checkpoints.add(currentindex);
		System.out.println("[INFO]:		Palavra:     " + dicionario.get((int)currentindex));
		System.out.println("[INFO]: 	Posicao:     " + currentindex);
		System.out.println("[INFO]: 	Tempo Atual: " + ((System.nanoTime() / 1000000000.0) - tempo));
	}

	@Override
	public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
		long tamanho = dicionario.size();
		long pedaco = tamanho / escravos.size();
		long from = 0, to = pedaco-1;
		
		for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			if (from + to >= tamanho) {
				to = tamanho-1;
				ThreadDTO exec = new ThreadDTO(entry.getValue().getSlave(),ciphertext,knowntext, from, to);
				entry.getValue().setInicio(from);
				entry.getValue().setFim(to);
				workers.put(entry.getKey(), exec);
				entry.getValue().setTempo((double)System.nanoTime());
				exec.start();
				
			} else {
				ThreadDTO exec = new ThreadDTO(entry.getValue().getSlave(), ciphertext, knowntext, from, to);
				entry.getValue().setInicio(from);
				entry.getValue().setFim(to);
				workers.put(entry.getKey(), exec);
				entry.getValue().setTempo((double)System.nanoTime());
				exec.start();
				to++;
				from = to;
				to += pedaco-1;
			}
		}

		for (Map.Entry<Integer, ThreadDTO> entry : workers.entrySet()){
			try {
				entry.getValue().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		for(Map.Entry<Long, Long> faltoso : listaFaltosos.entrySet()){
			redistribuirAttack(ciphertext ,knowntext ,faltoso.getKey(), faltoso.getValue());
		}
		
		Guess[] resultados = new Guess[listaGuess.size()];
		for (Guess g : listaGuess){
			int i = 0;
			resultados[i] = g;
			i++;
		}
		return resultados;
	}

	public void attachShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				/* Remove todos escravos da lista em caso de mestre offline */
				for (Map.Entry<Integer, SlaveData> es : escravos.entrySet()) {
					escravos.remove(es);
				}
				System.out.println("[INFO]: Master down, slaves are free.");
			}
		});
	}
	/* Inner class para auxiliar*/
	public class ThreadDTO extends Thread {
		public long inicio;
		public long fim;
		public final Slave escravo;
		public byte[] cipher;
		public byte[] known;

		public ThreadDTO(Slave es,byte[] ciphertext, byte[] knowntext,long initialwordindex,long finalwordindex) {
			this.inicio = initialwordindex;
			this.fim = finalwordindex;
			this.cipher = ciphertext;
			this.known = knowntext;
			this.escravo = es;
		}
		@Override
		public void run() {
				try {
					escravo.startSubAttack(cipher,known,inicio,fim,masterRef);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
		}
	}

}
