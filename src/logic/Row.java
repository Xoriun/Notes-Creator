package logic;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList; 

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gui.GuiHelper;
import gui.MainGui;
import settings.ColorSettings;

public class Row
{
	private AddRemoveControl controlPanel;
	private ArrayList<Cell> cells = new ArrayList<Cell>();
	private TodoLabel todoLabel;
	
	private Section section;
	private int rowIndex;
	
	public ArrayList<Cell> getCells() { return cells; }
	public JPanel getControlPanel() { return controlPanel; }
	public Section getSection() { return section; }
	public int getRowIndex() { return rowIndex; }
	void increaseRowIndex() { rowIndex ++; }
	void decreaseRowIndex() { rowIndex --; }
	public String getTodoString() { return todoLabel.todoString; }
	public JLabel getTodoPanel() { return todoLabel; }
	public void setTodoString(String todo_string) { todoLabel.todoString = todo_string; }
	
	static final boolean EMPTY = false;
	static final boolean LAST  = true;
	
	/**
	 * Creates an empty Row. Depending on last_row, the new row will be empty but full usable or will be the last row of the section which only contains the + button for a new row.
	 * 
	 * @param section The section this row will be part of.
	 * @param row_index The index this row will have.
	 * @param last_row Whether or not the new row is the last row of the section. Possible values are Row.EMPTY and Row.LAST.
	 * 
	 * @return The new row.
	 */
	Row(Section section, int row_index, boolean last_row)
	{
		rowIndex = row_index;
		this.section = section;
		
		if (last_row == Row.LAST)
		{
			todoLabel = new TodoLabel();
			controlPanel = AddRemoveControl.createAddRowControl(this);
		}
		else
		{
			todoLabel = new TodoLabel("");
			controlPanel = AddRemoveControl.createAddRemoveRowControl(this);
			for (int i = 0; i < FileOperations.numberOfColumns; i ++)
				cells.add(new Cell(this, i, " ") );
		}
	}
	
	Row(Section section, int row_index, Element row)
	{
		this.section = section;
		this.rowIndex = row_index;
		
		controlPanel = AddRemoveControl.createAddRemoveRowControl(this);
		
		NodeList todo = row.getElementsByTagName("todo");
		todoLabel = new TodoLabel(todo.getLength() == 1 ? todo.item(0).getTextContent() : "");
		
		NodeList cellNodes = row.getElementsByTagName("cell");
		if (cellNodes.getLength() > FileOperations.numberOfColumns)
			FileOperations.numberOfColumns = cellNodes.getLength();
		for (int col = 0; col < cellNodes.getLength(); col ++)
			cells.add(new Cell(this, col, (Element) cellNodes.item(col) ) );
	}
	
	public void setIndex(int new_index)
	{
		this.rowIndex = new_index;
	}
	
	void reloadImages()
	{
		for (Cell cell : cells) cell.relaodImages();
	}
	
	public void updateLightingSettings()
	{
		// todoLabel.updateLightingSettings();
		for (Component comp : controlPanel.getComponents() )
			comp.setForeground(MainGui.inEditMode ? ColorSettings.getTextColor() : ColorSettings.getBackgroundColor() );
		
		for (Cell cell : cells)
			cell.updateLightingSettings();
		
		todoLabel.updateLightingSettings();
	}
	
	public void updateEditMode()
	{
		controlPanel.updateEditMode();
		todoLabel.updateEditMode();
	}
	
	Element getXMLElement(Document doc)
	{
		Element result = doc.createElement("row");
		
		result.setAttribute("row_index", "" + rowIndex);
		
		for (Cell cell : cells)
			result.appendChild(cell.getXMLElement(doc) );
		
		if ( !todoLabel.todoString.isEmpty() )
		{
			Element todoElement = doc.createElement("todo");
			todoElement.setTextContent(todoLabel.todoString);
			result.appendChild(todoElement);
		}
		
		return result;
	}
	
	
	private final static MouseInputAdapter editTodoAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent event)
		{
			TodoLabel label = (TodoLabel) event.getComponent();
			String string = JOptionPane.showInputDialog(MainGui.window, "TODO", label.getTodoString() );
			if (string == null || string.equals(label.getTodoString() ) )
				return;
			
			label.updateTodoString(string);
			FileOperations.unsavedChanges = true;
			
			label.setIcon(MainGui.inEditMode && !string.isEmpty() ? GuiHelper.scaledTodoImageIcon : null);
			
			MainGui.arrangeContent();
			MainGui.spaceColums();
		}
	};
	
	private class TodoLabel extends JLabel
	{
		/** Automatically generated serailVersionUID */
		private static final long serialVersionUID = 1179200440597255671L;
		private String todoString;
		
		public String getTodoString() { return todoString; }
		
		public TodoLabel(String todo_string)
		{
			super("  +  ",
					MainGui.inEditMode && !todo_string.isEmpty() ? GuiHelper.scaledTodoImageIcon : null,
					SwingConstants.LEFT);
			this.todoString = todo_string;

			this.setOpaque(false);
			this.setForeground(ColorSettings.getTextColor() );
			this.addMouseListener(editTodoAdapter);
		}
		
		/**
		 * Creates an placeholder TodoLabel used on the last row of each section (no content, just the add row control).
		 */
		public TodoLabel()
		{
			super();
			this.todoString = "";
			this.setOpaque(false);
		}
		
		public void updateTodoString(String new_todo_string)
		{
			this.todoString = new_todo_string;
			this.updateIcon();
		}
		
		private void updateLightingSettings()
		{
			this.setForeground(ColorSettings.getTextColor() );
		}
		
		private void updateEditMode()
		{
			this.updateIcon();
		}
		
		private void updateIcon()
		{
			this.setIcon(MainGui.inEditMode && !todoString.isEmpty() ? GuiHelper.scaledTodoImageIcon : null);
			this.setHorizontalTextPosition(SwingConstants.LEADING);
		}
	}
}
