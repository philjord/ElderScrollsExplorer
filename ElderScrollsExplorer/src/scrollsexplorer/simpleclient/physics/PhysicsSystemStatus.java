package scrollsexplorer.simpleclient.physics;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import scrollsexplorer.simpleclient.physics.PhysicsDynamics.PhysicsStatus;
import tools.clock.PeriodicThread;
import tools.clock.PeriodicallyUpdated;

public class PhysicsSystemStatus extends JPanel
{
	private PhysicsSystem physicsSystem;

	private long MIN_TIME_BETWEEN_STEPS_MS = 1000;

	private PeriodicThread updateThread;

	private JLabel dynL = new JLabel("D000");

	private JLabel kinL = new JLabel("K000");

	private JLabel staL = new JLabel("S000");

	/*Write a timed thread that updates a multiline label in the dashboard that out put the dynamics counts of 
	D#
	K#
	S#
	And average step stime
	Make it so the controller actions fire but step simulation can be stopped
	*/
	public PhysicsSystemStatus(PhysicsSystem physicsSystem)
	{
		this.physicsSystem = physicsSystem;
		updateThread = new PeriodicThread("Physics System Status Thread", MIN_TIME_BETWEEN_STEPS_MS, new PeriodicallyUpdated()
		{
			public void runUpdate()
			{
				try
				{
					update();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("PhysicsSystemStatus.update() exception "+ e + " " + e.getStackTrace()[0]);
				}
			}

		});
		updateThread.start();

		this.setLayout(new GridLayout(3, 1));
		add(dynL);
		add(kinL);
		add(staL);
		doLayout();
		repaint();
	}

	private void update()
	{
		PhysicsStatus ps = physicsSystem.getPhysicsStatus();
		if (ps != null)
		{
			dynL.setText("D" + ps.dynCount);
			kinL.setText("K" + ps.kinCount);
			staL.setText("S" + ps.staCount);
			doLayout();
			repaint();
		}

	}

}
