
package ch.sleod.testautomation.framework.gui.models;

import javax.swing.table.DefaultTableModel;


public class UnEditableTableModel extends DefaultTableModel {

	@Override
	public boolean isCellEditable(int x, int y) {
		return false; //To change body of generated methods, choose Tools | Templates.
	}
}
