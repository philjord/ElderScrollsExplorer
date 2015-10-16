package scrollsexplorer.simpleclient;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.ShaderError;
import javax.media.j3d.ShaderErrorListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import nifbullet.JumpKeyListener;
import nifbullet.NavigationProcessorBullet;
import nifbullet.NavigationProcessorBullet.NbccProvider;
import nifbullet.cha.NBControlledChar;
import scrollsexplorer.ScrollsExplorer;
import scrollsexplorer.simpleclient.mouseover.ActionableMouseOverHandler;
import scrollsexplorer.simpleclient.mouseover.AdminMouseOverHandler;
import scrollsexplorer.simpleclient.physics.InstRECOStore;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools.ddstexture.DDSTextureLoader;
import tools3d.camera.CameraPanel;
import tools3d.camera.HMDCamDolly;
import tools3d.camera.HMDCameraPanel;
import tools3d.camera.HeadCamDolly;
import tools3d.camera.ICameraPanel;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.mixed3d2d.hud.hudelements.HUDCompass;
import tools3d.mixed3d2d.hud.hudelements.HUDFPSCounter;
import tools3d.mixed3d2d.hud.hudelements.HUDPosition;
import tools3d.navigation.AvatarCollisionInfo;
import tools3d.navigation.AvatarLocation;
import tools3d.navigation.NavigationInputAWTKey;
import tools3d.navigation.NavigationInputAWTMouseLocked;
import tools3d.navigation.NavigationTemporalBehaviour;
import tools3d.ovr.OculusException;
import tools3d.resolution.GraphicsSettings;
import tools3d.resolution.ScreenResolution;
import tools3d.universe.VisualPhysicalUniverse;
import tools3d.utils.scenegraph.LocationUpdateListener;
import utils.source.MeshSource;

/**
 * A class to pull teh keyboard nav, bullet phys, nif displayable, canvas2d3d overlays, 
 * physics display together, 
 * 
 * but no particular way to load nifs,esm,comms or anything else
 * 
 * @author philip
 *
 */
public class SimpleWalkSetup implements LocationUpdateListener
{
	public static boolean HMD_MODE = true;

	private JFrame frame = new JFrame();

	private boolean enabled = false;

	public VisualPhysicalUniverse universe;

	private BranchGroup modelGroup = new BranchGroup();

	private BranchGroup physicsGroup;

	private BranchGroup visualGroup;

	private BranchGroup behaviourBranch;

	private NavigationTemporalBehaviour navigationTemporalBehaviour;

	private NavigationProcessorBullet navigationProcessor;

	private ICameraPanel cameraPanel;

	private AvatarLocation avatarLocation = new AvatarLocation();

	private AvatarCollisionInfo avatarCollisionInfo = new AvatarCollisionInfo(avatarLocation, 0.5f, 1.8f, 0.35f, 0.8f);

	private NavigationInputAWTKey keyNavigationInputAWT;

	private NavigationInputAWTMouseLocked mouseInputListener;

	private JumpKeyListener jumpKeyListener;

	private MiscKeyHandler miscKeyHandler = new MiscKeyHandler();

	private boolean showHavok = false;

	private boolean showVisual = true;

	private HUDFPSCounter fpsCounter;

	private HUDPosition hudPos;

	private HUDCompass hudcompass;

	//private HUDPhysicsState hudPhysicsState;

	private PhysicsSystem physicsSystem;

	private ActionableMouseOverHandler cameraMouseOver;

	private AdminMouseOverHandler cameraAdminMouseOverHandler;

	private JTextField locField = new JTextField("0000,0000,0000");

	private JPanel warpPanel = new JPanel();

	private JTextField warpField = new JTextField("                ");

	private NbccProvider nbccProvider = new NbccProvider()
	{
		@Override
		public NBControlledChar getNBControlledChar()
		{
			return physicsSystem.getNBControlledChar();
		}
	};

	private boolean freefly = false;

	public SimpleWalkSetup(String frameName)
	{
		//kick off with a universe ***************************
		universe = new VisualPhysicalUniverse();

		//basic model and physics branch ************************
		modelGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		modelGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);

		physicsGroup = new BranchGroup();
		physicsGroup.setCapability(BranchGroup.ALLOW_DETACH);
		physicsGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		physicsGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		//modelGroup.addChild(physicsGroup); added if toggled on

		visualGroup = new BranchGroup();
		visualGroup.setCapability(BranchGroup.ALLOW_DETACH);
		visualGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		visualGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		modelGroup.addChild(visualGroup);

		universe.addToVisualBranch(modelGroup);
		behaviourBranch = new BranchGroup();
		behaviourBranch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		behaviourBranch.setCapability(Group.ALLOW_CHILDREN_WRITE);

		// Create ambient light	and add it ************************
		Color3f alColor = new Color3f(1f, 1f, 1f);
		AmbientLight ambLight = new AmbientLight(true, alColor);
		ambLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		ambLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		DirectionalLight dirLight = new DirectionalLight(true, alColor, new Vector3f(0f, -1f, 0f));
		dirLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		dirLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		BranchGroup lightsBG = new BranchGroup();
		lightsBG.addChild(ambLight);
		lightsBG.addChild(dirLight);
		universe.addToVisualBranch(lightsBG);

		// add the time keepers to the universe ************************

		//mouse/keyboard
		navigationTemporalBehaviour = new NavigationTemporalBehaviour();

		//jbullet
		navigationProcessor = new NavigationProcessorBullet(nbccProvider, avatarLocation);
		navigationTemporalBehaviour.addNavigationProcessor(navigationProcessor);
		behaviourBranch.addChild(navigationTemporalBehaviour);

		//now we have timekeep and camera panel add mouse and keyboard inputs ************************
		keyNavigationInputAWT = new NavigationInputAWTKey(navigationProcessor);
		NavigationInputAWTKey.VERTICAL_RATE = 50f;

		mouseInputListener = new NavigationInputAWTMouseLocked();
		mouseInputListener.setNavigationProcessor(navigationProcessor);
		// dont' start mouse locked as its a pain
		//mouseInputListener.setCanvas(cameraPanel.getCanvas3D2D());

		//add jump key and vis/phy toggle key listenres for fun ************************
		jumpKeyListener = new JumpKeyListener(nbccProvider);

		//just an fps for fun
		fpsCounter = new HUDFPSCounter();
		hudPos = new HUDPosition();
		hudcompass = new HUDCompass();
		//hudPhysicsState = new HUDPhysicsState();

		universe.addToBehaviorBranch(fpsCounter.getBehaviorBranchGroup());
		//universe.addToBehaviorBranch(hudPhysicsState.getBehaviorBranchGroup());

		avatarLocation.addAvatarLocationListener(hudPos);
		avatarLocation.addAvatarLocationListener(hudcompass);

		avatarLocation.addAvatarLocationListener(this);
		warpPanel.setLayout(new FlowLayout());
		warpPanel.add(warpField);
		warpField.setSize(200, 20);
		ActionListener warpActionListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String warp = warpField.getText().trim();
				String[] parts = warp.split("[^\\d-]+");

				if (parts.length == 3)
				{
					warp(new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
				}
			}
		};
		warpField.addActionListener(warpActionListener);
		JButton warpButton = new JButton("Go");
		warpPanel.add(warpButton);
		warpButton.addActionListener(warpActionListener);

		frame.setTitle(frameName);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		universe.addToBehaviorBranch(behaviourBranch);

		// Add a ShaderErrorListener
		universe.addShaderErrorListener(new ShaderErrorListener()
		{
			public void errorOccurred(ShaderError error)
			{
				error.printVerbose();
				JOptionPane.showMessageDialog(null, error.toString(), "ShaderError", JOptionPane.ERROR_MESSAGE);
			}
		});

	}

	/**
	 * Only for listening to shutdown
	 * @return
	 */
	public JFrame getJFrame()
	{
		return frame;
	}

	@Override
	public void locationUpdated(Quat4f rot, Vector3f trans)
	{
		locField.setText(("" + trans.x).split("\\.")[0] + "," + ("" + trans.y).split("\\.")[0] + "," + ("" + trans.z).split("\\.")[0]);
	}

	public void warp(Vector3f origin)
	{
		if (physicsSystem != null && physicsSystem.getNBControlledChar() != null)
		{
			physicsSystem.getNBControlledChar().getCharacterController().warp(origin);
		}

	}

	public void configure(MeshSource meshSource, InstRECOStore instRECOStore)
	{
		// set up and run the physics system************************************************

		physicsSystem = new PhysicsSystem(instRECOStore, avatarLocation, behaviourBranch, meshSource);

		ScrollsExplorer.dashboard.setPhysicSystem(physicsSystem);

		cameraMouseOver = new ActionableMouseOverHandler(physicsSystem);

		cameraAdminMouseOverHandler = new AdminMouseOverHandler(physicsSystem);

		//hudPhysicsState.setHudPhysicsStateData(physicsSystem);

		//cameraPanel.startRendering();//JRE7 crash bug work around, doesn't work some times:(
		GraphicsSettings gs = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(SimpleWalkSetup.class), frame, false,
				true, false);
		if (gs != null)
		{
			setupGraphicsSetting(gs);
		}

	}

	public void setupGraphicsSetting(GraphicsSettings gs)
	{
		if (cameraPanel == null)
		{
			// must record start state to restore later
			boolean isLive = enabled;

			if (isLive)
			{
				setEnabled(false);
			}

			// clean any old gear
			/*	if (cameraPanel != null)
				{
					// reverse of construction below basically
					avatarLocation.removeAvatarLocationListener(cameraPanel.getDolly());
					cameraPanel.getCanvas3D2D().getHudShapeRoot().detach();
					Canvas3D2D canvas3D2D = cameraPanel.getCanvas3D2D();
					canvas3D2D.removeKeyListener(keyNavigationInputAWT);
					canvas3D2D.removeKeyListener(jumpKeyListener);
					canvas3D2D.removeKeyListener(miscKeyHandler);
					fpsCounter.removeFromCanvas(canvas3D2D);
					hudPos.removeFromCanvas(canvas3D2D);
					hudcompass.removeFromCanvas(canvas3D2D);
					hudPhysicsState.removeFromCanvas(canvas3D2D);

					frame.getContentPane().remove((JPanel) cameraPanel);
				}*/

			HMD_MODE = gs.isOculusView();

			//create the camera panel ************************
			if (HMD_MODE)
			{
				System.out.println("HMD mode");
				try
				{
					cameraPanel = new HMDCameraPanel(universe);
					// and the dolly it rides on
					HMDCamDolly hcd = new HMDCamDolly(avatarCollisionInfo);
					cameraPanel.setDolly(hcd);

					//disable pitch in body
					navigationProcessor.setNoPitch(true);
					navigationTemporalBehaviour.addNavigationProcessor(hcd);
					cameraPanel.getCanvas3D2D().addKeyListener(new HMDKeyHandler(hcd));
				}
				catch (OculusException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "No Oculus or failure", "Oculus", JOptionPane.ERROR_MESSAGE);
				}

			}

			//if HMD fails or not HMD
			if (cameraPanel == null)
			{
				cameraPanel = new CameraPanel(universe);
				// and the dolly it rides on
				HeadCamDolly headCamDolly = new HeadCamDolly(avatarCollisionInfo);
				cameraPanel.setDolly(headCamDolly);
			}

			frame.getContentPane().add((JPanel) cameraPanel);

			avatarLocation.addAvatarLocationListener(cameraPanel.getDolly());
			cameraPanel.getDolly().locationUpdated(avatarLocation.get(new Quat4f()), avatarLocation.get(new Vector3f()));
			cameraPanel.getDolly().setHudShape(cameraPanel.getCanvas3D2D().getHudShapeRoot());

			DDSTextureLoader.setAnisotropicFilterDegree(gs.getAnisotropicFilterDegree());
			cameraPanel.setSceneAntialiasingEnable(gs.isAaRequired());

			Canvas3D2D canvas3D2D = cameraPanel.getCanvas3D2D();
			canvas3D2D.addKeyListener(keyNavigationInputAWT);
			canvas3D2D.addKeyListener(jumpKeyListener);
			canvas3D2D.addKeyListener(miscKeyHandler);
			fpsCounter.addToCanvas(canvas3D2D);
			hudPos.addToCanvas(canvas3D2D);
			hudcompass.addToCanvas(canvas3D2D);
			//hudPhysicsState.addToCanvas(canvas3D2D);

			//allow tab for mouse lock
			canvas3D2D.setFocusTraversalKeysEnabled(false);

			if (isLive)
			{
				setEnabled(true);
			}
		}
	}

	public void resetGraphicsSetting()
	{
		GraphicsSettings gs = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(SimpleWalkSetup.class), frame, false,
				false, true);
		if (gs != null)
		{
			setupGraphicsSetting(gs);
		}
	}

	public void setEnabled(boolean enable)
	{
		if (enable != enabled)
		{
			System.out.println("Setting Enabled " + enable);
			// start the processor up ************************
			navigationProcessor.setActive(enable);
			if (enable)
			{
				cameraMouseOver.setConfig(cameraPanel.getCanvas3D2D());
				cameraAdminMouseOverHandler.setConfig(cameraPanel.getCanvas3D2D());
				physicsSystem.unpause();
				frame.setVisible(true);
				cameraPanel.startRendering();
			}
			else
			{
				cameraMouseOver.setConfig(null);
				cameraAdminMouseOverHandler.setConfig(null);
				physicsSystem.pause();
				//frame.setVisible(false);
				//cameraPanel.stopRendering();// this kills the J3d stuff like changin the resolution
			}
			enabled = enable;
		}

	}

	public void setFreeFly(boolean ff)
	{
		if (physicsSystem.getNBControlledChar() != null)
		{
			physicsSystem.getNBControlledChar().getCharacterController().setFreeFly(ff);
		}
		keyNavigationInputAWT.setAllowVerticalMovement(ff);
	}

	public PhysicsSystem getPhysicsSystem()
	{
		return physicsSystem;
	}

	public void addToVisualBranch(Group newGroup)
	{
		visualGroup.addChild(newGroup);
	}

	public void addToPhysicalBranch(Group group)
	{
		physicsGroup.addChild(group);
	}

	public void toggleHavok()
	{
		showHavok = !showHavok;
		if (showHavok && physicsGroup.getParent() == null)
		{
			modelGroup.addChild(physicsGroup);
		}
		else if (!showHavok && physicsGroup.getParent() != null)
		{
			physicsGroup.detach();
		}
	}

	public void toggleVisual()
	{
		showVisual = !showVisual;
		if (showVisual && visualGroup.getParent() == null)
		{
			modelGroup.addChild(visualGroup);
		}
		else if (!showVisual && visualGroup.getParent() != null)
		{
			visualGroup.detach();
		}
	}

	public AvatarLocation getAvatarLocation()
	{
		return avatarLocation;
	}

	public Component getLocField()
	{
		return locField;
	}

	public Component getWarpField()
	{
		return warpPanel;
	}

	public void setPhysicsEnabled(boolean enable)
	{
		physicsSystem.getPhysicsLocaleDynamics().setSkipStepSim(!enable);
	}

	public AvatarCollisionInfo getAvatarCollisionInfo()
	{
		return avatarCollisionInfo;
	}

	private class HMDKeyHandler extends KeyAdapter
	{
		private HMDCamDolly hcd;

		public HMDKeyHandler(HMDCamDolly hcd)
		{
			this.hcd = hcd;
			System.out.println("-,+ move eye dist");
			System.out.println("B reset oculus");
			System.out.println("F11 send output to oculus");
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_MINUS)
			{
				System.out.println("IPD-");
				hcd.changeIPD(0.95f);
			}
			else if (e.getKeyCode() == KeyEvent.VK_EQUALS)
			{
				System.out.println("IPD+");
				hcd.changeIPD(1.05f);
			}
			else if (e.getKeyCode() == KeyEvent.VK_B)
			{
				System.out.println("resetting Rift");
				hcd.reset();
			}
			else if (e.getKeyCode() == KeyEvent.VK_F11)
			{
				System.out.println("sending to Rift");
				hcd.sendToRift();
			}

		}
	}

	private class MiscKeyHandler extends KeyAdapter
	{
		public MiscKeyHandler()
		{
			/*System.out.println("Esc exit");
			System.out.println("H toggle havok display");
			System.out.println("L toggle visual display");
			System.out.println("TAB toggle mouse lock");
			System.out.println("F toggle freefly");
			System.out.println("J display jbullet debug");*/
		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you wish to exit?");
				if (result == JOptionPane.OK_OPTION)
				{
					// allow listeners to clean up on exit (save setting etc)
					// this will exit
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
			}
			else if (e.getKeyCode() == KeyEvent.VK_H)
			{
				//TODO: physics line rendering makes fps drop by 25%???
				toggleHavok();
			}
			else if (e.getKeyCode() == KeyEvent.VK_L)
			{
				toggleVisual();
			}
			else if (e.getKeyCode() == KeyEvent.VK_F)
			{
				freefly = !freefly;
				setFreeFly(freefly);
			}
			else if (e.getKeyCode() == KeyEvent.VK_J)
			{
				physicsSystem.getPhysicsLocaleDynamics().setDisplayDebug(true);
			}
			else if (e.getKeyCode() == KeyEvent.VK_TAB)
			{
				if (mouseInputListener.hasCanvas())
				{
					mouseInputListener.setCanvas(null);
				}
				else
				{
					mouseInputListener.setCanvas(cameraPanel.getCanvas3D2D());
				}
			}
		}
	}

}
