/**
 * Guess.java
 */
package br.inf.ufes.pp2015_01;
import java.io.Serializable;

public class Guess implements Serializable {
	private String key;		
	// chave candidata
	
	private byte[] message;	
	// mensagem decriptografada com a chave candidata

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public byte[] getMessage() {
		return message;
	}
	public void setMessage(byte[] message) {
		this.message = message;
	}
	
}
