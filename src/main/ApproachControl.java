import javax.swing.JFrame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Dimension;


/*
  TO-DO

  - Game loop
    - Adding new aircraft to fill the buffer
  - Incursion detection: < 3 nm horizontal, < 1000 ft vertical
  - Airports
    - Finish KFJK (waypoints, maybe more runways)
    - More options
  - Home/main screen (?)
  - In-file documentation
 */


/*
  References
  - https://www.youtube.com/watch?v=He6PDSR4qFk&t=30s&ab_channel=PlasticPilot
  - channel APPcalyptus
  - https://twitter.com/APPControl
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
