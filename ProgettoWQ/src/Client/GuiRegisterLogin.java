package Client;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Font;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.ImageIcon;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.awt.event.ActionEvent;
import javax.swing.UIManager;

/** Interfaccia iniziale (Login - Register)
 * 
 * @author selenegerali
 *
 */
public class GuiRegisterLogin {
	

	public JFrame frame;
	private JTextField textUsername;
	private JPasswordField textPassword;
	

	/**
	 * Create the application.
	 */
	public GuiRegisterLogin() {
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		Client client=new Client();
		frame = new JFrame();
		frame.getContentPane().setForeground(UIManager.getColor("CheckBox.foreground"));
		frame.getContentPane().setBackground(new Color(175, 238, 238));
		frame.setBounds(100, 100, 698, 464);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		textUsername = new JTextField();
		textUsername.setBounds(423, 105, 257, 49);
		frame.getContentPane().add(textUsername);
		textUsername.setColumns(10);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setFont(new Font("Chalkduster", Font.PLAIN, 14));
		lblUsername.setBounds(323, 121, 88, 16);
		frame.getContentPane().add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Chalkduster", Font.PLAIN, 14));
		lblPassword.setBounds(323, 221, 110, 16);
		frame.getContentPane().add(lblPassword);
		
		textPassword = new JPasswordField();
		textPassword.setBounds(423, 205, 257, 49);
		frame.getContentPane().add(textPassword);
		
		
		JButton btnLogin = new JButton("LOGIN");
		btnLogin.setForeground(Color.BLACK);
		btnLogin.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				//controllo se i campi sono vuoti
				if(textUsername.getText().isEmpty() || textPassword.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Controlla i campi vuoti");
				}
				else {
					
					try {
						//chiamo la funzione per effettuare il login
						client.login(textUsername.getText(), textPassword.getText());
					
						textUsername.setText(null);
						textPassword.setText(null);
							
							
					} catch (Exception e1) {
							
						e1.printStackTrace();
					}
				}
				
			}
		});
		btnLogin.setBackground(Color.WHITE);
		btnLogin.setFont(new Font("Chalkduster", Font.PLAIN, 14));
		btnLogin.setBounds(323, 325, 163, 39);
		frame.getContentPane().add(btnLogin);
		
		JButton btnRegister = new JButton("REGISTER");
		btnRegister.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				//controllo se i campi sono vuoti
				if(textUsername.getText().isEmpty() || textPassword.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Controlla i campi vuoti");
				}
				else {
					try {
						String code;
						int registered=0;
						
						//chiamo la funzione per registrare l'utente
						 registered=Client.serverObject.user_registration(textUsername.getText(),textPassword.getText());
						 textUsername.setText(null);
						 textPassword.setText(null);
		
						code=Client.codeResult(registered);
						//visualizzo l'esito dell'operazione
						JOptionPane.showMessageDialog(null, code);
					}
					catch(NullPointerException  | RemoteException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnRegister.setFont(new Font("Chalkduster", Font.PLAIN, 14));
		btnRegister.setBounds(517, 325, 163, 39);
		frame.getContentPane().add(btnRegister);
		
		JButton btnX = new JButton("X");
		btnX.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(frame, "Sei sicuro di voler uscire?","Login Systems", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_NO_OPTION) {
					System.exit(0);
				}
				
			}
		});
		btnX.setFont(new Font("Chalkduster", Font.PLAIN, 14));
		btnX.setBounds(636, 25, 44, 29);
		frame.getContentPane().add(btnX);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(72, 209, 204));
		panel.setBounds(0, 0, 300, 442);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblWorldQuiz = new JLabel("WORLD QUIZ");
		lblWorldQuiz.setFont(new Font("Future Worlds", lblWorldQuiz.getFont().getStyle(), lblWorldQuiz.getFont().getSize() + 18));
		lblWorldQuiz.setBounds(44, 24, 196, 58);
		panel.add(lblWorldQuiz);
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setIcon(new ImageIcon(GuiRegisterLogin.class.getResource("/images/apps.55720.13510798887439604.a595eabd-fbf0-4f2b-8197-7798e5997885.492e921a-0231-4aa4-a5d9-2a9df70dcfb6.png")));
		lblNewLabel.setBounds(0, 101, 301, 288);
		panel.add(lblNewLabel);
	}
	
	
	
	
	
}
