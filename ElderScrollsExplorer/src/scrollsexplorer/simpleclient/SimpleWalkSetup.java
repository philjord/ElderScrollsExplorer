package scrollsexplorer.simpleclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
import scrollsexplorer.simpleclient.mouseover.ActionableMouseOverHandler;
import scrollsexplorer.simpleclient.mouseover.AdminMouseOverHandler;
import scrollsexplorer.simpleclient.physics.ClientPhysicsSystem;
import scrollsexplorer.simpleclient.physics.InstRECOStore;
import tools3d.camera.CameraPanel;
import tools3d.camera.HeadCamDolly;
import tools3d.mixed3d2d.hud.hudelements.HUDCompass;
import tools3d.mixed3d2d.hud.hudelements.HUDFPSCounter;
import tools3d.mixed3d2d.hud.hudelements.HUDPosition;
import tools3d.navigation.AvatarCollisionInfo;
import tools3d.navigation.AvatarLocation;
import tools3d.navigation.NavigationInputAWTKey;
import tools3d.navigation.NavigationInputAWTMouseLocked;
import tools3d.navigation.NavigationTemporalBehaviour;
import tools3d.universe.VisualPhysicalUniverse;
import tools3d.utils.Utils3D;
import tools3d.utils.resolution.GraphicsSettings;
import tools3d.utils.resolution.ScreenResolution;
import tools3d.utils.scenegraph.LocationUpdateListener;
import utils.source.MeshSource;
import esmj3d.j3d.j3drecords.inst.J3dRECOInst;

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

	private JFrame frame = new JFrame();

	public VisualPhysicalUniverse universe;

	private BranchGroup modelGroup = new BranchGroup();

	private BranchGroup physicsGroup;

	private BranchGroup visualGroup;

	private BranchGroup behaviourBranch;

	private NavigationTemporalBehaviour navigationTemporalBehaviour;

	private NavigationProcessorBullet navigationProcessor;

	private CameraPanel cameraPanel;

	private HeadCamDolly headCamDolly;

	private AvatarLocation avatarLocation = new AvatarLocation();

	private AvatarCollisionInfo avatarCollisionInfo = new AvatarCollisionInfo(avatarLocation, 0.5f, 1.8f, 0.35f, 0.8f);

	private NavigationInputAWTKey keyNavigationInputAWT;

	private NavigationInputAWTMouseLocked mouseInputListener;

	private JumpKeyListener jumpKeyListener;

	private MiscKeyHandler miscKeyHandler = new MiscKeyHandler();

	private boolean showHavok = true;

	private boolean showVisual = true;

	private HUDFPSCounter fpsCounter;

	private HUDPosition hudPos;

	private HUDCompass hudcompass;

	private ClientPhysicsSystem clientPhysicsSystem;

	private ActionableMouseOverHandler cameraMouseOver;

	private AdminMouseOverHandler cameraAdminMouseOverHandler;

	private PhysicsUpdateBehaviour currentPhysicsUpdateBehaviour;

	private JTextField locField = new JTextField("0000,0000,0000");

	private NbccProvider nbccProvider = new NbccProvider()
	{
		@Override
		public NBControlledChar getNBControlledChar()
		{
			return clientPhysicsSystem.getNBControlledChar();
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
		modelGroup.addChild(physicsGroup);

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

		//create the camera panel ************************
		cameraPanel = new CameraPanel(universe);
		// and the dolly it rides on
		headCamDolly = new HeadCamDolly(avatarCollisionInfo);
		cameraPanel.setDolly(headCamDolly);
		avatarLocation.addAvatarLocationListener(headCamDolly);
		headCamDolly.locationUpdated(avatarLocation.get(new Quat4f()), avatarLocation.get(new Vector3f()));

		//now we have timekeep and camera panel add mouse and keyboard inputs ************************
		keyNavigationInputAWT = new NavigationInputAWTKey(navigationProcessor);
		NavigationInputAWTKey.VERTICAL_RATE = 50f;
		cameraPanel.getCanvas3D2D().addKeyListener(keyNavigationInputAWT);

		mouseInputListener = new NavigationInputAWTMouseLocked();
		mouseInputListener.setNavigationProcessor(navigationProcessor);
		// dont' start mouse locked as its a pain
		//mouseInputListener.setCanvas(cameraPanel.getCanvas3D2D());

		//add jump key and vis/phy toggle key listenres for fun ************************
		jumpKeyListener = new JumpKeyListener(nbccProvider);
		cameraPanel.getCanvas3D2D().addKeyListener(jumpKeyListener);
		cameraPanel.getCanvas3D2D().addKeyListener(miscKeyHandler);

		//just an fps for fun
		fpsCounter = new HUDFPSCounter();
		hudPos = new HUDPosition();
		hudcompass = new HUDCompass();

		universe.addToBehaviorBranch(fpsCounter.getBehaviorBranchGroup());
		fpsCounter.addToCanvas(cameraPanel.getCanvas3D2D());
		hudPos.addToCanvas(cameraPanel.getCanvas3D2D());
		hudcompass.addToCanvas(cameraPanel.getCanvas3D2D());
		avatarLocation.addAvatarLocationListener(hudPos);
		avatarLocation.addAvatarLocationListener(hudcompass);

		avatarLocation.addAvatarLocationListener(this);

		locField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String[] parts = locField.getText().split(",");
				warp(new Vector3f(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
			}
		});

		//allow tab for mouse lock
		cameraPanel.getCanvas3D2D().setFocusTraversalKeysEnabled(false);

		frame.setTitle(frameName);
		frame.getContentPane().add(cameraPanel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		universe.addToBehaviorBranch(behaviourBranch);

		headCamDolly.getPlatformGeometry().addChild(cameraPanel.getCanvas3D2D().getHudShapeRoot());

	}

	@Override
	public void locationUpdated(Quat4f rot, Vector3f trans)
	{
		locField.setText(("" + trans.x).split("\\.")[0] + "," + ("" + trans.y).split("\\.")[0] + "," + ("" + trans.z).split("\\.")[0]);
	}

	public void warp(Vector3f origin)
	{
		clientPhysicsSystem.getNBControlledChar().getCharacterController().warp(origin);

	}

	public void configure(MeshSource meshSource)
	{
		// set up and run the physics system************************************************
		InstRECOStore charChangeListener = new InstRECOStore()
		{
			@Override
			public void applyCharChange(J3dRECOInst instReco, Quat4f newRotation, Vector3f newTranslation)
			{
				System.out.println("do somethig here?");
			}

		};

		clientPhysicsSystem = new ClientPhysicsSystem(charChangeListener, avatarLocation, behaviourBranch, meshSource);

		currentPhysicsUpdateBehaviour = new PhysicsUpdateBehaviour(new Runnable()
		{
			@Override
			public void run()
			{
				clientPhysicsSystem.physicsTick();
			}
		});
		BranchGroup bg = new BranchGroup();
		bg.addChild(currentPhysicsUpdateBehaviour);
		behaviourBranch.addChild(bg);

		cameraMouseOver = new ActionableMouseOverHandler(clientPhysicsSystem);

		cameraAdminMouseOverHandler = new AdminMouseOverHandler(clientPhysicsSystem);

		GraphicsSettings dlg = ScreenResolution.organiseResolution(Preferences.userNodeForPackage(SimpleWalkSetup.class), frame, false, true, false);
		cameraPanel.getCanvas3D2D().getView().setSceneAntialiasingEnable(dlg.isAaRequired());
	}

	public void setEnabled(boolean enable)
	{
		System.out.println("setEnabled " + enable);
		// start the processor up ************************
		navigationProcessor.setActive(enable);
		currentPhysicsUpdateBehaviour.setEnable(enable);
		if (enable)
		{
			cameraMouseOver.setConfig(cameraPanel.getCanvas3D2D());
			universe.addToBehaviorBranch(cameraMouseOver);
			cameraAdminMouseOverHandler.setConfig(cameraPanel.getCanvas3D2D());
			universe.addToBehaviorBranch(cameraAdminMouseOverHandler);
			frame.setVisible(true);
		}
		else
		{
			cameraMouseOver.setConfig(null);
			universe.removeFromBehaviorBranch(cameraMouseOver);
			cameraAdminMouseOverHandler.setConfig(null);
			universe.removeFromBehaviorBranch(cameraAdminMouseOverHandler);
			frame.setVisible(false);
		}
		cameraPanel.startRendering();
	}

	public void setFreeFly(boolean ff)
	{
		if (clientPhysicsSystem.getNBControlledChar() != null)
		{
			clientPhysicsSystem.getNBControlledChar().getCharacterController().setFreeFly(ff);
		}
		keyNavigationInputAWT.setAllowVerticalMovement(ff);
	}

	public ClientPhysicsSystem getClientPhysicsSystem()
	{
		return clientPhysicsSystem;
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

	public JTextField getLocField()
	{
		return locField;
	}

	public void setPhysicsEnabled(boolean enable)
	{
		currentPhysicsUpdateBehaviour.setEnable(enable);
	}

	private class MiscKeyHandler extends KeyAdapter
	{

		public MiscKeyHandler()
		{
			System.out.println("H toggle havok display");
			System.out.println("L toggle visual display");
			System.out.println("TAB toggle mouse lock");
			System.out.println("F toggle freefly");
			System.out.println("J display jbullet debug");

		}

		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you wish to exit?");
				if (result == JOptionPane.OK_OPTION)
				{
					System.exit(0);
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
				clientPhysicsSystem.getPhysicsLocaleDynamics().setDisplayDebug(true);
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

	public class PhysicsUpdateBehaviour extends Behavior
	{
		private WakeupCondition FPSCriterion = new WakeupOnElapsedFrames(0, false);

		private Runnable runner;

		public PhysicsUpdateBehaviour(Runnable runner)
		{
			this.runner = runner;
			setSchedulingBounds(Utils3D.defaultBounds);
			setEnable(false);
		}

		public void initialize()
		{
			wakeupOn(FPSCriterion);
		}

		@SuppressWarnings(
		{ "unchecked", "rawtypes" })
		public void processStimulus(Enumeration criteria)
		{
			if (runner != null)
			{
				long start = System.currentTimeMillis();
				runner.run();
				if ((System.currentTimeMillis() - start) > 50)
					System.out.println("Update behave took " + (System.currentTimeMillis() - start));
				wakeupOn(FPSCriterion);
			}
		}
	}

}
