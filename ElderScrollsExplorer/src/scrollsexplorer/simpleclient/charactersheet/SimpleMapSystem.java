package scrollsexplorer.simpleclient.charactersheet;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import awt.tools3d.mixed3d2d.overlay.swing.JInternalFrame3D;
import awt.tools3d.mixed3d2d.overlay.swing.Panel3D;

public class SimpleMapSystem extends JInternalFrame3D
{
	private JCheckBox jcb = new JCheckBox("tickable item 2");

	public SimpleMapSystem(Panel3D panel3D)
	{
		super(panel3D, "Simple Inventory", 300, 200, true);
		setLayout(new GridLayout(5, 3));

		setLocation(300, 0);
		add(new JLabel("Inventory item one"));
		add(jcb);

		getContentPane().doLayout();
		setVisible(false);
		
		//Morrowind use teh color of cell system to show teh uncovered map!
		
		// oblivion?
		
		//fallout mini wasteland nif file system?
		
		//skyrim map nif file system?
		
	}

}
