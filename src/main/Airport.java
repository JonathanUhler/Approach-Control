import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Ellipse2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;


public class Airport extends JComponent implements MouseListener {

	public static enum Code {
		KJFK(false),
		KSFO(false),
		EGLL(false),
		KPAO(true);


		private boolean makeGA;
		

		private Code(boolean makeGA) {
			this.makeGA = makeGA;
		}


		public boolean isGA() {
			return this.makeGA;
		}
		
	}
	

	// Display information
	private static int pxPerMile;

	// Airport information
	private Code code;
	private int radarRange; // In nm
	private int vertSeparation; // In ft
	private int horizSeparation; // In nm
	private double acPerMin;
	private Aircraft selected;
	private Aircraft[] aircraft;
	private Waypoint[] waypoints;
	private Waypoint[] inbound;
	private Waypoint[] outbound;

	// Score information
	private int flights;
	private boolean hasConflict;


	public Airport(Code code) {
		this.setFocusable(true);
		this.addMouseListener(this);

		// Initialize code information
		if (code == null)
			throw new NullPointerException("code code cannot be null");
		this.code = code;
		this.radarRange = (!this.code.isGA()) ? 40 : 20;
		this.vertSeparation = (!this.code.isGA()) ? 1000 : 500;
		this.horizSeparation = (!this.code.isGA()) ? 3 : 1;

		// Initialize waypoint/aircraft information
		this.selected = null;
		this.initialize(code);
		this.waypoints = new Waypoint[this.inbound.length + this.outbound.length];
		for (int i = 0; i < this.outbound.length; i++)
			this.waypoints[i] = this.outbound[i];
		for (int i = this.outbound.length; i < this.waypoints.length; i++)
			this.waypoints[i] = this.inbound[i - this.outbound.length];
		this.calculateConstants();
		this.aircraft = new Aircraft[this.waypoints.length];
		this.flights = 0;
		this.hasConflict = false;

		// Check that the call to initialize() was successful for this code
		if (this.acPerMin <= 0)
			throw new IllegalArgumentException("acPerMin not initialized correctly for code " +
											   this.code.name());

		// Add starting aircraft
		int minAircraft = 1;
		int maxAircraft = Math.max(minAircraft, this.waypoints.length / 2);
		int numAircraft = (int) (Math.random() * (maxAircraft - minAircraft + 1)) + minAircraft;
		for (int i = 0; i < numAircraft; i++)
			this.addAircraft();
	}


	private void initialize(Code code) {
		switch (code) {
		case KJFK:
			this.acPerMin = 0.6;
			this.inbound = new Waypoint[] {
				new Runway("13L", this.radarRange / 2 + 1.23, this.radarRange / 2 - 0.58, 10000),
				new Runway("13R", this.radarRange / 2, this.radarRange / 2, 14511)
			};
			this.outbound = new Waypoint[] {
				new Airway("ALB", 180, this.radarRange / 5,     1),
				new Airway("MHT", 180, 3 * this.radarRange / 5, 1),
				new Airway("ACK", 270, this.radarRange - 1,     2 * this.radarRange / 5),
				new Airway("DNY", 0,   2 * this.radarRange / 5, this.radarRange - 1)
			};
			break;
		case KSFO:
			this.acPerMin = 0.9;
			this.inbound = new Waypoint[] {
				new Runway("28R", this.radarRange / 2, this.radarRange / 2, 11870),
				new Runway("28L", this.radarRange / 2 + 0.14 * 5, this.radarRange / 2 + 0.22 * 5,
						   11381)
			};
			this.outbound = new Waypoint[] {
				new Airway("RNO", 220, this.radarRange - 1, 1),
				new Airway("LAX", 290, this.radarRange - 1, this.radarRange - 1),
				new Airway("HNL", 90,  1,                   this.radarRange / 2)
			};
			break;
		case EGLL:
			this.acPerMin = 1.33;
			this.inbound = new Waypoint[] {
				new Runway("27R", this.radarRange / 2, this.radarRange / 2, 12802),
				new Runway("27L", this.radarRange / 2, this.radarRange / 2 + 0.76, 12008)
			};
			this.outbound = new Waypoint[] {
				new Airway("BNN", 180, this.radarRange / 2,     1),
				new Airway("LAM", 180, 4 * this.radarRange / 5, 1),
				new Airway("BIG", 0,   4 * this.radarRange / 5, this.radarRange - 1),
				new Airway("CPT", 90,  1,                       this.radarRange / 2)
			};
			break;
		case KPAO:
			this.acPerMin = 0.36;
			this.inbound = new Waypoint[] {
				new Runway("31", this.radarRange / 2, this.radarRange / 2, 2443)
			};
			this.outbound = new Waypoint[] {
				new Airway("SQL", 100, 1,                       this.radarRange / 4),
				new Airway("SJC", 280, this.radarRange - 1,     3 * this.radarRange / 4),
				new Airway("LVK", 190, 4 * this.radarRange / 5, 1)
			};
			break;
		default:
			throw new IllegalArgumentException("invalid code code " + code.name());
		}
	}


	private void calculateConstants() {
		int w = super.getBounds().width;
		int h = super.getBounds().height;
		
		Airport.pxPerMile = Math.min(w, h) / this.radarRange;
		if (Airport.pxPerMile == 0)
			Airport.pxPerMile = 1;
	}


	public static int pxPerMile() {
		return Airport.pxPerMile;
	}


	public Aircraft getSelected() {
		return this.selected;
	}


	public int getFlights() {
		return this.flights;
	}


	public boolean hasConflict() {
		return this.hasConflict;
	}


	private boolean addAircraft() {
		// Find null array index
		int index = -1;
		for (int i = 0; i < this.aircraft.length; i++) {
			if (this.aircraft[i] == null) {
				index = i;
				break;
			}
		}
		
		if (index == -1)
			return false;

		// Get origin waypoint
		Waypoint origin = this.waypoints[(int) (Math.random() * this.waypoints.length)];
		if (origin == null)
			throw new NullPointerException("waypoint cannot be null, for code " + this.code.name());

		// Get target waypoint
		Waypoint target = origin instanceof Runway ?
			this.outbound[(int) (Math.random() * this.outbound.length)] :
			this.inbound[(int) (Math.random() * this.inbound.length)];
		if (target == null)
			throw new NullPointerException("waypoint cannot be null, for code " + this.code.name());

		// Add aircraft
		Aircraft a = new Aircraft(target, this.code.isGA());
		int hdg = origin.getExitHdg();
		a.setCurrentHdg(hdg);
		a.setLocation(origin.getX(), origin.getY());
		if (this.noConflict(a)) {
			this.aircraft[index] = a;
			return true;
		}
		return false;
	}


	private boolean noConflict(Aircraft aircraft1) {
		for (Aircraft aircraft2 : this.aircraft) {
			if (aircraft2 == null || aircraft2 == aircraft1)
				continue;

			double alt1 = aircraft1.getCurrentAlt();
			double alt2 = aircraft2.getCurrentAlt();
			double x1 = aircraft1.getX();
			double x2 = aircraft2.getX();
			double y1 = aircraft1.getY();
			double y2 = aircraft2.getY();
			double separation = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

			if (Math.abs(alt1 - alt2) < this.vertSeparation && separation < this.horizSeparation)
				return false;
		}

		return true;
	}


	private boolean checkSeparation(Graphics g) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.setFont(new Font("Courier New", Font.BOLD, (int) (Airport.pxPerMile * 0.7)));
		boolean hasSeparation = true;
		
		for (int i = 0; i < this.aircraft.length; i++) {
			for (int j = i + 1; j < this.aircraft.length; j++) {
				Aircraft aircraft1 = this.aircraft[i];
				Aircraft aircraft2 = this.aircraft[j];
				if (aircraft1 == null || aircraft2 == null)
					continue;
				
				double alt1 = aircraft1.getCurrentAlt();
				double alt2 = aircraft2.getCurrentAlt();
				double x1 = aircraft1.getX();
				double x2 = aircraft2.getX();
				double y1 = aircraft1.getY();
				double y2 = aircraft2.getY();
				double dx = Math.abs(x1 - x2);
				double dy = Math.abs(y1 - y2);
				double separation = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

				// Conflict detection
				if (Math.abs(alt1 - alt2) < this.vertSeparation &&
					separation < this.horizSeparation * (5.0 / 3.0))
				{
					// Set color based on distance
					if (separation < this.horizSeparation) {
						gg.setColor(new Color(255, 0, 0));
						hasSeparation = false;
					}
					else {
						// Draw current separation
						String separationStr = Double.toString(Math.round(separation * 10) / 10.0);
						int strW = gg.getFontMetrics().stringWidth(separationStr);
						int strH = gg.getFontMetrics().getHeight();
						int strX =
							(int) ((Math.min(x1, x2) + dx / 2) * Airport.pxPerMile) - strW / 2;
						int strY =
							(int) ((Math.min(y1, y2) + dy / 2) * Airport.pxPerMile) - strH / 2;

						// Draw solid rectangle background with black separation text
						gg.setColor(Screen.RADAR_COLOR);
						gg.fillRect(strX, strY, strW, strH);
						gg.setColor(new Color(0, 0, 0));
						gg.drawString(separationStr, strX, strY + (int) (strH * 0.7));

						gg.setColor(Screen.RADAR_COLOR);
					}

					// Draw connecting line
					gg.draw(new Line2D.Double(x1 * Airport.pxPerMile, y1 * Airport.pxPerMile,
											  x2 * Airport.pxPerMile, y2 * Airport.pxPerMile));
				}
			}
		}

		// Dispose graphics copy
		gg.dispose();

		// Return status
		return hasSeparation;
	}


	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gg = (Graphics2D) g.create();
		
		int w = super.getBounds().width;
		int h = super.getBounds().height;

		// Draw background
		gg.setColor(Screen.RADAR_COLOR.darker().darker().darker().darker());
	    for (int x = Airport.pxPerMile; x < w; x += Airport.pxPerMile)
			gg.drawLine(x, 0, x, h);
		for (int y = Airport.pxPerMile; y < h; y += Airport.pxPerMile)
			gg.drawLine(0, y, w, y);

		// Draw waypoints
		for (Waypoint waypoint : this.waypoints) {
			if (waypoint != null)
				waypoint.paintComponent(g);
		}

		// Draw aircraft
		boolean aircraftLost = false;
		for (int i = 0; i < this.aircraft.length; i++) {
			Aircraft aircraft = this.aircraft[i];
			if (aircraft == null)
				continue;

			// Draw and update size as needed
			aircraft.setPreferredSize(Airport.pxPerMile / 2);
			aircraft.paintComponent(g, this.selected == aircraft);

			// Check if near waypoint and remove
			if (aircraft.atTarget()) {
				this.aircraft[i] = null;
				if (this.selected == aircraft)
					this.selected = null;
				this.flights++;
			}
			
			// Check if this aircraft has left the airspace uncleared
			double aircraftX = aircraft.getX();
			double aircraftY = aircraft.getY();
			if (!aircraft.isCleared() &&
				(aircraftX < 0 || aircraftX > this.radarRange ||
				 aircraftY < 0 || aircraftY > this.radarRange))
			{
				aircraftLost = true;

				// Draw red ring
				double sepRingX = (aircraftX - this.horizSeparation / 2.0) * Airport.pxPerMile;
				double sepRingY = (aircraftY - this.horizSeparation / 2.0) * Airport.pxPerMile;
				gg.setColor(new Color(255, 0, 0));
				gg.draw(new Ellipse2D.Double(sepRingX, sepRingY,
											 Airport.pxPerMile * this.horizSeparation,
											 Airport.pxPerMile * this.horizSeparation));
			}
		}

		// Check separation, drawing warning lines as needed
		boolean hasSeparation = this.checkSeparation(g);

		// Check for failure condition
		if (!hasSeparation || aircraftLost)
			this.hasConflict = true;

		// Add new aircraft based on the number of aircraft (ac) per minute for this airport
		double secPerAC = (1 / this.acPerMin) * 60;
		int addChance = (int) (Math.random() * (Screen.FRAME_RATE * secPerAC / Screen.gameSpeed()));
		if (addChance == 0)
			this.addAircraft();

		// Dispose graphics copy
		gg.dispose();
	}


	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		this.calculateConstants();
	}


	@Override
	public void mousePressed(MouseEvent e) {
		double clickX = e.getX();
		double clickY = e.getY();

		this.selected = null;

		for (Aircraft aircraft : this.aircraft) {
			if (aircraft == null)
				continue;
			
			double aircraftX = aircraft.getPxX();
			double aircraftY = aircraft.getPxY();
			double aircraftSize = aircraft.getSize();

			if (clickX > aircraftX - aircraftSize && clickX < aircraftX + aircraftSize * 2 &&
				clickY > aircraftY - aircraftSize && clickY < aircraftY + aircraftSize * 2)
			{
			    this.selected = aircraft;
			}
		}		
	}


	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

}
