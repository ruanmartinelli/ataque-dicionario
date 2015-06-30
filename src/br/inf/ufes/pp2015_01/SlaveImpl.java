package br.inf.ufes.pp2015_01;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SlaveImpl implements Slave{
	
	private static int id;
	private String nome;
	private long currentIndex = 0;
	private static List<String> dicionario = new ArrayList<String>();
	private SlaveManager sm;
	
	public SlaveImpl(){
	}
	
	/* Gera Dicionario. */
	private static void makeDicionario(){
		try {
			BufferedReader br = null;
			String linha;
			br = new BufferedReader(new FileReader("dictionary.txt"));
			
			/* Varre cada linha do Dicionario.*/
			while ((linha = br.readLine()) != null) {
				dicionario.add(linha);
			}
			br.close();
		} catch (IOException e1) {
			System.out
					.println("[DEBUG]: Arquivo dicionario do escravo nao encontrado.");
		}
	}
	
	private static List<String> getSublista (Integer inicio, Integer fim){
		return dicionario.subList(inicio, fim+1);
	}
	
	public static int longToIntSeguro(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            ("[ERRO]: Dicionario grande demais.");
	    }
	    return (int) l;
	}
	
	public static byte[] decrypt(String key, byte[] ciphertext){
		//chave =  new byte[key.length()];
		//message = new byte[ciphertext.length];
		byte[] decrypted = null;
		try{
			byte[] chave = key.getBytes();
			SecretKeySpec keySpec = new SecretKeySpec(chave, "Blowfish");
			
			byte[] message = ciphertext;
			//System.out.println("[DEBUG]: Message size (bytes) = "+ message.length);
			
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			decrypted = cipher.doFinal(message);
			
			return decrypted;
		}catch (javax.crypto.BadPaddingException a) {
			// essa excecao e jogada quando a senha esta incorreta
			// porem nao quer dizer que a senha esta correta se nao jogar essa excecao
			//System.out.println("Senha invalida.");
			return decrypted;
		
		
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException e) {
			System.out.println("[ERRO]: Erro ao decriptografar mensagem com a chave "+ key);
			//e.printStackTrace();
			return decrypted;
		}
	}
	
	public int indexOf(byte[] outerArray, byte[] smallerArray) {
	    for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
	        boolean found = true;
	        for(int j = 0; j < smallerArray.length; ++j) {
	           if (outerArray[i+j] != smallerArray[j]) {
	               found = false;
	               break;
	           }
	        }
	        if (found) return i;
	     }
	   return -1;  
	}
	
	@Override
	public void startSubAttack(byte[] ciphertext, byte[] knowntext,long initialwordindex, long finalwordindex,SlaveManager callbackinterface) throws RemoteException {
		//final long aux = currentIndex + initialwordindex;
		long cachorro = initialwordindex;
		currentIndex = initialwordindex;
		sm =  callbackinterface;
		//Checkpoint a cada 10 segundos
				Timer timer = new Timer();  
				timer.scheduleAtFixedRate(  
				        new TimerTask() {  
				            public void run() {  
				            	try {
				            		sm.checkpoint(SlaveImpl.this.currentIndex-1);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
				            }  
				        }, 10000, 10000);
		
		for(String palavra : getSublista(longToIntSeguro(initialwordindex),longToIntSeguro(finalwordindex))){
			byte[] resposta = decrypt(palavra, ciphertext);			
			
			if(resposta != null && indexOf(resposta, knowntext) != -1){
				Guess candidata = new Guess();
				candidata.setKey(palavra);
				candidata.setMessage(resposta);
				System.out.println("@Current ante de mandar: "+cachorro);
				callbackinterface.foundGuess(cachorro, candidata);
			}
			cachorro++;
		}
		//imprime o ultimo checkpoint antes de terminar
		//aux2.checkpoint(currentIndex);
		//Thread.interrupted();
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
		final Master aux3 = mestre;
		final SlaveImpl escravo = new SlaveImpl();
		try {
			escravo.setId(UUID.randomUUID().toString());
			
			//Slave stub = (Slave) UnicastRemoteObject.exportObject(escravo, 2001);
			final Slave stub = (Slave) UnicastRemoteObject.exportObject(escravo, 0);
			
			//chama addSlave de 30 em 30 segundos
			Timer timer = new Timer();  
			timer.scheduleAtFixedRate(  
			        new TimerTask() {  
			            public void run() {  
			            	try {
								id = aux3.addSlave(stub, escravo.getId());
							} catch (RemoteException e) {
								e.printStackTrace();
							}
			            }  
			        }, 0, 30000);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	/* Desregistra o escravo */
	public static void attachShutDownHook(final Master mestre) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					mestre.removeSlave(id);
					System.out.println("Slave free!");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args) {
		
		makeDicionario();
		registerSlave(getMaster(""));
		attachShutDownHook(getMaster(""));
		
	}
	
	
	//Getters and Setters
	public String getId() {
		return nome;
	}
	
	public void setId(String id) {
		this.nome = id;
	}
}
