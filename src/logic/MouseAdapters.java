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
	public final static MouseInputAdapter editCellAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if ( !MainGui.inEditMode) return;
			
			int buttonPressed = e.getButton();
			
			if (buttonPressed == 1 || buttonPressed == 3)
			{
				CellEditDialog.processCell( (Cell) e.getComponent() );
				if (buttonPressed == 3)
					PopupMenues.processCellRightClick(e);
			}
		}
	};
	
	public final static WindowAdapter windowOnCloseAdapter = new WindowAdapter()
	{
		@Override
		public void windowClosing(WindowEvent e)
		{
			if (FileOperations.unsavedChanges)
				PopupAlerts.unsavedChangesDialog();
			else
				MainGui.exit();
		}
	};
	
	public final static MouseInputAdapter addSectionAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (MainGui.inEditMode)
			{
				FileOperations.unsavedChanges = true;
				int section_index = ( (AddRemoveControl) e.getComponent().getParent() ).getSectionIndex();
				MainGui.sectionsList.add(section_index, Section.creatEmptySection() );
				MainGui.arrangeContent();
				MainGui.spaceColums();
				edit.SectionManagerDialog.updateSectionManagerDialog();
			}
		}
	};

	public final static MouseInputAdapter removeSectionAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (MainGui.inEditMode)
			{
				FileOperations.unsavedChanges = true;
				int section_index = ( (AddRemoveControl) e.getComponent().getParent() ).getSectionIndex();
				MainGui.sectionsList.remove(section_index);
				MainGui.arrangeContent();
				MainGui.spaceColums();
				edit.SectionManagerDialog.updateSectionManagerDialog();
			}
		}
	};

	public final static MouseInputAdapter addRowAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (MainGui.inEditMode)
			{
				FileOperations.unsavedChanges = true;
				AddRemoveControl control = (AddRemoveControl) e.getComponent().getParent();
				Section section = control.getRow().getSection();
				
				// adding actual row
				section.addContentLine(control.getRow().getRowIndex() );
				
				// increasing the rowIndices of the following rows
				for (Row any_row : section.getRows() )
					if (any_row.getRowIndex() >= control.getRow().getRowIndex() )
						any_row.increaseRowIndex();
					
				// redrawing GUI
				MainGui.arrangeContent();
				MainGui.spaceColums();
			}
		}
	};

	public final static MouseInputAdapter removeRowAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (MainGui.inEditMode)
			{
				FileOperations.unsavedChanges = true;
				AddRemoveControl control = (AddRemoveControl) e.getComponent().getParent();
				Section section = control.getRow().getSection();
				
				// removing actual row
				section.removeContentLine(control.getRow().getRowIndex() );
				
				// decreasing the rowIndices of the following rows
				for (Row any_row : section.getRows() )
					if (any_row.getRowIndex() >= control.getRow().getRowIndex() )
						any_row.decreaseRowIndex();
					
				// redrawing GUI
				MainGui.arrangeContent();
				MainGui.spaceColums();
			}
		}
	};
	
	public final static MouseInputAdapter actionsAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if ( !SpeedRunMode.speedrunModeEnabled)
				return;
			
			for (String[] action : ( (Cell) e.getComponent() ).getActionsArray() )
			{
				switch (action[0] )
				{
					case "text_to_clipboard":
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(action[1] ), null);
						break;
					
					case "file_to_clipboard":
						String output = "";
						try
						{
							BufferedReader reader = new BufferedReader(new FileReader(new File(action[1] ) ) );
							String line = "";
							while ((line = reader.readLine()) != null)
								output += line + "\n";
							reader.close();
						} catch (FileNotFoundException ex)
						{
							// TODO Auto-generated catch block
							ex.printStackTrace();
						} catch (IOException ex)
						{
							// TODO Auto-generated catch block
							ex.printStackTrace();
						}
						
						if (output.length() > 0)
							output = output.substring(0, output.length() - 1);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(output), null);
						break;
				}
			}
		}
	};
	
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
	
	public static MouseInputAdapter getEditSectionTitleAdapter(JLabel label, int current_section_index)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				String newText = JOptionPane.showInputDialog(MainGui.window, "Set the section title!", label.getText());
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
	
	public static MouseInputAdapter getEditTodoAdapter(int current_row, Row row, JPanel panel, JLabel label, JLabel icon)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				String string = JOptionPane.showInputDialog(MainGui.window, "TODO", row.getTodoString());
				if (string != null)
				{
					row.setTodoString(string);
					FileOperations.unsavedChanges = true;
					
					if (string.isEmpty() && icon != null) // remove existing icon
					{
						panel.remove(icon);
						MainGui.labelsIconsHideWhenNotInEdit.remove(icon);
						label.removeMouseListener(this);
						label.addMouseListener(getEditTodoAdapter(current_row, row, panel, label, null));
					}
					
					if (!string.isEmpty() && icon == null) // add new icon
					{
						JLabel new_icon = new JLabel(GuiHelper.scaledTodoImageIcon);
						panel.add(new_icon);
						new_icon.setVisible(MainGui.inEditMode);
						MainGui.labelsIconsHideWhenNotInEdit.add(new_icon);
						label.removeMouseListener(this);
						label.addMouseListener(getEditTodoAdapter(current_row, row, panel, label, new_icon));
					}
				}
				
				MainGui.arrangeContent();
				MainGui.spaceColums();
			}
		};
	}
}