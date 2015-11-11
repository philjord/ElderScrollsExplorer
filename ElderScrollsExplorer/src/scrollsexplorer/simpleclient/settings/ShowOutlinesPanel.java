/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import scrollsexplorer.simpleclient.SimpleWalkSetup;
import tools.swing.VerticalFlowLayout;
import esmj3d.j3d.BethRenderSettings;

/**
 * @author Administrator
 *
 */
public class ShowOutlinesPanel extends JPanel
{
	private JSlider globalAmbLightLevel = new JSlider(0, 100, (int) (BethRenderSettings.getGlobalAmbLightLevel() * 100));

	private JSlider globalDirLightLevel = new JSlider(0, 100, (int) (BethRenderSettings.getGlobalDirLightLevel() * 100));

	private JCheckBox enablePlacedLights = new JCheckBox("Enable Placed Lights", BethRenderSettings.isEnablePlacedLights());

	private JCheckBox outlineCharsTick = new JCheckBox("Outline Characters", BethRenderSettings.isOutlineChars());

	private JCheckBox outlineDoorsTick = new JCheckBox("Outline Doors", BethRenderSettings.isOutlineDoors());

	private JCheckBox outlineContsTick = new JCheckBox("Outline Containers", BethRenderSettings.isOutlineConts());

	private JCheckBox outlinePartsTick = new JCheckBox("Outline Particles", BethRenderSettings.isOutlineParts());

	private JCheckBox outlineFocusedTick = new JCheckBox("Outline Focused Object", BethRenderSettings.isOutlineParts());

	protected SimpleWalkSetup simpleWalkSetup;

	public ShowOutlinesPanel(SimpleWalkSetup _simpleWalkSetup)
	{
		this.simpleWalkSetup = _simpleWalkSetup;
		//this.setLayout(new GridLayout2(-1, 3));
		this.setLayout(new VerticalFlowLayout());

		globalAmbLightLevel.setBorder(new TitledBorder("globalAmbLightLevel"));
		globalAmbLightLevel.setMajorTickSpacing(25);
		globalAmbLightLevel.setPaintTicks(true);
		globalAmbLightLevel.setPaintLabels(true);
		add(globalAmbLightLevel);
		globalAmbLightLevel.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setGlobalAmbLightLevel(globalAmbLightLevel.getValue() / 100f);
				simpleWalkSetup.setGlobalAmbLightLevel(globalAmbLightLevel.getValue() / 100f);
			}
		});

		globalDirLightLevel.setBorder(new TitledBorder("globalDirLightLevel"));
		globalDirLightLevel.setMajorTickSpacing(25);
		globalDirLightLevel.setPaintTicks(true);
		globalDirLightLevel.setPaintLabels(true);
		add(globalDirLightLevel);
		globalDirLightLevel.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setGlobalDirLightLevel(globalDirLightLevel.getValue() / 100f);
				simpleWalkSetup.setGlobalDirLightLevel(globalDirLightLevel.getValue() / 100f);
			}
		});

		add(enablePlacedLights);
		enablePlacedLights.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setEnablePlacedLights(enablePlacedLights.isSelected());
			}
		});

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

		add(outlineFocusedTick);
		outlineFocusedTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setOutlineFocused(outlineFocusedTick.isSelected());
			}
		});
	}

}
