/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
public class GraphicsSettingsPanel extends JPanel
{
	private JButton lowSettings = new JButton("Low");

	private JButton medSettings = new JButton("Med");

	private JButton highSettings = new JButton("High");

	private JSlider FAR_LOAD_DISTSlider = new JSlider(0, 64, BethRenderSettings.getFarLoadGridCount());

	private JSlider NEAR_LOAD_DISTSlider = new JSlider(0, 16, BethRenderSettings.getNearLoadGridCount());

	private JSlider LOD_LOAD_DIST_MAXSlider = new JSlider(0, 192, BethRenderSettings.getLOD_LOAD_DIST_MAX());

	private JSlider actorFade = new JSlider(0, BethRenderSettings.ACTOR_FADE_MAX, BethRenderSettings.ACTOR_FADE_DEFAULT);

	private JSlider itemFade = new JSlider(0, BethRenderSettings.ITEM_FADE_MAX, BethRenderSettings.ITEM_FADE_DEFAULT);

	private JSlider objectFade = new JSlider(0, BethRenderSettings.OBJECT_FADE_MAX, BethRenderSettings.OBJECT_FADE_DEFAULT);

	private JCheckBox showPhysicTick = new JCheckBox("Show Physics", BethRenderSettings.isShowPhysic());

	private JCheckBox showEditorMarkers = new JCheckBox("Show Editor Markers", BethRenderSettings.isShowEditorMarkers());

	private JCheckBox showDistantBuildingsTick = new JCheckBox("Distant Buildings", BethRenderSettings.isShowDistantBuildings());

	private JCheckBox showDistantTreesTick = new JCheckBox("Distant Trees", BethRenderSettings.isShowDistantTrees());

	private JCheckBox enablePhysicsTick = new JCheckBox("Enable Physics", true);

	private SimpleWalkSetup simpleWalkSetup;

	public GraphicsSettingsPanel(SimpleWalkSetup _simpleWalkSetup)
	{
		this.simpleWalkSetup = _simpleWalkSetup;
		//this.setLayout(new GridLayout2(-1, 3));
		this.setLayout(new VerticalFlowLayout());

		ButtonGroup setG = new ButtonGroup();
		setG.add(lowSettings);
		setG.add(medSettings);
		setG.add(highSettings);
		JPanel bpan = new JPanel();
		bpan.setLayout(new FlowLayout());
		bpan.add(lowSettings);
		bpan.add(medSettings);
		bpan.add(highSettings);
		add(bpan);

		lowSettings.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("low");
				NEAR_LOAD_DISTSlider.setValue(1);
				FAR_LOAD_DISTSlider.setValue(2);
				actorFade.setValue(25);
				itemFade.setValue(50);
				objectFade.setValue(50);
			}
		});
		medSettings.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("med");
				NEAR_LOAD_DISTSlider.setValue(2);
				FAR_LOAD_DISTSlider.setValue(4);
				actorFade.setValue(50);
				itemFade.setValue(100);
				objectFade.setValue(100);
			}
		});
		highSettings.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("high");
				NEAR_LOAD_DISTSlider.setValue(4);
				FAR_LOAD_DISTSlider.setValue(8);
				actorFade.setValue(100);
				itemFade.setValue(200);
				objectFade.setValue(200);
			}
		});

		FAR_LOAD_DISTSlider.setBorder(new TitledBorder("Far Load Grids"));
		FAR_LOAD_DISTSlider.setMajorTickSpacing(8);
		FAR_LOAD_DISTSlider.setPaintTicks(true);
		FAR_LOAD_DISTSlider.setPaintLabels(true);
		add(FAR_LOAD_DISTSlider);
		FAR_LOAD_DISTSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setFarLoadGridCount(FAR_LOAD_DISTSlider.getValue());
			}
		});

		NEAR_LOAD_DISTSlider.setBorder(new TitledBorder("Near Load Grids"));
		NEAR_LOAD_DISTSlider.setMajorTickSpacing(2);
		NEAR_LOAD_DISTSlider.setPaintTicks(true);
		NEAR_LOAD_DISTSlider.setPaintLabels(true);
		add(NEAR_LOAD_DISTSlider);
		NEAR_LOAD_DISTSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setNearLoadGridCount(NEAR_LOAD_DISTSlider.getValue());
			}
		});

		LOD_LOAD_DIST_MAXSlider.setBorder(new TitledBorder("LOD_LOAD_DIST_MAX"));
		LOD_LOAD_DIST_MAXSlider.setMajorTickSpacing(32);
		LOD_LOAD_DIST_MAXSlider.setPaintTicks(true);
		LOD_LOAD_DIST_MAXSlider.setPaintLabels(true);
		add(LOD_LOAD_DIST_MAXSlider);
		LOD_LOAD_DIST_MAXSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setLOD_LOAD_DIST_MAX(LOD_LOAD_DIST_MAXSlider.getValue());
			}
		});

		actorFade.setBorder(new TitledBorder("Actor Fade"));
		actorFade.setMajorTickSpacing(BethRenderSettings.ACTOR_FADE_MAX / 5);
		actorFade.setPaintTicks(true);
		actorFade.setPaintLabels(true);
		add(actorFade);
		actorFade.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setActorFade(actorFade.getValue());
			}
		});
		itemFade.setBorder(new TitledBorder("Item Fade"));
		itemFade.setMajorTickSpacing(BethRenderSettings.ITEM_FADE_MAX / 5);
		itemFade.setPaintTicks(true);
		itemFade.setPaintLabels(true);
		add(itemFade);
		itemFade.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setItemFade(itemFade.getValue());
			}
		});
		objectFade.setBorder(new TitledBorder("Object Fade"));
		objectFade.setMajorTickSpacing(BethRenderSettings.OBJECT_FADE_MAX / 5);
		objectFade.setPaintTicks(true);
		objectFade.setPaintLabels(true);
		add(objectFade);
		objectFade.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setObjectFade(objectFade.getValue());
			}
		});

		add(showPhysicTick);
		showPhysicTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowPhysics(showPhysicTick.isSelected());
			}
		});
		add(showEditorMarkers);
		showEditorMarkers.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowEditorMarkers(showEditorMarkers.isSelected());
			}
		});
		add(showDistantBuildingsTick);
		showDistantBuildingsTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowDistantBuildings(showDistantBuildingsTick.isSelected());
			}
		});
		add(showDistantTreesTick);
		showDistantTreesTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				BethRenderSettings.setShowDistantTrees(showDistantTreesTick.isSelected());
			}
		});

		add(enablePhysicsTick);
		enablePhysicsTick.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				simpleWalkSetup.setPhysicsEnabled(enablePhysicsTick.isSelected());
			}
		});

	}

}