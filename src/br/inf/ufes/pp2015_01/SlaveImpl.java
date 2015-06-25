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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SlaveImpl implements Slave{
	
	private String id;

	private static List<String> dicionario;
	
	public SlaveImpl(){
		dicionario = new ArrayList<String>();
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
		return dicionario.subList(inicio, fim);
	}
	
	public static int longToIntSeguro(long l) {
	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            ("[ERRO]: Dicionario grande demais.");
	    }
	    return (int) l;
	}
	
	public static byte[] decrypt(String key, byte[] ciphertext){
		try{
			byte[] chave = key.getBytes();
			SecretKeySpec keySpec = new SecretKeySpec(chave, "Blowfish");
			
			byte[] message = ciphertext;
			System.out.println("[DEBUG]: Message size (bytes) = "+ message.length);
			
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] decrypted = cipher.doFinal(message);
			
			return decrypted;
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("[ERRO]: Erro ao decriptografar mensagem com a chave "+ key);
			e.printStackTrace();
			
		}
		return null;
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
		Integer currentIndex = 0;
		Guess candidata = new Guess();
		
		for(String palavra : getSublista(longToIntSeguro(initialwordindex),longToIntSeguro(finalwordindex))){
			currentIndex++;
			byte[] resposta = decrypt(palavra, ciphertext);
			
			if(indexOf(ciphertext, resposta) != -1){
				candidata.setKey(palavra);
				candidata.setMessage(resposta);
				callbackinterface.foundGuess(currentIndex + initialwordindex, candidata);
			}
		}
		
		//TODO Checkpoint a cada 10segundos
		
	/*	int tempo = (1000 * 10);   // 10 segundos.  
		int periodo = 1;  // quantidade de vezes a ser executado.  
		Timer timer = new Timer();  
		timer.scheduleAtFixedRate(  
		        new TimerTask() {  
		            public void run() {  
		                //aqui vai o checkpoint 
		            }  
		        }, tempo, periodo);
	*/
		
	
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
		makeDicionario();
		
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
