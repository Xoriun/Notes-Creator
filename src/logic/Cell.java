package logic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import gui.Abbreviations;
import gui.ColorSettings;
import gui.MainGui;
import gui.PopupAlerts;

public class Cell extends JPanel
{
	//auto-generated serialVersionUID
	private static final long serialVersionUID = 1L;
	
	private int row;
	private int col;
	private Section section;
	private boolean top_border, left_border;
	private String cellContentStr;
	private String cellActionsString;
	
	public Cell(Section section, String cell_str, int row, int col)
	{
		this.section = section;
		this.row = row;
		this.col = col;
		this.top_border = (row == 0);
		this.left_border = (col == 0);
		
		if (cell_str.contains(">>") )
		{
			String[] temp = cell_str.split(">>", 2);
			cellContentStr = temp[0];
			cellActionsString = temp[1];
		}
		else
		{
			cellContentStr = cell_str;
			cellActionsString = "";
		}
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS) );
		fillCellPanel(cellContentStr);
		setDefaultBorder();
		this.addMouseListener(MouseAdapters.getEditCellAdapter(this) );
		if (!cellActionsString.isEmpty() )
			this.addMouseListener(MouseAdapters.getCellActionAdapter(cellActionsString) );
		this.setOpaque(false);
	}
	
	public String getInfo()
	{
		return "Section " + section.getTitle() + " at [" + row + ", " + col + "]";
	}
	
	public String getContentString() { return section.getContent().get(row)[col]; }
	public void setContentString(String new_content) { section.getContent().get(row)[col] = new_content; } 
	public Section getSection() { return section; }
	public int getCol() { return col; }
	public int getRow() { return row; }
	public String getCellString() { return cellContentStr + (cellActionsString.isEmpty() ? "" : ">>" + cellActionsString); }
	
	public void setDefaultBorder()
	{
		this.setBorder(BorderFactory.createMatteBorder(top_border ? 1 : 0, left_border ? 1 : 0, 1, 1, ColorSettings.currentColorSetting.border) );
	}
	
	public void setSelectedBorder()
	{
		this.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.RED) );
	}
	
	public void clearContentAndListeners()
	{
		this.removeAll();
		for (MouseListener listener : this.getMouseListeners() )
			this.removeMouseListener(listener);
	}
	
	public void fillCellPanel(String cell_string)
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
					horizontal_panel.add(new TextLabel(str) );
				else // Icon
					if (str.contains(":") ) // layered Icon
						horizontal_panel.add(new LayeredIconLabel(str) );
					else // normal Icon
						horizontal_panel.add(new SimpleIconLabel(str) );
				text = !text;
			}
			horizontal_panel.setOpaque(false);
			this.add(horizontal_panel);
		}
	}
	
	private class TextLabel extends JLabel
	{
		//auto-generated serialVersionUID
		private static final long serialVersionUID = 162996911984988275L;

		public TextLabel(String str)
		{
			super(str);
			this.setFont(MainGui.font);
			this.setForeground(ColorSettings.currentColorSetting.text);
			MainGui.labelsText.add(this);
		}
	}
	
	private class SimpleIconLabel extends JLabel
	{
		//auto-generated serialVersionUID
		private static final long serialVersionUID = -8750448441950807834L;
		
		private String iconImageName;

		public SimpleIconLabel(String str)
		{
			iconImageName = Abbreviations.getAbbreviation(str); 
			String file_string = "Images\\" + iconImageName + ".png";
			ImageIcon icon;
			if (new File(file_string).exists() )
				icon = new ImageIcon(file_string);
			else
			{
				icon = new ImageIcon("Images\\Destroyed-icon.png");
				if (PopupAlerts.creatMissingImagesMessage)
				{
					String new_message = iconImageName + ".png" + (str.equals(iconImageName) ? "" : " (" + str + ")");
					if (!PopupAlerts.missingImagesMessage.contains(new_message) )
						PopupAlerts.missingImagesMessage += "\n" + new_message;
				}
			}
			
			this.setIcon(new ImageIcon(icon.getImage().getScaledInstance(MainGui.ImageSize, MainGui.ImageSize, Image.SCALE_DEFAULT) ) );
		}
	}
	
	private class LayeredIconLabel extends JLabel
	{
		//auto-generated serialVersionUID
		private static final long serialVersionUID = 5976606087766686091L;
		
		private String iconBackgroundName;
		private String iconForegroundName;

		public LayeredIconLabel(String str)
		{
			// Layered Images
			String[] images = str.split(":");
			if (images.length != 4) throw new RuntimeException("Error while parsing layered images: " + str + "! There have to be 2 images and 2 poition tags (t/b/c and l/r/c)");
			try
			{
				iconBackgroundName = Abbreviations.getAbbreviation(images[0] );
				iconForegroundName = Abbreviations.getAbbreviation(images[1] );
				
				// preparing Files
				File file_bg = new File("Images\\" + iconBackgroundName + ".png");
				File file_fg = new File("Images\\" + iconForegroundName + ".png");
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
						String new_message = iconBackgroundName + ".png" + (images[0].equals(iconBackgroundName) ? "" : " (" + images[0] + ")");
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
						String new_message = iconForegroundName + ".png" + (images[1].equals(iconForegroundName) ? "" : " (" + images[1] + ")");
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
				
				
				this.setIcon(new ImageIcon(scaled) );
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
