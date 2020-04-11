package roymcclure.juegos.mus.cliente.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import roymcclure.juegos.mus.cliente.logic.ClientGameState;
import roymcclure.juegos.mus.cliente.logic.Controller;
import roymcclure.juegos.mus.cliente.logic.Game;
import roymcclure.juegos.mus.cliente.logic.jobs.*;
import roymcclure.juegos.mus.cliente.network.ClientConnection;
import roymcclure.juegos.mus.common.logic.Language;

public class ClientWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6374380056738883771L;

	public static final int WIDTH = 1024, HEIGHT = 1024;
	
	JDialog connectionDialog;
	ClientConnection connection;
	public static ClientGameState clientGameState; 
	private Controller controller;
	
	public ClientWindow(String title) {
		// init client game state
		clientGameState = new ClientGameState();
		// job queues
		ControllerJobsQueue controllerJobs = new ControllerJobsQueue();
		ConnectionJobsQueue connectionJobs = new ConnectionJobsQueue();
		// game AKA UI thread
		Game game = new Game(clientGameState, controllerJobs);
		// connection & controller threads
		controller = new Controller(game.getHandler(), controllerJobs, connectionJobs);
		connection = new ClientConnection(connectionJobs, controllerJobs);
		
		setupFrame("MUS -- client", game);
		createConnectionDialog();
		showConnectionDialog();
		game.start();
		
		Thread t = new Thread(controller);
		t.start();
	}
	
	private void setupFrame(String title, Game game) {
		Dimension d = new Dimension(WIDTH,HEIGHT);
		this.setTitle(title);
		this.setMinimumSize(d);		
		this.setPreferredSize(d);
		this.setMaximumSize(d);
		this.setSize(WIDTH, HEIGHT);
		this.setLayout(new BorderLayout());
		this.getContentPane().setPreferredSize(d);
		this.getContentPane().setBackground(Color.decode("#1E7E1E"));
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((desktop.width - WIDTH) / 2,(desktop.height - HEIGHT) / 2);
		this.setVisible(true);		
		this.add(game);
		this.pack();

	}
	
	private void createConnectionDialog() {
		connectionDialog = new JDialog(this, Dialog.ModalityType.DOCUMENT_MODAL);
		connectionDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		Dimension d = new Dimension((int)(WIDTH * 0.50), (int)(HEIGHT * 0.20));
		connectionDialog.setSize(d);
		//connectionDialog.setDefaultCloseOperation(0);
		
		connectionDialog.setResizable(false);
		connectionDialog.setLocationRelativeTo(this);
		connectionDialog.setLayout(new GridBagLayout());
		connectionDialog.setTitle("Connection");
		JTextField txtUrl = new JTextField("127.0.0.1");
		JTextField txtPort = new JTextField("5678");
		JTextField txtName = new JTextField("Roy");
		JLabel lblUrl = new JLabel("URL:");
		JLabel lblPort = new JLabel("Port:");
		JLabel lblName = new JLabel("Name:");
		JButton b = new JButton("Connect");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//tell the connection object to try to connect
				int error = connection.connect(txtUrl.getText(), Integer.parseInt(txtPort.getText()));
				
				if (error == 0) {
					hideConnectionDialog();
					clientGameState.setGameState(Language.ClientGameState.AWAITING_GAME_STATE);
					connection.start();
					clientGameState.setPlayerName(txtName.getText());
					controller.postInitial();
				} else if (error==1 || error == 2) {
					b.setText("Unable to connect. Try again...");
				} else if (error == 3) {
					b.setText("Error transfering data. Try again...");
				}
			}
			
		});
		connectionDialog.getContentPane().add(lblUrl, cons(0,0,1,1,0,0, GridBagConstraints.HORIZONTAL));
		connectionDialog.getContentPane().add(txtUrl, cons(1,0,1,1,1.0f,1.0f,2));
		connectionDialog.getContentPane().add(lblPort, cons(0,1,1,1,0,0, GridBagConstraints.HORIZONTAL));
		connectionDialog.getContentPane().add(txtPort, cons(1,1,1,1,1.0f,1.0f,2));
		connectionDialog.getContentPane().add(lblName, cons(0,2,1,1,0,0, GridBagConstraints.HORIZONTAL));
		connectionDialog.getContentPane().add(txtName, cons(1,2,1,1,1.0f,1.0f,2));		
		connectionDialog.getContentPane().add(b, cons(0,3,2,1,1.0f,1.0f,2));
		
	}
	
	public void showConnectionDialog() {
		connectionDialog.setVisible(true);
	}
	
	public void hideConnectionDialog() {
		connectionDialog.setVisible(false);
	}
	
	public GridBagConstraints cons(int x, int y, int width, int height, float weightx, float weighty, int fill) {
		GridBagConstraints gb = new GridBagConstraints();
		gb.gridx = x;
		gb.gridy = y;
		gb.gridwidth = width;
		gb.gridheight = height;
		gb.weightx = weightx;
		gb.weighty = weighty;
		gb.fill= fill;
		return gb;
	}
	
}
