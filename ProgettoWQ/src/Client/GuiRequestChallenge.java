package Client;


import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

/** Interfaccia dedicata alla richiesta di sfida
 * 
 * @author selenegerali
 *
 */
public class GuiRequestChallenge {

	public JFrame frame;
	private String nickAmico;
	public InetAddress address;
	public int UDPport;
	public DatagramSocket UDPsock;
	private String user;
	

	
	
	/**
	 * Create the application.
	 */
	public GuiRequestChallenge(String user,String nickAmico,InetAddress address,int UDPport,DatagramSocket UDPsock) {
		this.nickAmico=nickAmico;
		this.address=address;
		this.UDPport=UDPport;
		this.UDPsock=UDPsock;
		this.user=user;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(0, 206, 209));
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblRichiestaDiSfida = new JLabel("WQ: SFIDA!");
		lblRichiestaDiSfida.setFont(new Font("Future Worlds", Font.PLAIN, 28));
		lblRichiestaDiSfida.setBounds(156, 25, 149, 29);
		frame.getContentPane().add(lblRichiestaDiSfida);
		
		JLabel lblNickamicoTiVuole = new JLabel(user+",");
		lblNickamicoTiVuole.setHorizontalAlignment(SwingConstants.CENTER);
		lblNickamicoTiVuole.setFont(new Font("Chalkduster", Font.PLAIN, 20));
		lblNickamicoTiVuole.setBounds(6, 81, 438, 39);
		frame.getContentPane().add(lblNickamicoTiVuole);
		
		JButton btnAccetta = new JButton("ACCEPT");
		btnAccetta.setFont(new Font("Herculanum", Font.PLAIN, 13));
		btnAccetta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text="ACCEPTED ";
				byte[] buff=text.getBytes();
				//creo il datagramma per l'invio
				DatagramPacket pk=new DatagramPacket(buff, buff.length, address, UDPport);
				try {
					//invio il datagramma
					UDPsock.send(pk);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				frame.setVisible(false);
				
				
				
				
			}
		});
		btnAccetta.setBounds(88, 202, 117, 29);
		frame.getContentPane().add(btnAccetta);
		
		JButton btnRifiuta = new JButton("REFUSE");
		btnRifiuta.setFont(new Font("Herculanum", Font.PLAIN, 13));
		btnRifiuta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text="REFUSED";
				byte[] buff=text.getBytes();
				//creo il datagramma per l'invio
				DatagramPacket pk=new DatagramPacket(buff, buff.length, address, UDPport);
				try {
					//invio il datagramma
					UDPsock.send(pk);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				frame.setVisible(false);
			}
		});
		btnRifiuta.setBounds(251, 203, 117, 29);
		frame.getContentPane().add(btnRifiuta);
		
		JLabel lblTiVuoleSfidare = new JLabel(nickAmico+" ti vuole sfidare!");
		lblTiVuoleSfidare.setHorizontalAlignment(SwingConstants.CENTER);
		lblTiVuoleSfidare.setFont(new Font("Chalkduster", Font.PLAIN, 20));
		lblTiVuoleSfidare.setBounds(16, 132, 428, 39);
		frame.getContentPane().add(lblTiVuoleSfidare);
		
		//creo un timer che allo scadere rende non più visibile l'interfaccia
		javax.swing.Timer timer=new javax.swing.Timer(10000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
				
			}
		});
		timer.setRepeats(false);
		timer.start();
		frame.setVisible(true);
		
	}

	
	
	
}
