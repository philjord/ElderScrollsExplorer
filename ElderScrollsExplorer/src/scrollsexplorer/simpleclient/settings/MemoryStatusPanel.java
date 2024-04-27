package scrollsexplorer.simpleclient.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import esmj3d.j3d.cell.J3dCELLGeneral;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
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
	
	private JCheckBox pauseLoading = new JCheckBox("Pause Laoding", false);

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
		add(pauseLoading);
		pauseLoading.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				J3dCELLGeneral.PAUSE_CELL_LOADING = pauseLoading.isSelected();					
			}
		});
		
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
