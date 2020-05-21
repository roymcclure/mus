package roymcclure.juegos.mus.cliente.logic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class HandView extends GameObject {

	private static int ANCHO_ICONO_MANO_FICHERO = 512;
	private static int ALTO_ICONO_MANO_FICHERO = 512;
	private static int ANCHO_ICONO_MANO = 32;
	private static int ALTO_ICONO_MANO = 32;	

	private static BufferedImage img = null;	
	
	public HandView(int x, int y, ID id) {
		super(x, y, id);
		try {
			if (img==null)
				img = ImageIO.read(new File("resources/hand.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(Graphics g) {
		Point q = new Point(0, 0);		
		g.drawImage(img, x, y, x + ANCHO_ICONO_MANO, y + ALTO_ICONO_MANO,
				q.x, q.y, q.x + ANCHO_ICONO_MANO_FICHERO, q.y + ALTO_ICONO_MANO_FICHERO, null);	


	}

}
