package br.inf.ufes.pp2015_01;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.UUID;

public class ClienteMain {
	
	 private static byte[] geraArquivo(){
		 
		 Random r = new Random();
		 int size = r.nextInt(100000 - 1000) + 1000;
		 
		 String fileName = UUID.randomUUID().toString();
		 
		 byte[] result = new byte[size];
		 Random random = new Random();
		 random.nextBytes(result);
		 System.out.println(result);
 
		 try {
			 FileOutputStream fos = new FileOutputStream(fileName);
			 fos.write(result);
			 fos.close();
		 } catch (IOException e) {
			 System.out.println("@DEBUG: Erro ao escrever Arquivo");
		 }
		 return result;
	 }
	 
	 private static byte[] geraArquivo(int tamanho){
		 
		 String fileName = UUID.randomUUID().toString();
		 
		 byte[] result = new byte[tamanho];
		 Random random = new Random();
		 random.nextBytes(result);
		 System.out.println(result);
		 
		 try {
			 FileOutputStream fos = new FileOutputStream(fileName);
			 fos.write(result);
			 fos.close();
		 } catch (IOException e) {
			 System.out.println("[DEBUG]: Erro ao escrever Arquivo");
		 }
		  return result;
	 }
	 
	 private static Master findMaster(String host){
		 Registry registry;
		 Master stub = null;
			try {
				registry = LocateRegistry.getRegistry(host);
				stub = (Master) registry.lookup("mestre");
			} catch (RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
				
		 return stub;
	 }
	 
	 
	public static void main(String[] args) {
		
		String fileName 		= args[2];
		String palavraConhecida = args[1];
		byte[] byteArray 		= null;

		//fileName = "asdasfas";
	
		try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
		{
			String sCurrentLine;
 
			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
			}
 
		} catch (IOException e) {
			
			System.out.println("[DEBUG]: Arquivo não encontrado.");
			if(args.length > 3){
				byteArray = geraArquivo(Integer.parseInt(args[3]));
			}else{
				byteArray = geraArquivo();
			}
		} 
		//TODO 
		Master master = findMaster(args[0]);
		//master.attack(byteArray, palavraConhecida);
		
	}

}
