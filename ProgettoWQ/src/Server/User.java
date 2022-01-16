package Server;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Utente di WQ
 * @author selenegerali
 *
 */
public class User {
	//transient serve per non serializzare la variabile
	public transient boolean online=false;
	public String username;
	public int points;
	public ArrayList<String> friends;
	private transient InetAddress inetadd;
	private transient int portUDP;
	public transient Socket socket;
	public transient boolean ischallenging=false;
	public transient HashMap<String,ArrayList<String>> selectWords;
	public transient int pointschallenge;
	public transient boolean exit=false;
	
	//costruttore
	public User(String username) {
		this.username=username;
		this.points=0;
		friends=new ArrayList<>();
	}
	
	/** Setta l'inet Address dell'utente
	 * 
	 * @param Inet Address dell'utente
	 */
	public void setAddress(String a) {
		try {
			inetadd=InetAddress.getByName(a);
		}
		catch(UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return Restituisce l'Inet Address dell'utente
	 */
	public InetAddress getAddress() {
		return inetadd;
	}
	
	
	/** Setta la porta dell'utente
	 * 
	 * @param p: porta
	 */
	public void setPort(int p) {
		portUDP=p;
	}
	
	/**
	 * 
	 * @return Restituisce la porta dell'utente
	 */
	public int getPort() {
		return portUDP;
	}
	
	/** Controlla se l'amico è presente o meno nella lista amici
	 * 
	 * @param nickAmico: Username dell'amico
	 * @return Restituisce 0 se il nome dell'amico èlo stesso del nome dell'utente o se l'amico è già presente nella lista degli amici dell'utente.
	 * Restituisce 1 se l'amico non è presente nella lista degli amici dell'utente.
	 */
	public int getfriend(String nickAmico) {
		if(nickAmico.equals(username))return 0;
		if(!friends.contains(nickAmico)) return 0;
		return 1;
		
	}
	
	/**Aggiunge l'amico nella lista amici dell'utente
	 * 
	 * @param nickAmico: Username dell'amico
	 * @return Restituisce 1 se aggiunge correttamente l'amico nella lista
	 * Restituisce 0 se il nome dell'amico èlo stesso del nome dell'utente o se l'amico è già presente nella lista degli amici dell'utente.
	 */
	public int addfriend(String nickAmico) {
		
		if(nickAmico.equals(username)|| friends.contains(nickAmico)) return 0;
		friends.add(nickAmico);
		return 1;
	}
	
	/** Rimuove l'amico dalla lista amici dell'utente
	 * 
	 * @param nickAmico: Username dell'amico
	 * @return Restituisce 1 se rimuove correttamente l'amico dalla lista, 0 altrimenti.
	 */
	public int removefriend(String nickAmico) {
		if(friends.contains(nickAmico)) {
			friends.remove(nickAmico);
			return 1;
		}
		else {
			return 0;
		}
	}
	
	/** Setta la variabile ischallenging che rappresenta se l'utente sta giocando oppure no
	 * 
	 * @param b: stato (true/false)
	 */
	public void setischallenging(boolean b) {
		ischallenging=b;
	}
	
	/**
	 * 
	 * @return Restituisce il valore della variabile ischallenging
	 */
	public boolean getIschallenging() {
		return ischallenging;
	}

}
