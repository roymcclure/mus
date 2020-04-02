package roymcclure.juegos.mus.server.UI;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;

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
		
		JTextField txtPort = new JTextField("5678");
		txtPort.setColumns(10);
		this.getContentPane().add(txtPort, BorderLayout.NORTH);
		
		JTextArea log = new JTextArea("");
		this.getContentPane().add(log, BorderLayout.CENTER);		
		
		JButton btnRun = new JButton("Start");
		this.getContentPane().add(btnRun, BorderLayout.SOUTH);
		
		btnRun.addActionListener(new BtnListener(this, log, btnRun,txtPort));
		
		this.setVisible(true);
		
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
					server.interrupt();
					server.join();
					System.out.println("stopping. thread state:"+server.getState());
					running = false;
					log.append("Server stopped.\n");						
					btnRun.setText("Start server");
				} catch (IOException e) {
					log.append("Error stopping server.\n");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					log.append("Starting server...\n");
					server = new SrvMus(log);
					System.out.println("starting. thread state:"+server.getState());
					server.start(txtPort.getText());
					server.start();
					running = true;						
					log.append("Server started.\n");
					btnRun.setText("Stop server");						
				} catch (NumberFormatException | IOException e) {
					log.append("Error starting server.\n");
				}
			}
		}
		
	}

}
