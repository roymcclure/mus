package roymcclure.juegos.mus.cliente.UI;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import roymcclure.juegos.mus.cliente.logic.ClientGameState;
import roymcclure.juegos.mus.cliente.logic.ClientController;
import roymcclure.juegos.mus.cliente.logic.Game;
import roymcclure.juegos.mus.common.logic.PlayerState;
import roymcclure.juegos.mus.common.logic.jobs.*;
import roymcclure.juegos.mus.cliente.network.ClientConnection;
import roymcclure.juegos.mus.cliente.network.GetNetworkAddress;

public class ClientWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6374380056738883771L;

	private static final String DEFAULT_PLAYER = "Player";
	private static final String DEFAULT_IP = "127.0.0.1";

	private static String player = DEFAULT_PLAYER;
	private static String ip = DEFAULT_IP;
	private static String port = "5678";


	JTextField txtUrl;
	JTextField txtPort;
	JTextField txtName;

	JDialog connectionDialog;
	public static ClientGameState clientGameState;
	private GameCanvas gameCanvas;
	private JFrame theWindow;
	private ClientController controller;
	public static Object semaphore;
	private Game game;

	public ClientWindow(String title, String windowPosition) {
		//		init client game state
		clientGameState = new ClientGameState();
		// job queues
		ControllerJobsQueue controllerJobs = new ControllerJobsQueue();
		ConnectionJobsQueue connectionJobs = new ConnectionJobsQueue();
		// UI
		gameCanvas = new GameCanvas(controllerJobs);
		// logic
		game = new Game(clientGameState, gameCanvas);

		semaphore = new Object();
		
		// Controller Thread
		// handleris used to tell the renderer to update the view
		// jobs are basically the pipes from where we receive and post
		// game is used because reasons
		controller = new ClientController(game.getHandler(), controllerJobs, connectionJobs);
	
		// connection & controller threads
		ClientConnection.setConnectionJobsQueue(connectionJobs);
		ClientConnection.setControllerJobsQueue(controllerJobs);

		setupFrame("MUS -- client", windowPosition);
		createConnectionDialog();
		// showConnectionDialog();
		theWindow = this;




		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(gameCanvas, "Are you sure you want to close this window?", "Close Window?",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					try {
						// tell the controller that we want to disconnect
						System.out.println("Interrupting connection threads...");
						ClientConnection.stop();
						System.out.println("Calling game.stop()...");
						game.stop();
						System.out.println("Calling controller.interrupt()...");
						controller.interrupt();
						System.out.println("Calling controller.join()...");						
						controller.join();

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
					System.exit(0);

				}
			}

			@Override
			public void windowOpened(java.awt.event.WindowEvent event) {
				showConnectionDialog();
			}
		});

	}

	private void setupFrame(String title, String windowPosition) {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension d = new Dimension(UIParameters.CANVAS_WIDTH,UIParameters.CANVAS_HEIGHT);
		this.setTitle(title);
		
		//menu
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menu_File = new JMenu("File");
		menuBar.add(menu_File);		
		JMenuItem options = new JMenuItem("Puntuacion global");
		options.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (ClientGameState.table()!=null) {
					PlayerState me = ClientGameState.me();
					if (me!=null) {
						byte my_seat = ClientGameState.table().getSeatOf(me.getID());
						if (my_seat > -1) {
							if (my_seat % 2==0) {
								JOptionPane.showMessageDialog(null, "Tu equipo: " + ClientGameState.table().getPiedras_norte_sur() + " piedras, " + ClientGameState.table().getJuegos_norte_sur() +" juegos, " + ClientGameState.table().getVacas_norte_sur() + " vacas");							
							} else {
								JOptionPane.showMessageDialog(null, "Tu equipo: piedras" + ClientGameState.table().getPiedras_oeste_este() + ", juegos:" + ClientGameState.table().getJuegos_oeste_este() +", vacas:" + ClientGameState.table().getVacas_oeste_este());							
							}
							
						} else {
							JOptionPane.showMessageDialog(null, "No estás sentado.");						
						}						
					} else {
						JOptionPane.showMessageDialog(null, "No estás sentado.");
					}
										
				} else {
					JOptionPane.showMessageDialog(null, "Ninguna puntuación.");					
				}

				
			}
		});

		menu_File.add(options);

		this.setJMenuBar(menuBar);

		this.setResizable(false);
		// Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
		int p = Integer.parseInt(windowPosition);
		this.setLocation((p % 2 )* UIParameters.CANVAS_WIDTH,(p / 2)*UIParameters.CANVAS_HEIGHT/2);
		//this.setLocation((desktop.width - WIDTH) / 2,(desktop.height - HEIGHT) / 2);
		this.add(gameCanvas);
		//this.setMinimumSize(d);
		//this.setMaximumSize(d);
		//this.setPreferredSize(d);

		//this.setMaximumSize(d);
		//this.setSize(WIDTH, HEIGHT);
		this.getContentPane().setLayout(new FlowLayout());
		this.getContentPane().setPreferredSize(d);
		this.getContentPane().setSize(d);
		this.getContentPane().setMinimumSize(d);
		this.getContentPane().setMaximumSize(d);		
		this.getContentPane().setBackground(Color.decode("#1E7E1E"));
		this.pack();
		/*JTextField chatTxtField = new JTextField();
		chatTxtField.setSize(new Dimension(500,100));
		chatTxtField.setLocation(20, 20);
		this.getContentPane().add(chatTxtField);*/

	}

	private void createConnectionDialog() {
		connectionDialog = new JDialog(this, Dialog.ModalityType.DOCUMENT_MODAL);
		connectionDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		Dimension d = new Dimension((int)(UIParameters.CANVAS_WIDTH * 0.50), (int)(UIParameters.CANVAS_HEIGHT * 0.20));
		connectionDialog.setSize(d);
		//connectionDialog.setDefaultCloseOperation(0);

		connectionDialog.setResizable(false);
		connectionDialog.setLocationRelativeTo(this);
		connectionDialog.setLayout(new GridBagLayout());
		connectionDialog.setTitle("Connection");
		txtUrl = new JTextField(ip);
		txtPort = new JTextField(port);
		txtName = new JTextField(player);
		JLabel lblUrl = new JLabel("URL:");
		JLabel lblPort = new JLabel("Port:");
		JLabel lblName = new JLabel("Name:");
		JButton b = new JButton("Connect");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isValidated(txtUrl.getText(),txtPort.getText(),txtName.getText())) {
					//tell the connection object to try to connect
					try {
						game.start();
						if (!Game.running) {
							synchronized(ClientWindow.semaphore) {
								ClientWindow.semaphore.wait();
							}
						}
						controller.start();		
					
						ClientConnection.connect(txtUrl.getText(), Integer.parseInt(txtPort.getText()));
						hideConnectionDialog();
						Thread connectionThread = new Thread(new ClientConnection());
						System.out.print("Starting connection thread...");
						connectionThread.start();
						System.out.println("started.");
						// forge player ID
						ClientGameState.setPlayerID(txtName.getText()+":"+GetNetworkAddress.GetAddress("mac").replace("-", ""));
						theWindow.setTitle(theWindow.getTitle() + " - " + txtName.getText());						
						ClientController.postInitialRequest();
					} catch (Exception exception) {
						exception.printStackTrace();
					}

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

	protected boolean isValidated(String text, String text2, String text3) {
		// TODO 
		return true;
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

	public void updateWithArgs(int i, String string) {
		if (i==0) {
			System.out.println("updating txtName text with " + string);
			txtName.setText(string);
		} else if(i==1) {
			System.out.println("updating txtUrl text with " + string);			
			txtUrl.setText(string);
		} else if(i==2) {
			System.out.println("updating txtPort text with " + string);			
			txtPort.setText(string);
		}

	}

}
