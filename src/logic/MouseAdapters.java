package logic;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import edit.CellEditDialog;
import gui.GuiHelper;
import gui.MainGui;
import gui.PopupAlerts;

public class MouseAdapters
{
	public static MouseInputAdapter getAddContentRowAdapter(Row row)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					
					// adding actual row
					row.getSection().addContentLine(row.getRowIndex() );
					
					// increasing the rowIndices of the following rows
					for (Row any_row : row.getSection().getRows() )
						if (any_row.getRowIndex() >= row.getRowIndex() )
							any_row.increaseRowIndex();
					
					// redrawing GUI
					MainGui.arrangeContent();
					MainGui.spaceColums();
				}
			}
		};
	}
	
	public static MouseInputAdapter getRemoveContentRowAdapter(Row row)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
				{
					FileOperations.unsavedChanges = true;
					
					// removing actual row
					row.getSection().removeContentLine(row.getRowIndex() );
					
					// decreasing the rowIndices of the following rows
					for (Row any_row : row.getSection().getRows() )
						if (any_row.getRowIndex() >= row.getRowIndex() )
							any_row.decreaseRowIndex();
					
					// redrawing GUI
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
					edit.SectionManagerDialog.updateSectionManagerDialog();
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
					edit.SectionManagerDialog.updateSectionManagerDialog();
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
					edit.SectionManagerDialog.updateSectionManagerDialog();
					FileOperations.unsavedChanges = true;
				}
			}
		};
	}

	public static MouseInputAdapter getEditCellAdapter(Cell cell)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				if (MainGui.inEditMode)
					CellEditDialog.processCell(cell);
			}
		};
	}
	
	public static MouseInputAdapter getEditTodoAdapter(int current_row, Row row, JPanel panel, JLabel label, JLabel icon)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				String string = JOptionPane.showInputDialog(MainGui.window, "TODO", row.getTodoString() );
				if (string != null)
				{
					row.setTodoString(string);
					FileOperations.unsavedChanges = true;
					
					if (string.isEmpty() && icon != null) // remove existing icon
					{
						panel.remove(icon);
						MainGui.labelsIconsHideWhenNotInEdit.remove(icon);
						label.removeMouseListener(this);
						label.addMouseListener(getEditTodoAdapter(current_row, row, panel, label, null) );
					}
					
					if (!string.isEmpty() && icon == null) // add new icon
					{
						JLabel new_icon = new JLabel(GuiHelper.getScaledImageIcon("Todo") );
						panel.add(new_icon);
						new_icon.setVisible(MainGui.inEditMode);
						MainGui.labelsIconsHideWhenNotInEdit.add(new_icon);
						label.removeMouseListener(this);
						label.addMouseListener(getEditTodoAdapter(current_row, row, panel, label, new_icon) );
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
		String action_command = "", action_param = "";
		try {
			String[] temp = action_str.split(":");
			action_command = temp[0];
			action_param = temp[1];
		}
		catch (Exception e)
		{
			throw new RuntimeException("Each action has to follow the syntax 'action_command':'action_parameter'!");
		}
		
		switch (action_command)
		{
			case "write_to_clipboard": // legacy
			case "text_to_clipboard": return getActionTextToClpiboardAdapter(action_param);
			case "file_to_clipboard": return getActionFileToClipboardAdapter(action_param);
			default : return null;
		}
	}
	
	public static MouseInputAdapter getActionTextToClpiboardAdapter(String textToWrite)
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
	
	public static MouseInputAdapter getActionFileToClipboardAdapter(String file_location)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent me)
			{
				if (MainGui.inEditMode)
					return;
				
				String output = "";
				try
				{
					BufferedReader reader = new BufferedReader(new FileReader(new File(file_location) ) );
					String line = "";
					while ( (line = reader.readLine() ) != null )
						output += line + "\n";
					reader.close();
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (output.length() > 0)
					output = output.substring(0, output.length() - 1);
	      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output), null);
			}
		};
	}
}