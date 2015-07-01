
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
	static Map<Long,Long> listafaltosos = new HashMap<Long,Long>();
	static Map<Integer,SlaveData> escravos = new HashMap<Integer, SlaveData>();
	static int indiceEscravo;
	static Master ref; 
	
	private static List<Long> checkpoints = new ArrayList<Long>();
	
	
	public MasterImpl(){
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

			ref = (Master) UnicastRemoteObject.exportObject(obj,2002);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("mestre", ref);
			obj.attachShutDownHook();

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
	
	//pega o ultimo checkpoint dado antes de um escravo morrer
	private long findCheckpointRemovedSlave(SlaveData escravoRemovido){
		long maiorCheckpoint = -1;
		for(Long cp : checkpoints){
			if(cp <= escravoRemovido.getFim() && cp >= escravoRemovido.getInicio()){
				maiorCheckpoint = cp;
			}
		}
		System.out.println("Maior ckeckpoint: "+maiorCheckpoint);
		return maiorCheckpoint;
	}
	
	//remove escravo e guarda o pedaco faltoso para distribuir
	@Override
	public void removeSlave(int slaveKey) throws RemoteException {
		SlaveData escravoRemovido = escravos.get(slaveKey);
		ThreadDTO threadInterrompida = workers.get(slaveKey);
		//pega ultimo checkpoint para saber oque falta fazer
		long ultimoIndiceEscravo = findCheckpointRemovedSlave(escravoRemovido );
		guardaPedaco(ultimoIndiceEscravo,escravoRemovido.getFim());

		escravos.remove(escravoRemovido.getId());
		System.out.println("Escravo Removido.");
		threadInterrompida.interrupt();

		}
	public void guardaPedaco(long inicial,long fim){
		listafaltosos.put(inicial,fim);
	}
	public void redistribuirAttack(byte[] ciphertext, byte[] knowntext,long initialwordindex, long finalwordindex){

		for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			ThreadDTO exec = new ThreadDTO(entry.getValue().getNome(),entry.getValue().getSlave(),ciphertext,knowntext,initialwordindex,finalwordindex);
			entry.getValue().setInicio(initialwordindex);
			entry.getValue().setFim(finalwordindex);
			workers.put(entry.getKey(), exec);
			exec.start();
			return;
		}

		/*for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			if (from + to >= tamanho) {
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
				to++;
				from = to;
				to += pedaco-1;
			}

		}*/
		
	}

	@Override
	public void foundGuess(long currentindex, Guess currentguess) throws RemoteException {
		listaguess.add(currentguess);
		System.out.println("FoundGuess :\n Index: "+ currentindex+"\n Chave Candidata:"+currentguess.getKey());
		
	}

	@Override
	public void checkpoint(long currentindex) throws RemoteException {
		checkpoints.add(currentindex);
		System.out.println("Checkpoint :"+dicionario.get((int)currentindex)+" Pos: "+currentindex);
	}

	@Override
	public Guess[] attack(byte[] ciphertext, byte[] knowntext) {
		//nao tratamos o caso para dicionarios com 1 palavra;
		long tamanho = dicionario.size();
		System.out.println(tamanho);
		long pedaco = tamanho / escravos.size();
		System.out.println("Pedaço: "+pedaco);
		long from = 0, to = pedaco-1;
		
		for (Map.Entry<Integer, SlaveData> entry : escravos.entrySet()) {
			if (from + to >= tamanho) {
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
				to++;
				from = to;
				to += pedaco-1;
			}

		}
		//espera as threads morreram
		for (Map.Entry<Integer, ThreadDTO> entry : workers.entrySet()){
			try {
				entry.getValue().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//redistribui ataque dos pedaços faltosos
		for(Map.Entry<Long, Long> l : listafaltosos.entrySet()){
			redistribuirAttack(ciphertext,knowntext,l.getKey(),l.getValue());
		}
		//finaliza a lista de resultado para retornar
		Guess[] resultados = new Guess[listaguess.size()];
		for (Guess g : listaguess){
			int cont=0;
			resultados[cont] = g;
			cont++;
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
				System.out.println("Master down, slaves are free.");
			}
		});
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
					escravo.startSubAttack(cipher,known,inicio,fim,ref);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
		}
	}

}
