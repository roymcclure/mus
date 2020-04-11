package roymcclure.juegos.mus.server.UI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import roymcclure.juegos.mus.server.logic.SrvMus;

public class ServerWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3021988155527724728L;

	private boolean running = false;
	
	private SrvMus server;
	
	public ServerWindow() {
		this.setSize(320,320);
		this.setTitle("Servidor Mus");
		this.setAlwaysOnTop(true);
		this.setLocation(2000, 200);
		JTextField txtCmd = new JTextField("");
		txtCmd.setColumns(10);
		this.getContentPane().add(txtCmd, BorderLayout.NORTH);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		JTextArea log = new JTextArea("");
		this.getContentPane().add(log, BorderLayout.CENTER);	
		
		DefaultCaret caret = (DefaultCaret)log.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
		
		
		JScrollPane scroll = new JScrollPane ( log );
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().add(scroll);
		
		
		JButton btnRun = new JButton("Start");
		this.getContentPane().add(btnRun, BorderLayout.SOUTH);
		
		btnRun.addActionListener(new BtnListener(this, log, btnRun,txtCmd));
		
		this.setVisible(true);
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
				// TODO Auto-generated method stub
				if (e.getKeyCode()==10) {
					server.runCommand(txtCmd.getText(), log);
					txtCmd.setText("");
				}
				

			}
		});
		
		server = new SrvMus(log);
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
		
	}
	
	class BtnListener implements ActionListener {
		
		JFrame srvWindow;
		JTextArea log;
		JButton btnRun;
		JTextField txtPort;
		
		public BtnListener(JFrame srvWindow_, JTextArea txtArea_, JButton jbtn, JTextField txtPort_) {
			srvWindow = srvWindow_;
			log = txtArea_;
			btnRun = jbtn;
			txtPort = txtPort_;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			if (running) {
				try {
					log.append("Stopping server...\n");
					server.halt();
					server.join();
					server.interrupt();
					System.out.println("stopping. thread state:"+server.getState());
					running = false;
					log.append("Server stopped.\n");						
					btnRun.setText("Start server");
					server = new SrvMus(log);
				} catch (IOException e) {
					log.append("Error stopping server.\n");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					System.out.println("starting. thread state:"+server.getState());
					server.start();
					running = true;						
					log.append("Server started.\n");
					btnRun.setText("Stop server");						
				} catch (NumberFormatException e) {
					log.append("Error starting server.\n");
				}
			}		
			
			
		}
		
		
	}

}
