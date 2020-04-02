package roymcclure.juegos.mus.cliente.logic;

import java.awt.Graphics;
import java.util.LinkedList;

public class Handler {

	LinkedList<GameObject> objects = new LinkedList<GameObject>();
	
	public void update() {
		for (int i=0; i<objects.size(); i++) {
			GameObject tempObject = objects.get(i);
			tempObject.tick();
		}
	}
	
	public void render(Graphics g) {
		for (int i=0; i<objects.size(); i++) {
			GameObject tempObject = objects.get(i);
			tempObject.render(g);
		}	
	}
	
	public void addObject(GameObject go) {
		this.objects.add(go);
	}
	
	public void removeObject(GameObject go) {
		this.objects.remove(go);
	}
	
	
}
