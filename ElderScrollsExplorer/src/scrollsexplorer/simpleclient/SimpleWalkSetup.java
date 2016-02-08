package scrollsexplorer.simpleclient;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.ShaderError;
import javax.media.j3d.ShaderErrorListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.sun.j3d.utils.universe.ViewingPlatform;

import awt.tools3d.mixed3d2d.hud.hudelements.HUDCompass;
import awt.tools3d.mixed3d2d.hud.hudelements.HUDText;
import awt.tools3d.resolution.GraphicsSettings;
import esmj3d.j3d.BethRenderSettings;
import nifbullet.NavigationProcessorBullet;
import nifbullet.NavigationProcessorBullet.NbccProvider;
import nifbullet.cha.NBControlledChar;
import scrollsexplorer.ScrollsExplorer;
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
import tools3d.mixed3d2d.curvehud.elements.HUDCrossHair;
import tools3d.mixed3d2d.curvehud.elements.HUDFPSCounter;
import tools3d.mixed3d2d.curvehud.elements.HUDPosition;
import tools3d.navigation.AvatarCollisionInfo;
import tools3d.navigation.AvatarLocation;
import tools3d.navigation.NavigationInputNewtKey;
import tools3d.navigation.NavigationInputNewtMouseLocked;
import tools3d.navigation.NavigationTemporalBehaviour;
import tools3d.ovr.OculusException;
import tools3d.universe.VisualPhysicalUniverse;
import tools3d.utils.scenegraph.LocationUpdateListener;
import utils.source.MeshSource;

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

//	private JFrame frame = new JFrame();

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

	private NavigationInputNewtKey keyNavigationInputNewt;

	private NavigationInputNewtMouseLocked newtMouseInputListener;

	private NewtJumpKeyListener jumpKeyListener;

	private NewtMiscKeyHandler newtMiscKeyHandler = new NewtMiscKeyHandler();

	private boolean showHavok = false;

	private boolean showVisual = true;

	private HUDFPSCounter fpsCounter;

	private HUDCompass hudcompass;

	private HUDCrossHair hudCrossHair;

	private HUDPosition hudPos;

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

	 

	//Can't use as threading causes massive trouble for scene loading
	//	private StructureUpdateBehavior structureUpdateBehavior;

	private NbccProvider nbccProvider = new NbccProvider() {
		@Override
		public NBControlledChar getNBControlledChar()
		{
			return physicsSystem.getNBControlledChar();
		}
	};

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
		keyNavigationInputNewt = new NavigationInputNewtKey(navigationProcessor);
		NavigationInputNewtKey.VERTICAL_RATE = 50f;

		//mouseInputListener = new NavigationInputAWTMouseLocked();
		//mouseInputListener.setNavigationProcessor(navigationProcessor);
		newtMouseInputListener = new NavigationInputNewtMouseLocked();
		newtMouseInputListener.setNavigationProcessor(navigationProcessor);

		// dont' start mouse locked as its a pain
		//mouseInputListener.setCanvas(cameraPanel.getCanvas3D2D());

		//add jump key and vis/phy toggle key listeners for fun ************************
		jumpKeyListener = new NewtJumpKeyListener(nbccProvider);

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

		//frame.setTitle(frameName);

		//frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		universe.addToBehaviorBranch(behaviourBranch);

		// Add a ShaderErrorListener
		universe.addShaderErrorListener(new ShaderErrorListener() {
			public void errorOccurred(ShaderError error)
			{
				error.printVerbose();
				JOptionPane.showMessageDialog(null, error.toString(), "ShaderError", JOptionPane.ERROR_MESSAGE);
			}
		});

		/*
		
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
				});*/
	}

	 
	/**
	 * Only for listening to shutdown
	 * @return
	 */
	public Window getWindow()
	{
		return cameraPanel.getCanvas3D2D().getGLWindow();
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

//	GraphicsSettings gs = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(SimpleWalkSetup.class), frame, false, true,
//				false, false);
//		if (gs != null)
		{
			setupGraphicsSetting(null);
		}
		
		//TODO: these must come form a new one of those ^
		
		DDSTextureLoader.setAnisotropicFilterDegree(8);
		cameraPanel.getCanvas3D2D().getGLWindow().setSize(1600, 1200);
		
		//frame.setSize(100,100);// oddly still needed
		
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

			HMD_MODE = false;//gs.isOculusView();

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
					cameraPanel.getCanvas3D2D().getGLWindow().addKeyListener(new HMDKeyHandler(hcd));
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

			//frame.getContentPane().add((JPanel) cameraPanel);

			avatarLocation.addAvatarLocationListener(cameraPanel.getDolly());
			cameraPanel.getDolly().locationUpdated(avatarLocation.get(new Quat4f()), avatarLocation.get(new Vector3f()));
			//cameraPanel.getDolly().setHudShape(cameraPanel.getCanvas3D2D().getHudShapeRoot());

//			DDSTextureLoader.setAnisotropicFilterDegree(gs.getAnisotropicFilterDegree());
//			cameraPanel.setSceneAntialiasingEnable(gs.isAaRequired());

			Canvas3D2D canvas3D2D = cameraPanel.getCanvas3D2D();
			canvas3D2D.getGLWindow().addKeyListener(keyNavigationInputNewt);
			canvas3D2D.getGLWindow().addKeyListener(jumpKeyListener);
			canvas3D2D.getGLWindow().addKeyListener(newtMiscKeyHandler);

			fpsCounter.addToCanvas(canvas3D2D);
			hudPos.addToCanvas(canvas3D2D);
			hudcompass.addToCanvas(canvas3D2D);
			//hudPhysicsState.addToCanvas(canvas3D2D);
			hudCrossHair.addToCanvas(canvas3D2D);

			//allow tab for mouse lock
//			canvas3D2D.setFocusTraversalKeysEnabled(false);

//			canvas3D2D.addComponentListener(canvasResizeListener);

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
/*		GraphicsSettings gs = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(SimpleWalkSetup.class), frame, false,
				false, true, false);
		if (gs != null)
		{
			setupGraphicsSetting(gs);
		}*/
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
				//frame.setVisible(true);
				cameraPanel.startRendering();
				
				cameraPanel.getCanvas3D2D().addNotify();
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
		keyNavigationInputNewt.setAllowVerticalMovement(ff);
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
			NavigationInputNewtKey.FORWARD_KEY = KeyEvent.VK_Z;
			//NavigationInputAWTKey.FAST_KEY = KeyEvent.VK_E;
			//NavigationInputAWTKey.BACK_KEY = KeyEvent.VK_S;
			NavigationInputNewtKey.LEFT_KEY = KeyEvent.VK_Q;
			//NavigationInputAWTKey.RIGHT_KEY = KeyEvent.VK_D;
			NavigationInputNewtKey.UP_KEY = KeyEvent.VK_A;
			NavigationInputNewtKey.DOWN_KEY = KeyEvent.VK_W;
		}
		else
		{
			NavigationInputNewtKey.FORWARD_KEY = KeyEvent.VK_W;
			//NavigationInputAWTKey.FAST_KEY = KeyEvent.VK_E;
			//NavigationInputAWTKey.BACK_KEY = KeyEvent.VK_S;
			NavigationInputNewtKey.LEFT_KEY = KeyEvent.VK_A;
			//NavigationInputAWTKey.RIGHT_KEY = KeyEvent.VK_D;
			NavigationInputNewtKey.UP_KEY = KeyEvent.VK_Q;
			NavigationInputNewtKey.DOWN_KEY = KeyEvent.VK_Z;
		}
	}

	private void setMouseLock(boolean mouseLock)
	{
		if (!mouseLock)
		{
			//mouseInputListener.setCanvas(null);
			newtMouseInputListener.setWindow(null);

			//note tab message only put up if tab used to unlock mouse
		}
		else
		{
			//mouseInputListener.setCanvas(cameraPanel.getCanvas3D2D());
			newtMouseInputListener.setWindow(cameraPanel.getCanvas3D2D().getGLWindow());

			// always clear the tab message regardless
			if (firstInstruction != null)
			{
				firstInstruction.removeFromCanvas();
			}
		}
	}

	private class HMDKeyHandler implements KeyListener
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

		@Override
		public void keyReleased(KeyEvent e)
		{

		}
	}

	private class NewtMiscKeyHandler implements KeyListener
	{
		public NewtMiscKeyHandler()
		{
			/*System.out.println("Esc exit");
			System.out.println("H toggle havok display");
			System.out.println("L toggle visual display");
			System.out.println("TAB toggle mouse lock");
			System.out.println("F toggle freefly");
			System.out.println("J display jbullet debug");*/
		}

		public void keyPressed(com.jogamp.newt.event.KeyEvent e)
		{
			if (e.getKeyCode() == com.jogamp.newt.event.KeyEvent.VK_ESCAPE)
			{
				System.out.println("Need a new exit dialog system");
				/*if (!exitDialogPane3D.isVisible())
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
				}*/
			}
			else if (e.getKeyCode() == com.jogamp.newt.event.KeyEvent.VK_H)
			{
				toggleHavok();
			}
			else if (e.getKeyCode() == com.jogamp.newt.event.KeyEvent.VK_L)
			{
				toggleVisual();
			}
			else if (e.getKeyCode() == com.jogamp.newt.event.KeyEvent.VK_F)
			{
				freefly = !freefly;
				setFreeFly(freefly);
			}
			else if (e.getKeyCode() == com.jogamp.newt.event.KeyEvent.VK_J)
			{
				physicsSystem.setDisplayDebug(true);
			}
			else if (e.getKeyCode() == com.jogamp.newt.event.KeyEvent.VK_TAB)
			{
				if (newtMouseInputListener.hasGLWindow())
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
			else if (e.getKeyCode() == com.jogamp.newt.event.KeyEvent.VK_I)
			{
				// simpleInventorySystem has a listener for the mouse lock
				System.out.println("Need a new inventory system");
			}
		}

		@Override
		public void keyReleased(com.jogamp.newt.event.KeyEvent arg0)
		{

		}
	}

}
