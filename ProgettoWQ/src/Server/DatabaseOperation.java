package Server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * DatabaseOperation si occupa di effettuare tutte le operazione del server
 * @author selenegerali
 *
 */

public class DatabaseOperation extends RemoteServer implements RegisterInterface {
	
	
	private static final long serialVersionUID = 1L;
	private HashMap<String,String> pass;
	public   HashMap<String,User> users;
	
	private static String passjson="./password.json";
	private static String userjson="./users.json";
	
	private static MessageDigest digest;
	
	private ArrayList<String> setWord;
	private HashMap<String,ArrayList<String>> selectWords;
	int K=6;
	private ArrayList<String> wordsTranslations;
	private BufferedWriter writesfidante;
	private BufferedWriter writesfidato;
	private ThChallenge thch;
	
	
	//costruttore
	public DatabaseOperation() {
		//se i file passjson e userjson non esistono li creo
		if(!Files.exists(Paths.get(passjson)) || !Files.exists(Paths.get(userjson))){
			pass=new HashMap<>();
			users=new HashMap<>();
			
		}
		else {
			//cre
			pass=new HashMap<>();
			users=new HashMap<>();
			Gson gs=new Gson();
			
			//password file
			String pjson=null;
			try {
				FileChannel channelIn=FileChannel.open(Paths.get(passjson), StandardOpenOption.READ);
				ByteBuffer buff=ByteBuffer.allocateDirect(1024*1024);
				boolean stop=false;
				pjson="";
				while(!stop) {
					//lettura da file
					int bytesRead=channelIn.read(buff);
					if(bytesRead==-1) {
						stop=true;
					}
					
					buff.flip();
					while(buff.hasRemaining()) {
						//scrivo quello che ho letto in pjson
						pjson=pjson+StandardCharsets.UTF_8.decode(buff).toString();
					}
					buff.flip();
				}
				channelIn.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			Type typepass=new TypeToken<HashMap<String, String>>(){}.getType();
			//aggiorno la hash map delle password con i valori appena letti in pjson
			pass=gs.fromJson(pjson, typepass);
			
			//users file
			String ujson=null;
			try {
				FileChannel channelIn=FileChannel.open(Paths.get(userjson), StandardOpenOption.READ);
				ByteBuffer buff=ByteBuffer.allocateDirect(1024*1024);
				boolean stop=false;
				ujson="";
				while(!stop) {
					//lettura da file
					int bytesRead=channelIn.read(buff);
					if(bytesRead==-1) {
						stop=true;
					}
					
					buff.flip();
					while(buff.hasRemaining()) {
						//scrivo quello che ho letto in ujson

						ujson=ujson+StandardCharsets.UTF_8.decode(buff).toString();
					}
					buff.flip();
				}
				channelIn.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			Type typeuser=new TypeToken<HashMap<String, User>>(){}.getType();
			//aggiorno la hash map degli utenti con i valori appena letti in ujson
			users=gs.fromJson(ujson, typeuser);
			
			
			
		}
	}
	
	/** Effettua la registrazione dell'utente
	 * 
	 * @param nickUtente: Username dell'utente
	 * @param password: Password dell'utente
	 * @return: Restituisce 10 se è andato tutto a buon fine, 9 se i campi sono vuoti, 11 altrimenti
	 * 
	 */
	public synchronized int user_registration(String nickUtente, String password) throws RemoteException, NullPointerException{
		//controllo i valori di nickUtente e password
		if (nickUtente == null) throw new NullPointerException("Invalid username");
		if (password == null) throw new NullPointerException("Invalid password");
		
		//controllo se i campi sono vuoti
		if(nickUtente.equals("") || password.equals("")) return 2;
		//controllo se nickUtente è già registrato
		if(pass.containsKey(nickUtente)) return 4;
		//inserisco l'utente con la relativa password crittografata
		if(pass.put(nickUtente, myHash(nickUtente + password))==null) {
			//controllo se l'utente non è presente nell ahash map degli utenti
			if(!users.containsKey(nickUtente)) {
				//inserisco l'utente nella hash map degli utenti, inizializzando l'utente
				users.put(nickUtente, new User(nickUtente));
				
				//aggiorno i file
				update_file_password();
				update_file_user();
				
				
				return 3;
			}
			else return 4;
		}
		else return 4;
		
	}
	/** Effettua la login dell'utente
	 * 
	 * @param nickUtente: Username dell'utente
	 * @param password: Password dell'utente
	 * @param address: Inet Address dell'utente
	 * @param port: porta di ascolto dell0utente
	 * @param socket: socket dell'utente
	 * @return Restituisce 12 se è andato tutto bene, 15 se l'utente era già online, 13 se la password era sbagliata, 14 se l'utente non era registrato
	 */
	public synchronized int user_login(String nickUtente, String password, String address, int port,Socket socket) {
		//controllo i valori di nickUtente, password e socket
		if (nickUtente == null) throw new NullPointerException("Invalid username");
		if (password == null) throw new NullPointerException("Invalid password");
		if(socket==null)throw new NullPointerException("Invalid socket");
		//controllo se nickutente è registrato
		if(pass.containsKey(nickUtente)) {
			//controllo la validità della password
			if(pass.get(nickUtente).equals(myHash(nickUtente+password))) {
				//controllo se l'utente era già online
				if(users.get(nickUtente).online==true) return 8;
				
				//setto le info dell'utente
				users.get(nickUtente).online=true;
				users.get(nickUtente).setPort(port);
				users.get(nickUtente).setAddress(address);
				users.get(nickUtente).socket=socket;
				return 5;
			}
			else return 6;
		}
		else return 7;
	}
	
	/** Si occupa di aggiungere l'amico alla lista degli amici dell'utente
	 * 
	 * @param nickUtente: Username dell'utente
	 * @param nickAmico: Username dell'amico
	 * @return: restituisce 18 se è andato tutto a buon fine, 17 se l'amico era già presente, 14 altrimenti. 
	 */
	public synchronized int add_friend(String nickUtente, String nickAmico) {
		//controllo i valori di nickUtente e password
		if (nickUtente == null) throw new NullPointerException("Invalid username");
		if (nickAmico == null) throw new NullPointerException("Invalid friend's name");
		
		//controllo che l'utente è presente nella hash map degli utenti
		if(users.containsKey(nickUtente)) {
			//controllo che l'amico è presente nella hash map degli utenti
			if(users.containsKey(nickAmico)) {
				//aggiungo l'amico agli amici dell'utente
				if(users.get(nickUtente).addfriend(nickAmico)==0) {
					return 10;
				}
				//aggiungo l'utente agli amici dell'amico
				if(users.get(nickAmico).addfriend(nickUtente)==0) {
					return 10;
				}
				
				//aggiorno il file
				update_file_user();
				
				return 11;
			}
			
		}
		
		return 7;
		
	}
	
	/** Si occupa di rimuovere l'amico dalla lista degli amici dell'utente
	 * 
	 * @param nickUtente: Username dell'utente
	 * @param nickAmico: Username dell'amico
	 * @return: Restituisce 20 se è andato tutto a buon fine, 19 se l'amico non era presente nella lista degli amici, 14 altrimenti.
	 */
	public synchronized int remove_friend(String nickUtente, String nickAmico) {
		//controllo i valori di nickUtente e password
		if (nickUtente == null) throw new NullPointerException("Invalid username");
		if (nickAmico == null) throw new NullPointerException("Invalid friend's name");
		
		//controllo che l'utente è presente nella hash map degli utenti
		if(users.containsKey(nickUtente)) {
			//controllo che l'amico è presente nella hash map degli utenti
			if(users.containsKey(nickAmico)) {
				//rimuovo l'amico dagli amici dell'utente
				if(users.get(nickUtente).removefriend(nickAmico)==0) {
					return 12;
				}
				//rimuovo l'utente dagli amici dell'amico
				if(users.get(nickAmico).removefriend(nickUtente)==0) {
					return 12;
				}
				//aggiorno il file
				update_file_user();
				return 13;
			}
			else return 7;
		}
		else return 7;
		
		
	}
	
	/**
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return Restituisce un JSONArray che rappresenta tutti gli amici del'utente
	 */
	@SuppressWarnings("unchecked")
	public JSONArray lista_amici(String nickUtente) {
		//controllo il valore di nickUtente
		if(nickUtente==null) throw new NullPointerException("Invalid username");
		//controllo che l'utente sia presente nel sistema
		if(users.containsKey(nickUtente)) {
			//creo un iteratore per scorrere l'array di amici dell'utente
			Iterator<String> it=users.get(nickUtente).friends.iterator();
			JSONArray list_friend=new JSONArray();
			//ciclo nell'array
			while(it.hasNext()) {
				//aggiungo al mio JSONArray l'amico
				list_friend.add(it.next());
			}
			return list_friend;
		}
		return null;
		
	}

	/** Si occupa di iniziare la sfida tra due utenti
	 * 
	 * @param nickUtente: Username dell'utente
	 * @param nickAmico: Username dell'amico
	 * @param datasock: SocketUDP dell'utente
	 * @return: Restiruisce 21 se è andato tutto a buon fine, 23 se è scattato il timeout della richiesta di sfida, 24 se la sfida è stata rifiutata,
	 * Restituisce 22 se l'amico non è online, 14 altrimenti
	 */
	public int sfida(String nickUtente, String nickAmico, DatagramSocket datasock) {
		//controllo i valori di nickUtente e nickAmico
		if (nickUtente == null) throw new NullPointerException("Invalid username");
		if (nickAmico == null) throw new NullPointerException("Invalid friend's name");
		
		//controllo che entrambi gli utenti siano presenti nel sistema e che siano amici
		if(users.containsKey(nickUtente)) {
			if(users.containsKey(nickAmico)) {
				if(users.get(nickUtente).getfriend(nickAmico)==1) {
					//controllo che sia online
					if(users.get(nickAmico).online==true) {
						//controllo che non sia occuopato in un altra sfida
						if(users.get(nickAmico).getIschallenging()==false) {
							
							//invio la richiesta di sfida all'amico
							String resultRequest=richiestaUDP(nickUtente,nickAmico,datasock);
							//attendo il risultato della richiesta di sfida
							if(resultRequest.equals("ACCEPTED")) { //se la richiesta è stata accettata
								System.out.println("Ha inizio la sfida tra "+ nickUtente +" e "+nickAmico);
								User sfidante=users.get(nickUtente);
								User sfidato=users.get(nickAmico);
								sfidante.ischallenging=true;
								sfidato.ischallenging=true;
								try {
									writesfidante=new BufferedWriter(new OutputStreamWriter(sfidante.socket.getOutputStream()));
									writesfidato=new BufferedWriter(new OutputStreamWriter(sfidato.socket.getOutputStream()));
									
								} catch (IOException e) {
									e.printStackTrace();
								}
								//leggo dal dizionario faccio la richiestra get e preparo le parole per la sfida
								chooseWords();
								//creo il thread per la sfida
								thch=new ThChallenge(sfidante,sfidato,selectWords,this);
								thch.start();
								
								return 14;
							}
							if(resultRequest.equals("TIMEOUT")) { //se è scattato il timeout
								return 16;
							}
							if(resultRequest.equals("REFUSED")) {//se la sfida è stata rifiutata
								return 17;
							}	
							
						}
					}
					return 22;
				}
				return 14;
			}
			return 14;
		}
		return 14;
		
	
		
		
	}
	
	/**
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return: Restituisce il punteggio dell'utente
	 */
	public int mostra_punteggio(String nickUtente) {
		//controllo il valore di nickUtente
		if(nickUtente==null) throw new NullPointerException();
		return users.get(nickUtente).points;
	}
	
	/**
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return: Restituisce la classifica aggiornata in ordine decrescente sottoforma di JSONArray
	 */
	@SuppressWarnings("unchecked")
	public synchronized JSONArray mostra_classifica(String nickUtente) {
		ArrayList<User> classifica=null;
		//controllo il valore di nickUtente
		if(nickUtente==null) throw new NullPointerException();
		//controllo che nickUtente sia presente nel sistema
		if(users.containsKey(nickUtente)) {
			//creo un iteratore che itera sugli amici dell'utente
			Iterator<String> it=users.get(nickUtente).friends.iterator();
			classifica=new ArrayList<>();
			//aggiungo alla classifica  l'utente e il suo punteggio
			classifica.add(users.get(nickUtente));
			while(it.hasNext()) {
				//aggiungo alla classifica tutti gli amici dell'utente con i relativi punteggi
				classifica.add(users.get(it.next()));
			}
			
			//ordino la classifica in ordine decrescente
			classifica.sort(new Comparator<User>() {

				@Override
				public int compare(User o1, User o2) {
					// TODO Auto-generated method stub
					return Integer.compare(o2.points, o1.points);
				}
			});
			
			//creo un iteratore che mi cicla sulla classifica ordinata
			Iterator<User> i_us=classifica.iterator();
			JSONArray json_cl=new JSONArray();
			JSONObject user;
			//ciclo per ogni utente
			while(i_us.hasNext()) {
				user=new JSONObject();
				User us=i_us.next();
				//inserisco nel JSONObject lo username dell'utente con il relativo punteggio
				user.put("username", us.username);
				user.put("points", us.points);
				//aggiungo al JSONArray da restituire il JSONObject
				json_cl.add(user);
				
			}
			return json_cl;
		}
		return null;
		
		
		
	}
	
	/**Effettua il logout dell'utente
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return: Restituisce 16 se l'operazione è andata a buon fine, 14 altrimenti
	 */
	public int logout(String nickUtente) {
		//controlla il valore di nickUtente
		if(nickUtente==null) throw new NullPointerException();
		//controlla che nickUtente sia presente nel sistema
		if(users.containsKey(nickUtente)) {
			users.get(nickUtente).online=false;
			return 9;
		}
		return 7;
	}
	
	/**Si occupa di inviare l'esito positivo della richiesta di sfida all'utente
	 * 
	 * @param username: Username dell'utente
	 * @param datasock: socket UDP dell'utente
	 */
	public void OKchallenge(String username, DatagramSocket datasock) {
		String text="ACCEPTED ";
		byte[] buff=text.getBytes();
		//creo il datagramma per l'invio
		DatagramPacket pk= new DatagramPacket(buff, buff.length, users.get(username).getAddress(),users.get(username).getPort());
		try {
			//invio il datagramma
			datasock.send(pk);
			System.out.println("Send to client mittente: " + text);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Si occupa di inviare l'esito negativo della richiesta di sfida all'utente
	 * 
	 * @param command: Stringa di esito della richiesta di sfida
	 * @param nickUtente: Username dell'utente
	 * @param nickAmico: Username dell'amico
	 * @param datasock: socket UDP
	 */
	public void NOchallenge(String command, String nickUtente,String nickAmico, DatagramSocket datasock) {
		String text=null;
		
		if(command.equals("REFUSED")) {
			text="REFUSED";
			
		}
		 if(command.equals("TIMEOUT")) {
			text=("TIMEOUT "+nickUtente);
			
		}
			byte[] buff=command.getBytes();
			//creo il datagramma per l'invio
			DatagramPacket pk=new DatagramPacket(buff, buff.length,users.get(nickAmico).getAddress(), users.get(nickAmico).getPort());
			DatagramPacket pk2=new DatagramPacket(buff, buff.length,users.get(nickUtente).getAddress(),users.get(nickUtente).getPort());
			try {
				//invio il datagramma
				datasock.send(pk);
				datasock.send(pk2);
				System.out.println("Send to client mittente: " + text);
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
	
	/**
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return: Restituisce il riferimento all'utente
	 */
	public synchronized User getUser(String nickUtente) {
		//controllo il valore di nickUtente
		if(nickUtente==null) throw new NullPointerException("Invalid nickname");
		//controllo che l'utente sia presente nel sistema
		if(users.containsKey(nickUtente)) {
			return users.get(nickUtente);
		}
		else {
			return null;
		}
	}
	
	/** Si occupa di inviare all'amico la richiesta di sfida e attende l'esito della risposta
	 * 
	 * @param nickUtente: Username dell'utente
	 * @param nickAmico: Username dell'amico
	 * @param datasock: socket UDP
	 * @return Restituisce l'esito della richiesta di sfida
	 */
	public String richiestaUDP(String nickUtente, String nickAmico, DatagramSocket datasock) {
		String txt="CHALLENGE " + nickUtente ;
		byte[] buff=txt.getBytes();
		//creo il datagramma per l'invio
		DatagramPacket pk= new DatagramPacket(buff, buff.length, users.get(nickAmico).getAddress(), users.get(nickAmico).getPort());
		try {
			//setto un timeout di 10 secondi
			datasock.setSoTimeout(10000);
			//invio la richiesta di sfida tramite udp
			datasock.send(pk);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		System.out.println("pacchetto UDP inviato al client destinatario: "+txt);
		
		
		//attesa della risposta
		buff=new byte[128];
		//creo un datagramma per la ricezione
		DatagramPacket receiveclient = new DatagramPacket(buff, buff.length);
		
			try {
				//ricevo la risposta
				datasock.receive(receiveclient);
			} 
			catch(SocketTimeoutException e) {
			System.out.println("UDPsocket scattato TIMEOUT");
			//se scatta il timeout mando allo sifdante TIMEOUT
			NOchallenge("TIMEOUT", nickUtente, nickAmico, datasock);
			
			return "TIMEOUT";
			
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		//leggo il datagramma ricevuto dal client
		String packetreceived=new String(receiveclient.getData(),0,receiveclient.getLength());
		System.out.println("Pacchetto ricevuto dal client in risposta alla richiesta: "+ packetreceived);
		String [] command=packetreceived.split("\\s+");
		if(command[0].equals("REFUSED")) {
			NOchallenge("REFUSED", nickUtente, nickAmico, datasock);
			return command[0];
			
		}
		if(command[0].equals("ACCEPTED")) {
			OKchallenge(nickUtente, datasock);
			OKchallenge(nickAmico, datasock);
			return command[0];
			
		}
		return null;
	}
	
	
	/**
	 * Si occpua di selezionare casualmente K parole e ottenere le possibili traduzioni
	 * @return
	 */
	public HashMap<String,ArrayList<String>> chooseWords(){
		
		try {
			//leggo le parole dal file e le memorizzo in un array
			BufferedReader buffReader=new BufferedReader(new FileReader(new File("Dictionary")));
			setWord = new ArrayList<>();
			String string;
			while((string=buffReader.readLine())!=null) {
				//aggiungo le parole lette dal file Dictionary
				setWord.add(string);
			}
			buffReader.close();
			
			//scelgo le k paole casuali 
			selectWords=new HashMap<>();
			//seleziono gli elementi in modo casuale
			Collections.shuffle(setWord);
			for(int i=0;i<K;i++) {
				String wd=setWord.get(i);
				//download della traduzione della parole
				
				URL url=new URL("https://api.mymemory.translated.net/get?q=" + wd + "!&langpair=it|en");
				HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
				conn.setRequestMethod("GET");
				BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inLine;
				StringBuilder content=new StringBuilder();
				while((inLine=reader.readLine())!=null) {
					content.append(inLine);
				}
				reader.close();
				
				//parsing del json ricevuto
				JsonElement json=new JsonParser().parse(content.toString());
				JsonArray translations=json.getAsJsonObject().get("matches").getAsJsonArray();
				wordsTranslations=new ArrayList<>();
				for(JsonElement match:translations) {
					String trad=match.getAsJsonObject().get("translation").getAsString()
							.toLowerCase()
							.replaceAll("!", "")
							.replaceAll("-", "");
					//aggiugno la traduzione della parola		
					wordsTranslations.add(trad);
					
				}
				//inserisco la parola con le sue traduzioni
				selectWords.put(wd, wordsTranslations);
				
			}
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		return selectWords;
	}
	
	
	/**Si occpua di decretare il vincitore e assegnare i relativi punteggi
	 * 
	 * @param sfidante: Riferimento dell'utente sfidante
	 * @param pointsSfidante: punti partita dello sfidante
	 * @param sfidato: Riferimento dell'utente sfidato
	 * @param pointsSfidato: punti partita dello sfidato
	 */
	public void challengEnd(User sfidante,int pointsSfidante,User sfidato, int pointsSfidato) {
		
		
		if(pointsSfidante>pointsSfidato) {
			//ha vinto lo sfidante, l'utente vincitore guadagna 3 punti
			sfidante.points=sfidante.points+pointsSfidante+3;
			sfidato.points=sfidato.points+pointsSfidato;
			
			//invio il risultato della sfida ai 2 utenti se entrambi hanno terminato
			try {
				if(sfidante.exit==false && sfidato.exit==false) {
					
					writesfidante.write("Hai Vinto la sfida!  Tuo punteggio: "+pointsSfidante+ "  punteggio avversario: "+ pointsSfidato+".  Punteggio totale: "+ sfidante.points);
					writesfidante.newLine();
					writesfidante.flush();
					writesfidato.write("Hai Perso la sfida!  Tuo punteggio: "+pointsSfidato+"  punteggio avversario: "+ pointsSfidante+".  Punteggio totale: "+ sfidato.points);
					writesfidato.newLine();
					writesfidato.flush();
				
				}
				else {
					if(sfidante.exit==true) {
						writesfidato.write("Hai Perso la sfida!  Tuo punteggio: "+pointsSfidato+"  punteggio avversario: "+ pointsSfidante+".  Punteggio totale: "+ sfidato.points);
						writesfidato.newLine();
						writesfidato.flush();
					}
					if(sfidato.exit==true) {
						writesfidante.write("Hai Vinto la sfida!  Tuo punteggio: "+pointsSfidante+ "  punteggio avversario: "+ pointsSfidato+".  Punteggio totale: "+ sfidante.points);
						writesfidante.newLine();
						writesfidante.flush();
					}
				}
				
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		
		else if(pointsSfidante<pointsSfidato) {
			//ha vinto lo sfidante, l'utente vincitore guadagna 3 punti
			sfidante.points=sfidante.points+pointsSfidante;
			sfidato.points=sfidato.points+pointsSfidato+3;
			
			//invio il risultato della sfida ai 2 utenti
			try {
				if(sfidante.exit==false && sfidato.exit==false) {
					writesfidante.write("Hai Perso la sfida!  Tuo punteggio: "+pointsSfidante+"  punteggio avversario: "+ pointsSfidato+".  Punteggio totale: "+ sfidante.points);
					writesfidante.newLine();
					writesfidante.flush();
					writesfidato.write("Hai Vinto la sfida!  Tuo punteggio: "+pointsSfidato+"  punteggio avversario: "+ pointsSfidante+".  Punteggio totale: "+ sfidato.points);
					writesfidato.newLine();
					writesfidato.flush();
				}
				
				else {
					if(sfidante.exit==true) {
						writesfidato.write("Hai Vinto la sfida!  Tuo punteggio: "+pointsSfidato+"  punteggio avversario: "+ pointsSfidante+".  Punteggio totale: "+ sfidato.points);
						writesfidato.newLine();
						writesfidato.flush();
					}
					if(sfidato.exit==true) {
						writesfidante.write("Hai Perso la sfida!  Tuo punteggio: "+pointsSfidante+"  punteggio avversario: "+ pointsSfidato+".  Punteggio totale: "+ sfidante.points);
						writesfidante.newLine();
						writesfidante.flush();
					}
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		else {
			//parità. in questo caso assegno +1 a entrambi i giocatori
			sfidante.points=sfidante.points+pointsSfidante+1;
			sfidato.points=sfidato.points+pointsSfidato+1;
			
			//invio il risultato della sfida ai 2 utenti

			try {
				if(sfidante.exit==false && sfidato.exit==false) {
					writesfidante.write("Pareggio!  Tuo punteggio: "+pointsSfidante+"  punteggio avversario: "+ pointsSfidato+".  Punteggio totale: "+ sfidante.points);
					writesfidante.newLine();
					writesfidante.flush();
					writesfidato.write("Pareggio!  Tuo punteggio: "+pointsSfidato+"  punteggio avversario: "+ pointsSfidante+".  Punteggio totale: "+ sfidato.points);
					writesfidato.newLine();
					writesfidato.flush();
				}
				else {
					if(sfidante.exit==true) {
						writesfidato.write("Pareggio!  Tuo punteggio: "+pointsSfidato+"  punteggio avversario: "+ pointsSfidante+".  Punteggio totale: "+ sfidato.points);
						writesfidato.newLine();
						writesfidato.flush();
					}
					if(sfidato.exit==true) {
						writesfidante.write("Pareggio!  Tuo punteggio: "+pointsSfidante+"  punteggio avversario: "+ pointsSfidato+".  Punteggio totale: "+ sfidante.points);
						writesfidante.newLine();
						writesfidante.flush();
					}
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		//salvo i dati nel file
		update_file_user();
		
		//termino il thread
		
		if(thch.isAlive()) {
			System.out.println("terminazione thread");
			thch.interrupt();
			
		}
		
	}
	
	/** Si occupa di crittografare la password
	 * 
	 * @param password: Password utente
	 * @return
	 */
	private static String myHash(String password) {
		byte[] hash = null;
		try {
			//algoritmo di hashing one-way
			digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (hash!=null) {
			//creo la stringa ottenuta dal byte array restituito dal digest
			StringBuffer hexString = new StringBuffer();
		    for (int i = 0; i < hash.length; i++) {
		    	String hex = Integer.toHexString(0xff & hash[i]);
		    	if(hex.length() == 1) hexString.append('0');
		        hexString.append(hex);
		    }
		    return hexString.toString();
		}
		else return null;
	}
	
	/**
	 * Aggiorna il file delle password
	 */
	public void update_file_password(){
		Gson gs=new Gson();
		String pjson=gs.toJson(pass);
		try {
			//buffer dove inserisco il mio json array
			ByteBuffer buff=ByteBuffer.wrap(pjson.getBytes("UTF-8"));
			try{
				Files.deleteIfExists(Paths.get(passjson));
				Files.createFile(Paths.get(passjson));
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			FileChannel channelOut=FileChannel.open(Paths.get(passjson), StandardOpenOption.WRITE);
			
			while(buff.hasRemaining()) {
				channelOut.write(buff);
			}
			channelOut.close();
		} 
		catch(IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Aggiorno il file degli utenti
	 */
	public void update_file_user(){
		
			
		
		Gson gs=new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
		String ujson=gs.toJson(users);
		try {
			//buffer dove inserisco il mio json array
			ByteBuffer buff=ByteBuffer.wrap(ujson.getBytes("UTF-8"));
			try{
				Files.deleteIfExists(Paths.get(userjson));
				Files.createFile(Paths.get(userjson));
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			FileChannel channelOut=FileChannel.open(Paths.get(userjson), StandardOpenOption.WRITE);
			
			while(buff.hasRemaining()) {
				channelOut.write(buff);
			}
			channelOut.close();
		} 
		catch(IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	

}
