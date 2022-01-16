package Client;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JList;
import javax.swing.JOptionPane;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

import javax.swing.JTextField;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Font;

/** Interfaccia delle operazioni da poter effettuare in WQ
 * 
 * @author selenegerali
 *
 */
public class GuiOperation {

	public JFrame frame;
	private JTextField textNickamico;
	private String user;
	private ArrayList<String> list;
	public JList<String> list_amici ;
	private Client cl;
	
	
	/**
	 * Create the application.
	 */
	
	public GuiOperation(Client cl) {
		
		
		this.cl=cl;
		this.user=cl.user;
		list=new ArrayList<>();
		initialize();
	
	}



	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initialize() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 502, 477);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 206, 209));
		panel.setForeground(new Color(0, 206, 209));
		panel.setBounds(0, 0, 248, 455);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		
		JButton btnLogout = new JButton("LOGOUT");
		btnLogout.setFont(new Font("Herculanum", Font.PLAIN, 13));
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//chiamo la funzione per il logout dell'utente
				int result=cl.logout(user);
				String code=Client.codeResult(result);
				//Visualizzo il risultato dell'operazione
				JOptionPane.showMessageDialog(null, code);
				frame.setVisible(false);
				
			}
		});
		btnLogout.setForeground(new Color(255, 0, 0));
		btnLogout.setBackground(new Color(255, 255, 255));
		btnLogout.setBounds(59, 418, 127, 29);
		panel.add(btnLogout);
		
		
		
		textNickamico = new JTextField();
		textNickamico.setBounds(99, 34, 143, 40);
		panel.add(textNickamico);
		textNickamico.setColumns(10);
		
		JLabel lblNickamico = new JLabel("NickAmico:");
		lblNickamico.setFont(new Font("Chalkduster", Font.PLAIN, 13));
		lblNickamico.setBounds(6, 46, 91, 16);
		panel.add(lblNickamico);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 144, 232, 21);
		panel.add(separator);
		
		JList list_cp = new JList();
		list_cp.setBounds(10, 212, 232, 194);
		panel.add(list_cp);
		
		JLabel lblWorldQuizzle = new JLabel("WORLD QUIZZLE:  "+user);
		lblWorldQuizzle.setFont(new Font("Chalkduster", Font.PLAIN, 13));
		lblWorldQuizzle.setHorizontalAlignment(SwingConstants.CENTER);
		lblWorldQuizzle.setBounds(6, 6, 236, 16);
		panel.add(lblWorldQuizzle);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(175, 238, 238));
		panel_1.setBounds(248, 0, 254, 455);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		
		DefaultListModel dlm1=new DefaultListModel();
		for(String us: list) {
			dlm1.addElement(us);
		}
		
		list_amici = new JList<String>(dlm1);
		list_amici.setBounds(6, 37, 242, 369);
		panel_1.add(list_amici);
		
		
		JButton btnMostraClassifica = new JButton("Show Rank");
		btnMostraClassifica.setFont(new Font("Herculanum", Font.BOLD, 13));
		btnMostraClassifica.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//chiamo la funzione per richiedere la classifica
				ArrayList<String> result=cl.show_rank(user);
				
				//mostro la classifica nella JList
				DefaultListModel cl1=new DefaultListModel();
				for(String us: result) {
					cl1.addElement(us);
				}
				list_cp.setModel(cl1);
			}
		});
		btnMostraClassifica.setBounds(0, 166, 112, 29);
		panel.add(btnMostraClassifica);
		
		JButton btnPunteggio = new JButton("Score");
		btnPunteggio.setFont(new Font("Herculanum", Font.PLAIN, 13));
		btnPunteggio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//chiamo la funzione per ottenere il punteggio
				int result=cl.show_score(user);
				
				//mostro le info del punteggio nella JList
				DefaultListModel cl2=new DefaultListModel();
				cl2.addElement("Il tuo punteggio è di: "+result+" punti.");
				list_cp.setModel(cl2);
			}
		});
		btnPunteggio.setBounds(136, 166, 112, 29);
		panel.add(btnPunteggio);
		
		
		
		JButton btnRemoveFriend = new JButton("CHALLENGE\n");
		btnRemoveFriend.setFont(new Font("Herculanum", Font.PLAIN, 13));
		btnRemoveFriend.setBounds(62, 418, 127, 29);
		panel_1.add(btnRemoveFriend);
		
		JButton btnViewListFriend = new JButton("LIST FRIENDS");
		btnViewListFriend.setFont(new Font("Herculanum", Font.PLAIN, 13));
		
		btnViewListFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//chiamo la funzione listfriend per andare ad aggiornare la lista
				list=cl.listfriend(user);
				
				//aggiorno la Jlist
				DefaultListModel dlm=new DefaultListModel();
				for(String us: list) {
					dlm.addElement(us);
				}
				list_amici.setModel(dlm);
				
				
			}
		});
		btnViewListFriend.setBounds(61, 6, 146, 29);
		panel_1.add(btnViewListFriend);
		btnRemoveFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//i mi restituisce il più piccolo indice di cella selezionato
				int i=list_amici.getSelectedIndex();
				//se i=-1 non è stata selezionata nessuna cella
				if(i==-1) JOptionPane.showMessageDialog(null, "Seleziona un amico dalla lista amici per inviargli una richiesta di sfisda!");
				else {
					//chiamo la funzione che richiede la sfida
					String amico=list_amici.getSelectedValue();
					int result=cl.sfida_amico(amico,user);
					//visualizzo il risultato dell'operazione 
					String code=Client.codeResult(result);
					if (result!=14) JOptionPane.showMessageDialog(null, code);
					
				}
				
			}
		});
		
		
		JButton btnAddFriend = new JButton("Add Friend");
		btnAddFriend.setFont(new Font("Herculanum", Font.BOLD, 13));
		btnAddFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//chiamo la funzione per aggiungere un amico
				int result=cl.addfriend(textNickamico.getText(),user);
				textNickamico.setText(null);
				//visualizzo il risultato dell'operazione 
				String code=Client.codeResult(result);
				JOptionPane.showMessageDialog(null, code);
					
					
				};
		});
		btnAddFriend.setBounds(0, 103, 127, 29);
		panel.add(btnAddFriend);
		
		
		
		JButton btnRemoveFriend_1 = new JButton("Remove Friend");
		btnRemoveFriend_1.setFont(new Font("Herculanum", Font.BOLD, 13));
		btnRemoveFriend_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//chiamo la funzione per rimuovere un amico
				int result=cl.removefriend(textNickamico.getText(),user);
				textNickamico.setText(null);
				//visualizzo il risultato dell'operazione 
				String code=Client.codeResult(result);
				JOptionPane.showMessageDialog(null, code);
				
				//invoco l'operazione di listfriend per andare ad aggiornare la lista
				list=cl.listfriend(user);
				
				//aggiorno la Jlist
				DefaultListModel dlm=new DefaultListModel();
				for(String us: list) {
					dlm.addElement(us);
				}
				list_amici.setModel(dlm);
				
				
			}
		});
		btnRemoveFriend_1.setBounds(121, 103, 127, 29);
		panel.add(btnRemoveFriend_1);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void riempilista(JList list_amici) {
		//invoco l'operazione di listfriend per andare ad aggiornare la lista
		list=cl.listfriend(user);
		
		//aggiorno la Jlist
		DefaultListModel dlm=new DefaultListModel();
		for(String us: list) {
			dlm.addElement(us);
		}
		list_amici.setModel(dlm);
	}
}
