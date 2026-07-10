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
public class DistanceSettingsPanel extends JPanel implements BethRenderSettings.UpdateListener {
	public JSlider	NEAR_LOAD_DISTSlider	= new JSlider(0, 16, BethRenderSettings.getNearLoadGridCount());

	public JSlider	FAR_LOAD_DISTSlider		= new JSlider(0, 64, BethRenderSettings.getFarLoadGridCount());

	public JSlider	LOD_LOAD_DIST_MAXSlider	= new JSlider(0, 192, BethRenderSettings.getLOD_LOAD_DIST_MAX());

	public JSlider	actorFade				= new JSlider(0, BethRenderSettings.ACTOR_FADE_MAX,
			BethRenderSettings.ACTOR_FADE_DEFAULT);

	public JSlider	itemFade				= new JSlider(0, BethRenderSettings.ITEM_FADE_MAX,
			BethRenderSettings.ITEM_FADE_DEFAULT);

	public JSlider	objectFade				= new JSlider(0, BethRenderSettings.OBJECT_FADE_MAX,
			BethRenderSettings.OBJECT_FADE_DEFAULT);

	public JSlider	fogDist					= new JSlider(BethRenderSettings.FOG_DIST_MIN, 1000,
			BethRenderSettings.FOG_DIST_DEFAULT);

	public DistanceSettingsPanel() {
		this.setLayout(new VerticalFlowLayout());

		NEAR_LOAD_DISTSlider.setBorder(new TitledBorder("Near Load Grids"));
		NEAR_LOAD_DISTSlider.setMajorTickSpacing(2);
		NEAR_LOAD_DISTSlider.setPaintTicks(true);
		NEAR_LOAD_DISTSlider.setPaintLabels(true);
		add(NEAR_LOAD_DISTSlider);
		NEAR_LOAD_DISTSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(DistanceSettingsPanel.this);
				BethRenderSettings.setNearLoadGridCount(NEAR_LOAD_DISTSlider.getValue());
				BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
			}
		});

		FAR_LOAD_DISTSlider.setBorder(new TitledBorder("Far Load Grids"));
		FAR_LOAD_DISTSlider.setMajorTickSpacing(8);
		FAR_LOAD_DISTSlider.setPaintTicks(true);
		FAR_LOAD_DISTSlider.setPaintLabels(true);
		add(FAR_LOAD_DISTSlider);
		FAR_LOAD_DISTSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(DistanceSettingsPanel.this);
				BethRenderSettings.setFarLoadGridCount(FAR_LOAD_DISTSlider.getValue());
				BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
			}
		});

		LOD_LOAD_DIST_MAXSlider.setBorder(new TitledBorder("LOD_LOAD_DIST_MAX"));
		LOD_LOAD_DIST_MAXSlider.setMajorTickSpacing(32);
		LOD_LOAD_DIST_MAXSlider.setPaintTicks(true);
		LOD_LOAD_DIST_MAXSlider.setPaintLabels(true);
		add(LOD_LOAD_DIST_MAXSlider);
		LOD_LOAD_DIST_MAXSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(DistanceSettingsPanel.this);
				BethRenderSettings.setLOD_LOAD_DIST_MAX(LOD_LOAD_DIST_MAXSlider.getValue());
				BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
			}
		});

		actorFade.setBorder(new TitledBorder("Actor Fade"));
		actorFade.setMajorTickSpacing(BethRenderSettings.ACTOR_FADE_MAX / 5);
		actorFade.setPaintTicks(true);
		actorFade.setPaintLabels(true);
		add(actorFade);
		actorFade.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(DistanceSettingsPanel.this);
				BethRenderSettings.setActorFade(actorFade.getValue());
				BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
			}
		});
		itemFade.setBorder(new TitledBorder("Item Fade"));
		itemFade.setMajorTickSpacing(BethRenderSettings.ITEM_FADE_MAX / 5);
		itemFade.setPaintTicks(true);
		itemFade.setPaintLabels(true);
		add(itemFade);
		itemFade.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(DistanceSettingsPanel.this);
				BethRenderSettings.setItemFade(itemFade.getValue());
				BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
			}
		});
		objectFade.setBorder(new TitledBorder("Object Fade"));
		objectFade.setMajorTickSpacing(BethRenderSettings.OBJECT_FADE_MAX / 5);
		objectFade.setPaintTicks(true);
		objectFade.setPaintLabels(true);
		add(objectFade);
		objectFade.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(DistanceSettingsPanel.this);
				BethRenderSettings.setObjectFade(objectFade.getValue());
				BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
			}
		});

		fogDist.setBorder(new TitledBorder("Fog Dist"));
		fogDist.setMajorTickSpacing(200);
		fogDist.setPaintTicks(true);
		fogDist.setPaintLabels(true);
		add(fogDist);
		fogDist.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(DistanceSettingsPanel.this);
				BethRenderSettings.setFogDist(fogDist.getValue());
				BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
			}
		});
		BethRenderSettings.addUpdateListener(DistanceSettingsPanel.this);
	}

	@Override
	public void renderSettingsUpdated() {
		//notice tes3 makes the grid counts seem a bit weird
		NEAR_LOAD_DISTSlider
				.setValue((BethRenderSettings.isTes3() ? 2 : 1) * BethRenderSettings.getNearLoadGridCount());
		FAR_LOAD_DISTSlider.setValue((BethRenderSettings.isTes3() ? 2 : 1) * BethRenderSettings.getFarLoadGridCount());
		LOD_LOAD_DIST_MAXSlider.setValue(BethRenderSettings.getLOD_LOAD_DIST_MAX());
		actorFade.setValue(BethRenderSettings.getActorFade());
		itemFade.setValue(BethRenderSettings.getItemFade());
		objectFade.setValue(BethRenderSettings.getObjectFade());
		fogDist.setValue(BethRenderSettings.getFogDist());
	}

}
