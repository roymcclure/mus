package roymcclure.juegos.mus.cliente.UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import roymcclure.juegos.mus.cliente.logic.Game;
import roymcclure.juegos.mus.cliente.logic.GameObject;
import roymcclure.juegos.mus.cliente.logic.ID;
import roymcclure.juegos.mus.common.logic.Language;

// this is the view of a card object
// where is rendered depends on the player that owns it
// what it renders depends on the id

public class CartaView extends GameObject {

	public static final int ANCHO_CARTA = (Game.WIDTH * 47) / (4 * 100);
	public static final int ALTO_CARTA = Game.HEIGHT * 25 / 100;
	public static final int X_INICIAL_NORTE_SUR = (int)(Game.WIDTH * 0.2425f);
	public static final int Y_INICIAL_OESTE_ESTE = (int)(Game.HEIGHT * 0.2425f);
	
	private static final int ANCHO_TOTAL_FICHERO = 2496; 
	private static final int ALTO_TOTAL_FICHERO = 1595;	
	private static final int ANCHO_CARTA_FICHERO = ANCHO_TOTAL_FICHERO / 12; 
	private static final int ALTO_CARTA_FICHERO = ALTO_TOTAL_FICHERO / 5;	
	
	private int carta_id;
	private int jugador;
	private int position_in_hand;
	
	
	private BufferedImage img;
	
	public int getPosition_in_hand() {
		return position_in_hand;
	}

	public void setPosition_in_hand(int position_in_hand) {
		this.position_in_hand = position_in_hand;
	}

	public int getJugador() {
		return jugador;
	}

	public void setJugador(int jugador) {
		this.jugador = jugador;
	}

	public CartaView(int x, int y, ID id) {
		super(x, y, id);
		try {
			img = ImageIO.read(new File("resources/baraja_completa.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getCarta_id() {
		return carta_id;
	}

	public void setCarta_id(int carta_id) {
		this.carta_id = carta_id;
	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}

	// es responsabilidad de cada vista definir su posicion?
	// no, es responsabilidad de los parámetros de la interfaz
	// quién define los parámetros de la interfaz?
	public static Point getBoardPosition(int jugador, int id_carta) {
		Point p = new Point();
		if (jugador %2 == 0) {
			p.x = X_INICIAL_NORTE_SUR + (id_carta * (ANCHO_CARTA + (Game.WIDTH / 100)));
			p.y = jugador == 0 ? 0 : (int)(Game.HEIGHT * 0.75f);
		} else {
			
		}
		return p;
	}
	
	@Override
	public void render(Graphics g) {
		// TODO Auto-generated method stub
		g.setColor(Color.white);
		g.fillRect(x, y,ANCHO_CARTA, ALTO_CARTA);
		renderCard(g,img);
		

	}
	
	// quien es el responsabe de devolver las coordenadas de las imagenes?
	// quien gestiona los datos de las imágenes?
	private void renderCard(Graphics g, BufferedImage img) {
		// destino primero, origen despues
		Point p = getBoardPosition(this.jugador, this.position_in_hand);
		Point q = new Point(getX(this.carta_id), getY(this.carta_id));
		g.drawImage(img, p.x, p.y, p.x + ANCHO_CARTA, p.y + ALTO_CARTA,
				q.x, q.y, q.x + ANCHO_CARTA_FICHERO, q.y + ALTO_CARTA_FICHERO, null);
	}
	
	private int getX(int carta_id) {
		return (carta_id % Language.GameDefinitions.CARDS_PER_SUIT) * ANCHO_CARTA_FICHERO;
	}

	private int getY(int carta_id) {
		return (carta_id / Language.GameDefinitions.CARDS_PER_SUIT) * ALTO_CARTA_FICHERO;		
	}
	

}
