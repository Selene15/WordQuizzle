package Server;


import java.util.ArrayList;

import java.util.HashMap;


/** Thread dedicato alla sfida, si occupa di inizializzare L'hashMap dei 2 utenti e attende che i due utenti terminino la sfida
 * 
 * @author selenegerali
 *
 */

public class ThChallenge extends Thread{
	
	private User sfidante;
	private User sfidato;
	private DatabaseOperation dbop;
	private HashMap<String,ArrayList<String>> selectWords;
	
	
	/** Costruttore
	 * 
	 * @param sfidante: Username dello sfidante
	 * @param sfidato: Username dello sfidato
	 * @param selectWords: Parole da tradurre
	 * @param dbop: Riferimento al DatabaseOperation
	 * 
	 */
	public ThChallenge(User sfidante, User sfidato, HashMap<String,ArrayList<String>> selectWords,DatabaseOperation dbop) {
		this.sfidante=sfidante;
		this.sfidato=sfidato;
		this.selectWords=selectWords;
		this.dbop=dbop;
		
	}
	
	public void run() {
		//inizializzo le hash map dei 2 utenti in sfida
		sfidante.selectWords=new HashMap<>(selectWords);
		sfidato.selectWords=new HashMap<>(selectWords);
		
		
		
		//attendo che entrambi gli utenti abbiano finito la sfida
		while(sfidante.ischallenging==true) {
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		while(sfidato.ischallenging==true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//chiamo la funzione per decretare il vincitore
		
		dbop.challengEnd(sfidante,sfidante.pointschallenge,sfidato,sfidato.pointschallenge);
		
	}

}

