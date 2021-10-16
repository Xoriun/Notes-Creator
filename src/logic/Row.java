package logic;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;

public class Row
{
	private JPanel controlPanel;
	private ArrayList<Cell> cells;
	private String todoString;
	private JPanel todoPanel;
	
	private Section section;
	private int rowIndex;
	
	public ArrayList<Cell> getCells() { return cells; }
	public JPanel getControlPanel() { return controlPanel; }
	public Section getSection() { return section; }
	public int getRowIndex() { return rowIndex; }
	public void increaseRowIndex() { rowIndex ++; }
	public void decreaseRowIndex() { rowIndex --; }
	public String getTodoString() { return todoString; }
	public JPanel getTodoPanel() { return todoPanel; }
	public void setTodoString(String todo_string) { todoString = todo_string; }
	
	/**
	 * Creates a empty Row only including the RowControls
	 * 
	 * @param section The section this row is a part of.
	 */
	public Row(Section section, int row_index)
	{
		rowIndex = row_index;
		this.section = section;
		todoPanel = new JPanel();
		todoPanel.setOpaque(false);
		todoString = "";
		
		controlPanel = getAddRemoveContentRowControl(true);
		cells = new ArrayList<Cell>();
	}
	
	/**
	 * Creates a new Row including the RowControls, all Cells and TodoControl
	 * 
	 * @param section The section this row is a part of.
	 * @param row_string The string that defines the row (including todo).
	 * @param row_index The index of the row within the section.
	 */
	public Row(Section section, String row_string, int row_index)
	{
		rowIndex = row_index;
		this.section = section;
		
		controlPanel = getAddRemoveContentRowControl(false);
		
		String[] line_arr = row_string.split(Pattern.quote("||"), 2);
		if (line_arr.length == 2)
			todoString = line_arr[1];
		else
			todoString = "";
		todoPanel = getTodoControl(0);
		
		cells = new ArrayList<Cell>();
		String[] cell_strings = line_arr[0].split(";", -1);
		for (int col = 0; col < cell_strings.length; col ++)
			cells.add(new Cell(this, cell_strings[col], col) );
		if (cell_strings.length > FileOperations.numberOfColumns) FileOperations.numberOfColumns = cell_strings.length;
	}
	
	private JPanel getAddRemoveContentRowControl(boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		Color color = MainGui.inEditMode ? ColorSettings.getTextColor() : ColorSettings.getBackgroundColor();
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(MouseAdapters.getAddContentRowAdapter(this) );
		
		MainGui.labelsTextsHideWhenNotInEdit.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(color);
			remove.addMouseListener(MouseAdapters.getRemoveContentRowAdapter(this) );
			
			MainGui.labelsTextsHideWhenNotInEdit.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	private JPanel getTodoControl(int current_row)
	{
		JPanel todo_panel = new JPanel();
		todo_panel.setLayout(new BoxLayout(todo_panel, BoxLayout.X_AXIS) );
		todo_panel.setOpaque(false);
		JLabel todo_label = new JLabel("  +  ");
		
		todo_label.setForeground(ColorSettings.getTextColor() );
		todo_label.setOpaque(false);
		todo_panel.add(todo_label);
		
		JLabel icon = null;
		if (!todoString.equals("") )
		{
			icon = new JLabel(GuiHelper.getScaledImageIcon("Todo") );
			todo_panel.add(icon);
			icon.setVisible(MainGui.inEditMode);
			MainGui.labelsIconsHideWhenNotInEdit.add(icon);
		}
		
		todo_label.addMouseListener(MouseAdapters.getEditTodoAdapter(current_row, this, todo_panel, todo_label, icon) );
		
		MainGui.labelsText.add(todo_label);
		return todo_panel;
	}
}
