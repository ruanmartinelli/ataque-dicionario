package br.inf.ufes.pp2015_01;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("unused")
public class ClienteMain {

	private static String fileName;
	private static byte[] palavraConhecida;
	private static byte[] byteArray;
	private static Guess[] resultado;
	
	public ClienteMain(){
		
	}
	
	/* Gera arquivo de tamanho aleatorio caso nao seja passado um vetor. */
	private static byte[] geraArquivo() {
		Random r = new Random();
		int size = r.nextInt(100000 - 1000) + 1000;

		return geraArquivo(size);
	}

	/* Gera arquivo de tamanho 'int tamanho' */
	private static byte[] geraArquivo(int tamanho) {

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

	/* Procura mestre no Registry e retorna a interface encontrada. */
	private static Attacker findMaster(String host) {
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

	public static int indexOf(byte[] outerArray, byte[] smallerArray) {
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
	
	private static void ataqueSerial(byte[] ciphertext, byte[] knowntext){
		/* Armazena o Dicionario em uma lista */
		List<String> dicionario = new ArrayList<String>();
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
			System.out.println("[DEBUG]: Arquivo dicionario do escravo nao encontrado.");
		}
		
		int index = 0;
		for(String palavra : dicionario){
			byte[] resposta = decrypt(palavra, ciphertext);	
			
			if(resposta != null && indexOf(resposta, knowntext) != -1){
				Guess candidata = new Guess();
				candidata.setKey(palavra);
				candidata.setMessage(resposta);
				System.out.println("[INFO]: Guess encontrada");
				System.out.println("	Chave: " + palavra + " / " + index);
			}
			index++;
		}
		System.out.println("[INFO]: Ataque serial concluído.");
	}
	
	private static void gerarCsvSerial(String argumento, byte[] palavraConhecida){
		String nome = "[csv-filename-not-given]";
		StringBuilder texto = new StringBuilder();
		nome = argumento;
		try {
			PrintWriter csv = new PrintWriter(nome + ".csv");
			
			for(int i = 1000 ; i < 100000 ; i = i + 500){
				byte[] msg = geraArquivo(i);
				Long begin = System.nanoTime();
				ataqueSerial(msg, palavraConhecida);
				Long end = System.nanoTime();
				
				Long tempoTotal = end - begin;
				
				texto.append(i);
				texto.append(",");
				texto.append(tempoTotal / 1000000000.0);
				texto.append("\n");
				
				csv.write(texto.toString()); /*Grafico TEMPO DE RESPOSTA x TAMANHO DA MENSAGEM*/
				texto.setLength(0);
			}
			
			csv.close();
		} catch (FileNotFoundException e1) {
			System.out.println("[ERRO]: Erro gerar arquivos CSVs.");
		}
	}
	
	private static void gerarCsv(String argumento, byte[] palavraConhecida, Attacker servicoAtaque){
		String nome = "[csv-filename-not-given]";
		StringBuilder texto = new StringBuilder();
		nome = argumento;
		try {
			PrintWriter csv = new PrintWriter(nome + ".csv");
			
			for(int i = 1000 ; i < 100000 ; i = i + 500){
				byte[] msg = geraArquivo(i);
				Long begin = System.nanoTime();
				servicoAtaque.attack(msg, palavraConhecida);
				Long end = System.nanoTime();
				
				Long tempoTotal = end - begin;
				
				texto.append(i);
				texto.append(",");
				texto.append(tempoTotal / 1000000000.0);
				texto.append("\n");
				
				csv.write(texto.toString()); /*Grafico TEMPO DE RESPOSTA x TAMANHO DA MENSAGEM*/
				texto.setLength(0);
			}
			
			csv.close();
		} catch (FileNotFoundException | RemoteException e1) {
			System.out.println("[ERRO]: Erro gerar arquivos CSVs.");
		}
	}
	
	
	public static void main(String[] args) {

		/*
		 * Argumentos: 
		 * 
		 * [0]: tipo de execucao
		 * 
		 * [1P]: endereco do mestre
		 * [2P]: palavra conhecida
		 * [3P]: arquivo MC
		 * [4P]: nome do arquivo
		 * [5P - OPCIONAL]: tamanho do vetor
		 * 
		 * [1S]: palavra conhecida
		 * [2S]: arquivo MC
		 * [3S]: nome do arquivo 
		 *  
		 */
		if(args[0].equals("paralelo")){
			fileName 		= args[3];
			palavraConhecida = args[2].getBytes();
			byteArray 		= null;
			resultado 		= new Guess[400];

			Path path = Paths.get(fileName);
			try {
				byteArray = Files.readAllBytes(path);
			} catch (IOException e1) {
				System.out.println("[DEBUG]: Arquivo" + fileName +" nao encontrado.");
				if (args.length > 5) {
					byteArray = geraArquivo(Integer.parseInt(args[4]));
				} else {
					byteArray = geraArquivo();
				}
			}
			Attacker master = findMaster(args[1]);
			
			/*
			try {
				resultado = master.attack(byteArray, palavraConhecida);
				
			} catch (RemoteException e) {
				System.out.println("[DEBUG]: Erro na execucao do Mestre");
			}
			
			if(resultado.length > 0){
				try {
					for(int i = 0 ; i < resultado.length ; i++){
						FileOutputStream fos = new FileOutputStream(resultado[i].getKey()+".msg");
						fos.write(resultado[i].getMessage());
						fos.close();
					}
				} catch (IOException e) {
					System.out.println("[DEBUG]: Erro ao escrever Arquivo msg");
				}
			}
			 */			
			gerarCsv(args[4], palavraConhecida, master);
		}
		
		if(args[0].equals("serial")){
			gerarCsvSerial(args[3], palavraConhecida);
		}
	}

}





