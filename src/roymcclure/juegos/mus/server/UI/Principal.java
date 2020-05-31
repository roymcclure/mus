package roymcclure.juegos.mus.server.UI;

import javax.swing.SwingUtilities;

import roymcclure.juegos.mus.common.logic.GameState;
import roymcclure.juegos.mus.common.logic.PlayerState;
import roymcclure.juegos.mus.common.logic.cards.Carta;
import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import roymcclure.juegos.mus.server.logic.SrvMus;




public class Principal {

	public static void main(String[] args) {
		

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				File cfgFile = new File("server.cfg");
				FileReader fr;
				try {
					fr = new FileReader(cfgFile);
					BufferedReader br = new BufferedReader(fr);
					String line = "";
					while ((line = br.readLine())!=null) {
						int eq_indx = line.indexOf('=')+1;
						if (line.contains("listening_port")) {
							//line = line.substring(eq_indx).trim();
							//GameState.base_vacas_partida = Byte.parseByte(line);							
						}
						if (line.contains("piedras_to_juego")) {
							line = line.substring(eq_indx).trim();
							GameState.base_piedras_juego = Byte.parseByte(line);							
						}
						if (line.contains("juegos_to_vaca")) {
							line = line.substring(eq_indx).trim();
							GameState.base_juegos_vaca = Byte.parseByte(line);							
						}
						if (line.contains("vacas_to_partida")) {
							line = line.substring(eq_indx).trim();
							GameState.base_vacas_partida = Byte.parseByte(line);
						}						
					}					
					ServerWindow serverWindow = new ServerWindow();
					SrvMus server = new SrvMus(serverWindow);
					serverWindow.setServer(server);
					serverWindow.setVisible(true);
					if(args.length >= 1) {
						if(args[0].equals("--runOnStart")) {
							serverWindow.runOnStart();
						}
					}
					fr.close();
					br.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					System.out.println("Could not open config file. Aborting.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error reading file. Aborting.");
				}
				
				
				/*
				PlayerState p = new PlayerState();
				Carta[] cartas = new Carta[CARDS_PER_HAND];
				cartas[0] = new Carta((byte) 5);
				cartas[1] = new Carta((byte) 4);
				cartas[2] = new Carta((byte) 10);
				cartas[3] = new Carta((byte) 11);
				p.setCartas(cartas);
				System.out.println("valor de pares " + p.valorPares());*/
				
			}			
		});

	}

}
