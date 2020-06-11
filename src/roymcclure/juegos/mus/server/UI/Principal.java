package roymcclure.juegos.mus.server.UI;

import javax.swing.SwingUtilities;

import roymcclure.juegos.mus.common.logic.PlayerState;
import roymcclure.juegos.mus.common.logic.cards.Carta;
import roymcclure.juegos.mus.common.logic.cards.Jugadas;

import static roymcclure.juegos.mus.common.logic.Language.GameDefinitions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import roymcclure.juegos.mus.server.logic.SrvMus;




public class Principal {

	public static void main(String[] args) {
		

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					InputStream in = this.getClass().getResourceAsStream("/server.cfg");
					BufferedReader br = null;
					FileReader fr = null;					
					if (in!=null) {
						br = new BufferedReader(new InputStreamReader(in));						
					} else {
						File cfgFile = new File("server.cfg");
						fr = new FileReader(cfgFile);
						br = new BufferedReader(fr);
					}
					String line = "";
					while ((line = br.readLine())!=null) {
						int eq_indx = line.indexOf('=')+1;
						if (line.contains("listening_port")) {
							//line = line.substring(eq_indx).trim();
							//GameState.base_vacas_partida = Byte.parseByte(line);							
						}
						if (line.contains("piedras_to_juego")) {
							line = line.substring(eq_indx).trim();
							//GameState.base_piedras_juego = Byte.parseByte(line);							
						}
						if (line.contains("juegos_to_vaca")) {
							line = line.substring(eq_indx).trim();
							//GameState.base_juegos_vaca = Byte.parseByte(line);							
						}
						if (line.contains("vacas_to_partida")) {
							line = line.substring(eq_indx).trim();
							//GameState.base_vacas_partida = Byte.parseByte(line);
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
					if (fr!=null) {
						fr.close();
					}
					br.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
					System.out.println("Could not open config file. Aborting.");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error reading file. Aborting.");
				}
				
				
				
				PlayerState mano = new PlayerState();
				Carta[] cartas = new Carta[CARDS_PER_HAND];
				cartas[0] = new Carta((byte) 1);
				cartas[1] = new Carta((byte) 1);
				cartas[2] = new Carta((byte) 3);
				cartas[3] = new Carta((byte) 4);
				mano.setCartas(cartas);

				PlayerState postre = new PlayerState();
				Carta[] cartas2 = new Carta[CARDS_PER_HAND];
				cartas2[0] = new Carta((byte) 1);
				cartas2[1] = new Carta((byte) 1);
				cartas2[2] = new Carta((byte) 3);
				cartas2[3] = new Carta((byte) 4);
				postre.setCartas(cartas2);				
				
				
				System.out.println(" resultado:" +Jugadas.ganaAChica(mano, postre));
				
			}			
		});

	}

}
