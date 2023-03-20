import java.awt.Graphics;
import java.awt.Point;


public abstract class Waypoint {

	private String identifier;
	private int exitHdg; // In deg
	private int altFromExtrema; // In ft
	private double x; // In nm
	private double y; // In nm
	

	public Waypoint(String identifier, int exitHdg, double x, double y) {
		this.identifier = identifier;
		this.exitHdg = exitHdg;
		int maxAlt = 4000;
		int minAlt = 0;
		int alt = (int) (Math.random() * (maxAlt - minAlt + 1)) + minAlt;
		// Allow clearance when altitude is <= 2000 ft for landing or == max - alt for exiting
		this.altFromExtrema = this instanceof Runway ? 2000 : AircraftMath.round(alt, Aircraft.ALT_INTERVAL);
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


	public double getTargetX() {
		return this.x;
	}


	public double getTargetY() {
		return this.y;
	}


	public boolean inRange(double aircraftX, double aircraftY) {
		double error = 0.5;
		return aircraftX >= this.getX() - error && aircraftX <= this.getX() + error &&
			   aircraftY >= this.getY() - error && aircraftY <= this.getY() + error;
	}


	public boolean inTargetRange(double aircraftX, double aircraftY) {
		double error = 0.5;
		return aircraftX >= this.getTargetX() - error && aircraftX <= this.getTargetX() + error &&
			   aircraftY >= this.getTargetY() - error && aircraftY <= this.getTargetY() + error;
	}


	public boolean atAlt(double aircraftAlt, double aircraftMaxAlt) {
	    if (this instanceof Runway)
			return aircraftAlt <= this.altFromExtrema;
		return (int) aircraftAlt == aircraftMaxAlt - this.altFromExtrema;
	}


	public abstract void paintComponent(Graphics g);


	@Override
	public String toString() {
		return this.identifier;
	}

}
