package roymcclure.juegos.mus.server.UI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import roymcclure.juegos.mus.server.logic.SrvMus;

public class ServerWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3021988155527724728L;

	private boolean running = false;
	String lastCommand = "";
	
	public void setServer(SrvMus server) {
		this.server = server;
	}

	private SrvMus server;
	private JTextArea log;
	private JButton btnRun;
	
	public ServerWindow() {
		this.setSize(320,720);
		this.setTitle("Servidor Mus");
		this.setAlwaysOnTop(true);
		this.setLocation(2000, 200);
		JTextField txtCmd = new JTextField("");
		txtCmd.setColumns(10);
		this.getContentPane().add(txtCmd, BorderLayout.NORTH);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		log = new JTextArea("");
		this.getContentPane().add(log, BorderLayout.CENTER);	
		
		DefaultCaret caret = (DefaultCaret)log.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
		
		
		JScrollPane scroll = new JScrollPane ( log );
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().add(scroll);
		
		
		btnRun = new JButton("Start");
		this.getContentPane().add(btnRun, BorderLayout.SOUTH);
		
		btnRun.addActionListener(new BtnListener(this, log, btnRun,txtCmd));		

		txtCmd.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==10) { // enter
					lastCommand = txtCmd.getText();
					server.runCommand(txtCmd.getText(), log);
					txtCmd.setText("");
				}
				if (e.getKeyCode()==38) { // up
					txtCmd.setText(lastCommand);					
				}

			}
		});		

		JFrame frame = this;
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to close this window?", "Close Window?",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		        	try {
						server.halt();
		        		server.join();
			        	System.exit(0);		        		
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

		        }
		    }
		});
		
		// sys info
		
		int cores = Runtime.getRuntime().availableProcessors();
		this.log("Number of cores:" + cores);
		String hostname = "Unknown";

		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		    this.log("Host name:" + hostname);    
		}
		catch (UnknownHostException ex)
		{
		    System.out.println("Hostname can not be resolved");
		}

		/* Total amount of free memory available to the JVM */
		this.log("Free memory (bytes): " + 
				Runtime.getRuntime().freeMemory());
		
		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		log("Maximum memory (bytes): " + 
				(maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

		/* Total memory currently in use by the JVM */
		  log("Total memory (bytes): " + 
		  Runtime.getRuntime().totalMemory());
		  /* Get a list of all filesystem roots on this system */
		  File[] roots = File.listRoots();

		  /* For each filesystem root, print some info */
		  /*
		  for (File root : roots) {
		    log("File system root: " + root.getAbsolutePath());
		    log("Total space (bytes): " + root.getTotalSpace());
		    log("Free space (bytes): " + root.getFreeSpace());
		    log("Usable space (bytes): " + root.getUsableSpace());
		  }*/		  
		
	}
	
	class BtnListener implements ActionListener {
		
		ServerWindow srvWindow;
		JTextArea log;
		JButton btnRun;
		JTextField txtPort;
		
		public BtnListener(ServerWindow srvWindow_, JTextArea txtArea_, JButton jbtn, JTextField txtPort_) {
			srvWindow = srvWindow_;
			log = txtArea_;
			btnRun = jbtn;
			txtPort = txtPort_;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			if (running) {
				try {
					log("Stopping server...");
					server.halt();
					server.join();
					server.interrupt();
					System.out.println("stopping. thread state:"+server.getState());
					running = false;
					log("Server stopped.");						
					btnRun.setText("Start server");
					server = new SrvMus(srvWindow);
				} catch (IOException e) {
					log("Error stopping server.");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					System.out.println("starting. thread state:"+server.getState());
					server.start();
					running = true;						
					log("Server started.");
					btnRun.setText("Stop server");						
				} catch (NumberFormatException e) {
					log("Error starting server.");
				}
			}		
			
			
		}
		
		
	}

	public void log(String msg) {
		this.log.append(msg + "\n");
		
	}

	public void runOnStart() {
		btnRun.doClick();
		
	}

}
