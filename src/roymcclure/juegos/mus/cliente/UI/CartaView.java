package roymcclure.juegos.mus.cliente.UI;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import roymcclure.juegos.mus.cliente.logic.GameObject;
import roymcclure.juegos.mus.cliente.logic.ID;
import roymcclure.juegos.mus.common.logic.Language;

import static roymcclure.juegos.mus.cliente.UI.UIParameters.*;

// this is the view of a card object
// where is rendered depends on the player that owns it
// what it renders depends on the id

public class CartaView extends GameObject {

	private int carta_id;
	
	private static BufferedImage img = null;

	public CartaView(int x, int y, ID id) {
		super(x, y, id);
		try {
			if (img==null) {
				InputStream in = this.getClass().getResourceAsStream("/resources/Baraja_completa.png");
				if (in != null) {
					img = ImageIO.read(in);
				}
				else {
					img = ImageIO.read(new File("resources/Baraja_completa.png"));					
				}

			}

			
		} catch (IOException e) {
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
	public void tick() {}

	@Override
	public void render(Graphics g) {
		Point q = new Point(getX(this.carta_id), getY(this.carta_id));
		g.drawImage(img, x, y, x + ANCHO_CARTA_RENDER, y + ALTO_CARTA_RENDER,
				q.x, q.y, q.x + ANCHO_CARTA_FICHERO, q.y + ALTO_CARTA_FICHERO, null);	

	}
	
	// get X and Y from file
	private int getX(int carta_id) {
		return (carta_id % Language.GameDefinitions.CARDS_PER_SUIT) * ANCHO_CARTA_FICHERO;
	}

	private int getY(int carta_id) {
		return (carta_id / Language.GameDefinitions.CARDS_PER_SUIT) * ALTO_CARTA_FICHERO;		
	}
	

}
