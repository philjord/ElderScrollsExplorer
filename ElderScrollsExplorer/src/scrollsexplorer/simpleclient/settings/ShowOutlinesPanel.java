/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import esmj3d.j3d.BethRenderSettings;
import scrollsexplorer.simpleclient.BethWorldVisualBranch;
import tools.swing.VerticalFlowLayout;

/**
 * @author Administrator
 *
 */
public class ShowOutlinesPanel extends JPanel implements BethRenderSettings.UpdateListener {
	private JCheckBox	outlineLightsTick		= new JCheckBox("Outline Lights", BethRenderSettings.isOutlineLights());

	private JCheckBox	outlineCharsTick		= new JCheckBox("Outline Characters",
			BethRenderSettings.isOutlineChars());

	private JCheckBox	outlineDoorsTick		= new JCheckBox("Outline Doors", BethRenderSettings.isOutlineDoors());

	private JCheckBox	outlineContsTick		= new JCheckBox("Outline Containers",
			BethRenderSettings.isOutlineConts());

	private JCheckBox	outlinePartsTick		= new JCheckBox("Outline Particles",
			BethRenderSettings.isOutlineParts());

	private JCheckBox	outlineFocusedTick		= new JCheckBox("Outline Focused Object",
			BethRenderSettings.isOutlineFocused());

	private JCheckBox	showDebugCellGridTick	= new JCheckBox("Show Debug Cell Grid", false);

	public ShowOutlinesPanel() {
		this.setLayout(new VerticalFlowLayout());

		add(outlineLightsTick);
		outlineLightsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(ShowOutlinesPanel.this);
				BethRenderSettings.setOutlineLights(outlineLightsTick.isSelected());
				BethRenderSettings.addUpdateListener(ShowOutlinesPanel.this);
			}
		});

		add(outlineCharsTick);
		outlineCharsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(ShowOutlinesPanel.this);
				BethRenderSettings.setOutlineChars(outlineCharsTick.isSelected());
				BethRenderSettings.addUpdateListener(ShowOutlinesPanel.this);
			}
		});

		add(outlineDoorsTick);
		outlineDoorsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(ShowOutlinesPanel.this);
				BethRenderSettings.setOutlineDoors(outlineDoorsTick.isSelected());
				BethRenderSettings.addUpdateListener(ShowOutlinesPanel.this);
			}
		});

		add(outlineContsTick);
		outlineContsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(ShowOutlinesPanel.this);
				BethRenderSettings.setOutlineConts(outlineContsTick.isSelected());
				BethRenderSettings.addUpdateListener(ShowOutlinesPanel.this);
			}
		});

		add(outlinePartsTick);
		outlinePartsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(ShowOutlinesPanel.this);
				BethRenderSettings.setOutlineParts(outlinePartsTick.isSelected());
				BethRenderSettings.addUpdateListener(ShowOutlinesPanel.this);
			}
		});

		add(outlineFocusedTick);
		outlineFocusedTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(ShowOutlinesPanel.this);
				BethRenderSettings.setOutlineFocused(outlineFocusedTick.isSelected());
				BethRenderSettings.addUpdateListener(ShowOutlinesPanel.this);
			}
		});

		add(showDebugCellGridTick);
		showDebugCellGridTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//TODO: why not art of render settings?
				BethWorldVisualBranch.SHOW_DEBUG_MAKERS = showDebugCellGridTick.isSelected();
			}
		});

		BethRenderSettings.addUpdateListener(ShowOutlinesPanel.this);
	}

	@Override
	public void renderSettingsUpdated() {
		outlineLightsTick.setSelected(BethRenderSettings.isOutlineLights());
		outlineCharsTick.setSelected(BethRenderSettings.isOutlineChars());
		outlineDoorsTick.setSelected(BethRenderSettings.isOutlineDoors());
		outlineContsTick.setSelected(BethRenderSettings.isOutlineConts());
		outlinePartsTick.setSelected(BethRenderSettings.isOutlineParts());
		outlineFocusedTick.setSelected(BethRenderSettings.isOutlineFocused());

		//TODO: why not art of render settigns?
		//showDebugCellGridTick.setSelected( BethRenderSettings.is);

	}

}
