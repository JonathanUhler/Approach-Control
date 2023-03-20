import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Font;


public class Runway extends Waypoint {

	private double length; // In nm
	

	public Runway(String identifier, double x, double y, int length, int hdg) {
		super(identifier, hdg, x, y);
		this.length = (length) * (1.0 / 6076.0); // Convert from ft to nm
	}


	@Override
	public double getTargetX() {
		double rad = AircraftMath.hdgToRad(super.getExitHdg());
		double startPxX = super.getX();
		double endPxX = this.length * Math.cos(rad);

		return startPxX - endPxX * 1.5;
	}


	@Override
	public double getTargetY() {
		double rad = AircraftMath.hdgToRad(super.getExitHdg());
		double startPxY = super.getY();
		double endPxY = this.length * Math.sin(rad);

		return startPxY + endPxY * 1.5;
	}


	@Override
	public void paintComponent(Graphics g) {
		Graphics2D gg = (Graphics2D) g.create();

		double rad = AircraftMath.hdgToRad(super.getExitHdg());
		int pxPerMile = Airport.pxPerMile();
		int startPxX = (int) (super.getX() * pxPerMile);
		int startPxY = (int) (super.getY() * pxPerMile);
		int endPxX = (int) (this.length * Math.cos(rad) * pxPerMile);
		int endPxY = (int) (this.length * Math.sin(rad) * pxPerMile);

		// Draw line and runway number
		gg.setColor(Screen.RADAR_COLOR);
		gg.setFont(new Font("Courier New", Font.PLAIN, pxPerMile));
		gg.drawLine(startPxX, startPxY, startPxX + endPxX, startPxY - endPxY);
		gg.drawString(super.getIdentifier(), startPxX, startPxY);

		// Draw entry triangle
		int triPxX = startPxX - (int) (endPxX * 1.5);
		int triPxY = startPxY + (int) (endPxY * 1.5);
		int triSize = Math.max(1, pxPerMile / 3);
		int[] triangleX = new int[] {triPxX - triSize, triPxX, triPxX + triSize};
		int[] triangleY = new int[] {triPxY + triSize, triPxY - triSize, triPxY + triSize};
		gg.drawPolygon(triangleX, triangleY, 3);

		// Draw approach plate dotted lines
		gg.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
									 0, new float[] {triSize}, triSize));
		gg.drawLine(triPxX, triPxY, startPxX, startPxY);

		// Dispose graphics copy
		gg.dispose();
	}

}
