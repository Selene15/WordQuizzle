package Client;
import java.io.BufferedWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import java.awt.Font;

/** Interfaccia dedicata alla sfida
 * 
 * @author selenegerali
 *
 */
public class GuiChallenge {

	public JFrame frame;
	public JTextField textField;
	public JLabel lblNewLabel_1;
	
	private String user;
	private BufferedWriter writer;
	private JLabel lblNewLabel_2;
	private JLabel lblNewLabel_3;
	private JLabel lblNewLabel_4;
	public JLabel lblNewLabel_5;
	public JLabel lblNewLabel_6;
	public JLabel lblNewLabel_7;
	public JLabel lblNewLabel_8;
	private JLabel lblUtente;
	private JLabel lblNewLabel_9;
	public JProgressBar progressBar ;
	public int i=0;
	private JButton btnNewButton_1;
	public int count=1;
	private Client client;


	/**
	 * Create the application.
	 */
	public GuiChallenge(Client client,String user,BufferedWriter writer) {
		
		this.user=user;
		this.writer=writer;
		this.client=client;
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
		
		textField = new JTextField();
		textField.setBounds(266, 150, 150, 26);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("ITALIAN WORD");
		lblNewLabel.setFont(new Font("Chalkduster", Font.PLAIN, 13));
		lblNewLabel.setBounds(62, 28, 113, 16);
		frame.getContentPane().add(lblNewLabel);
		
		JSeparator separator = new JSeparator();
		separator.setBackground(new Color(0, 0, 0));
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setBounds(216, 18, 5, 254);
		frame.getContentPane().add(separator);
		
		lblNewLabel_1 = new JLabel("Parola da tradurre");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(28, 150, 166, 26);
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel lblEnglishWord = new JLabel("ENGLISH WORD");
		lblEnglishWord.setFont(new Font("Chalkduster", Font.PLAIN, 13));
		lblEnglishWord.setBounds(291, 28, 113, 16);
		frame.getContentPane().add(lblEnglishWord);
		
		JButton btnNewButton = new JButton("SEND WORD");
		btnNewButton.setFont(new Font("Herculanum", Font.PLAIN, 13));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//controllo che il textField non sia vuoto
				if(!textField.getText().isEmpty()) {
					//imposto la parola tradotta da inviare
					String wordTosend=textField.getText() + " " + user;
					System.out.println(("Ho inviato la parola tradotta " + wordTosend));
					count++;
					
					//invio la parola tradotta
					op_write(wordTosend);
					//resetto il timer
					client.timer.cancel();
					client.timer=null;
					//aggiorno la progressBar
					i++;
					progressBar.setValue(i);
					
				}
				textField.setText(null);
			}
		});
		btnNewButton.setBounds(285, 199, 106, 29);
		frame.getContentPane().add(btnNewButton);
		
		lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setIcon(new ImageIcon(GuiChallenge.class.getResource("/images/kisspng-london-flag-of-the-united-kingdom-zazzle-flag-of-e-england-5ab872bd64ef36.8283229615220374374134.png")));
		lblNewLabel_2.setBounds(256, 46, 204, 103);
		frame.getContentPane().add(lblNewLabel_2);
		
		lblNewLabel_3 = new JLabel("");
		lblNewLabel_3.setBounds(216, 46, 188, 103);
		frame.getContentPane().add(lblNewLabel_3);
		
		lblNewLabel_4 = new JLabel("");
		lblNewLabel_4.setIcon(new ImageIcon(GuiChallenge.class.getResource("/images/o.466550.jpg")));
		lblNewLabel_4.setBounds(28, 46, 188, 96);
		frame.getContentPane().add(lblNewLabel_4);
		
		lblNewLabel_5 = new JLabel("");
		lblNewLabel_5.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel_5.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_5.setBounds(26, 175, 176, 16);
		frame.getContentPane().add(lblNewLabel_5);
		
		lblNewLabel_6 = new JLabel("");
		lblNewLabel_6.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel_6.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_6.setBounds(28, 204, 176, 16);
		frame.getContentPane().add(lblNewLabel_6);
		
		lblNewLabel_7 = new JLabel("");
		lblNewLabel_7.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel_7.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_7.setBounds(28, 232, 176, 16);
		frame.getContentPane().add(lblNewLabel_7);
		
		lblNewLabel_8 = new JLabel("");
		lblNewLabel_8.setVerticalAlignment(SwingConstants.TOP);
		lblNewLabel_8.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_8.setBounds(28, 256, 176, 16);
		frame.getContentPane().add(lblNewLabel_8);
		
		lblUtente = new JLabel("Utente:");
		lblUtente.setFont(new Font("Chalkduster", Font.PLAIN, 13));
		lblUtente.setHorizontalAlignment(SwingConstants.LEFT);
		lblUtente.setBounds(178, 6, 51, 16);
		frame.getContentPane().add(lblUtente);
		
		lblNewLabel_9 = new JLabel(user);
		lblNewLabel_9.setFont(new Font("Chalkduster", Font.PLAIN, 13));
		lblNewLabel_9.setBounds(233, 6, 157, 16);
		frame.getContentPane().add(lblNewLabel_9);
		
		progressBar = new JProgressBar(0,6);
		progressBar.setFont(new Font("Future Worlds", Font.PLAIN, 13));
		progressBar.setBounds(266, 240, 150, 20);
		frame.getContentPane().add(progressBar);
		
		progressBar.setValue(0); 
		  
        progressBar.setStringPainted(true); 
        
        btnNewButton_1 = new JButton("X");
        btnNewButton_1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		System.out.println("Abbandono la partita");
        		if(JOptionPane.showConfirmDialog(frame, "Sei sicuro di voler abbandonare la sfida?","Login Systems", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_NO_OPTION) {
        			frame.setVisible(false);
        			//invio l'operazione per usicre dalla sfida al server
        			op_write("EXITCHALLENGE ");
        			//resetto il timer
        			client.timer.cancel();
        			

				}
        		
        		
        	}
        });
        btnNewButton_1.setForeground(new Color(255, 0, 0));
        btnNewButton_1.setBounds(402, 2, 42, 26);
        frame.getContentPane().add(btnNewButton_1);
  
		
	}
	
	/** Invia al server l'operazione da effettuare 
	 * 
	 * @param text: Stringa da inviare al server
	 * @return: Restiruisce 0 se l'operazione è andata a buon fine, -1 altrimenti
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
}
