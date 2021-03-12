import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Dialog.ModalityType;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.TextField;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

public class Logic
{
	public static String[][] content;
	public static String[] sections;
	public static Integer[] sectionIndices;
	public static int maxRowLength;
	public static boolean unsavedChanges = false;
	
	public static void main(String[] args)
	{
		new Gui();
		Gui.colorSettings = getColorSettings();
		getContentFromFile(Gui.fileDirectory + Gui.fileName);
		Gui.arrangeContent(content);
		Gui.draw();
	}
	
	public static ColorSetting[] getColorSettings()
	{
		ArrayList<ColorSetting> colors_list = new ArrayList<ColorSetting>();
		File settings = new File("settings.txt");
		
		try {
			if (settings.createNewFile() )
			{
				ColorSetting[] res = new ColorSetting[] { new ColorSetting("Light" , Color.BLACK, Color.BLACK, Color.WHITE),
																									new ColorSetting("Dark"  , Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK),
																									new ColorSetting("Custom", Color.WHITE, Color.WHITE, Color.WHITE)
																									};
				updateSettingsFile(res);
				return res;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(settings) );
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!");
		}
		
		String line, name = "";
		int[][] colors = new int[3][3];
		try {
			for (int i = 0; (line = reader.readLine() ) != null; i = ++i % 4)
			{
				if (i == 0) name = line;
				else colors[i-1] = Stream.of(line.split(":") ).mapToInt(Integer::parseInt).toArray();
				if (i == 3)
					colors_list.add(new ColorSetting(name,	new Color(colors[0][0], colors[0][1], colors[0][2] ),
																									new Color(colors[1][0], colors[1][1], colors[1][2] ),
																									new Color(colors[2][0], colors[2][1], colors[2][2] )
																									) );
			}
			
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while reading settings file!");
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("Error while reading settings file!");
		}
		
		return colors_list.toArray(new ColorSetting[colors_list.size() ] );
	}
	
	public static void getContentFromFile(String file)
	{
		ArrayList<String[]> content_list = new ArrayList<String[]>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file) );
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!");
		}
		
		String header_string;
		String line_string;
		try {
			if ( (line_string = reader.readLine() ) != null)
				header_string = line_string.substring(line_string.startsWith("ï") ? 3 : 0);
			else
			{
				reader.close();
				throw new RuntimeException("Invalid header!");
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while reading header!");
		}
		
		String[] header = header_string.split(";");
		content_list.add(header);
		int length = header.length;
		String empty_line = "";
		for (int i = 1; i < length; i ++) empty_line += ";";
		String[] line;
		
		try {
			while ( (line_string = reader.readLine() ) != null)
			{
				if (line_string.equals(empty_line) ) continue;
				line = line_string.split(";");
				if (line.length > length)
				{
					reader.close();
					throw new RuntimeException();
				}
				else if (line.length < length)
				{
					String[] dummy = new String[length];
					for (int i = 0; i < length; i ++)
						if (i < line.length)
							dummy[i] = line[i];
						else
							dummy[i] = "";
					line = dummy;
				}
				
				content_list.add(line);
			}
			
			maxRowLength = content_list.get(0).length;
			content = content_list.toArray(new String[content_list.size() ][maxRowLength] );
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while reading file!");
		}
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
		{
			JPanel[] dummy = new JPanel[maxRowLength + 1];
			for (int i = 1; i <= maxRowLength; i ++)
				dummy[i] = new JPanel();
			panels_list.add(dummy);
		}
		
		// rows
		for (;row < content.length; row ++)
		{
			//if (content[row][0].equals("") && content[row][1].equals("") && content[row][2].equals("") )
			//	continue;
			if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
				break;
			
			JPanel[] panel_row = new JPanel[maxRowLength + 1];
			gbc.gridx = col = 0;
			JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(row, false);
			section_panel.add(add_remove_content_row_controls, gbc);
			panel_row[col] = add_remove_content_row_controls;
			
			col ++;
			
			for (String cell_str : content[row] )
			{
				gbc.gridx = col;
				JPanel cell = new JPanel();
				
				boolean left_border = (col == 1);
				boolean top_border  = (row == Gui.row || (row == Gui.row + 1 && !subsection.title.equals("") ) );
				
				fillCellPanel(cell, cell_str.replace("\\n", "\n").replace("->", "⇨"), left_border, top_border );
				cell.addMouseListener(getCellEdit(cell, left_border, top_border, row, col - 1) );
				cell.setOpaque(false);
				section_panel.add(cell, gbc);
				panel_row[col] = cell;
				col ++;
			}
			panels_list.add(panel_row);
			gbc.gridy ++;
		}
		gbc.gridx = col = 0;
		JPanel add_remove_content_row_controls = getAddRemoveContentRowControl(row, true);
		section_panel.add(add_remove_content_row_controls, gbc);
		panels_list.add(new JPanel[] {add_remove_content_row_controls} );
		
		Gui.row = row;
		subsection.content = panels_list.toArray(new JPanel[panels_list.size() ][panels_list.get(0).length] );
		return subsection;
	}
	
	public static JPanel getAddRemoveContentRowControl(int current_row, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		Color color = Gui.inEditMode ? Gui.currentColorSetting.text : Gui.currentColorSetting.background;
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(new MouseInputAdapter() {
																@Override
																public void mouseClicked(MouseEvent e)
																{
																	if (Gui.inEditMode)
																	{
																		unsavedChanges = true;
																		addContentLine(current_row);
																		Gui.arrangeContent(content);
																		Gui.draw();
																	}
																} } );
		
		Gui.addRemoveRowControlsList.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(color);
			remove.addMouseListener(new MouseInputAdapter() {
																	@Override
																	public void mouseClicked(MouseEvent e)
																	{
																		if (Gui.inEditMode)
																		{
																			unsavedChanges = true;
																			removeContentLine(current_row);
																			Gui.arrangeContent(content);
																			Gui.draw();
																		}
																	} } );
			
			Gui.addRemoveRowControlsList.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	public static void removeContentLine(int row_to_remove)
	{
		String[][] new_content = new String[content.length - 1][content[0].length];
		for (int i = 0; i < new_content.length; i ++)
			new_content[i] = content[i < row_to_remove ? i : i + 1];
		content = new_content;
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
	
	public static void addContentLine(int row_to_add)
	{
		String[][] new_content = new String[content.length + 1][content[0].length + 1];
		String[] newLine = new String[content[0].length];
		for (int i = 0; i < content[0].length; i ++) newLine[i] = " ";
		for (int i = 0; i < new_content.length; i ++)
			if (i == row_to_add)
				new_content[i] = newLine;
			else
				new_content[i] = content[i < row_to_add ? i : i - 1];
		content = new_content;
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
					Gui.labels.add(label);
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
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
		
		updateSettingsFile(Gui.colorSettings);
		options.dispose();
	}
	
	public static void updateSettingsFile(ColorSetting[] color_settings)
	{
		try
		{
			FileWriter writer = new FileWriter(new File("settings.txt"), false);
			for (ColorSetting color : color_settings)
			{
				writer.write(color.name + "\n" +
										color.text.getRed() +       ":" + color.text.getGreen() +       ":" + color.text.getBlue() + "\n" +
										color.border.getRed() +     ":" + color.border.getGreen() +     ":" + color.border.getBlue() + "\n" +
										color.background.getRed() + ":" + color.background.getGreen() + ":" + color.background.getBlue()  + "\n");
			}
			writer.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	/**
	public MouseInputAdapter getTextEdit(JLabel label)
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
	
	public static MouseInputAdapter getCellEdit(JPanel cell, boolean left_border, boolean top_border, int row, int col)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				if (Gui.inEditMode)
				{	
					String string = JOptionPane.showInputDialog(null, "Set the text!", content[row][col] );
					if (string != null) {
						content[row][col] = string; 
						cell.removeAll();
						fillCellPanel(cell, string.replace("\\n", "\n").replace("->", "⇨"), left_border, top_border);
						cell.validate();
						Gui.repaint();
						unsavedChanges = true;
					}
				}
			}
		};
	}
	
	public static void saveFile()
	{
		try (PrintWriter out = new PrintWriter(Gui.fileDirectory + Gui.fileName) )
		{
			for (String[] row : content)
			{
				String rowStr = "";
				for (String cell : row)
					rowStr += cell + ";";
				out.println(rowStr.substring(0, rowStr.length() - 1) );
			}
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			unsavedChanges = false;
		}
	}
	
	/**
	public MouseInputAdapter getIconEdit(File mainFile, File secondaryFile)
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
	
	public static File getIconFile(File old_file)
	{
		FileDialog dialog = new FileDialog(new Frame(), "Select File to Open");
    dialog.setMode(FileDialog.LOAD);
    dialog.setVisible(true);
    if (dialog.getFile() != null) return new File(dialog.getFile() );
    else return old_file;
	}
	
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
