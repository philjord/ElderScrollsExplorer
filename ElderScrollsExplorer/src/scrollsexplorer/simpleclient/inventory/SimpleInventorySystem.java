package scrollsexplorer.simpleclient.inventory;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import tools3d.mixed3d2d.overlay.swing.JInternalFrame3D;
import tools3d.mixed3d2d.overlay.swing.Panel3D;

public class SimpleInventorySystem extends JInternalFrame3D
{
	private JCheckBox jcb = new JCheckBox("tickable item 2");

	public SimpleInventorySystem(Panel3D panel3D)
	{
		super(panel3D, "Simple Inventory", 300, 200, true);
		setLayout(new GridLayout(5, 3));

		setLocation(300, 0);
		add(new JLabel("Inventory item one"));
		add(jcb);

		getContentPane().doLayout();
		setVisible(false);
	}

}
