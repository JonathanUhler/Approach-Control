import javax.swing.JFrame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Dimension;


/*
  TO-DO

  - Waypoint altitudes: must be at that altitude to clear "direct to"
  - Game loop
    - Adding new aircraft to fill the buffer
	- Removing aircraft at their goal
  - Incursion detection: < 3 nm horizontal, < 1000 ft vertical
  - Quality of life: more airport options
  - In-file documentation
 */


public class ApproachControl {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Approach Control");
		Screen screen = new Screen();

		frame.addComponentListener(new ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					if (e.getComponent().equals(frame)) {
						Dimension d = frame.getContentPane().getSize();
						screen.setPreferredSize(d);
					}
				}
			});

		frame.add(screen);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

		screen.update();
	}

}
