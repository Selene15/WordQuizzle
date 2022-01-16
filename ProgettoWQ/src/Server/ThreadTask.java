package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;

 
/** Thread dedicato al singolo client che si connette
 * 
 * @author selenegerali
 *
 */

public class ThreadTask implements Runnable{

	private DatabaseOperation dbop;
	private Socket TCPsock;
	private DatagramSocket UDPsock;
	private String username;
	private BufferedReader read;
	private BufferedWriter write;
	public boolean play=false;
	public int count=1;
	
	//costruttore
	public ThreadTask(DatabaseOperation dbop, Socket sock) {
		this.dbop=dbop;
		this.TCPsock=sock;
	}
	
	public void run() {
		try {
			
			read=new BufferedReader(new InputStreamReader(TCPsock.getInputStream()));
			write=new BufferedWriter(new OutputStreamWriter(TCPsock.getOutputStream()));
			
			//in ascolto di operazioni in arrivo dal client
			while(true) {
				//leggo la stringa ricevuta dal client
				String stdin=read.readLine();
				if(stdin==null) {
					op_write(Integer.toString(9));
					
				}
				
				System.out.println("ThreadTASK - Operation: " + stdin);
				
				if (username!=null) { 
					System.out.println("USER "+ username + " ischallenging: " +dbop.users.get(username).ischallenging);
					
				}
			
				//tokenizzo la stringa ricevuta dal client
				String[] tok=stdin.split("\\s+");
				System.out.println("tok "+tok[0]);
			
				if(tok[0].equals("LOGIN")) {
					username=tok[1];
					//chiamo la funzione per effettuare il login
					int result=dbop.user_login(tok[1],tok[2], tok[3], Integer.parseInt(tok[4]),TCPsock);
					
					//se il login è andato a buon fine, creo la socket per le richieste di sfida
					System.out.println("result: " + result);
					if(result==5) {
						
						UDPsock=new DatagramSocket();
						
					}
					//invio al client il risultato dell'operazione
					op_write(Integer.toString(result));
					
					
					//se il login è fallito esco dal thread
					if(result!=5) {
						System.out.println(Thread.currentThread().getName() + "ThreadTask Exit");
						return;
					}
				}
				
				
				else if(tok[0].equals("ADDFRIEND")) {
					//chiamo la funzione per aggiungere l'amico
					int result=dbop.add_friend(tok[1], tok[2]);
					//invio al client il risultato dell'operazione
					op_write(Integer.toString(result));
					
					
				}
				
				else if(tok[0].equals("LISTFRIEND")){
					//chiamo la funzione che restituisce la lista amici
					String friendlist=dbop.lista_amici(tok[1]).toJSONString();
					//invio al client la lista amici
					op_write(friendlist);
					
				}
				
				else if(tok[0].equals("REMOVEFRIEND")) {
					//chiamo la funzione per rimuovere l'amico
					int result=dbop.remove_friend(tok[1], tok[2]);
					//invio al client il risultato dell'operazione
					op_write(Integer.toString(result));
					
				}
				else if(tok[0].equals("CHALLENGE")) {
					//creo una porta random per la sfida
					//int portsfidaTCP=(int)((Math.random()*((65535-1024)+1))+1024);
					
					//int result=dbop.sfida(tok[1],tok[2],UDPsock,portsfidaTCP);
					//chiamo la funzione per la sfida
					int result=dbop.sfida(tok[1],tok[2],UDPsock);
					//invio al client il risultato dell'operazione
					op_write(Integer.toString(result));
				}
				
				else if(tok[0].equals("SCORE")) {
					//chiamo la funzione che mi restituisce il punteggio di un utente
					int result=dbop.mostra_punteggio(tok[1]);
					//invio al client il punteggio
					op_write(Integer.toString(result));
					
				}
				
				else if(tok[0].equals("RANK")) {
					//chiamo la funzione che mi restituisce la classifica
					String cl=dbop.mostra_classifica(tok[1]).toJSONString();
					//invio al client la classifica
					op_write(cl);
					
				}
				
				else if(tok[0].equals("LOGOUT")) {
					//chiamo la funzione per effettuare il logout
					
					int result=dbop.logout(tok[1]);
					System.out.println("logout: " + result);
					//invio al client il risultato dell'operazione
					op_write(Integer.toString(result));
					//chiudo la socket UDP
					UDPsock.close();
					//chiudo la socket TCP
					TCPsock.close();
					System.out.println(Thread.currentThread().getName() + "Exiting");
					break;
				}
				
				else if(tok[0].equals("STARTCH")){
					//chiamo la funzione per dare inizio alla sfida
					startChallenge();
				}
	
				else {
					op_write(Integer.toString(9));
					
				}
			}
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Funzione che invia le stringhe relative alla sfida al client
	 */
	public void startChallenge() {
		
		
		//aspetto 4 secondi per l'inizio della sfida
				try {
					Thread.sleep(7000);
				}
				catch(InterruptedException e) {
					
				}
		//setto alcune info dell'utente
				dbop.users.get(username).exit=false;
		dbop.users.get(username).setischallenging(true);
		dbop.users.get(username).pointschallenge=0;
		
		
		String stdin;
		try {
			
			System.out.println("selectword: "+dbop.users.get(username).selectWords);
			
			//invio la stringa di inizio sfida al client
			op_write("STARTCHALLENGE");
			
			//attendo che il thread della partita mi inizializzi la hash map delle parole da tradurre
			while(dbop.users.get(username).selectWords==null) {
			}
			//Scorro le parole da tradurre
			for(String word:dbop.users.get(username).selectWords.keySet()){
				
				System.out.println("Scrivo sulla socket del client la parola "+word);
				//invio la parola al client
				op_write(word);
				
				try { 
					Thread.sleep(1000); 
				}
				catch (InterruptedException ignored) {}
				
				
				//attendo la risposta
				stdin=read.readLine();
				
				
				System.out.println("Parola tradotta letta: " + stdin);
				String[] tok=stdin.split("\\s+");
				//array contenente tutte le traduzioni della parola
				ArrayList<String> wordsToget=dbop.users.get(username).selectWords.get(word);
				
				if(tok[0].equals("TIMEOUT")) { //scattato timeout, il giocatore non ha risposto
					//aggiungo 0 punti ai punti partita dell'utente
					
					dbop.users.get(username).pointschallenge+=0;
				}
				else if(tok[0].equals("EXITCHALLENGE")) {
					op_write("EXIT");
					dbop.users.get(username).exit=true;
					break;
				}
				else if(wordsToget.contains(tok[0].toLowerCase())) { //il giocatore ha risposto correttamente
					//aggiungo 3 punti ai punti partita dell'utente
					dbop.users.get(username).pointschallenge+=3;
					
				}
				else {//ha risposto in modo sbagliato
					
					//tolgo 1 punt ai punti partita dell'utente
					dbop.users.get(username).pointschallenge-=1;
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		//Avverto l'utente della fine della sfida
		if(dbop.users.get(username).exit==false) {
			op_write("CHALLENGEEND");
		}
			
		//setto alcune info dell'utente
		dbop.users.get(username).setischallenging(false);
		dbop.users.get(username).selectWords=null;
		
	}
	
	/** Si occupa di inviare messaggi al client
	 * 
	 * @param str: stringa da inviare al client
	 */
	public void op_write(String str) {
		try {
			write.write(str);
			write.newLine();
			write.flush();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	
}
