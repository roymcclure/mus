package roymcclure.juegos.mus.cliente.UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import roymcclure.juegos.mus.cliente.logic.GameObject;
import roymcclure.juegos.mus.cliente.logic.ID;

public class GenericButtonView extends GameObject {

	
	public static final int WIDTH= 160, HEIGHT=40, VGAP = 10;	
	private String text;
	
	public GenericButtonView(int x, int y, String text, ID id) {
		super(x, y, id);
		this.text = text;
	}

	@Override
	public void tick() {
		
		
	}

	
	// destination, source
	@Override
	public void render(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(x, y, WIDTH, HEIGHT);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.BLACK);
		g2d.drawString(text, x+WIDTH/2, y+HEIGHT/2);		
	}
}
