import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;


public class Controls extends JComponent implements MouseListener {

	// Aircraft information
	private Aircraft owner;
	private int maxAlt; // In ft
	private int minAlt; // In ft
	private int maxSpd; // In kt
	private int minSpd; // In kt
	private int altCount;
	private int spdCount;

	// Display information
	private int w;
	private int h;
	private int compassSize;
	private int compassOffsetX;
	private int compassOffsetY;
	private int margin;
	private Font font;
	private int strHeight;
	private int spdTextWidth;
	private int altTextWidth;
	private int textHeight;
	private int textOffsetY;
	private int directToWidth;
	private int directToOffsetX;
	private int directToOffsetY;


	public Controls(Aircraft owner) {
		this.setFocusable(true);
		this.addMouseListener(this);
		
		this.owner = owner;
		this.maxAlt = (int) AircraftMath.round(this.owner.getMaxAlt(), Aircraft.ALT_INTERVAL);
		this.minAlt = this.owner.getMinAlt();
		this.maxSpd = (int) AircraftMath.round(this.owner.getMaxSpd(), Aircraft.SPD_INTERVAL);
		this.minSpd = this.owner.getMinSpd();
		this.altCount = ((this.maxAlt - this.minAlt) / Aircraft.ALT_INTERVAL);
		this.spdCount = ((this.maxSpd - this.minSpd) / Aircraft.SPD_INTERVAL);
		this.calculateConstants();
	}


	private void calculateConstants() {
		this.w = super.getBounds().width;
		this.h = super.getBounds().height;
		this.font = new Font("Courier New", Font.PLAIN, Math.min(this.w, this.h) / 20);
		this.compassSize = Math.min(this.h / 2, w);
		this.margin = this.compassSize / 10;
		this.compassSize -= this.margin * 2;
		this.compassOffsetX = (this.w - this.compassSize) / 2;
		this.compassOffsetY = this.margin;
		this.textOffsetY = this.compassSize + this.margin * 2 + this.strHeight;
		this.textHeight = (this.h - this.margin) - this.textOffsetY;
		this.directToOffsetX =
			this.compassOffsetX + (this.compassSize / 2) - (this.directToWidth / 2);
		this.directToOffsetY = this.compassOffsetY + (this.compassSize / 4);
	}
	

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(this.font);
		this.strHeight = g.getFontMetrics().getHeight();
		
		Color radarColor = Screen.RADAR_COLOR;
		Color lightRadarColor =
			new Color(radarColor.getRed(), radarColor.getGreen(), radarColor.getBlue(), 100);
		g.setColor(radarColor);

		
		// Draw compass
		int compassRadius = this.compassSize / 2;
		for (int hdg = 0; hdg < 360; hdg += Aircraft.HDG_INTERVAL) {
			// Prepare string with heading number
			String str = Integer.toString(hdg);
			if (hdg == 0)
				str = "360";
			str = String.format("%3s", str).replace(" ", "0");

			// Draw string in the correct place, using the unit circle. The x/y positions both
			// have the offset compassOffset_ + compassRadius which shifts to where the compass
			// should be drawn (that is compassOffset_) and then adds the radius which is the
			// origin of the unit circle operation.
			double rad = AircraftMath.hdgToRad(hdg);
			int x = (int) (compassRadius * Math.cos(rad));
			int y = (int) (compassRadius * Math.sin(rad));
			x = this.compassOffsetX + compassRadius + x;
			y = this.compassOffsetY + compassRadius - y;

			// Only draw every third heading number, otherwise draw a dot
			if (hdg % (Aircraft.HDG_INTERVAL * 3) == 0)
				g.drawString(str,
							 x - (g.getFontMetrics().stringWidth(str) / 2),
							 y + this.strHeight / 3);
			else
				g.drawOval(x, y, this.compassSize / 70, this.compassSize / 70);
		}

		// Draw compass arrow
		double rad = AircraftMath.hdgToRad(this.owner.getTargetHdg());
		Point arrowOrigin = new Point(this.compassOffsetX + compassRadius,
									  this.compassOffsetY + compassRadius);
		Point arrowTerminal = new Point((int) (compassRadius * Math.cos(rad)),
										(int) (compassRadius * Math.sin(rad)));
		g.drawLine(arrowOrigin.x, arrowOrigin.y,
				   arrowOrigin.x + arrowTerminal.x,
				   arrowOrigin.y - arrowTerminal.y);

		// Draw "direct to" button
		String directToStr = "Cleared " + this.owner.getTarget();
		this.directToWidth = g.getFontMetrics().stringWidth(directToStr);
		g.setColor(this.owner.isCleared() ? lightRadarColor : new Color(0, 0, 0, 100));
		g.fillRect(this.directToOffsetX, this.directToOffsetY, this.directToWidth, this.strHeight);
		g.setColor(radarColor);
		g.drawRect(this.directToOffsetX, this.directToOffsetY, this.directToWidth, this.strHeight);
		if (!this.owner.canBeCleared())
			g.setColor(radarColor.darker().darker());
		g.drawString(directToStr,
					 this.directToOffsetX,
					 this.directToOffsetY + (int) (this.strHeight * 0.8));
		

		// Draw airspeed and altitude spinners
		g.setColor(radarColor);
		
		int totalSpdHeight = this.strHeight * this.spdCount;
		int totalAltHeight = this.strHeight * this.altCount;

		int spdY = this.textOffsetY;
		int spdMargin = this.textHeight / this.spdCount;
		this.spdTextWidth = 0;
		for (int spd = maxSpd; spd >= minSpd; spd -= Aircraft.SPD_INTERVAL) {
			String str = Integer.toString(spd);
			// Text width update for click detection
			int textWidth = g.getFontMetrics().stringWidth(str);
			if (textWidth > this.spdTextWidth)
				this.spdTextWidth = textWidth;

			// Draw string and box if needed
			g.drawString(str, this.margin, spdY);
			if (spd == (int) this.owner.getTargetSpd())
				g.drawRect(this.margin, spdY - (int) (this.strHeight * 0.8),
						   textWidth, this.strHeight);
			spdY += spdMargin;
		}

		int altY = this.textOffsetY;
		int altMargin = this.textHeight / this.altCount;
		this.altTextWidth = 0;
		for (int alt = maxAlt; alt >= minAlt; alt -= Aircraft.ALT_INTERVAL) {
			String str = Integer.toString(alt);
			// Text width update for click detection
			int textWidth = g.getFontMetrics().stringWidth(str);
			if (textWidth > this.altTextWidth)
				this.altTextWidth = textWidth;

			// Draw background highlight if this is the altitude needed for approach clearance
			if (this.owner.getTarget().atAlt(alt, this.owner.getMaxAlt())) {
				g.setColor(lightRadarColor);
				g.fillRect(this.w - this.margin - textWidth,
						   altY - (int) (this.strHeight * 0.8),
						   textWidth, this.strHeight);
			}

			// Draw string and box if needed
			g.setColor(radarColor);
			g.drawString(str, this.w - this.margin - textWidth, altY);
			if (alt == (int) this.owner.getTargetAlt())
				g.drawRect(this.w - this.margin - textWidth,
						   altY - (int) (this.strHeight * 0.8),
						   textWidth, this.strHeight);
			altY += altMargin;
		}
	}


	@Override
	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		this.calculateConstants();
	}


	@Override
	public void mousePressed(MouseEvent e) {
		int clickX = e.getX();
		int clickY = e.getY();


		// "Direct to" travel
		if (clickX >= this.directToOffsetX && clickX <= this.directToOffsetX + this.directToWidth &&
			clickY >= this.directToOffsetY && clickY <= this.directToOffsetY + this.strHeight)
		{
			this.owner.toggleClearance();
			return;
		}

		
		// Compass heading update
		if (clickX > this.compassOffsetX && clickX < this.compassOffsetX + this.compassSize &&
			clickY > this.compassOffsetY && clickY < this.compassOffsetY + this.compassSize)
		{
			this.owner.cancelClearance();
			
			int compassCenterX = this.compassOffsetX + this.compassSize / 2;
			int compassCenterY = this.compassOffsetY + this.compassSize / 2;
			// Coordinate on the unit circle
			double x = clickX * 1.0 - compassCenterX * 1.0;
			double y = compassCenterY * 1.0 - clickY * 1.0;
			double r = Math.sqrt(x*x + y*y);
			double rad;
			if (x >= 0)
				rad = Math.asin(y / r);
			else
				rad = Math.PI - Math.asin(y / r);

			this.owner.setTargetHdg(AircraftMath.radToHdg(rad));
			return;
		}


		// Speed update
		int spdTextX = this.margin;
		int spdTextY = this.textOffsetY - this.strHeight;
		int spdTextW = this.spdTextWidth;
		int spdTextH = this.textHeight + this.strHeight;
		if (clickX >= spdTextX && clickX <= spdTextX + spdTextW &&
			clickY >= spdTextY && clickY <= spdTextY + spdTextH)
		{
			int spdClickY = clickY - spdTextY;
			int spdTextMargin = this.textHeight / this.spdCount;
			int boxClicked = spdClickY / spdTextMargin;
			int distFromMax = (this.spdCount - boxClicked);
			int spd = this.minSpd + (Aircraft.SPD_INTERVAL * distFromMax);
			this.owner.setTargetSpd(spd);
			return;
		}


		// Altitude update
		int altTextX = this.w - this.margin - this.altTextWidth;
		int altTextY = this.textOffsetY - this.strHeight;
		int altTextW = this.altTextWidth;
		int altTextH = this.textHeight + this.strHeight;
		if (clickX >= altTextX && clickX <= altTextX + altTextW &&
			clickY >= altTextY && clickY <= altTextY + altTextH)
		{
			int altClickY = clickY - altTextY;
			int altTextMargin = this.textHeight / this.altCount;
			int boxClicked = altClickY / altTextMargin;
			int distFromMax = (this.altCount - boxClicked);
			int alt = this.minAlt + (Aircraft.ALT_INTERVAL * distFromMax);
			this.owner.setTargetAlt(alt);
			return;
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
