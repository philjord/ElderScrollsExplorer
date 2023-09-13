/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import esmj3d.j3d.BethRenderSettings;
import tools.swing.VerticalFlowLayout;

/**
 * @author Administrator
 *
 */
public class DistanceSettingsPanel extends JPanel
{
	private JSlider NEAR_LOAD_DISTSlider = new JSlider(0, 16, BethRenderSettings.getNearLoadGridCount());
	
	private JSlider FAR_LOAD_DISTSlider = new JSlider(0, 64, BethRenderSettings.getFarLoadGridCount());

	private JSlider LOD_LOAD_DIST_MAXSlider = new JSlider(0, 192, BethRenderSettings.getLOD_LOAD_DIST_MAX());

	private JSlider actorFade = new JSlider(0, BethRenderSettings.ACTOR_FADE_MAX, BethRenderSettings.ACTOR_FADE_DEFAULT);

	private JSlider itemFade = new JSlider(0, BethRenderSettings.ITEM_FADE_MAX, BethRenderSettings.ITEM_FADE_DEFAULT);

	private JSlider objectFade = new JSlider(0, BethRenderSettings.OBJECT_FADE_MAX, BethRenderSettings.OBJECT_FADE_DEFAULT);
	
	private JSlider fogDist = new JSlider(BethRenderSettings.FOG_DIST_MIN, 1000, BethRenderSettings.FOG_DIST_DEFAULT);

	public DistanceSettingsPanel()
	{
		this.setLayout(new VerticalFlowLayout());

		NEAR_LOAD_DISTSlider.setBorder(new TitledBorder("Near Load Grids"));
		NEAR_LOAD_DISTSlider.setMajorTickSpacing(2);
		NEAR_LOAD_DISTSlider.setPaintTicks(true);
		NEAR_LOAD_DISTSlider.setPaintLabels(true);
		add(NEAR_LOAD_DISTSlider);
		NEAR_LOAD_DISTSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setNearLoadGridCount(NEAR_LOAD_DISTSlider.getValue());
			}
		});
		
		FAR_LOAD_DISTSlider.setBorder(new TitledBorder("Far Load Grids"));
		FAR_LOAD_DISTSlider.setMajorTickSpacing(8);
		FAR_LOAD_DISTSlider.setPaintTicks(true);
		FAR_LOAD_DISTSlider.setPaintLabels(true);
		add(FAR_LOAD_DISTSlider);
		FAR_LOAD_DISTSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setFarLoadGridCount(FAR_LOAD_DISTSlider.getValue());
			}
		});

		LOD_LOAD_DIST_MAXSlider.setBorder(new TitledBorder("LOD_LOAD_DIST_MAX"));
		LOD_LOAD_DIST_MAXSlider.setMajorTickSpacing(32);
		LOD_LOAD_DIST_MAXSlider.setPaintTicks(true);
		LOD_LOAD_DIST_MAXSlider.setPaintLabels(true);
		add(LOD_LOAD_DIST_MAXSlider);
		LOD_LOAD_DIST_MAXSlider.addChangeListener(new ChangeListener() {
			@Override
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
		actorFade.addChangeListener(new ChangeListener() {
			@Override
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
		itemFade.addChangeListener(new ChangeListener() {
			@Override
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
		objectFade.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setObjectFade(objectFade.getValue());
			}
		});
		
		
		
		
		fogDist.setBorder(new TitledBorder("Fog Dist"));
		fogDist.setMajorTickSpacing(200);
		fogDist.setPaintTicks(true);
		fogDist.setPaintLabels(true);
		add(fogDist);
		fogDist.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				BethRenderSettings.setFogDist(fogDist.getValue());
			}
		});

	}

}
