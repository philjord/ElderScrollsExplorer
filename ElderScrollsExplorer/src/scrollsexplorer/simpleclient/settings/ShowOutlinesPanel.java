/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import scrollsexplorer.simpleclient.SimpleWalkSetup;
import tools.swing.VerticalFlowLayout;
import esmj3d.j3d.BethRenderSettings;

/**
 * @author Administrator
 *
 */
public class ShowOutlinesPanel extends JPanel
{
	private JCheckBox outlineCharsTick = new JCheckBox("Outline Characters", BethRenderSettings.isOutlineChars());

	private JCheckBox outlineDoorsTick = new JCheckBox("Outline Doors", BethRenderSettings.isOutlineDoors());

	private JCheckBox outlineContsTick = new JCheckBox("Outline Containers", BethRenderSettings.isOutlineConts());

	private JCheckBox outlinePartsTick = new JCheckBox("Outline Particles", BethRenderSettings.isOutlineParts());

	protected SimpleWalkSetup simpleWalkSetup;

	public ShowOutlinesPanel(SimpleWalkSetup _simpleWalkSetup)
	{
		this.simpleWalkSetup = _simpleWalkSetup;
		//this.setLayout(new GridLayout2(-1, 3));
		this.setLayout(new VerticalFlowLayout());

		add(outlineCharsTick);
		outlineCharsTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setOutlineChars(outlineCharsTick.isSelected());
			}
		});

		add(outlineDoorsTick);
		outlineDoorsTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setOutlineDoors(outlineDoorsTick.isSelected());
			}
		});

		add(outlineContsTick);
		outlineContsTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setOutlineConts(outlineContsTick.isSelected());
			}
		});

		add(outlinePartsTick);
		outlinePartsTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setOutlineParts(outlinePartsTick.isSelected());
			}
		});
	}

}
