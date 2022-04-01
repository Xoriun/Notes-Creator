package logic;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;

public class Row
{
	private JPanel controlPanel;
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	private String todoString = "";
	private JPanel todoPanel;
	
	private Section section;
	private int rowIndex;
	
	public ArrayList<Cell> getCells() { return cells; }
	public JPanel getControlPanel() { return controlPanel; }
	public Section getSection() { return section; }
	public int getRowIndex() { return rowIndex; }
	void increaseRowIndex() { rowIndex ++; }
	void decreaseRowIndex() { rowIndex --; }
	public String getTodoString() { return todoString; }
	public JPanel getTodoPanel() { return todoPanel; }
	public void setTodoString(String todo_string) { todoString = todo_string; }
	
	/**
	 * Creates a empty Row only including the RowControls
	 * 
	 * @param section The section this row is a part of.
	 */
	Row(Section section, int row_index)
	{
		rowIndex = row_index;
		this.section = section;
		todoPanel = new JPanel();
		todoPanel.setOpaque(false);
		todoString = "";
		
		controlPanel = AddRemoveControl.createAddRowControl(this);
	}
	
	/**
	 * Creates a new Row including the RowControls, all Cells and TodoControl
	 * 
	 * @param section The section this row is a part of.
	 * @param row_string The string that defines the row (including todo).
	 * @param row_index The index of the row within the section.
	 */
	Row(Section section, String row_string, int row_index)
	{
		rowIndex = row_index;
		this.section = section;
		
		controlPanel = AddRemoveControl.createAddRemoveRowControl(this);
		
		String[] line_arr = row_string.split(Pattern.quote("||"), 2);
		if (line_arr.length == 2)
			todoString = line_arr[1];
		else
			todoString = "";
		todoPanel = getTodoControl();
		
		String[] cell_strings = line_arr[0].split(";", -1);
		for (int col = 0; col < cell_strings.length; col ++)
			cells.add(new Cell(this, cell_strings[col], col) );
		if (cell_strings.length > FileOperations.numberOfColumns) FileOperations.numberOfColumns = cell_strings.length;
	}
	
	Row(Section section, int row_index, Element row)
	{
		this.section = section;
		this.rowIndex = row_index;
		
		controlPanel = AddRemoveControl.createAddRemoveRowControl(this);
		
		NodeList todo = row.getElementsByTagName("todo");
		if (todo.getLength() == 1)
			todoString = todo.item(0).getTextContent();
		todoPanel = getTodoControl();
		
		NodeList cellNodes = row.getElementsByTagName("cell");
		if (cellNodes.getLength() > FileOperations.numberOfColumns)
			FileOperations.numberOfColumns = cellNodes.getLength();
		for (int col = 0; col < cellNodes.getLength(); col ++)
			cells.add(new Cell(this, col, (Element) cellNodes.item(col) ) );
	}
	
	void reloadImages()
	{
		for (Cell cell : cells) cell.relaodImages();
	}
	
	private JPanel getTodoControl()
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
			icon = new JLabel(GuiHelper.scaledTodoImageIcon);
			todo_panel.add(icon);
			icon.setVisible(MainGui.inEditMode);
			MainGui.labelsIconsHideWhenNotInEdit.add(icon);
		}
		
		todo_label.addMouseListener(MouseAdapters.getEditTodoAdapter(this, todo_panel, todo_label, icon) );
		
		MainGui.labelsText.add(todo_label);
		return todo_panel;
	}
	
	Element getXMLElement(Document doc)
	{
		Element result = doc.createElement("row");
		
		for (Cell cell : cells)
			result.appendChild(cell.getXMLElement(doc) );
		
		if ( !todoString.isEmpty() )
		{
			Element todoElement = doc.createElement("todo");
			todoElement.setTextContent(todoString);
			result.appendChild(todoElement);
		}
		
		return result;
	}
}
