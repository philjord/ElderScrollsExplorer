/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient;

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

import tools.GridLayout2;
import esmj3d.j3d.BethRenderSettings;

/**
 * @author Administrator
 *
 */
public class ESESettingsPanel extends JPanel
{

	private JButton lowSettings = new JButton("Low");

	private JButton medSettings = new JButton("Med");

	private JButton highSettings = new JButton("High");

	private JSlider FAR_LOAD_DISTSlider = new JSlider(0, 64, BethRenderSettings.getFarLoadGridCount());

	private JSlider NEAR_LOAD_DISTSlider = new JSlider(0, 16, BethRenderSettings.getNearLoadGridCount());

	private JSlider CHAR_MOVE_UPDATE_DISTSlider = new JSlider(0, 32, (int) BethRenderSettings.getCHAR_MOVE_UPDATE_DIST());

	private JSlider actorFade = new JSlider(0, BethRenderSettings.ACTOR_FADE_MAX, BethRenderSettings.ACTOR_FADE_DEFAULT);

	private JSlider itemFade = new JSlider(0, BethRenderSettings.ITEM_FADE_MAX, BethRenderSettings.ITEM_FADE_DEFAULT);

	private JSlider objectFade = new JSlider(0, BethRenderSettings.OBJECT_FADE_MAX, BethRenderSettings.OBJECT_FADE_DEFAULT);

	private JCheckBox showPhysicTick = new JCheckBox("Show Physics", BethRenderSettings.isShowPhysic());

	private JCheckBox showEditorMarkers = new JCheckBox("Show Editor Markers", BethRenderSettings.isShowEditorMarkers());

	private JCheckBox showDistantBuildingsTick = new JCheckBox("Distant Buildings", BethRenderSettings.isShowDistantBuildings());

	private JCheckBox showDistantTreesTick = new JCheckBox("Distant Trees", BethRenderSettings.isShowDistantTrees());

	private JCheckBox enablePhysicsTick = new JCheckBox("Enable Physics", true);

	private SimpleWalkSetup simpleWalkSetup;

	public ESESettingsPanel(SimpleWalkSetup _simpleWalkSetup)
	{
		this.simpleWalkSetup = _simpleWalkSetup;
		this.setLayout(new GridLayout2(-1, 3));

		ButtonGroup setG = new ButtonGroup();
		setG.add(lowSettings);
		setG.add(medSettings);
		setG.add(highSettings);
		add(lowSettings);
		add(medSettings);
		add(highSettings);
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

		CHAR_MOVE_UPDATE_DISTSlider.setBorder(new TitledBorder("CHAR_MOVE_UPDATE_DIST"));
		CHAR_MOVE_UPDATE_DISTSlider.setMajorTickSpacing(4);
		CHAR_MOVE_UPDATE_DISTSlider.setPaintTicks(true);
		CHAR_MOVE_UPDATE_DISTSlider.setPaintLabels(true);
		add(CHAR_MOVE_UPDATE_DISTSlider);
		CHAR_MOVE_UPDATE_DISTSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setCHAR_MOVE_UPDATE_DIST(CHAR_MOVE_UPDATE_DISTSlider.getValue());
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
