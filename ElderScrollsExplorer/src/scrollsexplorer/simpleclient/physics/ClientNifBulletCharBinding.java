package scrollsexplorer.simpleclient.physics;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import nifbullet.cha.KinematicCharacterController2.CharacterPositionListener;
import nifbullet.cha.NBControlledChar;
import tools3d.navigation.AvatarLocation;

/**
 * Interface object to register with nifbullet for updates and pass them to the space trader model
 * @author philip
 *
 */
public class ClientNifBulletCharBinding implements NifBulletBinding, CharacterPositionListener
{
	protected boolean updateRequired = false;

	protected Vector3f newTranslation = new Vector3f();

	protected Quat4f newRotation = new Quat4f();

	//deburners
	protected Vector3f currTranslation = new Vector3f();

	protected Quat4f currRotation = new Quat4f();

	private AvatarLocation avatarLocation;

	public ClientNifBulletCharBinding(AvatarLocation avatarLocation, NBControlledChar nifBulletChar)
	{
		nifBulletChar.setCharacterPositionListener(this);
		this.avatarLocation = avatarLocation;
	}

	@Override
	public synchronized void applyToModel()
	{
		if (updateRequired)
		{
			if (avatarLocation != null)
			{
				// get current out for check
				//avatarLocation.get(currRotation, currTranslation);
				//if (!currTranslation.epsilonEquals(newTranslation, 0.001f))
				{
					//ignore rotation from kcc, for some reason it's always late
					//avatarLocation.set(newRotation, newTranslation);
					avatarLocation.setTranslation(newTranslation);
				}
			}
			else
			{
				System.out.println("Why is avatarLocation null for ClientNifBulletBinding " + this);
			}
			updateRequired = false;
		}
	}

	@Override
	public synchronized void positionChanged(Vector3f newPosition2, Quat4f newRotation2)
	{
		if (Float.isNaN(newPosition2.x))
		{
			System.out.println("NAN detected in ServerNifBulletBinding.setTransform position!");
			return;
		}

		this.newTranslation.set(newPosition2);
		this.newRotation.set(newRotation2);

		updateRequired = true;
	}
}
