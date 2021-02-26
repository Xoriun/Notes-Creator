import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Dialog.ModalityType;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
	Gui gui;
	public static String[][] content;
	public static int maxRowLength;
	public static boolean unsavedChanges = false; 
	
	public Logic()
	{
		gui = new Gui(this, getColorSettings() );
		getContentFromFile(gui.fileDirectory + gui.fileName);
		gui.arrangeContent(content);
		gui.draw();
	}
	
	public ColorSetting[] getColorSettings()
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
		
		String headerS;
		String lineS;
		try {
			if ( (lineS = reader.readLine() ) != null)
				headerS = lineS.substring(lineS.startsWith("ï") ? 3 : 0);
			else
			{
				reader.close();
				throw new RuntimeException("Invalid header!");
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while reading header!");
		}
		
		String[] header = headerS.split(";");
		content_list.add(header);
		int length = header.length;
		String[] line;
		
		try {
			while ( (lineS = reader.readLine() ) != null)
			{
				line = lineS.split(";");
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
	
	public JPanel[][] fillNotesSubPanel(JPanel sub_panel, GridBagConstraints gbc)
	{
		int row = gui.row, col = 0;
		sub_panel.removeAll();
		sub_panel.setAlignmentY(Component.LEFT_ALIGNMENT);
		
		ArrayList<JPanel[]> panelsList = new ArrayList<JPanel[]>();

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		
		String title = "";
		if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
		{
			title = content[row][0].substring(3, content[row][0].length() - 3);
			TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), title, TitledBorder.LEFT, TitledBorder.TOP);
			border.setTitleColor(gui.current_custom_color_setting.text);
			sub_panel.setBorder(border);
			row ++;
		}
		
		for (;row < content.length; row ++)
		{
			if (content[row][0].equals("") && content[row][1].equals("") && content[row][2].equals("") )
				continue;
			if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
				break;
			
			JPanel[] panel_row = new JPanel[maxRowLength + 1];
			gbc.gridx = col = 0;
			JPanel addRemoverowControls = getAddRemoveRowControl(row, false);
			sub_panel.add(addRemoverowControls, gbc);
			panel_row[col] = addRemoverowControls;
			
			col ++;
			
			for (String cell_str : content[row] )
			{
				gbc.gridx = col;
				JPanel cell = new JPanel();
				
				boolean leftBorder = (col == 1);
				boolean topBorder  = (row == gui.row || (row == gui.row + 1 && !title.equals("") ) );
				
				fillCellPanel(cell, cell_str.replace("\\n", "\n").replace("->", "⇨"), leftBorder, topBorder );
				cell.addMouseListener(getCellEdit(cell, leftBorder, topBorder, row, col - 1) );
				cell.setOpaque(false);
				sub_panel.add(cell, gbc);
				panel_row[col] = cell;
				col ++;
			}
			panelsList.add(panel_row);
			gbc.gridy ++;
		}
		gbc.gridx = col = 0;
		JPanel addRemoverowControls = getAddRemoveRowControl(row, true);
		sub_panel.add(addRemoverowControls, gbc);
		panelsList.add(new JPanel[] {addRemoverowControls} );
		
		gui.row = row;
		return panelsList.toArray(new JPanel[panelsList.size() ][panelsList.get(0).length] );
	}
	
	public JPanel getAddRemoveRowControl(int currentRow, boolean onlyAdd)
	{
		JPanel control = new JPanel(new GridLayout(onlyAdd ? 1 : 2, 2) );
		Color color = Gui.inEditMode ? gui.current_custom_color_setting.text : gui.current_custom_color_setting.background;
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(new MouseInputAdapter() {
																@Override
																public void mouseClicked(MouseEvent e)
																{
																	if (Gui.inEditMode)
																	{
																		unsavedChanges = true;
																		addLine(currentRow);
																		gui.arrangeContent(content);
																		gui.draw();
																	}
																} } );
		
		gui.addRemoveRowControlsList.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! onlyAdd)
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
																			removeLine(currentRow);
																			gui.arrangeContent(content);
																			gui.draw();
																		}
																	} } );
			
			gui.addRemoveRowControlsList.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	public void removeLine(int rowToRemove)
	{
		String[][] newContent = new String[content.length - 1][content[0].length];
		for (int i = 0; i < newContent.length; i ++)
			newContent[i] = content[i < rowToRemove ? i : i + 1];
		content = newContent;
	}
	
	public void addLine(int rowToAdd)
	{
		String[][] newContent = new String[content.length + 1][content[0].length + 1];
		String[] newLine = new String[content[0].length];
		for (int i = 0; i < content[0].length; i ++) newLine[i] = " ";
		for (int i = 0; i < newContent.length; i ++)
			if (i == rowToAdd)
				newContent[i] = newLine;
			else
				newContent[i] = content[i < rowToAdd ? i : i - 1];
		content = newContent;
	}
	
	public void fillCellPanel(JPanel v_panel, String cell, boolean leftBorder, boolean topBorder)
	{
		v_panel.setLayout(new BoxLayout(v_panel, BoxLayout.Y_AXIS) );
		
		for (String row : cell.split("\n") )
		{
			JPanel h_panel = new JPanel();
			h_panel.setLayout(new BoxLayout(h_panel, BoxLayout.X_AXIS) );
			h_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			boolean text = true;
			for (String str : row.split("#") )
			{
				if (text)
				{
					JLabel label = new JLabel(str);
					label.setFont(gui.font);
					label.setForeground(gui.current_custom_color_setting.text);
					//label.addMouseListener(getTextEdit(label) );
					gui.labels.add(label);
					h_panel.add(label);
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
							final BufferedImage backGround = ImageIO.read(file_bG.exists() ? file_bG : error);
							final BufferedImage foreGround = ImageIO.read(file_fG.exists() ? file_fG : error);
							final BufferedImage layerDot = ImageIO.read(new File("Images\\Layer_dot.png")); // Black layer between images for better visibility
							final BufferedImage scaled = new BufferedImage(Gui.ImageSize, Gui.ImageSize, BufferedImage.TYPE_INT_ARGB); // empty BufferedImage to draw on
							Graphics g = scaled.getGraphics();
							
							// drawing images
							g.drawImage(backGround, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
							int dotImageSize = 2 * Gui.ImageSize / 3;
							int smallImageSize = 3 * Gui.ImageSize / 5;
							int x_dot = images[2].equals("t") ? 0 : (Gui.ImageSize - dotImageSize) / (images[2].equals("c") ? 2 : 1);
							int y_dot = images[3].equals("l") ? 0 : (Gui.ImageSize - dotImageSize) / (images[3].equals("c") ? 2 : 1);
							int x = images[2].equals("t") ? 0 : (Gui.ImageSize - smallImageSize) / (images[2].equals("c") ? 2 : 1);
							int y = images[3].equals("l") ? 0 : (Gui.ImageSize - smallImageSize) / (images[3].equals("c") ? 2 : 1);
							g.drawImage(layerDot, x_dot, y_dot, dotImageSize, dotImageSize, null);
							g.drawImage(foreGround, x, y, smallImageSize, smallImageSize, null);
							
							
							h_panel.add(new JLabel(new ImageIcon(scaled) ) );
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
						h_panel.add(label);
						}
				}
				text = !text;
			}
			h_panel.setOpaque(false);
			v_panel.add(h_panel);
		}
		
		v_panel.setBorder(BorderFactory.createMatteBorder(topBorder ? 1 : 0, leftBorder ? 1 : 0, 1, 1, gui.current_custom_color_setting.border) );
		gui.cells.add(v_panel);
		v_panel.setOpaque(false);
	}
	
	public void fillColorSettingsPane(JPanel optionsPanel, ColorSetting colorSetting)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = gbc.gridy = 0;
		fillColorSettingsRow(optionsPanel, gbc, colorSetting.text, "Text");
		fillColorSettingsRow(optionsPanel, gbc, colorSetting.border, "Border");
		fillColorSettingsRow(optionsPanel, gbc, colorSetting.background, "Background   ");
	}
	
	public void fillColorSettingsRow(JPanel panel, GridBagConstraints gbc, Color color, String name)
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
	
	public void updateCustomColorSettings(JDialog options)
	{
		int[][] colors = new int[3][3];
		getAllComponents(options).stream().filter(comp -> comp instanceof TextField).forEach(text -> 
		{
			text.setBackground(Color.white);
			String name = text.getName();
			colors[name.startsWith("Text") ? 0 : name.startsWith("Border") ? 1 : 2][name.endsWith("r") ? 0 : name.endsWith("g") ? 1 : 2] = getColorInt( (TextField) text);
		});
		
		for (int[] row : colors) for (int cell : row) if (cell < 0 || cell > 255) return;
		
		gui.color_settings[2].text 			 = new Color(colors[0][0], colors[0][1], colors[0][2]);
		gui.color_settings[2].border		 = new Color(colors[1][0], colors[1][1], colors[1][2]);
		gui.color_settings[2].background = new Color(colors[2][0], colors[2][1], colors[2][2]);
		gui.current_custom_color_setting = gui.color_settings[2];
		
		updateSettingsFile(gui.color_settings);
		options.dispose();
	}
	
	public void updateSettingsFile(ColorSetting[] colorSettings)
	{
		try
		{
			FileWriter writer = new FileWriter(new File("settings.txt"), false);
			for (ColorSetting color : colorSettings)
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
	
	public List<Component> getAllComponents(final Container c)
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
	
	public MouseInputAdapter getCellEdit(JPanel cell, boolean left_border, boolean top_border, int row, int col)
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
						gui.repaint();
						unsavedChanges = true;
					}
				}
			}
		};
	}
	
	public void saveFile()
	{
		try (PrintWriter out = new PrintWriter(gui.fileDirectory + gui.fileName) )
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
	
	public File getIconFile(File oldFile)
	{
		FileDialog dialog = new FileDialog(new Frame(), "Select File to Open");
    dialog.setMode(FileDialog.LOAD);
    dialog.setVisible(true);
    if (dialog.getFile() != null) return new File(dialog.getFile() );
    else return oldFile;
	}
	
	public int getColorInt(TextField text)
	{
		int res = 0;
		try {
			res = Integer.parseInt( ( (TextField) text).getText() );
		} catch (NumberFormatException e) { res = -1;  }
		
		if (res < 0 || res > 255)
			text.setBackground(Color.red);
		
		return res;
	}
	
	public static void main(String[] args)
	{
		new Logic();
	}
}
