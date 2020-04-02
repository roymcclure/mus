package roymcclure.juegos.mus.cliente.UI;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.ConnectException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import roymcclure.juegos.mus.cliente.logic.Game;
import roymcclure.juegos.mus.cliente.network.ClientConnection;

public class ClientWindow extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6374380056738883771L;

	public static final int WIDTH = 1024, HEIGHT = 1024;
	
	JFrame frame;
	JDialog connectionDialog;
	ClientConnection connection;
	
	public ClientWindow(String title) {
		
		connection = new ClientConnection(this);
		Game game = new Game();
		setupFrame("MUS -- client", game);
		createConnectionDialog();
		showConnectionDialog();
		game.start();
		connection.start();
	}
	
	private void setupFrame(String title, Game game) {
		Dimension d = new Dimension(WIDTH,HEIGHT);
		frame = new JFrame(title);
		frame.setMinimumSize(d);		
		frame.setPreferredSize(d);
		frame.setMaximumSize(d);
		frame.setLayout(new BorderLayout());
		frame.getContentPane().setPreferredSize(d);
		frame.getContentPane().setBackground(Color.decode("#1E7E1E"));
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocation(100,100);
		frame.setVisible(true);		
		frame.add(game);

	}
	
	private void createConnectionDialog() {
		connectionDialog = new JDialog(frame, Dialog.ModalityType.DOCUMENT_MODAL);
		connectionDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		Dimension d = new Dimension((int)(WIDTH * 0.50), (int)(HEIGHT * 0.20));
		connectionDialog.setSize(d);
		//connectionDialog.setDefaultCloseOperation(0);
		
		connectionDialog.setResizable(false);
		connectionDialog.setLocationRelativeTo(frame);
		connectionDialog.setLayout(new GridBagLayout());
		connectionDialog.setTitle("Connection");
		JTextField txtUrl = new JTextField("127.0.0.1");
		JTextField txtPort = new JTextField("5678");
		JLabel lblUrl = new JLabel("URL:");
		JLabel lblPort = new JLabel("Port:");		
		JButton b = new JButton("Connect");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// we retrieve url and port
				// we try to connect to the server.
				// we ask the thread 
				// the connection thread handles it
				//
				int error = connection.connect(txtUrl.getText(), Integer.parseInt(txtPort.getText()));
				if (error == 0) {
					hideConnectionDialog();
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
		connectionDialog.getContentPane().add(b, cons(0,2,2,1,1.0f,1.0f,2));
		
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
