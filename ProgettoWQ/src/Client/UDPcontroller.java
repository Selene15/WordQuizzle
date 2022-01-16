package Client;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/** Thread che si dedica ai messaggi scambiati tra client e server tramite UDP
 * 
 * @author selenegerali
 *
 */


public class UDPcontroller implements Runnable{
	public DatagramSocket UDPsock;
	private Client client;
	public InetAddress address;
	public int UDPport;
	private static GuiRequestChallenge window1;
	public boolean busyinChallenge=false;
	
	

	//costruttore
	public UDPcontroller(Client client, DatagramSocket UDPsock) {
		this.client=client;
		this.UDPsock=UDPsock;
	}
	
	
	public void run() {
		byte[] buff=new byte[1024];
		//creo un datagram packet per la ricezione
		DatagramPacket pkreceived=new DatagramPacket(buff, buff.length);
		
		//mi metto in attesa indefinita di datagrammi
		while(true) {
			System.out.println("waiting for UDP datagrams");
			try {
				//ricevo il datagramma
				UDPsock.receive(pkreceived);
			} 
			catch(SocketException e1) {
				break;
			}
			catch (IOException e) {
				
				e.printStackTrace();
				
				
			}
			
			//trasformo il datagramma ricevuto in stringa
			String stringreceived=new String(pkreceived.getData(),0,pkreceived.getLength());
			System.out.println("Datagram received: " + stringreceived);
			String[] command=stringreceived.split("\\s+");
			
		
			if(command[0].equals("CHALLENGE")) {
				//se è gia occupato in altre sfide, rifiuto automaticamente
				if(busyinChallenge==true) {
					String text="REFUSED";
					byte[] buffer=text.getBytes();
					//creo un datagramma per l'invio
					DatagramPacket pk=new DatagramPacket(buffer, buffer.length, address, UDPport);
					try {
						//invio il datagramma
						UDPsock.send(pk);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				//se non è occupato posso madargli la richiesta di sfida
				else {
					//nome dell'amico da sfidare
					String nickAmico=command[1];
					//indirizzo
					address=pkreceived.getAddress();
					//porta UDP
					UDPport=pkreceived.getPort();
					
					//invoco l'interfaccia per la richiesta di sfida
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							window1 = new GuiRequestChallenge(client.user,nickAmico,address,UDPport,UDPsock);
							try {
								
								window1.frame.setVisible(true);
								window1.frame.setAlwaysOnTop(true);
								System.out.println("window "+window1);
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}	
			}
			
			//se la sfida è stata accettata chiamo la funzione che da inizio alla sfida
			if(command[0].equals("ACCEPTED")) {
				
				
				client.listenToWords();	
				
							
				
			}
			
			
			
		}
	}
	
	
}

