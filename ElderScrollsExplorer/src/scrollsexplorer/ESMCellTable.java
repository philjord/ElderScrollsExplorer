package scrollsexplorer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import org.jogamp.vecmath.Quat4f;
import org.jogamp.vecmath.Vector3f;

import esfilemanager.common.PluginException;
import esfilemanager.common.data.plugin.IMaster;
import esfilemanager.common.data.plugin.PluginRecord;
import esfilemanager.loader.FormToFilePointer;
import tools3d.utils.YawPitch;

public class ESMCellTable extends JTable {

	protected boolean			HIDE_WIP_CELLS	= false;

	private ScrollsExplorer		scrollsExplorer;
	private DefaultTableModel	tableModel;

	private String[]			columnNames		= new String[] {"File", "Int/Ext", "Cell Id", "Name"};

	public ESMCellTable(ScrollsExplorer scrollsExplorer) {
		this.scrollsExplorer = scrollsExplorer;
		tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // disallow editing of the table
			}

			@Override
			public Class<? extends Object> getColumnClass(int c) {
				if (getValueAt(0, c) != null)
					return getValueAt(0, c).getClass();
				else
					return Object.class;
			}
		};

		this.setModel(tableModel);

		this.setRowSorter(new TableRowSorter<DefaultTableModel>(tableModel));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int cellFormId = ((Integer)tableModel
						.getValueAt(ESMCellTable.this.convertRowIndexToModel(ESMCellTable.this.getSelectedRow()), 2));
				YawPitch yp = new YawPitch();
				Vector3f trans = new Vector3f();

				// firstly set our location to a door in the newly clicked cell (cos where else is sensible?)
				ScrollsExplorerNewt.findADoor(cellFormId, scrollsExplorer.getSelectedGameConfig(),
						scrollsExplorer.getEsmManager(), trans, yp);
				scrollsExplorer.getSimpleWalkSetup().getAvatarLocation().set(yp.get(new Quat4f()), trans);
				scrollsExplorer.display(cellFormId);

			}

		});
	}

	public void loadTableCells(GameConfig newGameConfig, int prevCellformid) {

		// if we are already laoded start by unloading
		tableModel.getDataVector().clear();

		try {
			// show which esm files have this cell referred
			HashMap<Integer, StringBuffer> loadedIds = new HashMap<Integer, StringBuffer>();

			for (IMaster master : scrollsExplorer.getEsmManager().getMasters()) {
				//TODO: tribunal and bloodmoon are finding nothing but 0??

				// add Ext
				for (Integer formId : master.getAllWRLDTopGroupFormIds()) {
					StringBuffer sb = loadedIds.get(formId);
					if (sb == null) {
						sb = new StringBuffer(master.getName());
						PluginRecord pr = master.getWRLD(formId);

						if (HIDE_WIP_CELLS) {
							//Fallout4 has lots of unnamed cells that seem generally bu and lots of COPY1234 also bum, and PackIn...
							// presumably there's some sort of folder structure I'm missing?
							if (pr.getEditorID().startsWith("COPY") || pr.getEditorID().startsWith("PackIn"))
								continue;

							//Fallout76
							if (pr.getEditorID().length() == 0	|| pr.getEditorID().startsWith("Test")
								|| pr.getEditorID().startsWith("TEST") || pr.getEditorID().startsWith("Debug")
								|| pr.getEditorID().startsWith("zCUT"))
								continue;
						}

						if (prevCellformid == formId)
							tableModel.insertRow(0, new Object[] {sb, "Ext", formId, pr});
						else
							tableModel.addRow(new Object[] {sb, "Ext", formId, pr});

						loadedIds.put(formId, sb);
					} else {
						// so this makes the File column value for this row be updated with both esm names
						sb.append("/" + master.getName());
					}

				}

				//add Int
				for (FormToFilePointer cp : master.getAllInteriorCELLFormIds()) {
					int formId = cp.formId;
					StringBuffer sb = loadedIds.get(formId);
					// check if this id has already been put in the table, if not...
					if (sb == null) {
						sb = new StringBuffer(master.getName());
						PluginRecord pr = master.getInteriorCELL(formId);

						if (HIDE_WIP_CELLS) {
							//Fallout4 has lots of unnamed cells that seem generally bu and lots of COPY1234 also bum, and PackIn...
							// presumably there's some sort of folder structure I'm missing?
							if (pr.getEditorID().startsWith("COPY") || pr.getEditorID().startsWith("PackIn"))
								continue;

							//Fallout76
							if (pr.getEditorID().length() == 0	|| pr.getEditorID().startsWith("Test")
								|| pr.getEditorID().startsWith("TEST") || pr.getEditorID().startsWith("Debug")
								|| pr.getEditorID().startsWith("zCUT"))
								continue;
						}

						if (prevCellformid == formId)
							tableModel.insertRow(0, new Object[] {sb, "Int", formId, pr});
						else
							tableModel.addRow(new Object[] {sb, "Int", formId, pr});

						loadedIds.put(formId, sb);
					} else {
						// otherwise make the File column value for this row be updated with both esm names
						sb.append("/" + master.getName());
					}
				}
			}
		} catch (

		DataFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (PluginException e1) {
			e1.printStackTrace();
		}

		this.getColumnModel().getColumn(1).setMaxWidth(30);
		this.getColumnModel().getColumn(2).setMaxWidth(60);

	}
}
