package logic;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import gui.MainGui;
import gui.PopupAlerts;

public class MouseAdapters
{
	public static MouseInputAdapter getAddContentRowAdapter(int row_to_add, Section section)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					section.addContentLine(row_to_add);
					MainGui.arrangeContent();
					MainGui.spaceColums();
				}
			}
		};
	}
	
	public static MouseInputAdapter getRemoveContentRowAdapter(int row_to_remove, Section section)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					section.removeContentLine(row_to_remove);
					MainGui.arrangeContent();
					MainGui.spaceColums();
				}
			}
		};
	}
	
	public static ActionListener getAddContentColAdapter(int col_to_add)
	{
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					FileOperations.numberOfColumns ++;
					for (Section section : MainGui.sectionsList)
						section.addContentColumn(col_to_add);
					MainGui.arrangeContent();
					MainGui.spaceColums();
				}
			}
		};
	}
	
	public static ActionListener getRemoveContentColAdapter(int col_to_remove)
	{
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					FileOperations.numberOfColumns --;
					for (Section section : MainGui.sectionsList)
						section.removeContentColumn(col_to_remove);
					MainGui.arrangeContent();
					MainGui.spaceColums();
				}
			}
		};
	}
	
	public static MouseInputAdapter getAddSectionAdapter(int current_section_index)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					MainGui.sectionsList.add(current_section_index, new Section() );
					MainGui.arrangeContent();
					MainGui.spaceColums();
					MainGui.updateSectionManagerDialog();
				}
			}
		};
	}
	
	public static MouseInputAdapter getRemoveSectionAdapter(int current_section_index)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					MainGui.sectionsList.remove(current_section_index);
					MainGui.arrangeContent();
					MainGui.spaceColums();
					MainGui.updateSectionManagerDialog();
				}
			}
		};
	}
	
	public static MouseInputAdapter getEditSectionTitleAdapter(JLabel label, int current_section_index)
	{
		return new MouseInputAdapter() {
			@Override
	    public void mouseClicked(MouseEvent e)
			{
				String newText = JOptionPane.showInputDialog(MainGui.window, "Set the section title!", label.getText() );
				if (newText != null)
				{
					label.setText(newText);
					MainGui.sectionsList.get(current_section_index).setTitle(newText);
					MainGui.arrangeContent();
					MainGui.spaceColums();
					MainGui.updateSectionManagerDialog();
					FileOperations.unsavedChanges = true;
				}
			}
		};
	}

	public static MouseInputAdapter getEditCellAdapter(JPanel cell, String current_text, String[] row, int col)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
				{
					String string = JOptionPane.showInputDialog(MainGui.window, "Set the text!", current_text);
					if (string != null && !string.equals(current_text) )
					{
						// resetting cell
						cell.removeAll();
						cell.removeMouseListener(this);
						
						// updating content of section
						row[col] = string; 
						
						// refilling cell
						Section.fillCellPanel(cell, string.split(">>", 2)[0] );
						if (string.contains(">>") )
							for (String action_str : string.split(">>", 2)[1].split("#", -1) )
								cell.addMouseListener(getCellActionAdapter(action_str) );
						cell.addMouseListener(getEditCellAdapter(cell, string, row, col) );
						
						// reorganizing gui
						MainGui.spaceColums();
						FileOperations.unsavedChanges = true;
					}
				}
			}
		};
	}
	
	public static MouseInputAdapter getEditTodoAdapter(int current_row, Section section, JPanel panel, JLabel label, JLabel icon)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				String string = JOptionPane.showInputDialog(MainGui.window, "TODO", section.getTodo().get(current_row) );
				if (string != null)
				{
					section.getTodo().set(current_row, string);
					FileOperations.unsavedChanges = true;
					
					if (string.isEmpty() && icon != null) // remove existing icon
					{
						panel.remove(icon);
						MainGui.labelsHideUnhide.remove(icon);
						label.removeMouseListener(this);
						label.addMouseListener(getEditTodoAdapter(current_row, section, panel, label, null) );
					}
					if (!string.isEmpty() && icon == null) // add new icon
					{
						JLabel new_icon = new JLabel(new ImageIcon(new ImageIcon("Images\\Not-enough-repair-packs-icon" + ".png").getImage().getScaledInstance(MainGui.ImageSize, MainGui.ImageSize, Image.SCALE_DEFAULT) ) );
						panel.add(new_icon);
						new_icon.setVisible(MainGui.inEditMode);
						MainGui.labelsHideUnhide.add(new_icon);
						label.removeMouseListener(this);
						label.addMouseListener(getEditTodoAdapter(current_row, section, panel, label, new_icon) );
					}
				}

				MainGui.arrangeContent();
				MainGui.spaceColums();
			}
		};
	}

	public static WindowAdapter getWindowOnCloseAdapter()
	{
		return new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (FileOperations.unsavedChanges)
					PopupAlerts.unsavedChangesDialog();
				else
					MainGui.exit();
			}
		};
	}
	
	public static MouseInputAdapter getCellActionAdapter(String action_str)
	{
		String[] actions = action_str.split("#");
		for (String action : actions)
		{
			String action_command = "", action_param = "";
			try {
				String[] temp = action.split(":");
				action_command = temp[0];
				action_param = temp[1];
			}
			catch (Exception e)
			{
				throw new RuntimeException("Each action has to follow the syntax 'action_command':'action_parameter'!");
			}
			
			switch (action_command)
			{
				case "write_to_clipboard": return getActionWriteToClpiboardAdapter(action_param);
			}
		}
		
		return null;
	}
	
	public static MouseInputAdapter getActionWriteToClpiboardAdapter(String textToWrite)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				if (!MainGui.inEditMode)
		      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textToWrite), null);
			}
		};
	}
}