/**
 * Master.java
 */
package br.inf.ufes.pp2015_01;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master extends Remote, SlaveManager, Attacker{
	// o mestre � um SlaveManager e um Attacker
}
