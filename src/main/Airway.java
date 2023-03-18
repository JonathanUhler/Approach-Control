import java.awt.Graphics;
import java.awt.Font;


public class Airway extends Waypoint {	

	public Airway(String identifier, int exitHdg, double x, double y) {
		super(identifier, exitHdg, x, y);
	}


	@Override
	public void paintComponent(Graphics g) {
		int pxX = (int) (super.getX() * Airport.pxPerMile());
		int pxY = (int) (super.getY() * Airport.pxPerMile());
		int pxSize = Airport.pxPerMile();
		
		g.setColor(Screen.RADAR_COLOR);
	    g.setFont(new Font("Courier New", Font.PLAIN, pxSize));

		int[] triangleX = new int[] {pxX - (pxSize / 2), pxX, pxX + (pxSize / 2)};
		int[] triangleY = new int[] {pxY + (pxSize / 2), pxY - (pxSize) / 2, pxY + (pxSize / 2)};

		g.drawPolygon(triangleX, triangleY, 3);
		g.drawString(super.getIdentifier(), pxX + (pxSize / 2), pxY);
	}

}
