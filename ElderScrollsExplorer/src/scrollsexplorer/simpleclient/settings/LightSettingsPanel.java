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

import tools.swing.VerticalFlowLayout;
import esmj3d.j3d.BethRenderSettings;
import scrollsexplorer.simpleclient.SimpleWalkSetupInterface;

/**
 * @author Administrator
 *
 */
public class LightSettingsPanel extends JPanel implements BethRenderSettings.UpdateListener {
	private JSlider						globalAmbLightLevel	= new JSlider(0, 100,
			(int)(BethRenderSettings.getGlobalAmbLightLevel() * 100));

	private JSlider						globalDirLightLevel	= new JSlider(0, 100,
			(int)(BethRenderSettings.getGlobalDirLightLevel() * 100));

	private JCheckBox					enableDirLight		= new JCheckBox("Enable Dir Light",
			BethRenderSettings.isEnableDirLight());

	private JCheckBox					enablePlacedLights	= new JCheckBox("Enable Placed Lights",
			BethRenderSettings.isEnablePlacedLights());

	private JCheckBox					enableTorchLight	= new JCheckBox("Enable Torch Light",
			BethRenderSettings.isEnableTorchLight());

	protected SimpleWalkSetupInterface	simpleWalkSetup;

	public LightSettingsPanel(SimpleWalkSetupInterface _simpleWalkSetup) {
		this.simpleWalkSetup = _simpleWalkSetup;
		//this.setLayout(new GridLayout2(-1, 3));
		this.setLayout(new VerticalFlowLayout());

		globalAmbLightLevel.setBorder(new TitledBorder("globalAmbLightLevel"));
		globalAmbLightLevel.setMajorTickSpacing(25);
		globalAmbLightLevel.setPaintTicks(true);
		globalAmbLightLevel.setPaintLabels(true);
		add(globalAmbLightLevel);
		globalAmbLightLevel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(LightSettingsPanel.this);
				BethRenderSettings.setGlobalAmbLightLevel(globalAmbLightLevel.getValue() / 100f);				
				BethRenderSettings.addUpdateListener(LightSettingsPanel.this);
			}
		});

		globalDirLightLevel.setBorder(new TitledBorder("globalDirLightLevel"));
		globalDirLightLevel.setMajorTickSpacing(25);
		globalDirLightLevel.setPaintTicks(true);
		globalDirLightLevel.setPaintLabels(true);
		add(globalDirLightLevel);
		globalDirLightLevel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BethRenderSettings.removeUpdateListener(LightSettingsPanel.this);
				BethRenderSettings.setGlobalDirLightLevel(globalDirLightLevel.getValue() / 100f);				
				BethRenderSettings.addUpdateListener(LightSettingsPanel.this);
			}
		});

		add(enableDirLight);
		enableDirLight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(LightSettingsPanel.this);
				BethRenderSettings.setGlobalDirLightEnabled(enableDirLight.isSelected());				
				BethRenderSettings.addUpdateListener(LightSettingsPanel.this);
			}
		});

		add(enablePlacedLights);
		enablePlacedLights.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(LightSettingsPanel.this);
				BethRenderSettings.setEnablePlacedLights(enablePlacedLights.isSelected());
				BethRenderSettings.addUpdateListener(LightSettingsPanel.this);
			}
		});

		add(enableTorchLight);
		enableTorchLight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				BethRenderSettings.removeUpdateListener(LightSettingsPanel.this);
				BethRenderSettings.setEnableTorchLight(enableTorchLight.isSelected());
				BethRenderSettings.addUpdateListener(LightSettingsPanel.this);
			}
		});

		BethRenderSettings.addUpdateListener(this);
	}

	@Override
	public void renderSettingsUpdated() {
		// Visually update when the rendersetting are touch programmatically
		globalAmbLightLevel.setValue((int)(BethRenderSettings.getGlobalAmbLightLevel() * 100));
		globalDirLightLevel.setValue((int)(BethRenderSettings.getGlobalDirLightLevel() * 100));
		enableDirLight.setSelected(BethRenderSettings.isEnableDirLight());
		enablePlacedLights.setSelected(BethRenderSettings.isEnablePlacedLights());
		enableTorchLight.setSelected(BethRenderSettings.isEnableTorchLight());
	}

}
