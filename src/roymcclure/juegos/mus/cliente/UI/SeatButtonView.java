package roymcclure.juegos.mus.cliente.UI;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import roymcclure.juegos.mus.cliente.logic.GameObject;
import roymcclure.juegos.mus.cliente.logic.ID;

public class SeatButtonView extends GameObject {

	private BufferedImage img;
	
	public static final int WIDTH= 160, HEIGHT=64;
	
	
	public SeatButtonView(int x, int y, ID id) {
		super(x, y, id);
		try {
			img = ImageIO.read(new File("resources/seatbutton.png"));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void tick() {
		
		
	}

	
	// destination, source
	@Override
	public void render(Graphics g) {
		int width = img.getWidth();
		int height = img.getHeight();		
		g.drawImage(img, getX(), getY(), getX() + width, getY() + height, 0, 0, width, height, null);
		
	}

}
