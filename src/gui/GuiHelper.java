package gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class GuiHelper
{
	private final static int ImageSize = 30;
	
	public final static BufferedImage missingImageBuffered = loadMissingImageBufferedImage();
	public final static BufferedImage layeredDotBuffered = loadLayeredDotImage();
	public final static ImageIcon scaledTodoImageIcon = getScaledTodoImageIcon();
	public final static ImageIcon scaledLinebreakImageIcon = getScaledLinebreakImageIcon();
	
	/**
	 * Returns a TitledBorder with title as title and EtchedBorder as the underlying Border.
	 * The title's font and color are set according to the current settings.
	 * 
	 * @param title The title of the border.
	 * @return The TitledBorder object
	 */
	public static TitledBorder getTitledBorderWithCorrectTextColor(String title)
	{
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), title);
		border.setTitleFont(MainGui.titleFont);
		border.setTitleColor(ColorSettings.getTextColor() );
		return border;
	}
	
	/**
	 * Returns a MatteBorder with width 2 and color as the text.
	 * 
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getDialogBorder()
	{
		return BorderFactory.createMatteBorder(2, 2, 2, 2, ColorSettings.getTextColor() );
	}
	
	/**
	 * Returns a red MatteBorder implying that the cell containing this border is currently selected.
	 * 
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getSelectedBorder()
	{
		return BorderFactory.createMatteBorder(1, 1, 1, 1, Color.RED);
	}
	
	/**
	 * Returns an empty Border.
	 * 
	 * @return The Border object.
	 */
	public static Border getEmptyBorder()
	{
		return BorderFactory.createEmptyBorder();
	}
	
	/**
	 * Returns a MatteBorder with the current border color.
	 * 
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getDefaultBorder()
	{
		return BorderFactory.createMatteBorder(1, 1, 1, 1, ColorSettings.getBorderColor() );
	}
	
	/**
	 * Returns a MatteBorder with the current border color.
	 * 
	 * @param top A boolean indicating if the top part of the border should be painted.
	 * @param left A boolean indicating if the left part of the border should be painted.
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getDefaultBorder(boolean top, boolean left)
	{
		return BorderFactory.createMatteBorder(top ? 1 : 0, left ? 1 : 0, 1, 1, ColorSettings.getBorderColor() );
	}
	
	/**
	 * Returns an ImageIcon (given by the abbreviation image_abbr) of size GuiHelper.ImageSize by GuiHelper.ImageSize pixels.
	 * 
	 * @param image_abbr Abbreviation (or name, including any path-structure within the Images folder) of the image.
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledImageIconFromAbbreviation(String image_abbr)
	{
		return getScaledImageIcon(image_abbr);
	}
	
	/**
	 * Returns an ImageIcon (given by the name image_name) of size GuiHelper.ImageSize by GuiHelper.ImageSize pixels.
	 * 
	 * @param iamge_name Name (including any path-structure within the Images folder) of the image.
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledImageIcon(String iamge_name)
	{
		File file = new File("Images\\" + iamge_name + ".png");
		try
		{
			ImageIcon icon = new ImageIcon(file.exists() ? ImageIO.read(file) : missingImageBuffered);
		  return new ImageIcon(icon.getImage().getScaledInstance(ImageSize, ImageSize, Image.SCALE_DEFAULT) );
		} catch (IOException e)
		{
			throw new RuntimeException("Error while laoding '" + iamge_name + ".png'");
		}
	}
	
	/**
	 * Returns an ImageIcon (given by the abbreviation main_image_abbr) as the main icon (GuiHelper.ImageSize by GuiHelper.ImageSize pixels) and a smaller icon (given by the abbreviation layered_image_abbr) (3/5 of the size of the main image) in front of it.
	 * The smaller image can be in 9 different locations, determined by horizontal_alignment and vertical_alignment.
	 * 
	 * @param main_image_abbr Abbreviation (or name, including any path-structure within the Images folder) of the main image.
	 * @param layered_image_abbr Abbreviation (or name, including any path-structure within the Images folder) of the layered image.
	 * @param horizontal_alignment Horizontal alignment of the smaller image. Possible values: l, c, r
	 * @param vertical_alignment Vertical alignment of the smaller image. Possible values: t, c, b
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledLayeredImageFromAbbreviations(String main_image_abbr, String layered_image_abbr, String horizontal_alignment, String vertical_alignment)
	{
		return getScaledLayeredImage(Abbreviations.getNameFromAbbreviation(main_image_abbr), Abbreviations.getNameFromAbbreviation(layered_image_abbr), horizontal_alignment, vertical_alignment);
	}
	
	/**
	 * Returns an ImageIcon (given by the name main_image_name) as the main icon (GuiHelper.ImageSize by GuiHelper.ImageSize pixels) and a smaller icon (given by the name layered_image_name) (3/5 of the size of the main image) in front of it.
	 * The smaller image can be in 9 different locations, determined by horizontal_alignment and vertical_alignment.
	 * 
	 * @param main_image_name Name (including any path-structure within the Images folder) of the main image.
	 * @param layered_image_name Name (including any path-structure within the Images folder) of the layered image.
	 * @param horizontal_alignment Horizontal alignment of the smaller image. Possible values: l, c, r
	 * @param vertical_alignment Vertical alignment of the smaller image. Possible values: t, c, b
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledLayeredImage(String main_image_name, String layered_image_name, String horizontal_alignment, String vertical_alignment)
	{
		try
		{
			// preparing Files
			File file_bg = new File("Images\\" + main_image_name + ".png");
			File file_fg = new File("Images\\" + layered_image_name + ".png");
			
			// preparing BufferedImages
			final BufferedImage main_image;
			final BufferedImage layered_image;
			
			if (file_bg.exists() )
				main_image = ImageIO.read(file_bg);
			else
				main_image = missingImageBuffered;
			
			if (file_fg.exists() )
				layered_image = ImageIO.read(file_fg);
			else
				layered_image = missingImageBuffered;
			
			final BufferedImage scaled = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_INT_ARGB); // empty BufferedImage to draw on
			Graphics g = scaled.getGraphics();
			
			// drawing images
			int dot_image_size   = 2 * ImageSize / 3;
			int small_image_size = 3 * ImageSize / 5;
			int x_dot = horizontal_alignment.equals("t") ? 0 : (ImageSize - dot_image_size  ) / (horizontal_alignment.equals("c") ? 2 : 1);
			int y_dot = vertical_alignment  .equals("l") ? 0 : (ImageSize - dot_image_size  ) / (vertical_alignment  .equals("c") ? 2 : 1);
			int x 	  = horizontal_alignment.equals("t") ? 0 : (ImageSize - small_image_size) / (horizontal_alignment.equals("c") ? 2 : 1);
			int y 	  = vertical_alignment  .equals("l") ? 0 : (ImageSize - small_image_size) / (vertical_alignment  .equals("c") ? 2 : 1);
			g.drawImage(main_image,         0,     0,     scaled.getWidth(), scaled.getHeight(), null);
			g.drawImage(layeredDotBuffered, x_dot, y_dot, dot_image_size,    dot_image_size,     null);
			g.drawImage(layered_image,      x,     y,     small_image_size,  small_image_size,   null);
			
			return new ImageIcon(scaled);
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setLocationToCenter(Container container)
	{
		setLocationToCenter(container, 1000);
	}
	
	public static void setLocationToCenter(Container container, int max_height_rel_to_window)
	{
		Dimension dim_container  = container.getSize();
		Dimension dim_window = MainGui.window.getSize();
		
		if (dim_container.height > dim_window.height + max_height_rel_to_window)
		{
			dim_container.height = dim_window.height + max_height_rel_to_window;
			container.setPreferredSize(dim_container);
		}
		
		container.setLocation(MainGui.window.getLocation().x + (dim_window.width - dim_container.width)/2, MainGui.window.getLocation().y + (dim_window.height - dim_container.height)/2);
	}
	
	public static JLabel getLeftAlignedNonOpaqueJLabelWithCurrentTextColor(String text)
	{
		JLabel label = new JLabel(text, SwingConstants.LEFT);
		label.setForeground(ColorSettings.currentColorSetting.text);
		label.setOpaque(false);
		return label;
	}

	private static BufferedImage loadMissingImageBufferedImage()
	{
		try
		{
			return ImageIO.read(GuiHelper.class.getClassLoader().getResourceAsStream("Missing_image.png") );
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Missing_Image.png'", false);
			throw new RuntimeException("Error while loading 'Missing_Image.png'");
		} catch (IllegalArgumentException e)
		{
			MainGui.displayErrorAndExit("Fatal error while loading 'Missing_Image.png', file doesn't exist!", true);
			throw new RuntimeException("Fatal error while loading 'Missing_Image.png', file doesn't exist!");
		}
	}

	private static BufferedImage loadLayeredDotImage()
	{
		try
		{
			return ImageIO.read(GuiHelper.class.getClassLoader().getResource("Layer_dot.png") );
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Layere_dot.png'", false);
			throw new RuntimeException("Error while loading 'Layere_dot.png'");
		} catch (IllegalArgumentException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Layere_dot.png', file doesn't exist!", true);
			throw new RuntimeException("Error while loading 'Layere_dot.png', file doesn't exist!");
		}
	}

	private static ImageIcon getScaledTodoImageIcon()
	{
		try
		{
			return new ImageIcon(ImageIO.read(GuiHelper.class.getClassLoader().getResource("Todo.png") ).getScaledInstance(ImageSize, ImageSize, Image.SCALE_DEFAULT) );
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Todo.png'", false);
			throw new RuntimeException("Error while loading 'Todo.png'");
		} catch (IllegalArgumentException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Todo.png', file doesn't exist!", true);
			throw new RuntimeException("Error while loading 'Todo.png', file doesn't exist!");
		}
	}

	private static ImageIcon getScaledLinebreakImageIcon()
	{
		try
		{
			return new ImageIcon(ImageIO.read(GuiHelper.class.getClassLoader().getResource("Linebreak.png") ).getScaledInstance(ImageSize, ImageSize, Image.SCALE_DEFAULT) );
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Linebreak.png'", false);
			throw new RuntimeException("Error while loading 'Linebreak.png'");
		} catch (IllegalArgumentException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Linebreak.png', file doesn't exist!", true);
			throw new RuntimeException("Error while loading 'Linebreak.png', file doesn't exist!");
		}
	}
}
