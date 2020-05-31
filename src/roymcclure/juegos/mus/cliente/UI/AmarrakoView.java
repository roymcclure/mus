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

public class AmarrakoView extends GameObject {


	private BufferedImage img=null;
	
	public AmarrakoView(int x, int y, ID id) {
		super(x, y, id);
		try {
			if (img==null) {
				InputStream in = this.getClass().getResourceAsStream(id == ID.Amarrako? "/resources/franmarrako.png" : "/resources/gonmarrako.png");
				if (in != null) {
					img = ImageIO.read(in);
				}
				else {
					img = ImageIO.read(new File(id == ID.Amarrako ? "resources/franmarrako.png" : "resources/gonmarrako.png"));					
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
		g.drawImage(img, x, y, x + UIParameters.ANCHO_AMARRAKO_RENDER, y +UIParameters.ALTO_AMARRAKO_RENDER,
				q.x, q.y, q.x + UIParameters.ALTO_AMARRAKO_FICHERO, q.y + UIParameters.ANCHO_AMARRAKO_FICHERO, null);

	}

}
