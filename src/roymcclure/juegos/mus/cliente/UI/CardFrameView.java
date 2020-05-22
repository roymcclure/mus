package roymcclure.juegos.mus.cliente.UI;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import roymcclure.juegos.mus.cliente.logic.GameObject;
import roymcclure.juegos.mus.cliente.logic.ID;

import static roymcclure.juegos.mus.cliente.UI.UIParameters.*;

// this is the view of a card object
// where is rendered depends on the player that owns it
// what it renders depends on the id

public class CardFrameView extends GameObject {

	private BufferedImage img = null;
	
	public CardFrameView(int x, int y, ID id) {
		super(x, y, id);
		try {
			if (img==null) {
				InputStream in = null;
				switch(id) {
				case CartaFrame:
					in = this.getClass().getResourceAsStream("/resources/carta_frame.png");
					if (in != null) {
						img = ImageIO.read(in);
					}
					else {
						img = ImageIO.read(new File("resources/carta_frame.png"));					
					}
					break;
				case CartaFrameSelected:
					in = this.getClass().getResourceAsStream("/resources/carta_frame_selected.png");
					if (in != null) {
						img = ImageIO.read(in);
					}
					else {
						img = ImageIO.read(new File("resources/carta_frame_selected.png"));					
					}
					break;
				default:
					break;
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void tick() {}

	@Override
	public void render(Graphics g) {
		g.drawImage(img, x, y, x + ANCHO_CARTA, y + ALTO_CARTA,
				0, 0, ANCHO_CARTA_FICHERO, ALTO_CARTA_FICHERO, null);	

	}

}
