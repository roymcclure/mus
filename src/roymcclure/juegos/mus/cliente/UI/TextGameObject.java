package roymcclure.juegos.mus.cliente.UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import roymcclure.juegos.mus.cliente.logic.GameObject;
import roymcclure.juegos.mus.cliente.logic.ID;

public class TextGameObject extends GameObject {

	private String text;
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TextGameObject(int x, int y, ID id) {
		super(x, y, id);
		
	}

	@Override
	public void tick() {}

	@Override
	public void render(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.drawString(text, x, y);
	}

}
