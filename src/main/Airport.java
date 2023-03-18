import javax.swing.JComponent;
import java.awt.Graphics;
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
	private Aircraft selected;
	private Aircraft[] aircraft;
	private Waypoint[] waypoints;
	private Waypoint[] inbound;
	private Waypoint[] outbound;


	public Airport(Code icao) {
		this.setFocusable(true);
		this.addMouseListener(this);
		
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

		// Add starting aircraft
		int minAircraft = 1;
		int maxAircraft = Math.max(minAircraft, this.waypoints.length);
		int numAircraft = (int) (Math.random() * (maxAircraft - minAircraft + 1)) + minAircraft;
		for (int i = 0; i < numAircraft; i++) {
			Waypoint origin = this.waypoints[i % this.waypoints.length];
			if (origin == null)
				throw new NullPointerException("waypoint cannot be null, for icao " + this.icao.name());
			
			Waypoint target = origin instanceof Runway ?
				this.outbound[(int) (Math.random() * this.outbound.length)] :
				this.inbound[(int) (Math.random() * this.inbound.length)];
			if (target == null)
				throw new NullPointerException("waypoint cannot be null, for icao " + this.icao.name());
			
			Aircraft a = new Aircraft(target);
			int hdg = origin.getExitHdg();
			a.setCurrentHdg(hdg);
			a.setLocation(origin.getX(), origin.getY());
			this.aircraft[i] = a;
		}
	}


	private void initialize(Code icao) {
		switch (icao) {
		case KJFK:
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

			if (aircraft != null) {
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
		}
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
