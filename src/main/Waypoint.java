import java.awt.Graphics;
import java.awt.Point;


public abstract class Waypoint {

	private String identifier;
	private int exitHdg; // In deg
	private double x; // In nm
	private double y; // In nm
	

	public Waypoint(String identifier, int exitHdg, double x, double y) {
		this.identifier = identifier;
		this.exitHdg = exitHdg;
		this.x = x;
		this.y = y;
	}


	public String getIdentifier() {
		return this.identifier;
	}


	public int getExitHdg() {
		return this.exitHdg;
	}


	public double getX() {
		return this.x;
	}


	public double getY() {
		return this.y;
	}


	public abstract void paintComponent(Graphics g);


	@Override
	public String toString() {
		return this.identifier;
	}

}
