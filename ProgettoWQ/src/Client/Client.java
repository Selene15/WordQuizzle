package Client;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/** Client di WQ
 * 
 * @author selenegerali
 *
 */

public class Client {

	public Socket TCPsock;
	private int portUDP;
	private DatagramSocket UDPsock;
	public BufferedWriter writer;
	public BufferedReader reader;
	public String user;
	private UDPcontroller threadUdp;
	public int TCPportsfida;
	private static GuiChallenge window ;
	private Thread thUdp;
	public static RegisterInterface serverObject;
	private static GuiRegisterLogin window1;
	private GuiOperation window2;
	public Timer timer;
	private Client rif_cl;
	
	

	
	
	public Client() {
		
		this.TCPsock=null;
		this.portUDP=0;
		this.UDPsock=null;
		this.writer=null;
		this.reader=null;
		this.user=null;
		
		
	}
	
	
	
	public static void main(String[] args)   {
		
	
		//Ottiene un riferimento all'istanza remota
		
		Remote remoteObject;
	
	
		try {
			Registry r=LocateRegistry.getRegistry(30000);
			//oggetto remoto
			remoteObject=r.lookup("REGISTER_SERVER");
			serverObject=(RegisterInterface) remoteObject;
			
			
		//invoco l'interfaccia iniziale
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window1 = new GuiRegisterLogin();
					
					window1.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		}
	
	catch(Exception e) {
		e.printStackTrace();
	}
	}



	/** Richiede al server di effettuare il login dell'utente
	*@param nickUtente: Username dell'utente per effettuare il login
	*@param password: Password dell'utente
	*@return Restituisce un intero a seconda del risultato dell'operazione. L'operazione va a buon fine solo se l'utente è gia stato registrato a WQ
	*/
	public int login(String nickUtente, String password)  {
		
		user=nickUtente;
		int error=0;
		try {
			//socket per la connessione con il server
			TCPsock=new Socket("localhost", 5789);
			//porta UDP 
			portUDP=(int)((Math.random()*((65535-1024)+1))+1024);
			System.out.println("socket "+ TCPsock.getOutputStream());
			
			writer=new BufferedWriter(new OutputStreamWriter(TCPsock.getOutputStream()));
			reader=new BufferedReader(new InputStreamReader(TCPsock.getInputStream()));
			
			String text="LOGIN " + nickUtente + " " + password + " " + TCPsock.getInetAddress().getHostAddress() + " " + portUDP;
			//invio l'operazione da effettuare al server
			op_write(text);
			
			//leggo il risultato dell'operazione
			error=Integer.parseInt(reader.readLine());
			String code=Client.codeResult(error);
			JOptionPane.showMessageDialog(null, code);
			
			//se il login non è andato a buon fine
			if(error!=5) {
				System.out.println("CLIENT - Login failed");
				TCPsock.close();
				writer.close();
				reader.close();
				return error;
			}
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//se il login è andato a buon fine 
		window1.frame.setVisible(false);
		
		
		rif_cl=this;
		//invoco l'interfaccia delle operazioni di WQ
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window2 = new GuiOperation(rif_cl);
					window2.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
		
		System.out.println("CLIENT - UDP port:"+ portUDP);
		try {
			
			UDPsock=new DatagramSocket(portUDP);
		}
		catch(SocketException e) {
			e.printStackTrace();
		}
		
		//creo un thread per le richieste di sfida
		threadUdp=new UDPcontroller(this,UDPsock);
		thUdp=new Thread(threadUdp);
		thUdp.start();
		
		
		return error;
		
	}
	
	/** Richiede al server l'inserimento di un amico
	 * 
	 * @param nickAmico: Username dell'utente che richiede l'aggiunta dell'amico
	 * @param nickUtente: Username dell'amico da aggiungere
	 * @return Restituisce un intero a seconda del risultato dell'operazione
	 */
	public int addfriend(String nickAmico,String nickUtente) {
		

		int result;
		try {
			
			String text="ADDFRIEND " + nickUtente + " " + nickAmico;
			//invio l'operazione da effettuare al server
			op_write(text);
			
			//leggo il risultato dell'operazione
			result=Integer.parseInt(reader.readLine());
			return result;
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/** Richiede al server la lista degli amici di un utente
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return Restituisce un ArrayList contenente gli amici relativi all'utente. Null se qualcosa non è andato a buon fine.
	 */
	public ArrayList<String> listfriend(String nickUtente) {
		
		JSONParser parser = new JSONParser();
		String text="LISTFRIEND " + nickUtente;
		//invio l'operazione da effettuare al server
		op_write(text);
		
			try {
				//leggo la lista inviata dal server
				String listjson=reader.readLine();
				JSONArray jsonarray=null;
				ArrayList<String> list=new ArrayList<>();
				try {
					//converto una stringa in un JSONArray
					jsonarray=(JSONArray) parser.parse(listjson);
				
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				for(int i=0; i<jsonarray.size(); i++) {
					//inserisco gli username in un ArrayList di stringhe
					list.add(jsonarray.get(i).toString());
				}
				
				return list;
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return null;
			
	}
	
	/** Richiede al server la rimozione di un amico 
	 * 
	 * @param nickAmico: Username dell'amico da rimuovere
	 * @param nickUtente: Username dell'utente che richiede la rimozione di un amico
	 * @return Restituisce un intero a seconda del risultato dell'operazione
	 */
	
	public int removefriend(String nickAmico,String nickUtente) {
		int result;
		try {
			
			String text="REMOVEFRIEND " + nickUtente + " " + nickAmico;
			//invio l'operazione da effettuare al server
			op_write(text);
			
			//leggo il risultato dell'operazione dal server
			result=Integer.parseInt(reader.readLine());
			
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	
	}
	
	/** Richiede al server di sfidare un amico
	 * 
	 * @param nickAmico: Username dell'amico da sfidare
	 * @param nickUtente: Username dell'utente che richiede la sfida
	 * @return Restituisce un intero a seconda del risultato dell'operazione
	 */
	public int sfida_amico(String nickAmico, String nickUtente) {
		String text=("CHALLENGE " + nickUtente + " " + nickAmico);
		//invio l'operazione da effettuare al server
		op_write(text);
		try {
			//leggo il risultato dell'operazione dal server
			int result=Integer.parseInt(reader.readLine());
			return result;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
		
	}
	
	/** Richiesta del proprio punteggio
	 * 
	 * @param nickUtente: Username dell'utente 
	 * @return Restiruisce il punteggio relativo all'utente
	 */
	public int show_score(String nickUtente) {
		int result;
		try {
			String text="SCORE " + nickUtente;
			//invio l'operazione da effettuare al server
			op_write(text);
			
			//leggo il punteggio inviato dal server
			result=Integer.parseInt(reader.readLine());
			
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
		
	}
	
	/** Richiede la classifica aggiornata
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return Restituisce un ArrayList contenente la classifica aggiornata in ordine decrescente. Null se qualcosa non è andato a buon fine.
	 */
	public ArrayList<String> show_rank(String nickUtente) {
		JSONParser parser = new JSONParser();
		
		String text="RANK " + nickUtente;
		//invio l'operazione da effettuare al server
		op_write(text);
		
			try {
				//leggo la stringa inviata dal server
				String listjson=reader.readLine();
				
				JSONArray jsonarray=null;
				ArrayList<String> list=new ArrayList<>();
				
				try {
					//converto la stringa letta in un JSONArray
					jsonarray=(JSONArray) parser.parse(listjson);
				
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for(int i=0; i<jsonarray.size(); i++) {
					JSONObject user=(JSONObject) jsonarray.get(i);
					//inserisco gli username con il relativo punteggio in un arraylist di stringhe
					list.add(user.get("username") + " \t Points:" + user.get("points"));
				}
				return list;
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return null;
			
	}
	
	
	/** Richiede il logout da WQ
	 * 
	 * @param nickUtente: Username dell'utente
	 * @return Restituisce un intero a seconda del risultato dell'operazione

	 */
	public int logout(String nickUtente) {
		int result;
		try {
			String text="LOGOUT " + nickUtente;
			//invio l'operazione da effettuare al server
			op_write(text);
			
			//leggo il risultato dell'operazione 
			result=Integer.parseInt(reader.readLine());
			
			//termina UDPcontroller e chiude la socket UDP
			if (thUdp.isAlive()) {
				thUdp.interrupt();
				UDPsock.close();
				
			}
			
			if(result==16) {
				//se l'operazione è andata a buon fine invoco l'interfaccia iniziale
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							window1 = new GuiRegisterLogin();
							
							window1.frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	

	/** Riceve i messaggi relativi alla sfida 
	 * 
	 */
	public void listenToWords() {
		
		rif_cl=this;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window = new GuiChallenge(rif_cl,user,writer);
					window.frame.setVisible(true);
					JOptionPane.showMessageDialog(null, "Preparati! Sta iniziando la sfida");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//invoco l'interfaccia di gioco
		//window = new GuiChallenge(this,user,writer);
		//window.frame.setVisible(true);
		//JOptionPane.showMessageDialog(null, "Preparati! Sta iniziando la sfida");
		
		//invio l'operazione da effettuare al server (sono pronto per iniziare la sfida)
		op_write("STARTCH");
		boolean exit=false;
		while(true) {
			try {
			
				System.out.println("in attesa che arrivino messaggi dal server");
				//ricevo una stringa dal server
				String command=reader.readLine();
				String tok[]=command.split("\\s+");
				
				System.out.println("Parola arrivata dal server: "+tok[0]);
				
				if (tok[0].equals("STARTCHALLENGE")) {
					
					//setto inizio sfida nel udpController
					threadUdp.busyinChallenge=true;
					
				}
				else if(tok[0].equals("CHALLENGEEND")) {
					window.lblNewLabel_1.setText("Partita Terminata!");
					
					//leggo i risultati relativi alla sfida
					String result=reader.readLine();
					String tok2[]=result.split("  ");

					//visualizzo i risultati nell'interfaccia
					window.lblNewLabel_5.setText(tok2[0]);
					window.lblNewLabel_6.setText(tok2[1]);
					window.lblNewLabel_7.setText(tok2[2]);
					window.lblNewLabel_8.setText(tok2[3]);
					
					
					break;
					
					
				}
				else if(tok[0].equals("EXIT")) {
					exit=true;
					break;
				}
				
				//la stringa la letta è la parola da tradurre
				else {
					
					//visualizzo la parola da tradurre nell'interfaccia
					window.lblNewLabel_1.setText(tok[0]);
					
					//attivo un Timer di 10 secondi
					//Allo scadere, se l'utente non ha risposto, invio al server TIMEOUT
					timer = new Timer();
				    timer.scheduleAtFixedRate(new TimerTask() {
				        	

				            @Override
				            public void run() {
				                if(window.count<=6) {
				                	//controllo se la textField dell'interfaccia è vuota
					                if(window.textField.getText().isEmpty()) {
					                	//Invio TIMEOUT al server
					                	op_write("TIMEOUT");
					                	
					                	timer.cancel();
					                	timer=null;
					                	window.i++;
					                	//aggiorno la progressBar dell'interfaccia
					                	window.progressBar.setValue(window.i);
					                	window.count++;
					                }
				               }
				                
				            }
				            

				        }, 10000, 10000);
				     
				     
					
				}
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		//setto fine sfida nel udpController
		threadUdp.busyinChallenge=false;
		
		if(exit) {
			window.frame.setVisible(false);
		}
		else{
		//Attivo un timer di 5 secondi che allo scadere rende non più visibile l'interfaccia di gioco
			javax.swing.Timer timer=new javax.swing.Timer(5000, new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					window.frame.setVisible(false);
					window.frame.dispose();
					
				}
			});
			timer.setRepeats(false);
			timer.start();
			window.frame.setVisible(true);
		}
		
	
		
		
	}
	
	
	
	
	
	
	/** Invia al server l'operazione da effettuare 
	 * 
	 * @param text: Stringa relativa all'operazione da effettuare 
	 * @return Restituisce 0 se l'operazione è andata a buon fine, -1 altrimenti.
	 */
	public int op_write(String text) {
		
		System.out.println(text);
		try {
			writer.write(text);
			writer.newLine();
			writer.flush();
	
			return 0;
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return -1;
		
		
	}
	
	
	/** Converte il risultato dell'operazione in una stringa di testo
	 * 
	 * @param code: riusltato dell'operazione
	 * @return
	 */
	public static String codeResult(int code) {
		
		switch(code) {
			case 2: 
				return "Operazione non valida";
			case 3:
				return "Registrazione andata a buon fine";
			case 4:
				return "Nickname già presente";
			case 5:
				return "Login andato a buon fine";
			case 6:
				return "Password errata";
			case 7:
				return "Utente non presente nel sistema";
			case 8:
				return "Utente già online";
			case 9:
				return "Lougout andato a buon fine";
			case 10: 
				return "Nickamico già nella lista dei tuoi amici";
			case 11:
				return "Nickamico è stato aggiunto correttamente";
			case 12: 
				return "Non siete amici";
			case 13:
				return "Rimozione amicizia andata a buon fine";
			case 14:
				return "La sfida è stata accettata";
			case 15:
				return "Utente non online";
			case 16:
				return "Scattato il timer della richiesta di sfida!";
			case 17:
				return "La sfida è stata rifiutata!";
			default:
				return "Errore";
		}
	}

}
