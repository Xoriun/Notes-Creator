package logic;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import gui.ColorSettings;
import gui.MainGui;

public class Section extends JPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = -4515698488950573862L;
	
	
	private String header;
	private String title;
	
	private JPanel[][] contentCells;
	private ArrayList<JPanel> controlCells;
	private ArrayList<JPanel> todoCells;
	
	private ArrayList<String[]> content;
	private ArrayList<String> todo;
	private GridBagConstraints gbc;
	
	public static int[] maxWidths;
	private JPanel[] spacingPanels;
	private int scrollLocation;
	
	public JPanel[][] getContentCells() { return contentCells; }
	public ArrayList<JPanel> getControlCells() { return controlCells; }
	public ArrayList<JPanel> getTodoCells() { return todoCells; }
	public String getTitle() { return title; }
	public ArrayList<String> getTodo() { return todo; }
	public int getScrollLocation() { return scrollLocation; } 
	public ArrayList<String[]> getContent() { return content; }
	
	public Section()
	{
		content = new ArrayList<String[]>();
		todo = new ArrayList<String>();
		header = "New Section";
		fillPanel();
	}
	
	public Section(String header, ArrayList<String[]> content, ArrayList<String> todo)
	{
		this.content = content;
		this.todo = todo;
		this.header = header;
		fillPanel();
	}
	
	private void fillPanel()
	{
		this.removeAll();
		
		title = header.split(";")[0].replace("---", "");
		
		int col = 0;
		this.setLayout(new GridBagLayout() );
		this.setAlignmentY(Component.LEFT_ALIGNMENT);
		this.setOpaque(false);
		
		// list of cells in the new section
		controlCells = new ArrayList<JPanel>();
		ArrayList<JPanel[]> content_cells_list = new ArrayList<JPanel[]>();
		todoCells = new ArrayList<JPanel>();
		
		gbc = new GridBagConstraints(); 
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		
		// title
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), title, TitledBorder.LEFT, TitledBorder.TOP);
		border.setTitleFont(MainGui.titleFont);
		border.setTitleColor(ColorSettings.currentColorSetting.text);
		this.setBorder(border);
		
		if (content.isEmpty() ) // empty section
		{
			gbc.gridx = 0;
			JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(0, true);
			this.add(add_remove_content_row_controls, gbc);
			
			JPanel[] panels = new JPanel[FileOperations.numberOfColumns];
			for (int i = 1; i < FileOperations.numberOfColumns; i ++)
				panels[i] = new JPanel(); 
			
			controlCells.add(add_remove_content_row_controls);
			contentCells = new JPanel[][] {panels};
			todoCells.add(new JPanel() );
		}
		else
		{
			// row
			for (int row = 0; row < content.size(); row ++)
			{
				gbc.gridx = 0;
				
				// control
				JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(row, false);
				this.add(add_remove_content_row_controls, gbc);
				controlCells.add(add_remove_content_row_controls);
				
				// content
				JPanel[] panel_row = new JPanel[FileOperations.numberOfColumns];
				gbc.gridx = (col = 0) + 1;
				
				for (String cell_str : content.get(row) )
				{
					Cell cell_panel = new Cell(this, cell_str, row, col);
					this.add(cell_panel, gbc);
					panel_row[col] = cell_panel;
					
					col ++;
					gbc.gridx = col + 1;
				}
				
				content_cells_list.add(panel_row);
				
				// todo
				JPanel todo_control = getTodoControl(row);
				this.add(todo_control, gbc);
				todoCells.add(todo_control);
				
				gbc.gridy ++;
			}
			
			// controls for new row
			gbc.gridx = 0;
			JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(content.size(), true);
			this.add(add_remove_content_row_controls, gbc);
			
			contentCells = content_cells_list.toArray(new JPanel[content_cells_list.size() ][content_cells_list.get(0).length] );
		}
	}
	
	public JPanel getAddRemoveContentRowControl(int current_row, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		Color color = MainGui.inEditMode ? ColorSettings.currentColorSetting.text : ColorSettings.currentColorSetting.background;
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(MouseAdapters.getAddContentRowAdapter(current_row, this) );
		
		MainGui.labelsTextsHideWhenNotInEdit.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(color);
			remove.addMouseListener(MouseAdapters.getRemoveContentRowAdapter(current_row, this) );
			
			MainGui.labelsTextsHideWhenNotInEdit.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	public JPanel getTodoControl(int current_row)
	{
		JPanel todo_panel = new JPanel();
		todo_panel.setLayout(new BoxLayout(todo_panel, BoxLayout.X_AXIS) );
		todo_panel.setOpaque(false);
		JLabel todo_label = new JLabel("  +  ");
		
		todo_label.setForeground(ColorSettings.currentColorSetting.text);
		todo_label.setOpaque(false);
		todo_panel.add(todo_label);
		
		JLabel icon = null;
		if (!todo.get(current_row).equals("") )
		{
			icon = new JLabel(new ImageIcon(new ImageIcon("Images\\Not-enough-repair-packs-icon" + ".png").getImage().getScaledInstance(MainGui.ImageSize, MainGui.ImageSize, Image.SCALE_DEFAULT) ) );
			todo_panel.add(icon);
			icon.setVisible(MainGui.inEditMode);
			MainGui.labelsIconsHideWhenNotInEdit.add(icon);
		}
		
		todo_label.addMouseListener(MouseAdapters.getEditTodoAdapter(current_row, this, todo_panel, todo_label, icon) );
		
		MainGui.labelsText.add(todo_label);
		return todo_panel;
	}
	
	public void determineMaxWidth()
	{
		if (controlCells.size() == 1)
				return;
		if (maxWidths[0] < controlCells.get(0).getWidth() )
			maxWidths[0] = controlCells.get(0).getWidth();
		for (int col = 0; col < FileOperations.numberOfColumns; col ++)
			if (maxWidths[col + 1] < contentCells[0][col].getWidth() )
				maxWidths[col + 1] = contentCells[0][col].getWidth();
		if (maxWidths[FileOperations.numberOfColumns + 1] < todoCells.get(0).getWidth() )
			maxWidths[FileOperations.numberOfColumns + 1] = todoCells.get(0).getWidth();
	}
	
	public void addSpacingPanels()
	{
		spacingPanels = new JPanel[FileOperations.numberOfColumns + 2];
		gbc.gridy = -2;
		for (int col = 0; col < FileOperations.numberOfColumns + 2; col ++)
		{
			JPanel panel = new JPanel();
			panel.setPreferredSize(new Dimension(maxWidths[col], 0) );
			//panel.setMaximumSize(new Dimension(maxWidths[col], 0) );
			//panel.setSize(new Dimension(maxWidths[col], 0) );
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
		String[] new_line = new String[FileOperations.numberOfColumns];
		for (int i = 0; i < FileOperations.numberOfColumns; i ++)
			new_line[i] = ""; 
		content.add(row_to_add,  new_line);
		todo.add(row_to_add, "");
		
		fillPanel();
	}
	
	public void removeContentLine(int row_to_remove)
	{
		content.remove(row_to_remove);
		todo.remove(row_to_remove);
		
		fillPanel();
	}
	
	public void setTitle(String new_title)
	{
		this.title = new_title;
		if (!this.title.equals("") )
			new_title = " " + this.title + " ";
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), new_title, TitledBorder.LEFT, TitledBorder.TOP);
		border.setTitleFont(MainGui.titleFont);
		border.setTitleColor(ColorSettings.currentColorSetting.text);
		this.setBorder(border);
	}
	
	public void addContentColumn(int new_column)
	{
		content.replaceAll(e -> addCellFromStringArr(e, new_column) );
		fillPanel();
	}
	
	public void removeContentColumn(int old_column)
	{
		content.replaceAll(e -> removeCellFromStringArr(e, old_column) );
		fillPanel();
	}
	
	// for adding columns
	private static String[] addCellFromStringArr(String[] arr, int new_cell)
	{
		String[] res = new String[arr.length + 1];
		for (int i = 0; i < arr.length + 1; i ++)
			if (i == new_cell)
				res[i] = "  ";
			else
				res[i] = i < new_cell ? arr[i] : arr[i - 1]; 
		return res;
	}
	
	// for removing columns
	private static String[] removeCellFromStringArr(String[] arr, int old_cell)
	{
		String[] res = new String[arr.length - 1];
		for (int i = 0; i < arr.length - 1; i ++)
			res[i] = i < old_cell ? arr[i] : arr[i + 1]; 
		return res;
	}
	
	public String getSaveString()
	{
		String res = "---" + title + "---";
		for (int i = 0; i < content.size(); i ++)
		{
			res += "\n";
			for (String cell : content.get(i) )
				res += cell + ";";
			res = res.substring(0, res.length() - 1); // removing last semicolon
			if (!todo.get(i).isEmpty() )
				res += "||" + todo.get(i);
		}
		return res;
	}
}
