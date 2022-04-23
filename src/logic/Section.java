package logic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gui.ColorSettings;
import gui.GuiHelper;

public class Section extends JPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = -4515698488950573862L;
	
	private String title;
	
	private ArrayList<Row> rows = new ArrayList<Row>();
	
	private GridBagConstraints gbc;
	
	public static int[] maxWidths;
	private JPanel[] spacingPanels;
	private int scrollLocation;
	
	public String getTitle() { return title; }
	public int getScrollLocation() { return scrollLocation; }
	public ArrayList<Row> getRows() { return rows; }
	
	/**
	 * Creates an empty section containing an empty row.
	 * 
	 * @param header
	 */
	public Section(String header)
	{
		// title
		title = header;
		this.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor(title) );
		
		this.addEmptyRow();
		this.addLastRow();
		
		this.setLayout(new GridBagLayout() );
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		this.setAlignmentY(Component.LEFT_ALIGNMENT);
		this.setOpaque(false);
		this.fillPanel();
	}
	
	Section(Element section)
	{
		title = section.getElementsByTagName("title").item(0).getTextContent();
		this.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor(title) );
		
		NodeList rowElementsList = section.getElementsByTagName("row");
		for (int row_index = 0; row_index < rowElementsList.getLength(); row_index ++ )
			rows.add(new Row(this, row_index, (Element) rowElementsList.item(row_index) ) );
		
		this.setLayout(new GridBagLayout() );
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		this.setAlignmentY(Component.LEFT_ALIGNMENT);
		this.setOpaque(false);
		
		this.addLastRow();
		this.fillPanel();
	}
	
	private void addEmptyRow()
	{
		rows.add(new Row(this, rows.size(), Row.EMPTY) );
	}
	
	private void addLastRow()
	{
		rows.add(new Row(this, rows.size(), Row.LAST) );
	}
	
	public void reloadImages()
	{
		for (Row row : rows) row.reloadImages();
	}
	
	private void fillPanel()
	{
		this.removeAll();
		
		// list of cells in the new section
		gbc.gridy = 0;

		// row
		for (Row row : rows)
		{
			gbc.gridx = 0;
			
			// control
			this.add(row.getControlPanel(), gbc);
			
			// content
			for (Cell cell : row.getCells() )
			{
				gbc.gridx ++;
				this.add(cell, gbc);
			}
			
			// todo
			gbc.gridx ++;
			this.add(row.getTodoPanel(), gbc);
			
			gbc.gridy ++;
		}
		
		// controls for new row
		gbc.gridx = 0;
	}
	
	public void determineMaxWidth()
	{
		if (rows.size() == 1)
				return;
		if (maxWidths[0] < rows.get(0).getControlPanel().getWidth() )
			maxWidths[0] = rows.get(0).getControlPanel().getWidth();
		for (int col = 0; col < FileOperations.numberOfColumns; col ++)
			if (maxWidths[col + 1] < rows.get(0).getCells().get(col).getWidth() )
				maxWidths[col + 1] = rows.get(0).getCells().get(col).getWidth();
		if (maxWidths[FileOperations.numberOfColumns + 1] < rows.get(0).getTodoPanel().getWidth() )
			maxWidths[FileOperations.numberOfColumns + 1] = rows.get(0).getTodoPanel().getWidth();
	}
	
	public void addSpacingPanels()
	{
		spacingPanels = new JPanel[FileOperations.numberOfColumns + 2];
		gbc.gridy = -2;
		for (int col = 0; col < FileOperations.numberOfColumns + 2; col ++)
		{
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(maxWidths[col], 0) );
			spacingPanels[col] = panel;
			gbc.gridx = col;
			this.add(panel, gbc);
		}
	}
	
	public void removeSpacingPanels()
	{
		if (spacingPanels != null)
			for (JPanel panel : spacingPanels)
				this.remove(panel);
	}
	
	public void setLocation()
	{
		scrollLocation = this.getLocation().y;
	}
	
	void addContentLine(int row_to_add)
	{
		rows.add(row_to_add, new Row(this, row_to_add, Row.EMPTY) );
		
		fillPanel();
	}
	
	void removeContentLine(int row_to_remove)
	{
		rows.remove(row_to_remove);
		
		fillPanel();
	}
	
	public void setTitle(String new_title)
	{
		this.title = new_title;
		if (!this.title.equals("") )
			new_title = " " + this.title + " ";
		this.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor(new_title) );
	}
	
	void addContentColumn(int new_column)
	{
		for (Row row : rows)
		{
			for (Cell cell : row.getCells() )
				if (cell.getCol() >= new_column)
					cell.increaseCol();
			if (row.getRowIndex() != rows.size() - 1 )
				row.getCells().add(new_column, new Cell(row, new_column, " ") );
		}
		fillPanel();
	}
	
	void removeContentColumn(int old_column)
	{
		for (Row row : rows)
		{
			if (row.getRowIndex() != rows.size() - 1 )
				row.getCells().remove(old_column);
			for (Cell cell : row.getCells() )
				if (cell.getCol() > old_column)
					cell.decreaseCol();
		}
		fillPanel();
	}
	
	public void updateLightingSettings()
	{
		((TitledBorder) this.getBorder() ).setTitleColor(ColorSettings.getTextColor() );
		
		for (Row row : rows)
			row.updateLightingSettings();
	}
	
	public void updateEditMode()
	{
		for (Row row : rows)
			row.updateEditMode();
	}
	
	Element getXMLElement(Document doc)
	{
		Element result = doc.createElement("section");
		
		Element titleElement = doc.createElement("title");
		titleElement.setTextContent(title);
		result.appendChild(titleElement);
		
		for (Row row : rows)
			if ( !row.getCells().isEmpty() )
				result.appendChild(row.getXMLElement(doc) );
		
		return result;
	}
}
