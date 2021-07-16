package logic;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import gui.Abbreviations;
import gui.ColorSettings;
import gui.MainGui;
import gui.PopupAlerts;

public class Section extends JPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = -4515698488950573862L;
	
	
	private String header;
	private String title;
	
	private JPanel[][] cells;
	
	private ArrayList<String[]> content;
	private ArrayList<String> todo;
	private GridBagConstraints gbc;
	
	public static int[] maxWidths;
	private JPanel[] spacingPanels;
	private int scrollLocation;
	
	public Section(JPanel[][] content, String title, int startIndex)
	{
		this.cells = content;
		this.title = title;
	}
	
	public JPanel[][] getCells() { return cells; }
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
		ArrayList<JPanel[]> cells_list = new ArrayList<JPanel[]>();
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
			
			JPanel[] panels = new JPanel[FileOperations.numberOfColumns + 2];
			panels[0] = add_remove_content_row_controls;
			for (int i = 1; i < FileOperations.numberOfColumns + 2; i ++)
				panels[i] = new JPanel(); 
			
			cells_list.add(panels);
			cells = cells_list.toArray(new JPanel[cells_list.size() ][cells_list.get(0).length] );
		}
		else
		{
			// row
			for (int row = 0; row < content.size(); row ++)
			{
				JPanel[] panel_row = new JPanel[FileOperations.numberOfColumns + 2];
				gbc.gridx = col = 0;
				
				JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(row, false);
				this.add(add_remove_content_row_controls, gbc);
				panel_row[0] = add_remove_content_row_controls;
				
				col ++;
				
				// cell
				for (String cell_str : content.get(row) )
				{
					gbc.gridx = col;
					JPanel cell_panel = new JPanel();
					
					boolean left_border = (col == 1);
					boolean top_border  = (row == 0);
					
					String cell_str_actions = "", cell_str_content = cell_str;
					if (cell_str.contains(">>") )
					{
						String[] temp = cell_str.split(">>", 2);
						cell_str_content = temp[0];
						cell_str_actions = temp[1];
					}

					cell_panel.setLayout(new BoxLayout(cell_panel, BoxLayout.Y_AXIS) );
					fillCellPanel(cell_panel, cell_str_content);
					cell_panel.setBorder(BorderFactory.createMatteBorder(top_border ? 1 : 0, left_border ? 1 : 0, 1, 1, ColorSettings.currentColorSetting.border) );
					cell_panel.addMouseListener(MouseAdapters.getEditCellAdapter(cell_panel, cell_str, content.get(row), col-1) );
					if (!cell_str_actions.isEmpty() )
						cell_panel.addMouseListener(MouseAdapters.getCellActionAdapter(cell_str_actions) );
					cell_panel.setOpaque(false);
					this.add(cell_panel, gbc);
					panel_row[col] = cell_panel;
					col ++;
				}
				// cell end
	
				gbc.gridx = col;
				JPanel todo_control = getTodoControl(row);
				this.add(todo_control, gbc);
				panel_row[FileOperations.numberOfColumns + 1] = todo_control;
				
				cells_list.add(panel_row);
				gbc.gridy ++;
			}
			// row end
			
			// controls for new row
			gbc.gridx = 0;
			JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(content.size(), true);
			this.add(add_remove_content_row_controls, gbc);
			cells_list.add(new JPanel[] {add_remove_content_row_controls} );
			
			
			cells = cells_list.toArray(new JPanel[cells_list.size() ][cells_list.get(0).length] );
		}
		gbc.gridx = col = 0;
	}
	
	public static void fillCellPanel(JPanel cell_panel, String cell_string)
	{
		cell_string = cell_string.replace("\\n", "\n").replace("->", "⇨"); 
		for (String row : cell_string.split("\n") )
		{
			JPanel horizontal_panel = new JPanel();
			horizontal_panel.setLayout(new BoxLayout(horizontal_panel, BoxLayout.X_AXIS) );
			horizontal_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			boolean text = true;
			for (String str : row.split("#") )
			{
				if (text)
				{
					JLabel label = new JLabel(str);
					label.setFont(MainGui.font);
					label.setForeground(ColorSettings.currentColorSetting.text);
					//label.addMouseListener(getTextEdit(label) );
					MainGui.labelsTextcolor.add(label);
					horizontal_panel.add(label);
				}
				else
				{
					if (str.contains(":") )
					{
						// Layered Images
						String[] images = str.split(":");
						if (images.length != 4) throw new RuntimeException("Error while parsing layered images: " + str + "! There have to be 2 images and 2 poition tags (t/b/c and l/r/c)");
						try
						{
							// wiki Strings
							String wiki_str_bg = Abbreviations.getAbbreviation(images[0] );
							String wiki_str_fg = Abbreviations.getAbbreviation(images[1] );
							
							// preparing Files
							File file_bg = new File("Images\\" + wiki_str_bg + ".png");
							File file_fg = new File("Images\\" + wiki_str_fg + ".png");
							File error = new File("Images\\Destroyed-icon.png");
							
							// preparing BufferedImages
							final BufferedImage back_ground;
							final BufferedImage fore_ground;
							if (file_bg.exists() )
								back_ground = ImageIO.read(file_bg);
							else
							{
								back_ground = ImageIO.read(error);
								if (PopupAlerts.creatMissingImagesMessage)
								{
									String new_message = wiki_str_bg + ".png" + (images[0].equals(wiki_str_bg) ? "" : " (" + images[0] + ")");
									if (!PopupAlerts.missingImagesMessage.contains(new_message) )
										PopupAlerts.missingImagesMessage += "\n" + new_message;
								}
							}
							if (file_fg.exists() )
								fore_ground = ImageIO.read(file_fg);
							else
							{
								fore_ground = ImageIO.read(error);
								if (PopupAlerts.creatMissingImagesMessage)
								{
									String new_message = wiki_str_fg + ".png" + (images[1].equals(wiki_str_fg) ? "" : " (" + images[1] + ")");
									if (!PopupAlerts.missingImagesMessage.contains(new_message) )
										PopupAlerts.missingImagesMessage += "\n" + new_message;
								}
							}
							final BufferedImage layer_dot = ImageIO.read(new File("Images\\Layer_dot.png")); // Black layer between images for better visibility
							final BufferedImage scaled = new BufferedImage(MainGui.ImageSize, MainGui.ImageSize, BufferedImage.TYPE_INT_ARGB); // empty BufferedImage to draw on
							Graphics g = scaled.getGraphics();
							
							// drawing images
							g.drawImage(back_ground, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
							int dot_image_size = 2 * MainGui.ImageSize / 3;
							int small_image_size = 3 * MainGui.ImageSize / 5;
							int x_dot = images[2].equals("t") ? 0 : (MainGui.ImageSize - dot_image_size) / (images[2].equals("c") ? 2 : 1);
							int y_dot = images[3].equals("l") ? 0 : (MainGui.ImageSize - dot_image_size) / (images[3].equals("c") ? 2 : 1);
							int x = images[2].equals("t") ? 0 : (MainGui.ImageSize - small_image_size) / (images[2].equals("c") ? 2 : 1);
							int y = images[3].equals("l") ? 0 : (MainGui.ImageSize - small_image_size) / (images[3].equals("c") ? 2 : 1);
							g.drawImage(layer_dot, x_dot, y_dot, dot_image_size, dot_image_size, null);
							g.drawImage(fore_ground, x, y, small_image_size, small_image_size, null);
							
							
							horizontal_panel.add(new JLabel(new ImageIcon(scaled) ) );
						} catch (MalformedURLException e)
						{
							e.printStackTrace();
						} catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						// normal Image
						String wiki_str = Abbreviations.getAbbreviation(str); 
						String file_string = "Images\\" + wiki_str + ".png";
						ImageIcon icon;
						if (new File(file_string).exists() )
							icon = new ImageIcon(file_string);
						else
						{
							icon = new ImageIcon("Images\\Destroyed-icon.png");
							if (PopupAlerts.creatMissingImagesMessage)
							{
								String new_message = wiki_str + ".png" + (str.equals(wiki_str) ? "" : " (" + str + ")");
								if (!PopupAlerts.missingImagesMessage.contains(new_message) )
									PopupAlerts.missingImagesMessage += "\n" + new_message;
							}
						}
						JLabel label = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(MainGui.ImageSize, MainGui.ImageSize, Image.SCALE_DEFAULT) ) );
						//label.addMouseListener(getIconEdit(file, null) );
						horizontal_panel.add(label);
						}
				}
				text = !text;
			}
			horizontal_panel.setOpaque(false);
			cell_panel.add(horizontal_panel);
		}
	}
	
	public JPanel getAddRemoveContentRowControl(int current_row, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		Color color = MainGui.inEditMode ? ColorSettings.currentColorSetting.text : ColorSettings.currentColorSetting.background;
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(MouseAdapters.getAddContentRowAdapter(current_row, this) );
		
		MainGui.labelsBackgroundcolorTextcolor.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(color);
			remove.addMouseListener(MouseAdapters.getRemoveContentRowAdapter(current_row, this) );
			
			MainGui.labelsBackgroundcolorTextcolor.add(remove);
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
			MainGui.labelsHideUnhide.add(icon);
		}
		
		todo_label.addMouseListener(MouseAdapters.getEditTodoAdapter(current_row, this, todo_panel, todo_label, icon) );
		
		MainGui.labelsTextcolor.add(todo_label);
		return todo_panel;
	}
	
	public void determineMaxWidth()
	{
		for (int col = 0; col < FileOperations.numberOfColumns + 2; col ++)
			if (maxWidths[col] < cells[0][col].getWidth() )
				maxWidths[col] = cells[0][col].getWidth();
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
