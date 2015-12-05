package scrollsexplorer.simpleclient;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import scrollsexplorer.simpleclient.charactersheet.SimpleInventorySystem;
import scrollsexplorer.simpleclient.mouseover.ActionableMouseOverHandler;
import scrollsexplorer.simpleclient.mouseover.AdminMouseOverHandler;
import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import tools.ddstexture.DDSTextureLoader;
import tools3d.camera.CameraPanel;
import tools3d.camera.HMDCamDolly;
import tools3d.camera.HMDCameraPanel;
import tools3d.camera.HeadCamDolly;
import tools3d.camera.ICameraPanel;
import tools3d.mixed3d2d.Canvas3D2D;
import tools3d.mixed3d2d.hud.hudelements.HUDCompass;
import tools3d.mixed3d2d.hud.hudelements.HUDCrossHair;
import tools3d.mixed3d2d.hud.hudelements.HUDFPSCounter;
import tools3d.mixed3d2d.hud.hudelements.HUDPosition;
import tools3d.mixed3d2d.hud.hudelements.HUDText;
import tools3d.mixed3d2d.overlay.swing.Panel3D;
import tools3d.mixed3d2d.overlay.swing.util.ExitDialogPane3D;
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

import com.sun.j3d.utils.universe.ViewingPlatform;

import esmj3d.j3d.BethRenderSettings;

/**
 * A class to pull the keyboard nav, bullet phys, nif displayable, canvas2d3d overlays, 
 * physics display together, 
 * 
 * but no particular way to load nifs, esm, comms or anything else
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

	private HUDCrossHair hudCrossHair;

	//private HUDPhysicsState hudPhysicsState;

	private HUDText firstInstruction;

	private PhysicsSystem physicsSystem;

	private ActionableMouseOverHandler cameraMouseOver;

	private AdminMouseOverHandler cameraAdminMouseOverHandler;

	private JTextField locField = new JTextField("0000,0000,0000");

	private JPanel warpPanel = new JPanel();

	private JTextField warpField = new JTextField("                ");

	private boolean freefly = false;

	private AmbientLight ambLight = null;

	private DirectionalLight dirLight = null;

	private ComponentAdapter canvasResizeListener = new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e)
		{
			canvasResized();
		}

		@Override
		public void componentShown(ComponentEvent e)
		{
			canvasResized();
		}
	};

	//Can't use as threading causes massive trouble for scene loading
	//	private StructureUpdateBehavior structureUpdateBehavior;

	private NbccProvider nbccProvider = new NbccProvider() {
		@Override
		public NBControlledChar getNBControlledChar()
		{
			return physicsSystem.getNBControlledChar();
		}
	};

	//Panel3D gear
	private Panel3D fullScreenPanel3D;

	private ExitDialogPane3D exitDialogPane3D;

	//We are no longer in simple walk set up now!!!!
	private SimpleInventorySystem simpleInventorySystem;

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
		float ambl = BethRenderSettings.getGlobalAmbLightLevel();
		Color3f alColor = new Color3f(ambl, ambl, ambl);
		ambLight = new AmbientLight(true, alColor);
		//ambLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		ambLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		ambLight.setCapability(Light.ALLOW_COLOR_WRITE);
		float dirl = BethRenderSettings.getGlobalDirLightLevel();
		Color3f dirColor = new Color3f(dirl, dirl, dirl);
		dirLight = new DirectionalLight(true, dirColor, new Vector3f(0f, -1f, 0f));
		//dirLight.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_WRITE);
		dirLight.setInfluencingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), Double.POSITIVE_INFINITY));
		dirLight.setCapability(Light.ALLOW_COLOR_WRITE);
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

		//add mouse and keyboard inputs ************************
		keyNavigationInputAWT = new NavigationInputAWTKey(navigationProcessor);
		NavigationInputAWTKey.VERTICAL_RATE = 50f;

		mouseInputListener = new NavigationInputAWTMouseLocked();
		mouseInputListener.setNavigationProcessor(navigationProcessor);
		// dont' start mouse locked as its a pain
		//mouseInputListener.setCanvas(cameraPanel.getCanvas3D2D());

		//add jump key and vis/phy toggle key listeners for fun ************************
		jumpKeyListener = new JumpKeyListener(nbccProvider);

		//some hud gear
		fpsCounter = new HUDFPSCounter();
		hudPos = new HUDPosition();
		hudcompass = new HUDCompass();
		//hudPhysicsState = new HUDPhysicsState();
		hudCrossHair = new HUDCrossHair();

		behaviourBranch.addChild(fpsCounter.getBehaviorBranchGroup());
		//behaviourBranch.addChild(hudPhysicsState.getBehaviorBranchGroup());

		//	structureUpdateBehavior = new StructureUpdateBehavior();
		//	structureUpdateBehavior.setMaxElapsedTimeForCalls(20);
		//	behaviourBranch.addChild(structureUpdateBehavior);

		avatarLocation.addAvatarLocationListener(hudPos);
		avatarLocation.addAvatarLocationListener(hudcompass);

		avatarLocation.addAvatarLocationListener(this);
		warpPanel.setLayout(new FlowLayout());
		warpPanel.add(warpField);
		warpField.setSize(200, 20);
		ActionListener warpActionListener = new ActionListener() {
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
		universe.addShaderErrorListener(new ShaderErrorListener() {
			public void errorOccurred(ShaderError error)
			{
				error.printVerbose();
				JOptionPane.showMessageDialog(null, error.toString(), "ShaderError", JOptionPane.ERROR_MESSAGE);
			}
		});

		//Panel3D gear
		fullScreenPanel3D = new Panel3D();
		exitDialogPane3D = new ExitDialogPane3D(fullScreenPanel3D);
		exitDialogPane3D.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (exitDialogPane3D.isExitConfirmed())
				{
					// allow listeners to clean up on exit (save setting etc)
					// this will exit
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
				else
				{
					//hide dialog and lock mouse
					exitDialogPane3D.setVisible(false);
					setMouseLock(true);
				}

			}
		});

		simpleInventorySystem = new SimpleInventorySystem(fullScreenPanel3D);
		simpleInventorySystem.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e)
			{
				setMouseLock(false);
			}

			@Override
			public void componentHidden(ComponentEvent e)
			{
				setMouseLock(true);
			}
		});
	}

	protected void canvasResized()
	{
		Canvas3D2D c = cameraPanel.getCanvas3D2D();
		exitDialogPane3D.setLocation((c.getWidth() / 2) - (exitDialogPane3D.getWidth() / 2),
				(c.getHeight() / 2) - (exitDialogPane3D.getHeight() / 2));

		fullScreenPanel3D.redraw(true);
	}

	/**
	 * Only for listening to shutdown
	 * @return
	 */
	public JFrame getJFrame()
	{
		return frame;
	}

	private long lastLocationUpdate = 0;

	@Override
	public void locationUpdated(Quat4f rot, Vector3f trans)
	{
		if (System.currentTimeMillis() - lastLocationUpdate > 200)
		{
			//oddly this is mildly expensive so only update 5 times per second
			locField.setText(("" + trans.x).split("\\.")[0] + "," + ("" + trans.y).split("\\.")[0] + "," + ("" + trans.z).split("\\.")[0]);
			lastLocationUpdate = System.currentTimeMillis();
		}
	}

	public void changeLocation(Quat4f rot, Vector3f trans)
	{
		System.out.println("Moving to " + trans);
		//TODO: should I call warp now? not needed if only change cell uses the above
		warp(trans);
		getAvatarLocation().setTranslation(trans);
		getAvatarLocation().setRotation(rot);
	}

	private void warp(Vector3f origin)
	{
		if (physicsSystem != null && physicsSystem.getNBControlledChar() != null)
		{
			physicsSystem.getNBControlledChar().getCharacterController().warp(origin);
		}

	}

	public void setGlobalAmbLightLevel(float f)
	{
		Color3f alColor = new Color3f(f, f, f);
		ambLight.setColor(alColor);
	}

	public void setGlobalDirLightLevel(float f)
	{
		Color3f dirColor = new Color3f(f, f, f);
		dirLight.setColor(dirColor);
	}

	public void configure(MeshSource meshSource, SimpleBethCellManager simpleBethCellManager)
	{
		// set up and run the physics system************************************************

		physicsSystem = new PhysicsSystem(simpleBethCellManager, avatarCollisionInfo, behaviourBranch, meshSource);

		ScrollsExplorer.dashboard.setPhysicSystem(physicsSystem);

		cameraMouseOver = new ActionableMouseOverHandler(physicsSystem, simpleBethCellManager);

		cameraAdminMouseOverHandler = new AdminMouseOverHandler(physicsSystem);

		//hudPhysicsState.setHudPhysicsStateData(physicsSystem);

		GraphicsSettings gs = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(SimpleWalkSetup.class), frame, false, true,
				false, false);
		if (gs != null)
		{
			setupGraphicsSetting(gs);
		}
		
		cameraPanel.startRendering();

	}

	private void setupGraphicsSetting(GraphicsSettings gs)
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
			hudCrossHair.addToCanvas(canvas3D2D);

			//Panel3D gear
			fullScreenPanel3D.setConfig(canvas3D2D);
			exitDialogPane3D.setLocation((canvas3D2D.getWidth() / 2) - (exitDialogPane3D.getWidth() / 2),
					(canvas3D2D.getHeight() / 2) - (exitDialogPane3D.getHeight() / 2));

			//allow tab for mouse lock
			canvas3D2D.setFocusTraversalKeysEnabled(false);

			canvas3D2D.addComponentListener(canvasResizeListener);

			if (firstInstruction == null)
			{
				firstInstruction = new HUDText(canvas3D2D, new Rectangle(600, 60), 18);
				firstInstruction.setLocation(0, 200);
				firstInstruction.setText("Press tab to look around, press tab again to release mouse");
			}

			if (isLive)
			{
				setEnabled(true);
			}
		}
	}

	public void resetGraphicsSetting()
	{
		GraphicsSettings gs = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(SimpleWalkSetup.class), frame, false,
				false, true, false);
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
				//cameraPanel.stopRendering();// this kills the J3d stuff like removeNotify did
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

	public BranchGroup getVisualBranch()
	{
		return visualGroup;
	}

	public BranchGroup getPhysicalBranch()
	{
		return physicsGroup;
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
			//Bad no good 
			//structureUpdateBehavior.add(modelGroup, visualGroup);
			modelGroup.addChild(visualGroup);
		}
		else if (!showVisual && visualGroup.getParent() != null)
		{
			//structureUpdateBehavior.remove(modelGroup, visualGroup);
			visualGroup.detach();
		}
	}

	public void setVisualDisplayed(boolean newShowVisual)
	{
		if (newShowVisual && visualGroup.getParent() == null)
		{
			//structureUpdateBehavior.add(modelGroup, visualGroup);
			modelGroup.addChild(visualGroup);

		}
		else if (!newShowVisual && visualGroup.getParent() != null)
		{
			//structureUpdateBehavior.remove(modelGroup, visualGroup);
			visualGroup.detach();
		}

		showVisual = newShowVisual;
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

	public ViewingPlatform getViewingPlatform()
	{
		// this won't work for the HMD version for now, as it it 2 platforms
		return (ViewingPlatform) cameraPanel.getDolly();
	}

	public void setAzerty(boolean a)
	{
		if (a)
		{
			NavigationInputAWTKey.FORWARD_KEY = KeyEvent.VK_Z;
			//NavigationInputAWTKey.FAST_KEY = KeyEvent.VK_E;
			//NavigationInputAWTKey.BACK_KEY = KeyEvent.VK_S;
			NavigationInputAWTKey.LEFT_KEY = KeyEvent.VK_Q;
			//NavigationInputAWTKey.RIGHT_KEY = KeyEvent.VK_D;
			NavigationInputAWTKey.UP_KEY = KeyEvent.VK_A;
			NavigationInputAWTKey.DOWN_KEY = KeyEvent.VK_W;
		}
		else
		{
			NavigationInputAWTKey.FORWARD_KEY = KeyEvent.VK_W;
			//NavigationInputAWTKey.FAST_KEY = KeyEvent.VK_E;
			//NavigationInputAWTKey.BACK_KEY = KeyEvent.VK_S;
			NavigationInputAWTKey.LEFT_KEY = KeyEvent.VK_A;
			//NavigationInputAWTKey.RIGHT_KEY = KeyEvent.VK_D;
			NavigationInputAWTKey.UP_KEY = KeyEvent.VK_Q;
			NavigationInputAWTKey.DOWN_KEY = KeyEvent.VK_Z;
		}
	}

	private void setMouseLock(boolean mouseLock)
	{
		if (!mouseLock)
		{
			mouseInputListener.setCanvas(null);

			//note tab message only put up if tab used to unlock mouse
		}
		else
		{
			mouseInputListener.setCanvas(cameraPanel.getCanvas3D2D());

			// always clear the tab message regardless
			if (firstInstruction != null)
			{
				firstInstruction.removeFromCanvas();
			}
		}
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
				if (!exitDialogPane3D.isVisible())
				{
					//unlock mouse to interact
					setMouseLock(false);
					exitDialogPane3D.setVisible(true);
				}
				else
				{
					//hide dialog and lock mouse
					exitDialogPane3D.setVisible(false);
					setMouseLock(true);
				}
			}
			else if (e.getKeyCode() == KeyEvent.VK_H)
			{
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
				physicsSystem.setDisplayDebug(true);
			}
			else if (e.getKeyCode() == KeyEvent.VK_TAB)
			{
				if (mouseInputListener.hasCanvas())
				{
					setMouseLock(false);
					if (firstInstruction != null)
					{
						firstInstruction.addToCanvas(cameraPanel.getCanvas3D2D());
					}
				}
				else
				{
					setMouseLock(true);
				}
			}
			else if (e.getKeyCode() == KeyEvent.VK_I)
			{
				// simpleInventorySystem has a listener for the mouse lock
				simpleInventorySystem.setVisible(!simpleInventorySystem.isVisible());
			}
		}
	}

}
