package br.inf.ufes.pp2015_01;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/*Para testar as funcoes localmente*/
public class MainTeste {
	
	private static Map<Integer,String> ruanTaCerto = new HashMap<Integer,String>();
	private static Master master;
	//private static SlaveManager slaveManager;
	private static Slave s;
	static int cont=0;
	
	private static String nome = "Jorge";
	static int valor = 0;
	
	public static void main(String[] args) {
		ruanTaCerto.put(2,"ruan");
		ruanTaCerto.put(2,"bruno");
		ruanTaCerto.put(4,"brenda");
		ruanTaCerto.put(1,"helena");
		
		
		
		//ruanTaCerto.remove(2);
		
		System.out.println(ruanTaCerto.get(2));
		}
			
	}
	
	
	

