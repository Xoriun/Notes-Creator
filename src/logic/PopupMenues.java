package logic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import gui.MainGui;

public class PopupMenues
{
	private static JPopupMenu menu = initializePopupMenu();
	private static Cell currentCell;
	private static String copiedCellString = "";
	private static Cell[] neighbours;

	private static JMenuItem item_up;
	private static JMenuItem item_down;
	private static JMenuItem item_left;
	private static JMenuItem item_right;
	
	private static JPopupMenu initializePopupMenu()
	{
		JPopupMenu menu = new JPopupMenu();
		
		JMenuItem item_copy = new JMenuItem("Copy");
		JMenuItem item_paste = new JMenuItem("Paste");
		item_left = new JMenuItem("Switch Left");
		item_right = new JMenuItem("Switch Right");
		item_up = new JMenuItem("Switch Up");
		item_down = new JMenuItem("Switch Down");
		
		item_copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				copiedCellString = currentCell.getCellString();
			}
		});
		
		item_paste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentCell.updateCell(copiedCellString);
				MainGui.cellEditDialog.processCell(currentCell);
			}
		});
		
		menu.add(item_copy);
		menu.add(item_paste);
		menu.addSeparator();
		menu.add(item_up);
		menu.add(item_down);
		menu.add(item_left);
		menu.add(item_right);
		
		return menu;
	}
	
	private static void updateMoveItems()
	{
		// top
		for (ActionListener al : item_up.getActionListeners() )
			item_up.removeActionListener(al);
		item_up.setEnabled(neighbours[0] != null);
		if (neighbours[0] != null)
			item_up.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					String this_cell_str = currentCell.getCellString();
					String switch_cell_str = neighbours[0].getCellString();
					currentCell.updateCell(switch_cell_str);
					neighbours[0].updateCell(this_cell_str);
					
					currentCell = neighbours[0];
					MainGui.cellEditDialog.processCell(currentCell);
				}
			});

		// bottom
		for (ActionListener al : item_down.getActionListeners() )
			item_down.removeActionListener(al);
		item_down.setEnabled(neighbours[1] != null);
		if (neighbours[1] != null)
			item_down.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					String this_cell_str = currentCell.getCellString();
					String switch_cell_str = neighbours[1].getCellString();
					currentCell.updateCell(switch_cell_str);
					neighbours[1].updateCell(this_cell_str);
					
					currentCell = neighbours[1];
					MainGui.cellEditDialog.processCell(currentCell);
				}
			});

		// left
		for (ActionListener al : item_left.getActionListeners() )
			item_left.removeActionListener(al);
		item_left.setEnabled(neighbours[2] != null);
		if (neighbours[2] != null)
			item_left.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					String this_cell_str = currentCell.getCellString();
					String switch_cell_str = neighbours[2].getCellString();
					currentCell.updateCell(switch_cell_str);
					neighbours[2].updateCell(this_cell_str);
					
					currentCell = neighbours[2];
					MainGui.cellEditDialog.processCell(currentCell);
				}
			});

		// right
		for (ActionListener al : item_right.getActionListeners() )
			item_right.removeActionListener(al);
		item_right.setEnabled(neighbours[3] != null);
		if (neighbours[3] != null)
			item_right.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					String this_cell_str = currentCell.getCellString();
					String switch_cell_str = neighbours[3].getCellString();
					currentCell.updateCell(switch_cell_str);
					neighbours[3].updateCell(this_cell_str);
					
					currentCell = neighbours[3];
					MainGui.cellEditDialog.processCell(currentCell);
				}
			});
		
	}
	
	static void processCellRightClick(MouseEvent e)
	{
		// without the involeLater, the PopupMenue disappears immediately in most cases.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				currentCell = (Cell) e.getComponent();
				neighbours = currentCell.getNeighbours();
				updateMoveItems();
				menu.show(currentCell, e.getX(), e.getY() );
			}
		});
	}
}
