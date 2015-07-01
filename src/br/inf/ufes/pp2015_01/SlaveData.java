package br.inf.ufes.pp2015_01;
public class SlaveData {
	private String nome;
	private int id;
	private long inicio;
	private long fim;
	private Slave slave;
	private Double tempo;
	
	
	
	public Double getTempo() {
		return tempo / 1000000000.0;
	}
	public void setTempo(Double tempo) {
		this.tempo = tempo;
	}
	public Slave getSlave() {
		return slave;
	}
	public void setSlave(Slave slave) {
		this.slave = slave;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getInicio() {
		return inicio;
	}
	public void setInicio(long inicio) {
		this.inicio = inicio;
	}
	public long getFim() {
		return fim;
	}
	public void setFim(long fim) {
		this.fim = fim;
	}
	
}
