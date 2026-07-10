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
public class GraphicsSettingsPanel extends JPanel implements BethRenderSettings.UpdateListener {
	private JCheckBox	showPhysicTick				= new JCheckBox("Show Physics", BethRenderSettings.isShowPhysic());

	private JCheckBox	showPathGridTick			= new JCheckBox("Show Path Grid",
			BethRenderSettings.isShowPathGrid());

	private JCheckBox	isFogEnabledTick			= new JCheckBox("Fog Enabled", BethRenderSettings.isFogEnabled());

	private JCheckBox	showEditorMarkers			= new JCheckBox("Show Editor Markers",
			BethRenderSettings.isShowEditorMarkers());

	private JCheckBox	showDistantBuildingsTick	= new JCheckBox("Distant Buildings",
			BethRenderSettings.isShowDistantBuildings());

	private JCheckBox	showDistantTreesTick		= new JCheckBox("Distant Trees",
			BethRenderSettings.isShowDistantTrees());

	private JCheckBox	flipParentEnableDefaultTick	= new JCheckBox("Flip Parent Enable Default",
			BethRenderSettings.isFlipParentEnableDefault());

	public GraphicsSettingsPanel() {
		this.setLayout(new VerticalFlowLayout());

		add(showPhysicTick);
		showPhysicTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(GraphicsSettingsPanel.this);
				BethRenderSettings.setShowPhysics(showPhysicTick.isSelected());
				BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
			}
		});

		add(showPathGridTick);
		showPathGridTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(GraphicsSettingsPanel.this);
				BethRenderSettings.setShowPathGrid(showPathGridTick.isSelected());
				BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
			}
		});

		add(isFogEnabledTick);
		isFogEnabledTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(GraphicsSettingsPanel.this);
				BethRenderSettings.setFogEnabled(isFogEnabledTick.isSelected());
				BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
			}
		});

		add(showEditorMarkers);
		showEditorMarkers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(GraphicsSettingsPanel.this);
				BethRenderSettings.setShowEditorMarkers(showEditorMarkers.isSelected());
				BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
			}
		});

		add(showDistantBuildingsTick);
		showDistantBuildingsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(GraphicsSettingsPanel.this);
				BethRenderSettings.setShowDistantBuildings(showDistantBuildingsTick.isSelected());
				BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
			}
		});

		add(showDistantTreesTick);
		showDistantTreesTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(GraphicsSettingsPanel.this);
				BethRenderSettings.setShowDistantTrees(showDistantTreesTick.isSelected());
				BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
			}
		});

		add(flipParentEnableDefaultTick);
		flipParentEnableDefaultTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(GraphicsSettingsPanel.this);
				BethRenderSettings.setFlipParentEnableDefault(flipParentEnableDefaultTick.isSelected());
				BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
			}
		});
		BethRenderSettings.addUpdateListener(GraphicsSettingsPanel.this);
	}

	@Override
	public void renderSettingsUpdated() {
		// Visually update when the rendersetting are touch programmatically
		showPhysicTick.setSelected(BethRenderSettings.isShowPhysic());
		showPathGridTick.setSelected(BethRenderSettings.isShowPathGrid());
		isFogEnabledTick.setSelected(BethRenderSettings.isFogEnabled());
		showEditorMarkers.setSelected(BethRenderSettings.isShowEditorMarkers());
		showDistantBuildingsTick.setSelected(BethRenderSettings.isShowDistantBuildings());
		showDistantTreesTick.setSelected(BethRenderSettings.isShowDistantTrees());
		flipParentEnableDefaultTick.setSelected(BethRenderSettings.isFlipParentEnableDefault());
	}

}
