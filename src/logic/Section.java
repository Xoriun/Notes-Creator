package logic;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import gui.GuiHelper;

public class Section extends JPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = -4515698488950573862L;
	
	private String title;
	
	private ArrayList<Row> rows;
	
	private GridBagConstraints gbc;
	
	public static int[] maxWidths;
	private JPanel[] spacingPanels;
	private int scrollLocation;
	
	public String getTitle() { return title; }
	public int getScrollLocation() { return scrollLocation; }
	public ArrayList<Row> getRows() { return rows; }
	
	public Section(String header)
	{
		rows = new ArrayList<Row>();
		
		// title
		title = header.split(";")[0].replace("---", "");
		this.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor(title) );
		
		this.setLayout(new GridBagLayout() );
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		this.setAlignmentY(Component.LEFT_ALIGNMENT);
		this.setOpaque(false);
	}
	
	public static Section creatEmptySection()
	{
		Section section = new Section("title");
		section.addEmptyRow();
		section.fillPanel();
		return section;
	}
	
	public void addEmptyRow()
	{
		rows.add(new Row(this, rows.size() ) );
	}
	
	public void addRow(String row_string)
	{
		rows.add(new Row(this, row_string, rows.size() ) );
	}
	
	public void reloadImages()
	{
		for (Row row : rows) row.reloadImages();
	}
	
	public void fillPanel()
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
	
	public void addContentLine(int row_to_add)
	{
		String new_row_string = "";
		for (int i = 0; i < FileOperations.numberOfColumns - 1; i ++)
			new_row_string += ";";
		rows.add(row_to_add, new Row(this, new_row_string, row_to_add) );
		
		fillPanel();
	}
	
	public void removeContentLine(int row_to_remove)
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
	
	public void addContentColumn(int new_column)
	{
		for (Row row : rows)
		{
			for (Cell cell : row.getCells() )
				if (cell.getCol() >= new_column)
					cell.increaseCol();
			if (row.getRowIndex() != rows.size() - 1 )
				row.getCells().add(new_column, new Cell(row, " ", new_column) );
		}
		fillPanel();
	}
	
	public void removeContentColumn(int old_column)
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
	
	public String getSaveString()
	{
		String res = "---" + title + "---";
		for (Row row : rows)
		{
			res += "\n";
			for (Cell cell : row.getCells() )
				res += cell.getCellString() + ";";
			res = res.substring(0, res.length() - 1); // removing last semicolon (or in case of the empty control row the linebreak resulting in an empty strings for all empty control rows)
			if (!row.getTodoString().isEmpty() )
				res += "||" + row.getTodoString();
		}
		return res;
	}
}
