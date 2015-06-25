package br.inf.ufes.pp2015_01;

import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/*Para testar as funcoes localmente*/
public class MainTeste {
	
	private static Master master;
	//private static SlaveManager slaveManager;
	private static Slave s;
	static int cont=0;
	
	private static String nome = "Jorge";
	static int valor = 0;
	
	public static void main(String[] args) {
		
		Timer timer = new Timer();  
		timer.scheduleAtFixedRate(  
		        new TimerTask() {  
		            public void run() {  
							System.out.println("int = " + valor);
		            }  
		        }, 0, 5000);
		
		for(int i=0;i<1000000000;i++){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			valor++;
		}
			
	}
	
	
	
}
