import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.TextField;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class Logic
{
	public static String[][] content;
	public static String[] sections;
	public static Integer[] sectionIndices;
	public static String[] todoList;
	public static int maxRowLength;
	public static boolean unsavedChanges = false;
	
	public static void main(String[] args)
	{
		FileOperaitons.readSettingsFile();
		Gui.prepareGui();
		readAndDisplayNotes();
	}

	public static void readAndDisplayNotes()
	{
		FileOperaitons.getContentFromFile();
		Gui.arrangeContent();
		Gui.draw();
	}
	
	public static Subsection getSubsection(JPanel section_panel, GridBagConstraints gbc)
	{
		int row = Gui.row, col = 0;
		section_panel.removeAll();
		section_panel.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		Subsection subsection = new Subsection(row);
		
		ArrayList<JPanel[]> panels_list = new ArrayList<JPanel[]>();

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		
		// title
		subsection.title = "";
		if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
		{
			subsection.title = content[row][0].substring(3, content[row][0].length() - 3);
			TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), subsection.title, TitledBorder.LEFT, TitledBorder.TOP);
			border.setTitleColor(Gui.currentColorSetting.text);
			section_panel.setBorder(border);
			row ++;
		}
		
		if (row == content.length || (content[row][0].startsWith("---") && content[row][0].endsWith("---") ) ) // empty subsection
			subsection.content = new JPanel[0][0];
		else
		{
			// rows
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
	
				gbc.gridx = col;
				JPanel todo_control = getTodoControl(row);
				section_panel.add(todo_control, gbc);
				panel_row[maxRowLength + 1] = todo_control;
				
				panels_list.add(panel_row);
				gbc.gridy ++;
			}
			
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
							// preparing Files
							File file_bG = new File("Images\\" + Helper.getWikiName(images[0] ) + ".png");
							File file_fG = new File("Images\\" + Helper.getWikiName(images[1] ) + ".png");
							File error = new File("Images\\Destroyed-icon.png");
							
							// preparing BufferedImages
							final BufferedImage back_ground = ImageIO.read(file_bG.exists() ? file_bG : error);
							final BufferedImage fore_ground = ImageIO.read(file_fG.exists() ? file_fG : error);
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
						File file = new File("Images\\" + Helper.getWikiName(str) + ".png");
						JLabel label = new JLabel(new ImageIcon(new ImageIcon("Images\\" + (file.exists() ? Helper.getWikiName(str) : "Destroyed-icon") + ".png").getImage().getScaledInstance(Gui.ImageSize, Gui.ImageSize, Image.SCALE_DEFAULT) ) );
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
		String[][] new_content = new String[content.length + 1][content[0].length + 1];
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
		panel.add(new JLabel(name), gbc);
		
		gbc.gridx = 1;
		panel.add(new JLabel(" r "), gbc);
		gbc.gridx = 2;
		TextField text1 = new TextField("" + color.getRed() );
		text1.setName(name + "r");
		panel.add(text1, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		panel.add(new JLabel("g"), gbc);
		gbc.gridx = 2;
		TextField text2 = new TextField("" + color.getGreen() );
		text2.setName(name + "g");
		panel.add(text2, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		panel.add(new JLabel("b"), gbc);
		gbc.gridx = 2;
		TextField text3 = new TextField("" + color.getBlue() );
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
		
		FileOperaitons.updateSettingsFile();
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
