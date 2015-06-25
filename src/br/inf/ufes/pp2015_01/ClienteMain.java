package br.inf.ufes.pp2015_01;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.UUID;

public class ClienteMain {

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
	private static Master findMaster(String host) {
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

		/*
		 * Argumentos: 
		 * args[0]: endereco do mestre 
		 * args[1]: palavra conhecida
		 * args[2]: nome do arquivo criptografado 
		 * args[3]: tamanho do vetor
		 */

		String fileName = args[2];
		byte[] palavraConhecida = args[1].getBytes();
		byte[] byteArray = null;

		Path path = Paths.get(fileName);
		try {
			byteArray = Files.readAllBytes(path);
		} catch (IOException e1) {
			System.out.println("[DEBUG]: Arquivo" + fileName +" nao encontrado.");
			if (args.length > 3) {
				byteArray = geraArquivo(Integer.parseInt(args[3]));
			} else {
				byteArray = geraArquivo();
			}
		}

		
		Master master = findMaster(args[0]);

		try {
			master.attack(byteArray, palavraConhecida);

		} catch (RemoteException e) {
			System.out.println("[DEBUG]: Erro na execucao do Mestre");
		}

	}

}
