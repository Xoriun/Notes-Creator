import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.TextField;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

public class Logic
{
	public static String[][] content;
	public static String[] sections;
	public static Integer[] sectionIndices;
	public static String[] todoList;
	public static ArrayList<String[]> abbreviationsList = new ArrayList<String[]>();
	public static int maxRowLength;
	public static boolean unsavedChanges = false;
	public static String missingImagesMessage = "";
	public static boolean creatMissingImagesMessage = false;
	
	public static void main(String[] args)
	{
		Logger.getLogger(GlobalScreen.class.getPackage().getName() ).setLevel(Level.OFF);
		
		//**
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}
		//*/
		
		GlobalScreen.addNativeKeyListener(new Hotkeys() );
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				initializeLogic();
			}
		});
	}
	
	public static void initializeLogic()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName() );
		} catch (Exception e) { }
		
		FileOperaitons.readSettingsFile();
		Gui.prepareGui();
		readAndDisplayNotes();
	}

	public static void readAndDisplayNotes()
	{
		FileOperaitons.readNotesFile();
		Gui.arrangeContent();
		Gui.spaceColums();
	}
	
	public static Subsection getSubsection(JPanel section_panel, GridBagConstraints gbc)
	{
		int row = Gui.row, col = 0;
		section_panel.removeAll();
		section_panel.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		Subsection subsection = new Subsection(row);
		
		// list of panels in the new section
		ArrayList<JPanel[]> panels_list = new ArrayList<JPanel[]>();

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		
		// title
		subsection.title = "";
		if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
		{
			subsection.setTitle(content[row][0].substring(3, content[row][0].length() - 3) );
			TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), subsection.title, TitledBorder.LEFT, TitledBorder.TOP);
			border.setTitleFont(Gui.titleFont);
			border.setTitleColor(Gui.currentColorSetting.text);
			section_panel.setBorder(border);
			row ++;
		}
		
		if ( row == content.length || (content[row][0].startsWith("---") && content[row][0].endsWith("---") ) ) // empty section at the end || empty subsection)
		{
			gbc.gridx = 0;
			JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(row, true);
			section_panel.add(add_remove_content_row_controls, gbc);
			
			JPanel[] panels = new JPanel[maxRowLength + 2];
			panels[0] = add_remove_content_row_controls;
			for (int i = 1; i < maxRowLength + 2; i ++)
				panels[i] = new JPanel(); 
			
			panels_list.add(panels);
			subsection.content = panels_list.toArray(new JPanel[panels_list.size() ][panels_list.get(0).length] );
		}
		else
		{
			// row
			for (;row < content.length; row ++)
			{
				if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
					break;
				
				JPanel[] panel_row = new JPanel[maxRowLength + 2];
				gbc.gridx = col = 0;
				
				JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(row, false);
				section_panel.add(add_remove_content_row_controls, gbc);
				panel_row[0] = add_remove_content_row_controls;
				
				col ++;
				
				// cell
				for (String cell_str : content[row] )
				{
					gbc.gridx = col;
					JPanel cell = new JPanel();
					
					boolean left_border = (col == 1);
					boolean top_border  = (row == Gui.row || (row == Gui.row + 1 && !subsection.title.equals("") ) );
					
					String cell_str_actions = "", cell_str_content = cell_str;
					if (cell_str.contains(">>") )
					{
						String[] temp = cell_str.split(">>", 2);
						cell_str_content = temp[0];
						cell_str_actions = temp[1];
					}
					
					fillCellPanel(cell, cell_str_content.replace("\\n", "\n").replace("->", "⇨"), left_border, top_border );
					cell.addMouseListener(MouseAdapters.getCellEdit(cell, left_border, top_border, row, col - 1) );
					if (!cell_str_actions.equals("") )
						cell.addMouseListener(MouseAdapters.getCellAction(cell_str_actions) );
					cell.setOpaque(false);
					section_panel.add(cell, gbc);
					panel_row[col] = cell;
					col ++;
				}
				// cell end
	
				gbc.gridx = col;
				JPanel todo_control = getTodoControl(row);
				section_panel.add(todo_control, gbc);
				panel_row[maxRowLength + 1] = todo_control;
				
				panels_list.add(panel_row);
				gbc.gridy ++;
			}
			// row end
			
			// controls for new row
			gbc.gridx = 0;
			JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(row, true);
			section_panel.add(add_remove_content_row_controls, gbc);
			panels_list.add(new JPanel[] {add_remove_content_row_controls} );
			
			
			subsection.content = panels_list.toArray(new JPanel[panels_list.size() ][panels_list.get(0).length] );
		}
		gbc.gridx = col = 0;
		
		Gui.row = row;
		return subsection;
	}
	
	public static void fillCellPanel(JPanel cell_panel, String cell_string, boolean left_border, boolean top_border)
	{
		cell_panel.setLayout(new BoxLayout(cell_panel, BoxLayout.Y_AXIS) );
		
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
					label.setFont(Gui.font);
					label.setForeground(Gui.currentColorSetting.text);
					//label.addMouseListener(getTextEdit(label) );
					Gui.labelsTextcolor.add(label);
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
							String wiki_str_bg = getAbbreviation(images[0] );
							String wiki_str_fg = getAbbreviation(images[1] );
							
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
								if (creatMissingImagesMessage)
								{
									String new_message = wiki_str_bg + ".png" + (images[0].equals(wiki_str_bg) ? "" : " (" + images[0] + ")");
									if (!missingImagesMessage.contains(new_message) )
											missingImagesMessage += "\n" + new_message;
								}
							}
							if (file_fg.exists() )
								fore_ground = ImageIO.read(file_fg);
							else
							{
								fore_ground = ImageIO.read(error);
								if (creatMissingImagesMessage)
								{
									String new_message = wiki_str_fg + ".png" + (images[1].equals(wiki_str_fg) ? "" : " (" + images[1] + ")");
									if (!missingImagesMessage.contains(new_message) )
									missingImagesMessage += "\n" + new_message;
								}
							}
							final BufferedImage layer_dot = ImageIO.read(new File("Images\\Layer_dot.png")); // Black layer between images for better visibility
							final BufferedImage scaled = new BufferedImage(Gui.ImageSize, Gui.ImageSize, BufferedImage.TYPE_INT_ARGB); // empty BufferedImage to draw on
							Graphics g = scaled.getGraphics();
							
							// drawing images
							g.drawImage(back_ground, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
							int dot_image_size = 2 * Gui.ImageSize / 3;
							int small_image_size = 3 * Gui.ImageSize / 5;
							int x_dot = images[2].equals("t") ? 0 : (Gui.ImageSize - dot_image_size) / (images[2].equals("c") ? 2 : 1);
							int y_dot = images[3].equals("l") ? 0 : (Gui.ImageSize - dot_image_size) / (images[3].equals("c") ? 2 : 1);
							int x = images[2].equals("t") ? 0 : (Gui.ImageSize - small_image_size) / (images[2].equals("c") ? 2 : 1);
							int y = images[3].equals("l") ? 0 : (Gui.ImageSize - small_image_size) / (images[3].equals("c") ? 2 : 1);
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
						String wiki_str = getAbbreviation(str); 
						String file_string = "Images\\" + wiki_str + ".png";
						ImageIcon icon;
						if (new File(file_string).exists() )
							icon = new ImageIcon(file_string);
						else
						{
							icon = new ImageIcon("Images\\Destroyed-icon.png");
							if (creatMissingImagesMessage)
							{
								String new_message = wiki_str + ".png" + (str.equals(wiki_str) ? "" : " (" + str + ")");
								if (!missingImagesMessage.contains(new_message) )
								missingImagesMessage += "\n" + new_message;
							}
						}
						JLabel label = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(Gui.ImageSize, Gui.ImageSize, Image.SCALE_DEFAULT) ) );
						//label.addMouseListener(getIconEdit(file, null) );
						horizontal_panel.add(label);
						}
				}
				text = !text;
			}
			horizontal_panel.setOpaque(false);
			cell_panel.add(horizontal_panel);
		}
		
		cell_panel.setBorder(BorderFactory.createMatteBorder(top_border ? 1 : 0, left_border ? 1 : 0, 1, 1, Gui.currentColorSetting.border) );
		Gui.cells.add(cell_panel);
		cell_panel.setOpaque(false);
	}

	public static JPanel getAddRemoveContentRowControl(int current_row, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		Color color = Gui.inEditMode ? Gui.currentColorSetting.text : Gui.currentColorSetting.background;
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(MouseAdapters.getAddContentRowControl(current_row) );
		
		Gui.labelsBackgroundcolorTextcolor.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(color);
			remove.addMouseListener(MouseAdapters.getRemoveContentRowControl(current_row) );
			
			Gui.labelsBackgroundcolorTextcolor.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	public static JPanel getTodoControl(int current_row)
	{
		JPanel todo_panel = new JPanel();
		todo_panel.setLayout(new BoxLayout(todo_panel, BoxLayout.X_AXIS) );
		todo_panel.setOpaque(false);
		JLabel todo_label = new JLabel("  +  ");
		
		todo_label.setForeground(Gui.currentColorSetting.text);
		todo_label.setOpaque(false);
		todo_panel.add(todo_label);
		
		if (!todoList[current_row].equals("") )
		{
			JLabel icon = new JLabel(new ImageIcon(new ImageIcon("Images\\Not-enough-repair-packs-icon" + ".png").getImage().getScaledInstance(Gui.ImageSize, Gui.ImageSize, Image.SCALE_DEFAULT) ) );
			todo_panel.add(icon);
			icon.setVisible(Gui.inEditMode);
			Gui.labelsHideUnhide.add(icon);
		}
		
		todo_label.addMouseListener(MouseAdapters.getTodoEdit(current_row) );
		
		Gui.labelsTextcolor.add(todo_label);
		return todo_panel;
	}
	
	public static void removeContentLine(int row_to_remove)
	{
		String[][] new_content = new String[content.length - 1][content[0].length];
		String[] new_todo_list = new String[content.length - 1];
		for (int i = 0; i < new_content.length; i ++)
		{
			new_content  [i] = content [i < row_to_remove ? i : i + 1];
			new_todo_list[i] = todoList[i < row_to_remove ? i : i + 1];
		}
		content = new_content;
		todoList = new_todo_list;
	}
	
	public static void addContentLine(int row_to_add)
	{
		String[][] new_content = new String[content.length + 1][content[0].length];
		String[] newLine = new String[content[0].length];
		String[] new_todo_list = new String[content.length + 1];
		for (int i = 0; i < content[0].length; i ++) newLine[i] = " ";
		for (int i = 0; i < new_content.length; i ++)
			if (i == row_to_add)
			{
				new_content[i] = newLine;
				new_todo_list[i] = ""; 
			}
			else
			{
				new_content  [i] = content [i < row_to_add ? i : i - 1];
				new_todo_list[i] = todoList[i < row_to_add ? i : i - 1];
			}
		content = new_content;
		todoList = new_todo_list;
	}
	
	public static void removeContentColumn(int col_to_remove)
	{
		String[][] new_content = new String[content.length][content[0].length - 1];
		for (int row = 0; row < new_content.length; row ++)
			for (int col = 0; col < new_content[0].length; col ++)
				new_content[row][col] = content[row][col < col_to_remove ? col : col + 1];
		content = new_content;
		maxRowLength --;
	}
	
	public static void addContentColumn(int col_to_add)
	{
		String[][] new_content = new String[content.length][content[0].length + 1];
		for (int row = 0; row < new_content.length; row ++)
			for (int col = 0; col < new_content[0].length; col ++)
				if (col == col_to_add)
					new_content[row][col] = "";
				else
					new_content[row][col] = content[row][col < col_to_add ? col : col - 1];
		for (int row = 0; row < new_content.length; row ++)
			if ( !new_content[row][0].startsWith("---") || !new_content[row][0].endsWith("---") )
			{
				new_content[row][col_to_add] = "new colomn";
				break;
			}
		content = new_content;
		maxRowLength ++;
	}

	public static void removeSectionLine(int row_to_remove)
	{
		String[]  new_sections        = new String [sections.      length - 1];
		Integer[] new_section_indices = new Integer[sectionIndices.length - 1];
		for (int i = 0; i < new_sections.length; i ++)
		{
			new_sections       [i] = sections      [i < row_to_remove ? i : i + 1];
			new_section_indices[i] = sectionIndices[i < row_to_remove ? i : i + 1];
		}
		sections       = new_sections;
		sectionIndices = new_section_indices;
	}
	
	public static void addSectionLine(int row_to_add)
	{
		String[]  new_sections        = new String [sections.      length - 1];
		Integer[] new_section_indices = new Integer[sectionIndices.length - 1];
		for (int i = 0; i < new_sections.length; i ++)
			if (i == row_to_add)
			{
				new_sections       [i] = "";
				new_section_indices[i] = sectionIndices[i];
			}
			else
			{
				new_sections[i] = sections[i < row_to_add ? i : i - 1];
				new_section_indices [i] = i < row_to_add ? sectionIndices[i] : sectionIndices[i - 1] + 1;
			}
		sections       = new_sections;
		sectionIndices = new_section_indices;
	}
	
	public static void fillColorSettingsPane(JPanel options_panel, ColorSetting color_setting)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = gbc.gridy = 0;
		fillColorSettingsRow(options_panel, gbc, color_setting.text, "Text");
		fillColorSettingsRow(options_panel, gbc, color_setting.border, "Border");
		fillColorSettingsRow(options_panel, gbc, color_setting.background, "Background   ");
	}
	
	public static void fillColorSettingsRow(JPanel panel, GridBagConstraints gbc, Color color, String name)
	{
		
		gbc.gridx = 0;
		JLabel label_name = new JLabel(name);
		label_name.setForeground(Gui.currentColorSetting.text);
		label_name.setOpaque(false);
		panel.add(label_name, gbc);
		
		gbc.gridx = 1;
		JLabel label_r = new JLabel(" r ");
		label_r.setForeground(Gui.currentColorSetting.text);
		label_r.setOpaque(false);
		panel.add(label_r, gbc);
		gbc.gridx = 2;
		JTextField text1 = new JTextField("" + color.getRed() );
		text1.setForeground(Gui.currentColorSetting.text);
		text1.setBackground(Gui.currentColorSetting.background);
		text1.setBorder(BorderFactory.createMatteBorder(name.equals("Text") ? 1 : 0, 1, 1, 1, Gui.currentColorSetting.border) );
		text1.setName(name + "r");
		panel.add(text1, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		JLabel label_g = new JLabel(" g ");
		label_g.setForeground(Gui.currentColorSetting.text);
		label_g.setOpaque(false);
		panel.add(label_g, gbc);
		gbc.gridx = 2;
		JTextField text2 = new JTextField("" + color.getGreen() );
		text2.setForeground(Gui.currentColorSetting.text);
		text2.setBackground(Gui.currentColorSetting.background);
		text2.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Gui.currentColorSetting.border) );
		text2.setName(name + "g");
		panel.add(text2, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		JLabel label_b = new JLabel(" b ");
		label_b.setForeground(Gui.currentColorSetting.text);
		label_b.setOpaque(false);
		panel.add(label_b, gbc);
		gbc.gridx = 2;
		JTextField text3 = new JTextField("" + color.getBlue() );
		text3.setForeground(Gui.currentColorSetting.text);
		text3.setBackground(Gui.currentColorSetting.background);
		text3.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Gui.currentColorSetting.border) );
		text3.setName(name + "b");
		panel.add(text3, gbc);
		gbc.gridy ++;
	}
	
	public static void updateCustomColorSettings(JDialog options)
	{
		int[][] colors = new int[3][3];
		getAllComponents(options).stream().filter(comp -> comp instanceof TextField).forEach(text -> 
		{
			text.setBackground(Color.white);
			String name = text.getName();
			colors[name.startsWith("Text") ? 0 : name.startsWith("Border") ? 1 : 2][name.endsWith("r") ? 0 : name.endsWith("g") ? 1 : 2] = getColorInt( (TextField) text);
		});
		
		for (int[] row : colors) for (int cell : row) if (cell < 0 || cell > 255) return;
		
		Gui.colorSettings[2].text 			 = new Color(colors[0][0], colors[0][1], colors[0][2]);
		Gui.colorSettings[2].border		 = new Color(colors[1][0], colors[1][1], colors[1][2]);
		Gui.colorSettings[2].background = new Color(colors[2][0], colors[2][1], colors[2][2]);
		Gui.currentColorSetting = Gui.colorSettings[2];
		
		FileOperaitons.writeSettingsFile();
		options.dispose();
	}
	
	public static List<Component> getAllComponents(final Container c)
	{
    List<Component> compList = new ArrayList<Component>();
    for (Component comp : c.getComponents()) {
        compList.add(comp);
        if (comp instanceof Container)
            compList.addAll(getAllComponents((Container) comp));
    }
    return compList;
	}
	
	public static void getAbbreviationSettings(String abbr_location_copy, ArrayList<String[]> abbr_list_copy)
	{
		JDialog abbreviations_dialog = new JDialog(Gui.window);
		abbreviations_dialog.setModal(true);
		abbreviations_dialog.setTitle("Edit abbreviations");
		abbreviations_dialog.setMinimumSize(new Dimension(400, 200) );
		
		JPanel outer_panel = new JPanel();
		outer_panel.setLayout(new BoxLayout(outer_panel, BoxLayout.Y_AXIS) );
		outer_panel.setBackground(Gui.currentColorSetting.background);
		outer_panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Gui.currentColorSetting.text) );
		
		JPanel settings_panel = new JPanel();
		settings_panel.setLayout(new BoxLayout(settings_panel, BoxLayout.Y_AXIS) );
		settings_panel.setOpaque(false);
		settings_panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 2));
		
	  // file panel
			JPanel file_panel = new JPanel();
			file_panel.setLayout(new BoxLayout(file_panel, BoxLayout.Y_AXIS) );
			file_panel.setOpaque(false);
			
			// file location
			JPanel current_file_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			current_file_panel.setOpaque(false);
			JLabel current_file = new JLabel("Selected abbreviations file: " + abbr_location_copy);
			current_file.setForeground(Gui.currentColorSetting.text);
			current_file_panel.add(current_file);
			
			// load file
			JPanel file_actions_panel = new JPanel();
			file_actions_panel.setOpaque(false);
			JButton file_select = new JButton("Select abbreviations file");
			JButton file_new    = new JButton("New abbreviations file");
			JButton file_remove = new JButton("Remove abbreviations file");
			file_select.addActionListener(e -> {
				String new_abbr_loaction = FileOperaitons.selectAbbreviationsFile();
				abbreviations_dialog.dispose();
				ArrayList<String[]> new_abbr_list = FileOperaitons.readAbbriviationsFile(new_abbr_loaction);
				if (!new_abbr_list.isEmpty() )
					getAbbreviationSettings(new_abbr_loaction, new_abbr_list);
			} );
			file_new.addActionListener(e -> {
				abbreviations_dialog.dispose();
				getAbbreviationSettings(FileOperaitons.newAbbreviationsFile(abbr_list_copy), abbr_list_copy);
			} );
			file_remove.addActionListener(e -> {
				abbreviations_dialog.dispose();
				getAbbreviationSettings("", new ArrayList<String[]>() );
			} );
			file_actions_panel.add(file_select);
			file_actions_panel.add(file_new);
			file_actions_panel.add(file_remove);
	    
			// fill panels
			file_panel.add(current_file_panel);
			file_panel.add(file_actions_panel);
			
			settings_panel.add(file_panel);
		
		// abbreviationsList panel
			JPanel abbr_panel = new JPanel();
			abbr_panel.setLayout(new BoxLayout(abbr_panel, BoxLayout.Y_AXIS) );
			abbr_panel.setOpaque(false);
			JButton abbr_add_button = new JButton("Add abbreviation");
			
			// abbreviationsList list panel
			JPanel abbr_list_panel = new JPanel(new GridBagLayout() );
			abbr_list_panel.setBackground(Gui.currentColorSetting.background);
			GridBagConstraints gbc = new GridBagConstraints();
			Dimension min_dim = new Dimension(150,20);
			ArrayList<JTextField[]> textfield_list = new ArrayList<JTextField[]>();
			
			gbc.gridy = 0;
			for (String[] abbr : abbr_list_copy)
			{
				JTextField[] row = new JTextField[2];
				for (int i = 0; i < 2; i ++)
				{
					gbc.gridx = i;
					JTextField textfield = new JTextField(abbr[i] );
					textfield.setBorder(BorderFactory.createMatteBorder(gbc.gridy == 0 ? 1 : 0, 1, 1, i, Gui.currentColorSetting.border) );
					textfield.setPreferredSize(min_dim);
					row[i] = textfield;
					textfield.setOpaque(false);
					textfield.setForeground(Gui.currentColorSetting.text);
					abbr_list_panel.add(textfield, gbc);
				}
				textfield_list.add(row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(Gui.currentColorSetting.text);
				remove.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e)
					{
						abbreviations_dialog.dispose();
						textfield_list.remove(row);
						abbr_list_copy.remove(abbr);
						getAbbreviationSettings(abbr_location_copy, getAbbreviationsListFromTextfiles(textfield_list) );
					}
				} );
				gbc.gridx = 3;
				abbr_list_panel.add(remove, gbc);
				gbc.gridy ++;
			}
			
			// abbreviation add button
			JPanel abbr_add_panel = new JPanel();
			abbr_add_panel.setOpaque(false);
			abbr_add_button.addActionListener(e -> {
				String[] new_abbr = new String[] {"",""};
				abbr_list_copy.add(new_abbr);
				JTextField[] new_row = new JTextField[2];
				for (int i = 0; i < 2; i ++)
				{
					gbc.gridx = i;
					JTextField textfield = new JTextField("");
					textfield.setBorder(BorderFactory.createMatteBorder(gbc.gridy == 0 ? 1 : 0, 1, 1, i, Gui.currentColorSetting.border) );
					textfield.setPreferredSize(min_dim);
					new_row[i] = textfield;
					textfield.setOpaque(false);
					textfield.setForeground(Gui.currentColorSetting.text);
					abbr_list_panel.add(textfield, gbc);
				}
				textfield_list.add(new_row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(Gui.currentColorSetting.text);
				remove.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e)
					{
						abbreviations_dialog.dispose();
						textfield_list.remove(new_row);
						abbr_list_copy.remove(new_abbr);
						getAbbreviationSettings(abbr_location_copy, getAbbreviationsListFromTextfiles(textfield_list) );
					}
				} );
				gbc.gridx = 3;
				abbr_list_panel.add(remove, gbc);
				gbc.gridy ++;
				abbreviations_dialog.pack();
				abbreviations_dialog.repaint();
			} );
			abbr_add_panel.add(abbr_add_button);
			
			// fill panels
			JScrollPane abbr_scroll_pane = new JScrollPane(abbr_list_panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			abbr_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);
			abbr_scroll_pane.setBackground(Gui.currentColorSetting.background);
			abbr_scroll_pane.setBorder(null);
			abbr_panel.add(abbr_scroll_pane);
			abbr_panel.add(abbr_add_panel);
			
			settings_panel.add(abbr_panel);
		
		// controls panel
			JPanel controls_panel = new JPanel();
			controls_panel.setOpaque(false);
			
			JButton confirm = new JButton("Confirm");
			JButton cancel  = new JButton("Cancel");
			
			confirm.addActionListener(e -> {
				abbreviations_dialog.dispose();
				unsavedChanges = true;
				creatMissingImagesMessage = true;
				abbreviationsList = getAbbreviationsListFromTextfiles(textfield_list);
				FileOperaitons.fileAbbreviations = abbr_location_copy;
				FileOperaitons.saveAbbereviationsFile();
				Gui.arrangeContent();
				Gui.spaceColums();
			} );
			cancel .addActionListener(e -> { abbreviations_dialog.dispose(); } );
		
			controls_panel.add(confirm);
			controls_panel.add(cancel);
			
			settings_panel.add(controls_panel);
			
		outer_panel.add(settings_panel);
		abbreviations_dialog.add(outer_panel);
		abbreviations_dialog.pack();
		Gui.setLocation(abbreviations_dialog, -200);
		abbreviations_dialog.pack();
		abbreviations_dialog.setVisible(true);
		abbreviations_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public static ArrayList<String[]> getAbbreviationsListFromTextfiles(ArrayList<JTextField[]> textfields)
	{
		return textfields.stream().map(row -> new String[] {row[0].getText(), row[1].getText() } ).collect(Collectors.toCollection(ArrayList::new) );
	}
	
	public static String getAbbreviation(String str)
	{
		if (abbreviationsList != null)
			for (String[] abbr : abbreviationsList)
				if (str.equals(abbr[0]) )
					return abbr[1];
		return str;
	}
	
	/** public MouseInputAdapter getTextEdit(JLabel label)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				String newText = JOptionPane.showInputDialog(null, "Set the text!", label.getText() );
				if (newText != null) label.setText(newText);
			}
		};
	}
	*/
	
	/** public MouseInputAdapter getIconEdit(File mainFile, File secondaryFile)
	{
		return new MouseInputAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				JPanel mainPanel = new JPanel(new GridBagLayout() );
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridy = 0;
				
				// main Icon
				gbc.gridx = 0;
				mainPanel.add(new JLabel("Main Icon:"), gbc);
				gbc.gridx ++;
				mainPanel.add(new JLabel(mainFile.getName() ) );
				gbc.gridx ++;
				mainPanel.add(new JLabel(new ImageIcon(new ImageIcon(mainFile.exists() ? mainFile.getAbsolutePath() : "Images\\Destroyed-icon.png").getImage().getScaledInstance(Gui.ImageSize, Gui.ImageSize, Image.SCALE_DEFAULT) ) ) );
				gbc.gridx ++;
				mainPanel.add(new JButton("Change"), gbc);
				
				// checkbox secondary Icon
				gbc.gridy ++;
				gbc.gridx = 0;
				mainPanel.add(new JLabel("Use secondary Icon"), gbc);
				gbc.gridx ++;
				JCheckBox checkBox = new JCheckBox();
				mainPanel.add(checkBox, gbc);
				
				// secondary Icon
				gbc.gridy ++;
				gbc.gridx = 0;
				JLabel secLabel = new JLabel("Secundary Icon:");
				mainPanel.add(secLabel, gbc);
				gbc.gridx ++;
				JLabel secFileName = new JLabel(secondaryFile == null ? "" : secondaryFile.getName() );
				mainPanel.add(secFileName);
				gbc.gridx ++;
				JLabel secIcon = secondaryFile == null ? new JLabel() : new JLabel(new ImageIcon(new ImageIcon(secondaryFile.exists() ? secondaryFile.getAbsolutePath() : "Images\\Destroyed-icon.png").getImage().getScaledInstance(Gui.ImageSize, Gui.ImageSize, Image.SCALE_DEFAULT) ) );
				mainPanel.add(secIcon);
				gbc.gridx ++;
				JButton secButton = new JButton("Change");
				mainPanel.add(secButton, gbc);
				// secButton.addActionListener( e -> { secondaryFile = getIconFile(secondaryFile); } );
				
				// secondary Icon location
				gbc.gridy ++;
				gbc.gridx = 0;
				JLabel secLocationLabel = new JLabel("Location of secondary Icon");
				mainPanel.add(secLocationLabel, gbc);
				gbc.gridx ++;
				JComboBox<String> secVert = new JComboBox<String>( new String[] {"top", "center", "buttom"} );
				mainPanel.add(secVert, gbc);
				gbc.gridx ++;
				JComboBox<String> secHor = new JComboBox<String>( new String[] {"left", "center", "right"} );
				mainPanel.add(secHor, gbc);
				
				// confirm
				gbc.gridy ++;
				gbc.gridx = 0;
				gbc.gridwidth = 4;
				JButton confirm = new JButton("Confirm");
				JButton cancel = new JButton("Cancel");
				JPanel confirmPanel = new JPanel();
				confirmPanel.add(confirm);
				confirmPanel.add(cancel);
				mainPanel.add(confirmPanel, gbc);
				
				JDialog iconSelect = new JDialog();
				iconSelect.setModalityType(ModalityType.APPLICATION_MODAL);
				iconSelect.add(mainPanel);
				iconSelect.pack();
				iconSelect.setVisible(true);
			}
		};
	}
	*/
	
	/** public static File getIconFile(File old_file)
	{
		FileDialog dialog = new FileDialog(new Frame(), "Select File to Open");
    dialog.setMode(FileDialog.LOAD);
    dialog.setVisible(true);
    if (dialog.getFile() != null) return new File(dialog.getFile() );
    else return old_file;
	}*/
	
	public static int getColorInt(TextField text)
	{
		int res = 0;
		try {
			res = Integer.parseInt( ( (TextField) text).getText() );
		} catch (NumberFormatException e) { res = -1;  }
		
		if (res < 0 || res > 255)
			text.setBackground(Color.red);
		
		return res;
	}
}
