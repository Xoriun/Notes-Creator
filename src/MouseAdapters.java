import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

public class MouseAdapters
{
	public static MouseInputAdapter getAddContentRowControl(int current_row)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (Gui.inEditMode)
				{
					Logic.unsavedChanges = true;
					Logic.addContentLine(current_row);
					Gui.arrangeContent();
					Gui.spaceColums();
				}
			}
		};
	}
	
	public static MouseInputAdapter getRemoveContentRowControl(int current_row)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (Gui.inEditMode)
				{
					Logic.unsavedChanges = true;
					Logic.removeContentLine(current_row);
					Gui.arrangeContent();
					Gui.spaceColums();
				}
			}
		};
	}
	
	public static ActionListener addContentCol(int col_to_add)
	{
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (Gui.inEditMode)
				{
					Logic.unsavedChanges = true;
					Logic.addContentColumn(col_to_add);
					Gui.arrangeContent();
					Gui.spaceColums();
				}
			}
		};
	}
	
	public static ActionListener removeContentCol(int col_to_remove)
	{
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (Gui.inEditMode)
				{
					Logic.unsavedChanges = true;
					Logic.removeContentColumn(col_to_remove);
					Gui.arrangeContent();
					Gui.spaceColums();
				}
			}
		};
	}
	
	public static MouseInputAdapter getAddSectionControl(int current_row)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (Gui.inEditMode)
				{
					Logic.unsavedChanges = true;
					Logic.addContentLine(current_row);
					Logic.content[current_row][0] = "---New section---";
					Gui.arrangeContent();
					Gui.spaceColums();
					Gui.updateSectionManagerDialog();
				}
			}
		};
	}
	
	public static MouseInputAdapter getRemoveSectionControl(int current_row)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (Gui.inEditMode)
				{
					Logic.unsavedChanges = true;
					Logic.removeContentLine(current_row);
					Gui.arrangeContent();
					Gui.spaceColums();
					Gui.updateSectionManagerDialog();
				}
			}
		};
	}
	
	public static MouseInputAdapter getSectionTitleEdit(JLabel label, int current_row)
	{
		return new MouseInputAdapter() {
			@Override
	    public void mouseClicked(MouseEvent e)
			{
				String newText = JOptionPane.showInputDialog(null, "Set the section title!", label.getText() );
				if (newText != null)
				{
					label.setText(newText);
					Logic.content[current_row][0] = "---" + newText + "---";
					Gui.arrangeContent();
					Gui.spaceColums();
					Gui.updateSectionManagerDialog();
					Logic.unsavedChanges = true;
				}
			}
		};
	}

	public static MouseInputAdapter getCellEdit(JPanel cell, boolean left_border, boolean top_border, int row, int col)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				if (Gui.inEditMode)
				{	
					String string = JOptionPane.showInputDialog(null, "Set the text!", Logic.content[row][col] );
					if (string != null) {
						Logic.content[row][col] = string; 
						cell.removeAll();
						Logic.fillCellPanel(cell, string.split(">>", 2)[0].replace("\\n", "\n").replace("->", "⇨"), left_border, top_border);
						//Gui.repaint();
						Gui.spaceColums();
						Logic.unsavedChanges = true;
					}
				}
			}
		};
	}

	public static MouseInputAdapter getLabelEdit(JPanel cell, JLabel label, boolean left_border, boolean top_border, int row, int col)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				if (Gui.inEditMode)
				{	
					String string = JOptionPane.showInputDialog(null, "Set the text!", label.getText() );
					if (string != null) {
						Logic.content[row][col] = string; 
						cell.removeAll();
						Logic.fillCellPanel(cell, string.split(">>", 2)[0].replace("\\n", "\n").replace("->", "⇨"), left_border, top_border);
						//Gui.repaint();
						Gui.spaceColums();
						Logic.unsavedChanges = true;
					}
				}
			}
		};
	}
	
	public static MouseInputAdapter getTodoEdit(int current_row)
	{
		return new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				String string = JOptionPane.showInputDialog(null, "TODO", Logic.todoList[current_row] );
				if (string != null)
				{
					Logic.todoList[current_row] = string;
					//icon.setVisible(Gui.inEditMode && !string.equals("") );
					Logic.unsavedChanges = true;
				}
				Gui.arrangeContent();
				Gui.spaceColums();
			}
		};
	}

	public static WindowAdapter getOnCloseAdapter()
	{
		return new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (Logic.unsavedChanges)
					Gui.unsavedChangesDialog();
				else
					Gui.exit();
			}
		};
	}
	
	public static MouseInputAdapter getCellAction(String action_str)
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
				case "write_to_clipboard": return getCellWriteToClpiboard(action_param);
			}
		}
		
		return null;
	}
	
	public static MouseInputAdapter getCellWriteToClpiboard(String textToWrite)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				if (!Gui.inEditMode)
		      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(textToWrite), null);
			}
		};
	}
}