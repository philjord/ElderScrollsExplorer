/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import esmj3d.j3d.BethRenderSettings;
import tools.swing.VerticalFlowLayout;

/**
 * @author Administrator
 *
 */
public class GraphicsSettingsPanel extends JPanel
{
	private JCheckBox showPhysicTick = new JCheckBox("Show Physics", BethRenderSettings.isShowPhysic());

	private JCheckBox showPathGridTick = new JCheckBox("Show Path Grid", BethRenderSettings.isShowPathGrid());

	private JCheckBox isFogEnabledTick = new JCheckBox("Fog Enabled", BethRenderSettings.isFogEnabled());

	private JCheckBox showEditorMarkers = new JCheckBox("Show Editor Markers", BethRenderSettings.isShowEditorMarkers());

	private JCheckBox showDistantBuildingsTick = new JCheckBox("Distant Buildings", BethRenderSettings.isShowDistantBuildings());

	private JCheckBox showDistantTreesTick = new JCheckBox("Distant Trees", BethRenderSettings.isShowDistantTrees());

	private JCheckBox flipParentEnableDefaultTick = new JCheckBox("Flip Parent Enable Default",
			BethRenderSettings.isFlipParentEnableDefault());

	public GraphicsSettingsPanel()
	{
		this.setLayout(new VerticalFlowLayout());

		add(showPhysicTick);
		showPhysicTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowPhysics(showPhysicTick.isSelected());
			}
		});

		add(showPathGridTick);
		showPathGridTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowPathGrid(showPathGridTick.isSelected());
			}
		});

		add(isFogEnabledTick);
		isFogEnabledTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setFogEnabled(isFogEnabledTick.isSelected());
			}
		});

		add(showEditorMarkers);
		showEditorMarkers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowEditorMarkers(showEditorMarkers.isSelected());
			}
		});

		add(showDistantBuildingsTick);
		showDistantBuildingsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowDistantBuildings(showDistantBuildingsTick.isSelected());
			}
		});

		add(showDistantTreesTick);
		showDistantTreesTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowDistantTrees(showDistantTreesTick.isSelected());
			}
		});

		add(flipParentEnableDefaultTick);
		flipParentEnableDefaultTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setFlipParentEnableDefault(flipParentEnableDefaultTick.isSelected());
			}
		});

	}

}
