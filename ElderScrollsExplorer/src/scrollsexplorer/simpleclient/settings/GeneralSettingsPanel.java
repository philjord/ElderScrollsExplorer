/*
 * Created on Oct 28, 2006
 */
package scrollsexplorer.simpleclient.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import scrollsexplorer.ScrollsExplorer;
import scrollsexplorer.simpleclient.GlobalGameSettings;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools.swing.VerticalFlowLayout;

/**
 * @author Administrator
 * Notice we don't listen to BethRenderSettings
 * but we shoudl for enable physics nad free fly at teh very least cos simplewalk is listnering proper like
 */
public class GeneralSettingsPanel extends JPanel implements GlobalGameSettings.UpdateListener {
	private JCheckBox		autoLoadLastGameConfig	= new JCheckBox("Auto Load Last GameConfig",
			GlobalGameSettings.isAutoLoadLastGameConfig());
	private JCheckBox		autoLoadLastCell		= new JCheckBox("Auto Load Last Cell",
			GlobalGameSettings.isAutoLoadLastCell());

	private JCheckBox		isFreeFlyCheckBox		= new JCheckBox("Is Free Fly", GlobalGameSettings.isFreeFly());

	private JCheckBox		enablePhysicsTick		= new JCheckBox("Enable Physics",
			GlobalGameSettings.isEnablePhysics());

	private JCheckBox		slowPhysicsTick			= new JCheckBox("Slow Physics", GlobalGameSettings.isSlowPhysics());

	private ScrollsExplorer	scrollsExplorer;

	public GeneralSettingsPanel(ScrollsExplorer _scrollsExplorer) {

		this.scrollsExplorer = _scrollsExplorer;
		//this.setLayout(new GridLayout2(-1, 3));
		this.setLayout(new VerticalFlowLayout());

		add(autoLoadLastCell);
		autoLoadLastCell.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GlobalGameSettings.removeUpdateListener(GeneralSettingsPanel.this);
				GlobalGameSettings.setAutoLoadLastCell(autoLoadLastCell.isSelected());
				GlobalGameSettings.addUpdateListener(GeneralSettingsPanel.this);
			}
		});

		add(autoLoadLastGameConfig);
		autoLoadLastGameConfig.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GlobalGameSettings.removeUpdateListener(GeneralSettingsPanel.this);
				GlobalGameSettings.setAutoLoadLastGameConfig(autoLoadLastGameConfig.isSelected());
				GlobalGameSettings.addUpdateListener(GeneralSettingsPanel.this);
			}
		});

		add(isFreeFlyCheckBox);
		isFreeFlyCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GlobalGameSettings.removeUpdateListener(GeneralSettingsPanel.this);
				GlobalGameSettings.setIsFreeFly(isFreeFlyCheckBox.isSelected());
				GlobalGameSettings.addUpdateListener(GeneralSettingsPanel.this);
			}
		});

		add(enablePhysicsTick);
		enablePhysicsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GlobalGameSettings.removeUpdateListener(GeneralSettingsPanel.this);
				GlobalGameSettings.setEnablePhysics(enablePhysicsTick.isSelected());
				// TODO: make it a GlobalGameSetting listener
				scrollsExplorer.getSimpleWalkSetup().setPhysicsEnabled(enablePhysicsTick.isSelected());
				GlobalGameSettings.addUpdateListener(GeneralSettingsPanel.this);
			}
		});

		add(slowPhysicsTick);
		slowPhysicsTick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GlobalGameSettings.removeUpdateListener(GeneralSettingsPanel.this);
				GlobalGameSettings.setSlowPhysics(slowPhysicsTick.isSelected());
				// TODO: make it a GlobalGameSetting listener
				PhysicsSystem.DEBUG_UPDATE_STEP_SKIP = slowPhysicsTick.isSelected() ? 200 : 1;
				GlobalGameSettings.addUpdateListener(GeneralSettingsPanel.this);
			}
		});

		GlobalGameSettings.addUpdateListener(GeneralSettingsPanel.this);
	}

	@Override
	public void gameSettingsUpdated() {
		autoLoadLastCell.setSelected(GlobalGameSettings.isAutoLoadLastCell());
		autoLoadLastGameConfig.setSelected(GlobalGameSettings.isAutoLoadLastGameConfig());
		isFreeFlyCheckBox.setSelected(GlobalGameSettings.isFreeFly());
		enablePhysicsTick.setSelected(GlobalGameSettings.isEnablePhysics());
		slowPhysicsTick.setSelected(GlobalGameSettings.isSlowPhysics());

	}

}
