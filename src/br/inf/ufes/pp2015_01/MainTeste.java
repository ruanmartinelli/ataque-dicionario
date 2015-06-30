package br.inf.ufes.pp2015_01;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import javax.crypto.Cipher;


/*Para testar as funcoes localmente*/
public class MainTeste {
    
    private static Map<Integer,String> ruanTaCerto = new HashMap<Integer,String>();
    private static Master master;
    //private static SlaveManager slaveManager;
    private static Slave s;
    static int cont=0;
    
    private static String nome = "Jorge";
    static int valor = 0;
    
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
    
    static String completa = "ola galera.";
    static String parte = "era";
    static String keyM = "masterkey";
    
    static byte[] A = completa.getBytes();
    static byte[] B = parte.getBytes();
    static byte[] encrypted;
    static byte[] decrypted;	
    public static void main(String[] args) {

		try {
			byte[] key = keyM.getBytes();
			SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
			
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			
			byte [] message = A;
			System.out.println("message size (bytes) = "+message.length);
			
			encrypted = cipher.doFinal(message);
			
			//saveFile(args[1]+".cipher", encrypted);

		} catch (Exception e) {
			// don't try this at home
			e.printStackTrace();
		}


		try {
			
			byte[] key = keyM.getBytes();
			SecretKeySpec keySpec = new SecretKeySpec(key, "Blowfish");
			
			Cipher cipher = Cipher.getInstance("Blowfish");
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			
			byte[] message = A;
			System.out.println("message size (bytes) = "+ message.length);
			
			decrypted = cipher.doFinal(message);
			
			//saveFile(args[0]+".msg", decrypted);
		
		} catch (javax.crypto.BadPaddingException e) {
			// essa excecao e jogada quando a senha esta incorreta
			// porem nao quer dizer que a senha esta correta se nao jogar essa excecao
			System.out.println("Senha invalida.");
		
		} catch (Exception e) {
			//dont try this at home
			e.printStackTrace();
		}	


        
        System.out.println(indexOf(encrypted,decrypted));
        
        
        }
            
    }











