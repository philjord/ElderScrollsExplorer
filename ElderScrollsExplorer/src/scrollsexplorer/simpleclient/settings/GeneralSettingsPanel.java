/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import scrollsexplorer.ScrollsExplorer;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools.swing.VerticalFlowLayout;

/**
 * @author Administrator
 *
 */
public class GeneralSettingsPanel extends JPanel
{
	private JCheckBox autoLoadLastCell = new JCheckBox("Auto Load Last Cell", true);

	private JCheckBox enablePhysicsTick = new JCheckBox("Enable Physics", true);
	
	private JCheckBox slowPhysicsTick = new JCheckBox("Slow Physics", false);

	private ScrollsExplorer scrollsExplorer;

	public GeneralSettingsPanel(ScrollsExplorer _scrollsExplorer)
	{
		this.scrollsExplorer = _scrollsExplorer;
		//this.setLayout(new GridLayout2(-1, 3));
		this.setLayout(new VerticalFlowLayout());

		add(enablePhysicsTick);
		enablePhysicsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				scrollsExplorer.getSimpleWalkSetup().setPhysicsEnabled(enablePhysicsTick.isSelected());
			}
		});
		
		add(slowPhysicsTick);
		slowPhysicsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				PhysicsSystem.DEBUG_UPDATE_STEP_SKIP = slowPhysicsTick.isSelected() ? 200 : 1;					
			}
		});

		add(autoLoadLastCell);
		autoLoadLastCell.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				scrollsExplorer.setAutoLoadStartCell(autoLoadLastCell.isSelected());
			}
		});

	}

}
