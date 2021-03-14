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
					Gui.draw();
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
					Gui.draw();
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
					Gui.draw();
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
					Gui.draw();
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
					Gui.draw();
					Gui.updateSectionManagerDialog();
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
						Logic.fillCellPanel(cell, string.replace("\\n", "\n").replace("->", "⇨"), left_border, top_border);
						cell.validate();
						Gui.repaint();
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
				Gui.draw();
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
}