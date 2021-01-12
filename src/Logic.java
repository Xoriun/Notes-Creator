import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class Logic implements ActionListener
{
	Gui gui;
	public static String[][] content;
	
	public Logic()
	{
		gui = new Gui(this);
		getContentFromFile(gui.fileDirectory + gui.fileName);
		gui.draw(content);
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
		}
		catch (IOException e)
		{
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
							dummy[i]= line[i];
						else
						{
							dummy[i] = "";
						}
					line = dummy;
				}
				
				content_list.add(line);
			}
			
			content = content_list.toArray(new String[content_list.size() ][content_list.get(0).length] );
			reader.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error while reading file!");
		}
	}
	
	public static String getWikiName(String str)
	{
		switch (str)
		{
			case "Belt": return "Transport_belt";
			case "Ug_belt": return "Underground_belt";
			 
			case "Red_inserter": return "Long-handed_inserter";
			
			case "Power_pole": return "Small_electric_pole";
			case "Medium_power_pole": return "Medium_electric_pole";
			
			case "Rail": return "Straight_rail";
			
			case "E_miner": return "Electric_mining_drill";
			case "B_miner": return "Burner_mining_drill";
			
			case "Furnace": return "Stone_furnace";
			
			case "Assembler": return "Assembling_machine_1";
			case "Assembler_2": return "Assembling_machine_2";
			case "Refinery": return "Oil_refinery";
			
			case "Iron": return "Iron_plate";
			case "Copper": return "Copper_plate";
			case "Steel": return "Steel_plate";
			case "Plastic": return "Plastic_bar";
			
			case "Wire": return "Copper_cable";
			case "Gear": return "Iron_gear_wheel";
			case "Gc": return "Electronic_circuit";
			case "Red_circuit": return "Advanced_circuit";
			case "Engine": return "Engine_unit";
			case "Red_engine": return "Electric_engine_unit";
			case "Frame": case "Robot_frame": return "Flying_robot_frame";
			
			case "Oil": return "Crude_oil";
			case "Petroleum": return "Petrolem_gas";
			case "Advanced_oil": return "Advanced_oil_processing";
			case "Lub": return "Lubricant";
			
			case "Red_science": return "Automation_science_pack";
			case "Green_science": return "Logistic_science_pack";
			case "Blue_science": return "Chemical_science_pack";
			case "Purple_science": return "Productivity_science_pack";
			case "Yellow_science": return "Utility_science_pack";
			default:
				return str;
		}
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		String action = event.getActionCommand();
		//System.out.println(action);
		switch (action)
		{
			case "open":
				gui.getFile();
				refreshView();
				break;
			case "reload":
				gui.title_chaged = false;
				refreshView();
				break;
		}
	}
	
	public void refreshView()
	{
		getContentFromFile(gui.fileDirectory + gui.fileName);
		gui.draw(content);
	}
	
	public JPanel[][] fillGridBagPanel(JPanel sub_panel, GridBagConstraints gbc, JPanel[][] panels)
	{
		int row = gui.row, col = 0;
		int maxRowLength = content[0].length;
		ArrayList<JPanel[]> panels_list = new ArrayList<JPanel[]>();
		GridBagLayout layout = new GridBagLayout();
		
		sub_panel.setLayout(layout);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		
		String title = "";
		if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
		{
			title = content[row][0].substring(3, content[row][0].length() - 3);
			Border border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), title, TitledBorder.LEFT, TitledBorder.TOP);
			sub_panel.setBorder(border);
			row ++;
		}
		
		for (;row < content.length; row ++)
		{
			if (content[row][0].startsWith("---") && content[row][0].endsWith("---") )
				break;
			col = 0;
			JPanel[] panel_row = new JPanel[maxRowLength];
			for (String cell_str : content[row] )
			{
				gbc.gridx = col;
				JPanel cell = getCellPanel(cell_str.replace("->", "⇨"), col == 0, row == gui.row || (row == gui.row + 1 && !title.equals("") ) );
				sub_panel.add(cell, gbc);
				panel_row[col] = cell; 
				col ++;
			}
			panels_list.add(panel_row);
			gbc.gridy ++;
		}
		
		gui.row = row;
		return panels_list.toArray(new JPanel[panels_list.size() ][panels_list.get(0).length] );
	}
	
	public JPanel getCellPanel(String cell, boolean left_border, boolean top_border)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS) );
		String[] cells = cell.split("#");
		
		boolean text = true;
		for (String str : cells)
		{
			if (text)
			{
				JLabel label = new JLabel(str);
				label.setFont(gui.font);
				panel.add(label);
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
						File file_bG = new File("Images\\" + Logic.getWikiName(images[0] ) + ".png");
						File file_fG = new File("Images\\" + Logic.getWikiName(images[1] ) + ".png");
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
						
						
						panel.add(new JLabel(new ImageIcon(scaled) ) );
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
					File file = new File("Images\\" + Logic.getWikiName(str) + ".png");
					panel.add(new JLabel(new ImageIcon(new ImageIcon("Images\\" + (file.exists() ? Logic.getWikiName(str) : "Destroyed-icon") + ".png").getImage().getScaledInstance(Gui.ImageSize, Gui.ImageSize, Image.SCALE_DEFAULT) ) ) );
				}
			}
			text = !text;
		}
		
		panel.setBorder(BorderFactory.createMatteBorder(top_border ? 1 : 0, left_border ? 1 : 0, 1, 1, Color.BLACK) );
		return panel;
	}
	
	public static void main(String[] args)
	{
		new Logic();
	}
}
