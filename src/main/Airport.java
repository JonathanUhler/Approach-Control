import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;


public class Airport extends JComponent implements MouseListener {

	public static enum Code {
		KJFK
	}
	

	// Display information
	private final int radarRange = 40; // In nm
	private static int pxPerMile;

	// Airport information
	private Code icao;
	private double acPerMin;
	private Aircraft selected;
	private Aircraft[] aircraft;
	private Waypoint[] waypoints;
	private Waypoint[] inbound;
	private Waypoint[] outbound;


	public Airport(Code icao) {
		this.setFocusable(true);
		this.addMouseListener(this);

		// Initialize airport information
		if (icao == null)
			throw new NullPointerException("icao code cannot be null");
		this.icao = icao;
		this.selected = null;
		this.initialize(icao);
		this.waypoints = new Waypoint[this.inbound.length + this.outbound.length];
		for (int i = 0; i < this.outbound.length; i++)
			this.waypoints[i] = this.outbound[i];
		for (int i = this.outbound.length; i < this.waypoints.length; i++)
			this.waypoints[i] = this.inbound[i - this.outbound.length];
		this.calculateConstants();
		this.aircraft = new Aircraft[this.waypoints.length * 3];

		// Check that the call to initialize() was successful for this icao
		if (this.acPerMin <= 0)
			throw new IllegalArgumentException("acPerMin not initialized correctly for icao " + this.icao.name());

		// Add starting aircraft
		int minAircraft = 1;
		int maxAircraft = Math.max(minAircraft, this.waypoints.length);
		int numAircraft = (int) (Math.random() * (maxAircraft - minAircraft + 1)) + minAircraft;
		for (int i = 0; i < numAircraft; i++)
			this.addAircraft();
	}


	private void initialize(Code icao) {
		switch (icao) {
		case KJFK:
			this.acPerMin = 0.7;
			this.inbound = new Waypoint[] {
				new Runway("13L", this.radarRange / 2 + 1.23, this.radarRange / 2 - 0.58, 10000, 130),
				new Runway("13R", this.radarRange / 2, this.radarRange / 2, 14511, 130)
			};
			this.outbound = new Waypoint[] {
				new Airway("BIG", 90, 1, this.radarRange / 2)
			};
			break;
		default:
			throw new IllegalArgumentException("invalid icao code " + icao.name());
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
			throw new NullPointerException("waypoint cannot be null, for icao " + this.icao.name());

		// Get target waypoint
		Waypoint target = origin instanceof Runway ?
			this.outbound[(int) (Math.random() * this.outbound.length)] :
			this.inbound[(int) (Math.random() * this.inbound.length)];
		if (target == null)
			throw new NullPointerException("waypoint cannot be null, for icao " + this.icao.name());

		// Add aircraft
		Aircraft a = new Aircraft(target);
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

			if (Math.abs(alt1 - alt2) < 1000 && separation < 3)
				return false;
		}

		return true;
	}


	private void checkSeparation(Graphics g) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.setFont(new Font("Courier New", Font.BOLD, (int) (Airport.pxPerMile * 0.7)));
		
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
				if (Math.abs(alt1 - alt2) < 1000 && separation < 5) {
					// Set color based on distance
					if (separation < 3)
						gg.setColor(new Color(255, 0, 0));
					else {
						// Draw current separation
						String separationStr = Double.toString(Math.round(separation * 10) / 10.0);
						int strW = gg.getFontMetrics().stringWidth(separationStr);
						int strH = gg.getFontMetrics().getHeight();
						int strX = (int) ((Math.min(x1, x2) + dx / 2) * Airport.pxPerMile) - strW / 2;
						int strY = (int) ((Math.min(y1, y2) + dy / 2) * Airport.pxPerMile) - strH / 2;

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
	}


	@Override
	public void paintComponent(Graphics g) {
		int w = super.getBounds().width;
		int h = super.getBounds().height;

		// Draw background
		g.setColor(Screen.RADAR_COLOR.darker().darker().darker().darker());
	    for (int x = Airport.pxPerMile; x < w; x += Airport.pxPerMile)
			g.drawLine(x, 0, x, h);
		for (int y = Airport.pxPerMile; y < h; y += Airport.pxPerMile)
			g.drawLine(0, y, w, y);

		// Draw waypoints
		for (Waypoint waypoint : this.waypoints) {
			if (waypoint != null)
				waypoint.paintComponent(g);
		}

		// Draw aircraft
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
			}
		}

		// Check separation, drawing warning lines as needed
		this.checkSeparation(g);

		// Add new aircraft based on the number of aircraft (ac) per minute for this airport
		double secPerAC = (1 / this.acPerMin) * 60;
		int addChance = (int) (Math.random() * (Screen.FRAME_RATE * secPerAC / Screen.gameSpeed()));
		if (addChance == 0)
			this.addAircraft();
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
