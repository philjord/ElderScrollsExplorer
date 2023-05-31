package scrollsexplorer.simpleclient.settings;

import javax.swing.JPanel;

import tools.clock.PeriodicThread;
import tools.clock.PeriodicallyUpdated;
import tools.swing.VerticalFlowLayout;

/**
 * @author Administrator
 *
 */
public class MemoryStatusPanel extends JPanel {
	private long			MIN_TIME_BETWEEN_STEPS_MS	= 1000;

	private PeriodicThread	updateThread;

	public MemoryStatusPanel() {
		updateThread = new PeriodicThread("Physics System Status Thread", MIN_TIME_BETWEEN_STEPS_MS,
				new PeriodicallyUpdated() {
					@Override
					public void runUpdate() {
						try {
							update();
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println(
									"PhysicsSystemStatus.update() exception " + e + " " + e.getStackTrace() [0]);
						}
					}

				});
		updateThread.start();

		this.setLayout(new VerticalFlowLayout());
		//add(dynL);
		//add(kinL);
		//add(staL);
		doLayout();
		repaint();
	}

	private void update() {
		/*PhysicsStatus ps = physicsSystem.getPhysicsStatus();
		if (ps != null) {
			dynL.setText("D " + ps.dynCount);
			kinL.setText("K " + ps.kinCount);
			staL.setText("S " + ps.staCount);
			doLayout();
			repaint();
		}*/

	}

}
