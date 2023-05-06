import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Font;


public class Screen extends JPanel {

	public static final Color RADAR_COLOR = new Color(130, 240, 70);
	public static final int FRAME_RATE = 30;


	// Main menu components
	private JComboBox<Airport.Code> airportComboBox;
	private JButton playButton;
	
	
	// Game settings and components
	private JSpinner gameSpeedSpinner;
	private JButton toggleSepRingsButton;
	private JLabel scoreLabel;
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

		// Main menu components
		this.airportComboBox = new JComboBox<>(new Airport.Code[] {Airport.Code.KJFK,
																   Airport.Code.KSFO,
																   Airport.Code.EGLL,
																   Airport.Code.KPAO});
		this.playButton = new JButton("Start");

		this.playButton.addActionListener(e -> {
				this.airport = new Airport((Airport.Code) this.airportComboBox.getSelectedItem());
				this.displayGame();
			});

		// Game components
		this.gameSpeedSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 10, 1));
		this.toggleSepRingsButton = new JButton("Toggle Sep Rings");
		this.scoreLabel = new JLabel();

		this.gameSpeedSpinner.addChangeListener(e -> Screen.gameSpeed =
												(Integer) this.gameSpeedSpinner.getValue());
		this.toggleSepRingsButton.addActionListener(e ->
													Screen.showSepRings = !Screen.showSepRings);

		// Game objects
		this.controls = null;

		// Display main menu
		this.displayMain();
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


	private void clear() {
		this.removeAll();
		this.revalidate();
		this.repaint();
	}


	private void displayMain() {
		this.clear();

		this.add(this.airportComboBox);
		this.add(this.playButton);
	}


	private void displayGame() {
		this.clear();

		this.add(this.airport);
		this.add(this.scoreLabel);
		this.add(new JLabel("          Game Speed: "));
		this.add(this.gameSpeedSpinner);
		this.add(this.toggleSepRingsButton);
	}


	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Background
		this.setBackground(new Color(150, 150, 150));
		g.fillRect(this.margin, this.margin + this.settingsMargin,
				   this.effectiveScreenWidth, this.effectiveScreenHeight);

		// Draw main menu
		if (this.airport == null) {
			// Grid background
			g.setColor(Screen.RADAR_COLOR.darker().darker().darker());
			for (int i = this.margin;
				 i <= this.effectiveScreenWidth + this.margin;
				 i += this.margin)
			{
				g.drawLine(i, this.margin + this.settingsMargin,
						   i, this.margin + this.settingsMargin + this.effectiveScreenHeight);
			}
			for (int i = this.margin;
				 i <= this.effectiveScreenHeight + this.margin;
				 i += this.margin)
			{
				g.drawLine(this.margin, i + this.settingsMargin,
						   this.margin + this.effectiveScreenWidth, i + this.settingsMargin);
			}

			// Title
			g.setFont(new Font("Courier New", Font.BOLD,
							   Math.min(this.screenWidth, this.screenHeight) / 10));
			g.setColor(Screen.RADAR_COLOR);
			String titleStr = "Approach Control";
			int titleW = g.getFontMetrics().stringWidth(titleStr);
			int titleX = (this.screenWidth - titleW) / 2;
			g.drawString(titleStr, titleX, this.screenHeight / 2);
		}
		// Draw game
		else {
			// Background
			g.setColor(new Color(150, 150, 150));
			g.fillRect(this.radarOffsetX + this.radarWidth,
					   this.settingsMargin, this.margin, this.screenHeight);
			
			// Controls
			if (this.controls != null)
				this.controls.setBounds(this.controlsOffsetX, this.controlsOffsetY,
										this.controlsWidth, this.controlsHeight);
			
			// Update score
			this.scoreLabel.setText("Flights: " + this.airport.getFlights());
			
			// Update airport size as needed
			this.airport.setBounds(this.radarOffsetX, this.radarOffsetY,
								   this.radarWidth, this.radarHeight);
			
			// Game over screen
			if (this.airport.hasConflict()) {
				Screen.gameSpeed = 0;
				g.setColor(new Color(255, 0, 0));
				g.setFont(new Font("Courier New", Font.BOLD,
								   Math.min(this.radarWidth, this.radarHeight) / 10));
				
				String conflictStr = "Conflict!";
				int conflictStrW = g.getFontMetrics().stringWidth(conflictStr);
				int conflictStrX = this.radarOffsetX + (this.radarWidth - conflictStrW) / 2;
				g.drawString(conflictStr, conflictStrX, this.radarOffsetY + this.radarHeight / 2);
			}
		}
	}


	public void update() {
		while (true) {			
			long startMillis = System.currentTimeMillis();

			// Update the displayed controls
			if (this.airport != null) {
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
					if (this.controls != null)
						this.add(this.controls);
				}
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
