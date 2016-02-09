package scrollsexplorer;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import scrollsexplorer.simpleclient.physics.PhysicsSystem;
import scrollsexplorer.simpleclient.physics.PhysicsSystemStatus;

public class Dashboard extends IDashboard
{
	private JPanel mainPanel = new JPanel();

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
		scrollsexplorer.IDashboard.dashboard = this;
		mainPanel.setLayout(new FlowLayout());
		mainPanel.add(esmLoadingLabel);
		esmLoadingLabel.setBackground(Color.red);
		esmLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		esmLoadingLabel.setOpaque(false);
		mainPanel.add(cellLoadingLabel);
		cellLoadingLabel.setBackground(Color.red);
		cellLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		cellLoadingLabel.setOpaque(false);
		mainPanel.add(nearLoadingLabel);
		nearLoadingLabel.setBackground(Color.red);
		nearLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		nearLoadingLabel.setOpaque(false);
		mainPanel.add(farLoadingLabel);
		farLoadingLabel.setBackground(Color.red);
		farLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		farLoadingLabel.setOpaque(false);
		mainPanel.add(lodLoadingLabel);
		lodLoadingLabel.setBackground(Color.red);
		lodLoadingLabel.setBorder(new BevelBorder(BevelBorder.RAISED, Color.gray, Color.DARK_GRAY));
		lodLoadingLabel.setOpaque(false);

	}

	public JPanel getMainPanel()
	{
		return mainPanel;
	}

	public void setPhysicSystem(PhysicsSystem physicsSystem)
	{
		physicsSystemStatus = new PhysicsSystemStatus(physicsSystem);
		mainPanel.add(physicsSystemStatus);
		mainPanel.invalidate();
		mainPanel.doLayout();
		mainPanel.repaint();
	}

	public void setEsmLoading(int isLoading)
	{
		esmLoading += isLoading;
		esmLoadingLabel.setOpaque(esmLoading > 0);
		mainPanel.repaint();
	}

	public void setCellLoading(int isLoading)
	{
		cellLoading += isLoading;
		cellLoadingLabel.setOpaque(cellLoading > 0);
		mainPanel.repaint();
	}

	public void setNearLoading(int isLoading)
	{
		nearLoading += isLoading;
		nearLoadingLabel.setOpaque(nearLoading > 0);
		mainPanel.repaint();
	}

	public void setFarLoading(int isLoading)
	{
		farLoading += isLoading;
		farLoadingLabel.setOpaque(farLoading > 0);
		mainPanel.repaint();
	}

	public void setLodLoading(int isLoading)
	{
		lodLoading += isLoading;
		lodLoadingLabel.setOpaque(lodLoading > 0);
		mainPanel.repaint();
	}
}
