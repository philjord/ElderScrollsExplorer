package scrollsexplorer;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import scrollsexplorer.simpleclient.physics.PhysicsSystemStatus;

public class Dashboard extends JPanel
{
	private JLabel esmLoadingLabel = new JLabel("Esm");

	private JLabel cellLoadingLabel = new JLabel("Cell");

	private JLabel nearLoadingLabel = new JLabel("Near");

	private JLabel farLoadingLabel = new JLabel("Far");

	private JLabel lodLoadingLabel = new JLabel("Lod");

	private PhysicsSystemStatus physicsSystemStatus;

	private int esmLoading = 0;

	private int cellLoading = 0;

	private int nearLoading = 0;

	private int farLoading = 0;

	private int lodLoading = 0;

	public Dashboard()
	{
		this.setLayout(new GridLayout(-1, 1, 5, 10));
		add(esmLoadingLabel);
		esmLoadingLabel.setBackground(Color.red);
		esmLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		esmLoadingLabel.setOpaque(false);
		add(cellLoadingLabel);
		cellLoadingLabel.setBackground(Color.red);
		cellLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		cellLoadingLabel.setOpaque(false);
		add(nearLoadingLabel);
		nearLoadingLabel.setBackground(Color.red);
		nearLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		nearLoadingLabel.setOpaque(false);
		add(farLoadingLabel);
		farLoadingLabel.setBackground(Color.red);
		farLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		farLoadingLabel.setOpaque(false);
		add(lodLoadingLabel);
		lodLoadingLabel.setBackground(Color.red);
		lodLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		lodLoadingLabel.setOpaque(false);

	}

	public void setPhysicSystem(PhysicsSystem physicsSystem)
	{
		physicsSystemStatus = new PhysicsSystemStatus(physicsSystem);
		add(physicsSystemStatus);
		invalidate();
		doLayout();
		repaint();
	}

	public void setEsmLoading(int isLoading)
	{
		esmLoading += isLoading;
		esmLoadingLabel.setOpaque(esmLoading > 0);
		repaint();
	}

	public void setCellLoading(int isLoading)
	{
		cellLoading += isLoading;
		cellLoadingLabel.setOpaque(cellLoading > 0);
		repaint();
	}

	public void setNearLoading(int isLoading)
	{
		nearLoading += isLoading;
		nearLoadingLabel.setOpaque(nearLoading > 0);
		repaint();
	}

	public void setFarLoading(int isLoading)
	{
		farLoading += isLoading;
		farLoadingLabel.setOpaque(farLoading > 0);
		repaint();
	}

	public void setLodLoading(int isLoading)
	{
		lodLoading += isLoading;
		lodLoadingLabel.setOpaque(lodLoading > 0);
		repaint();
	}
}
