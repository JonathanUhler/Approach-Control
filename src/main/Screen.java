import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;


public class Screen extends JPanel {

	public static final Color RADAR_COLOR = new Color(130, 240, 70);
	public static final int FRAME_RATE = 30;

	
	// Game settings and components
	private JSpinner gameSpeedSpinner;
	private JButton toggleSepRingsButton;
	private static int gameSpeed = 1;
	private static boolean showSepRings = false;
	

	// Display information
	private int screenWidth;
	private int screenHeight;
	private int margin;
	private final int settingsMargin = 30;
	private int effectiveScreenWidth;
	private int effectiveScreenHeight;
	private int radarOffsetX;
	private int radarOffsetY;
	private int radarWidth;
	private int radarHeight;
	private int controlsOffsetX;
	private int controlsOffsetY;
	private int controlsWidth;
	private int controlsHeight;

	// World/game information
	private Airport airport;
	private Controls controls;
	

	public Screen() {
		// Constants
		this.screenWidth = 900;
		this.screenHeight = 600 + this.settingsMargin;
		this.calculateConstants();

		// Components
		this.gameSpeedSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
		this.toggleSepRingsButton = new JButton("Toggle Sep Rings");

		this.gameSpeedSpinner.addChangeListener(e -> Screen.gameSpeed = (Integer) this.gameSpeedSpinner.getValue());
		this.toggleSepRingsButton.addActionListener(e -> Screen.showSepRings = !Screen.showSepRings);

		this.add(new JLabel("Game Speed: "));
		this.add(this.gameSpeedSpinner);
		this.add(this.toggleSepRingsButton);

		// Game objects
		this.controls = null;

		// TEST CODE
		this.airport = new Airport(Airport.Code.KJFK);
		this.add(this.airport);
	}


	private void calculateConstants() {
		this.margin = (int) (Math.min(this.screenWidth, this.screenHeight) * 0.02);
		this.effectiveScreenWidth = this.screenWidth - this.margin * 2;
		this.effectiveScreenHeight = this.screenHeight - this.margin * 2 - this.settingsMargin;
		this.radarOffsetX = this.margin;
		this.radarOffsetY = this.margin + this.settingsMargin;
		this.radarWidth = (int) (this.effectiveScreenWidth * 0.6);
		this.radarHeight = this.effectiveScreenHeight;
		this.controlsOffsetX = this.radarOffsetX + this.radarWidth + this.margin;
		this.controlsOffsetY = this.margin + this.settingsMargin;
		this.controlsWidth = this.effectiveScreenWidth - this.radarWidth - this.margin;
		this.controlsHeight = this.effectiveScreenHeight;
	}


	public static int gameSpeed() {
		return Screen.gameSpeed;
	}


	public static boolean showSepRings() {
		return Screen.showSepRings;
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Background
		this.setBackground(new Color(150, 150, 150));
		g.fillRect(this.margin, this.margin + this.settingsMargin,
				   this.effectiveScreenWidth, this.effectiveScreenHeight);
		g.setColor(new Color(150, 150, 150));
		g.fillRect(this.radarOffsetX + this.radarWidth, this.settingsMargin, this.margin, this.screenHeight);

		// Controls
		if (this.controls != null)
			this.controls.setBounds(this.controlsOffsetX, this.controlsOffsetY,
									this.controlsWidth, this.controlsHeight);

		// TEST CODE
		this.airport.setBounds(this.radarOffsetX, this.radarOffsetY, this.radarWidth, this.radarHeight);
	}


	public void update() {
		while (true) {
			long startMillis = System.currentTimeMillis();

			// Update the displayed controls
			Aircraft selected = this.airport.getSelected();
			if (selected == null && this.controls != null) {
				this.remove(this.controls);
				this.controls = null;
			}
			else if (selected != null) {
				Controls controls = selected.getControls();
				if (this.controls != null)
					this.remove(this.controls);
				this.controls = controls;
				this.add(this.controls);
			}

			this.repaint();
			
			long endMillis = System.currentTimeMillis();

			// Wait for enough time to have the desired frame rate
			long deltaMillis = endMillis - startMillis;
			long baseMillisPerFrame = 1000 / Screen.FRAME_RATE;
			try {
				Thread.sleep(Math.max(0, baseMillisPerFrame - deltaMillis));
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				e.printStackTrace();
			}
		}
	}


	@Override
	public Dimension getPreferredSize() {
		return new Dimension(this.screenWidth, this.screenHeight);
	}


	@Override
	public void setPreferredSize(Dimension d) {
		super.setPreferredSize(new Dimension(d.width, (int)(d.width * 0.6)));
		this.screenWidth = d.width;
		this.screenHeight = d.height;
		this.calculateConstants();
	}

}
