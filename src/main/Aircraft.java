import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;


public class Aircraft {

	// Intervals for rounding and selection of values
	public static final int SPD_INTERVAL = 10;
	public static final int ALT_INTERVAL = 1000;
	public static final int HDG_INTERVAL = 10;

	// Aircraft choices. Units in feet, nm, knots. Data taken from wikipedia "specifications" section
	private static final Type[] TYPES = {new Type("B721", 42000, 127, 518), new Type("B722", 42000, 127, 515),
										 new Type("B731", 37000, 150, 473), new Type("B732", 37000, 150, 473),
										 new Type("B732", 37000, 150, 473), new Type("B733", 37000, 150, 473),
										 new Type("B734", 37000, 150, 473), new Type("B735", 37000, 150, 473),
										 new Type("B736", 41000, 150, 453), new Type("B737", 41000, 150, 453),
										 new Type("B738", 41000, 150, 453), new Type("B739", 41000, 150, 453),
										 new Type("B752", 42000, 135, 496), new Type("B753", 42000, 135, 496),
										 new Type("B762", 43100, 150, 486), new Type("B763", 43100, 150, 486),
										 new Type("B764", 43100, 150, 486), new Type("B772", 43100, 135, 511),
										 new Type("B773", 43100, 135, 511), new Type("B788", 43100, 140, 516),
										 new Type("B789", 43100, 153, 516), new Type("A318", 41000, 135, 470),
										 new Type("A319", 41000, 125, 470), new Type("A320", 41000, 135, 470),
										 new Type("A321", 41000, 138, 470), new Type("A332", 41000, 140, 470),
										 new Type("A333", 41000, 146, 470), new Type("A342", 41100, 148, 493),
										 new Type("A343", 41450, 148, 493), new Type("A345", 41450, 148, 493),
										 new Type("A345", 41450, 148, 493), new Type("A359", 43100, 140, 488),
										 new Type("A35K", 41450, 150, 488), new Type("E170", 41000, 130, 430),
										 new Type("E175", 41000, 130, 430), new Type("E190", 41000, 130, 447),
										 new Type("E195", 41000, 130, 447), new Type("E110", 21490, 100, 248),
										 new Type("E120", 29800, 87, 328), new Type("E135", 37000, 110, 450),
										 new Type("E140", 37000, 120, 450), new Type("E145", 37000, 122, 461)};
	private static final String[] AIRLINES = {"DAL", "AAL", "UAL", "DLH", "AFR", "KLM", "SWA", "CSN", "THY", "CES",
											  "RYR", "UAE", "AFL", "CCA", "QTR", "BAW", "QFA", "COA", "ASA", "EZY"};
	

	// Aircraft properties
	private Type type;
	private String id;
	private Controls controls;
	private Waypoint target;
	private boolean cleared;
	private double currentAlt; // In ft
	private double targetAlt; // In ft
	private double currentSpd; // In kt
	private double targetSpd; // In kt
	private double currentHdg; // In deg
	private double targetHdg; // In deg

	// Graphics
	private double x; // In nm
	private double y; // In nm
	private double size;
	

	public Aircraft(Waypoint target) {
		if (target == null)
			throw new NullPointerException("aircraft target cannot be null");
		
		// Set aircraft information
		this.type = Aircraft.TYPES[(int) (Math.random() * Aircraft.TYPES.length)];
		this.id = Aircraft.AIRLINES[(int) (Math.random() * Aircraft.AIRLINES.length)] + 
			      "" + ((int) (Math.random() * (9999 - 1000 + 1)) + 1000);
		this.controls = new Controls(this);
		this.target = target;
		this.cleared = false;

		// Set physical information
		boolean landing = target instanceof Runway;

		// Set altitude
		// Incoming/landing traffic: max / 2 <= y_start <= max
		// Outgoing/takeoff traffic: y_start == 0
		int minStartAlt = landing ? this.type.maxAlt / 2 : 0;
		int maxStartAlt = Math.max((landing ? this.type.maxAlt : 0), minStartAlt);
		int startAlt = (int) (Math.random() * (maxStartAlt - minStartAlt + 1)) + minStartAlt;
		this.currentAlt = AircraftMath.round(startAlt, Aircraft.ALT_INTERVAL);
		// Outgoing/takeoff traffic: min <= y_target <= max / 3
		int minEndAlt = this.type.minAlt;
		int maxEndAlt = Math.max((this.type.maxAlt / 3), minEndAlt);
		int endAlt = landing ? (int) this.currentAlt : (int) (Math.random() * (maxEndAlt - minEndAlt + 1) + minEndAlt);
		this.targetAlt = AircraftMath.round(endAlt, Aircraft.ALT_INTERVAL);

		// Set speed
		// Incoming/landing traffic: max / 2 <= v <= max
		// Outgoing/takeoff traffic: min <= v <= max / 2
		int minSpd = this.type.minSpd;
		int maxSpd = Math.max((landing ? this.type.maxSpd : this.type.maxSpd / 2), minSpd);
		int spd = (int) (Math.random() * (maxSpd - minSpd + 1)) + minSpd;
		this.currentSpd = AircraftMath.round(spd, Aircraft.SPD_INTERVAL);
		this.targetSpd = this.currentSpd;
		
	}


	public Controls getControls() {
		return this.controls;
	}


	public Waypoint getTarget() {
		return this.target;
	}


	public boolean atTarget() {
		double error = 0.5;
		
		// Check for final target position
		double targetX = this.target.getX();
		double targetY = this.target.getY();
		boolean atTarget = this.x >= targetX - error && this.x <= targetX + error &&
			               this.y >= targetY - error && this.y <= targetY + error &&
			               this.cleared;

		// Check for approach position. If true, "pass" control to tower
		double approachX = this.target.getTargetX();
		double approachY = this.target.getTargetY();
		if (this.target instanceof Runway && this.cleared &&
			this.x >= approachX - error && this.x <= approachX + error &&
			this.y >= approachY - error && this.y <= approachY + error)
		{
			this.controls = null; // Prevent takeover
			this.targetHdg = AircraftMath.hdgToTarget(this.x, this.y, this.target.getX(), this.target.getY());
			this.targetAlt = 0;
		}
		
		// Return result
		return atTarget;
	}


	public boolean isCleared() {
		return this.cleared;
	}


	public boolean canBeCleared() {
		return true;
	}


	public int getMaxAlt() {
		return this.type.maxAlt;
	}

	
	public int getMinAlt() {
		return this.type.minAlt;
	}


	public double getTargetAlt() {
		return this.targetAlt;
	}
	

	public int getMaxSpd() {
		return this.type.maxSpd;
	}
	

	public int getMinSpd() {
		return this.type.minSpd;
	}


	public double getTargetSpd() {
		return this.targetSpd;
	}


	public double getTargetHdg() {
		return this.targetHdg;
	}


	public double getX() {
		return this.x;
	}


	public double getY() {
		return this.y;
	}


	public double getPxX() {
		return this.x * Airport.pxPerMile();
	}


	public double getPxY() {
		return this.y * Airport.pxPerMile();
	}


	public double getSize() {
		return this.size;
	}


	public void toggleClearance() {
		if (this.cleared)
			this.cleared = false;
		else {
			if (this.canBeCleared())
				this.cleared = true;
		}
	}


	public void cancelClearance() {
		this.cleared = false;
	}


	public void setTargetAlt(double targetAlt) {
		targetAlt = AircraftMath.round(targetAlt, Aircraft.ALT_INTERVAL);
		targetAlt = Math.min(targetAlt, this.type.maxAlt);
		targetAlt = Math.max(targetAlt, this.type.minAlt);
		this.targetAlt = targetAlt;
	}


	public void setTargetSpd(double targetSpd) {
		targetSpd = AircraftMath.round(targetSpd, Aircraft.SPD_INTERVAL);
		targetSpd = Math.min(targetSpd, this.type.maxSpd);
		targetSpd = Math.max(targetSpd, this.type.minSpd);
		this.targetSpd = targetSpd;
	}


	public void setCurrentHdg(double currentHdg) {
		this.currentHdg = currentHdg;
		this.targetHdg = this.currentHdg;
	}


	public void setTargetHdg(double targetHdg) {
		targetHdg = AircraftMath.round(targetHdg, Aircraft.HDG_INTERVAL);
		this.targetHdg = AircraftMath.adjustHdg(targetHdg);
	}
	

	public void paintComponent(Graphics g, boolean selected) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

		double pxX = this.x * Airport.pxPerMile();
		double pxY = this.y * Airport.pxPerMile();
		double pxCenterX = pxX + this.size / 2.0;
		double pxCenterY = pxY + this.size / 2.0;
		double rad = (90.0 - this.currentHdg) * (Math.PI / 180);

		// Draw body
		gg.rotate(-rad, pxCenterX, pxCenterY); // Angle signs are reversed by Graphics2D::rotate
		gg.setStroke(new BasicStroke(1));
		gg.setColor(Screen.RADAR_COLOR);
		Rectangle body = new Rectangle();
		body.setRect(pxX, pxY, this.size, this.size);
		gg.draw(body);

		// Draw vector arrow
		double vectorLength = (this.currentSpd / 50) * this.size;
		gg.draw(new Line2D.Double(pxCenterX, pxCenterY, pxCenterX + vectorLength, pxCenterY));

		// Update physics model
		this.update();

		// Undo changes to graphics
		gg.rotate(rad, pxCenterX, pxCenterY);

		// Draw separation circles
		if (Screen.showSepRings()) {
			int pxPerMile = Airport.pxPerMile();
			gg.setColor(new Color(255, 0, 0));
			gg.draw(new Ellipse2D.Double(pxX - pxPerMile - this.size / 2,
										 pxY - pxPerMile - this.size / 2,
										 pxPerMile * 3, pxPerMile * 3));
		}

		// Draw information string
		String infoStr = selected ? this.toComplexString() : this.toSimpleString();
		String[] infoSplit = infoStr.split("\n");
		
		// Draw text
		int strY = (int) pxY;
		gg.setColor(Screen.RADAR_COLOR);
		gg.setFont(new Font("Courier New", Font.PLAIN, (int) (this.size * 1.7)));
		for (String line : infoSplit) {
			int strX = (this.currentHdg > 180) ?
				(int) (pxX + this.size * 2) :
				(int) (pxX - gg.getFontMetrics().stringWidth(line) - this.size);
			gg.drawString(line, strX, strY);
			strY += gg.getFontMetrics().getHeight();
		}

		// Dispose graphics copy
		gg.dispose();
	}


	private void update() {
		int gameSpeed = Screen.gameSpeed();
		
		// Physical information: velcoity, time, and distance. All units in nm and hr
		double rad = (90 - this.currentHdg) * (Math.PI / 180);
		double vx = this.currentSpd * Math.cos(rad); // In kt
		double vy = this.currentSpd * Math.sin(rad); // In kt
		double t_s = (1.0 / Screen.FRAME_RATE); // In sec (real world)
		double t = (t_s) * (1.0 / 60.0) * (1.0 / 60.0); // In hr (real world)
		double dx = vx * t; // In nm
		double dy = vy * t; // In nm

		// Increment the screen position based on the physical position moved (nm) in proportion to the pxPerMile
		this.x += dx * gameSpeed;
		this.y -= dy * gameSpeed;

		// Clearance heading update if still in control of the aircraft (not passed to tower yet)
		if (this.cleared && this.controls != null)
			this.targetHdg = AircraftMath.hdgToTarget(this.x, this.y,
													  this.target.getTargetX(), this.target.getTargetY());

		// Update speed, altitude, and heading
		double altChange = ((Math.random() * (18 - 15)) + 15) * (t_s); // Between 900-1100 fpm == 15-18 fps
		double hdgChange = (3) * (t_s); // Based on standard rate of 3 deg / sec
		this.currentSpd = AircraftMath.approachValue(this.currentSpd, this.targetSpd, t_s * gameSpeed);
		this.currentAlt = AircraftMath.approachValue(this.currentAlt, this.targetAlt, altChange * gameSpeed);
		this.currentHdg = AircraftMath.approachHdg(this.currentHdg, this.targetHdg, hdgChange * gameSpeed);
	}


	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}


	public void setPreferredSize(int size) {
		this.size = size;
	}


	public String toSimpleString() {
		return this.id + "\n" +
			   AircraftMath.round(this.currentAlt, 25) + " " + (int) this.currentSpd;
	}


	public String toComplexString() {
		return this.id + " " + this.type + "\n" +
			   this.target + " " + AircraftMath.round(this.targetAlt, 25) + "\n" +
			   AircraftMath.round(this.currentAlt, 25) + " " + (int) this.currentSpd;
	}



	private static class Type {

		public String id;
		public int minAlt; // In ft
		public int maxAlt; // In ft
		public int minSpd; // In kt
		public int maxSpd; // In kt


		public Type(String id, int maxAlt, int minSpd, int maxSpd) {
			if (id == null)
				throw new NullPointerException("type id cannot be null");
			if (maxAlt < 1000)
				throw new IllegalArgumentException("maxAlt " + maxAlt + " out of bounds");
			if (maxSpd < minSpd)
				throw new IllegalArgumentException("maxSpd " + minSpd + " out of bounds");
			if (minSpd < 0)
				throw new IllegalArgumentException("minSpd " + minSpd + " out of bounds");
			
			this.id = id;
			this.minAlt = 1000;
			this.maxAlt = (int) AircraftMath.round(maxAlt / 4, Aircraft.ALT_INTERVAL);
			this.minSpd = (int) AircraftMath.round(minSpd, Aircraft.SPD_INTERVAL);
			this.maxSpd = (int) AircraftMath.round(maxSpd / 2, Aircraft.SPD_INTERVAL);
		}


		@Override
		public String toString() {
			return this.id;
		}

	}

}
