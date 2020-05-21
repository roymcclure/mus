package roymcclure.juegos.mus.cliente.UI;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import roymcclure.juegos.mus.cliente.logic.GameObject;
import roymcclure.juegos.mus.cliente.logic.ID;

public class BocadilloView extends GameObject {

	private static BufferedImage img = null;
	private long start = 0;
	private long life_time;
	private static int image_width;
	private static int image_height;
	
	public BocadilloView(int x, int y, ID id, long life_time_ms, byte seat_id) {
		super(x, y, id);
		this.life_time = life_time_ms;
		try {
			if (img==null) {
				if (seat_id %2 ==0) {
					img = ImageIO.read(new File("resources/bocadillo_vertical.png"));					
				} else {
					img = ImageIO.read(new File("resources/bocadillo_horizontal.png"));					
				}
				image_width = img.getWidth();
				image_height = img.getHeight();
				switch(seat_id) {
				case 0:flipVertical();
					break;
					case 1:
						break;
					case 2:
						break;
					case 3:flipHorizontal();
						break;
				}
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void tick() {
		checkLifeTime();
	}

	private void checkLifeTime() {
		if (start == 0)
			start = System.nanoTime();
		long now = System.nanoTime();
		long timeElapsed = now - start;
		if ((timeElapsed / 1000000) > life_time) {
			this.setMarkedForRemoval(true);
		}		
	}
	
	@Override
	public void render(Graphics g) {
		Point q = new Point(0,0);		
		g.drawImage(img, x, y, x + UIParameters.BOCADILLO_ANCHO, y + UIParameters.BOCADILLO_ALTO,
				q.x, q.y, q.x + image_width, q.y + image_height, null);
		
	}

	private void flipVertical() {
		// Flip the image vertically
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -img.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		img = op.filter(img, null);
	}


	private void flipHorizontal() {
	// Flip the image horizontally
	AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
	tx.translate(-img.getWidth(null), 0);
	AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	img = op.filter(img, null);
	}

}
