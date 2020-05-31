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

import static roymcclure.juegos.mus.cliente.UI.UIParameters.*;

public class HandView extends GameObject {

	private static BufferedImage img = null;	
	
	public HandView(int x, int y, ID id) {
		super(x, y, id);
		try {
			if (img==null) {

				InputStream in = this.getClass().getResourceAsStream("/resources/hand.png");
				if (in != null) {
				img = ImageIO.read(in);
				}
				else {
					img = ImageIO.read(new File("resources/hand.png"));					
				}
			
			}
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
		g.drawImage(img, x, y, x + ANCHO_MANO_RENDER, y + ALTO_MANO_RENDER,
				q.x, q.y, q.x + ANCHO_MANO_FICHERO, q.y + ALTO_MANO_FICHERO, null);	


	}

}
