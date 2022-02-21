package gui;

import java.awt.Color;
import java.awt.Component;
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

import logic.FileOperations;

public class GuiHelper
{
	private final static int ImageSize = 30;
	
	public final static BufferedImage	missingImageBuffered			= loadMissingImageBufferedImage();
	public final static BufferedImage	layeredDotBuffered				= loadLayeredDotImage();
	public final static ImageIcon			scaledTodoImageIcon				= getScaledTodoImageIcon();
	public final static ImageIcon			scaledLinebreakImageIcon	= getScaledLinebreakImageIcon();
	
	public final static int LEFT   = 0;
	public final static int CENTER = 1;
	public final static int RIGHT  = 3;
	
	/**
	 * Returns a TitledBorder with title as title and EtchedBorder as the underlying
	 * Border. The title's font and color are set according to the current settings.
	 * 
	 * @param title
	 *          The title of the border.
	 * @return The TitledBorder object
	 */
	public static TitledBorder getTitledBorderWithCorrectTextColor(String title)
	{
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED), title);
		border.setTitleFont(MainGui.titleFont);
		border.setTitleColor(ColorSettings.getTextColor());
		return border;
	}
	
	/**
	 * Returns a MatteBorder with the provided width at the specified locations and color as the border.
	 * 
	 * @param width How much space should be added by the border.
	 * @param bottom Whether a border should be drawn at the bottom.
	 * @param right Whether a border should be drawn at the right.
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getMatteBorder(int width, boolean bottom, boolean right)
	{
		return BorderFactory.createMatteBorder(0, 0, bottom ? width : 0, right ? width : 0, ColorSettings.getBorderColor() );
	}
	
	/**
	 * Returns a MatteBorder with the provided width and color as the background.
	 * 
	 * @param width How much space should be added by the border.
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getSpacingBorder(int width)
	{
		return BorderFactory.createMatteBorder(width, width, width, width, ColorSettings.getBackgroundColor());
	}
	
	/**
	 * Returns a MatteBorder with width 2 and color as the text.
	 * 
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getDialogBorder()
	{
		return BorderFactory.createMatteBorder(2, 2, 2, 2, ColorSettings.getTextColor());
	}
	
	/**
	 * Returns a red MatteBorder implying that the cell containing this border is
	 * currently selected.
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
		return BorderFactory.createMatteBorder(1, 1, 1, 1, ColorSettings.getBorderColor());
	}
	
	/**
	 * Returns a MatteBorder with the current border color.
	 * 
	 * @param top
	 *          A boolean indicating if the top part of the border should be
	 *          painted.
	 * @param left
	 *          A boolean indicating if the left part of the border should be
	 *          painted.
	 * @return The MatteBorder object.
	 */
	public static MatteBorder getDefaultBorder(boolean top, boolean left)
	{
		return BorderFactory.createMatteBorder(top ? 1 : 0, left ? 1 : 0, 1, 1, ColorSettings.getBorderColor());
	}
	
	/**
	 * Returns an ImageIcon (given by the abbreviation image_abbr) of size
	 * GuiHelper.ImageSize by GuiHelper.ImageSize pixels.
	 * 
	 * @param image_abbr
	 *          Abbreviation (or name, including any path-structure within the
	 *          Images folder) of the image.
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledImageIconFromAbbreviation(String image_abbr)
	{
		return getScaledImageIcon(Abbreviations.getNameFromAbbreviation(image_abbr) );
	}
	
	/**
	 * Returns an ImageIcon (given by the name image_name) of size
	 * GuiHelper.ImageSize by GuiHelper.ImageSize pixels.
	 * 
	 * @param image_name
	 *          Name (including any path-structure within the Images folder) of the
	 *          image.
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledImageIcon(String image_name)
	{
		File file = new File(FileOperations.imagesDirectory + "\\" + image_name + ".png");
		try
		{
			final BufferedImage buffered_image;
			if (file.exists())
				buffered_image = ImageIO.read(file);
			else
			{
				buffered_image = missingImageBuffered;
				PopupAlerts.addMessageForMissingImage(image_name);
			}
			return new ImageIcon(new ImageIcon(buffered_image).getImage().getScaledInstance(ImageSize, ImageSize, Image.SCALE_DEFAULT));
		} catch (IOException e)
		{
			throw new RuntimeException("Error while laoding '" + image_name + ".png'");
		}
	}
	
	/**
	 * Returns an ImageIcon (given by the abbreviation main_image_abbr) as the main
	 * icon (GuiHelper.ImageSize by GuiHelper.ImageSize pixels) and a smaller icon
	 * (given by the abbreviation layered_image_abbr) (3/5 of the size of the main
	 * image) in front of it. The smaller image can be in 9 different locations,
	 * determined by horizontal_alignment and vertical_alignment.
	 * 
	 * @param main_image_abbr
	 *          Abbreviation (or name, including any path-structure within the
	 *          Images folder) of the main image.
	 * @param layered_image_abbr
	 *          Abbreviation (or name, including any path-structure within the
	 *          Images folder) of the layered image.
	 * @param horizontal_alignment
	 *          Horizontal alignment of the smaller image. Possible values: l, c, r
	 * @param vertical_alignment
	 *          Vertical alignment of the smaller image. Possible values: t, c, b
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledLayeredImageFromAbbreviations(String main_image_abbr, String layered_image_abbr, String horizontal_alignment, String vertical_alignment)
	{
		return getScaledLayeredImage(Abbreviations.getNameFromAbbreviation(main_image_abbr), Abbreviations.getNameFromAbbreviation(layered_image_abbr), horizontal_alignment, vertical_alignment);
	}
	
	/**
	 * Returns an ImageIcon (given by the name main_image_name) as the main icon
	 * (GuiHelper.ImageSize by GuiHelper.ImageSize pixels) and a smaller icon (given
	 * by the name layered_image_name) (3/5 of the size of the main image) in front
	 * of it. The smaller image can be in 9 different locations, determined by
	 * horizontal_alignment and vertical_alignment.
	 * 
	 * @param main_image_name
	 *          Name (including any path-structure within the Images folder) of the
	 *          main image.
	 * @param layered_image_name
	 *          Name (including any path-structure within the Images folder) of the
	 *          layered image.
	 * @param horizontal_alignment
	 *          Horizontal alignment of the smaller image. Possible values: l, c, r
	 * @param vertical_alignment
	 *          Vertical alignment of the smaller image. Possible values: t, c, b
	 * @return The ImageIcon object.
	 */
	public static ImageIcon getScaledLayeredImage(String main_image_name, String layered_image_name, String horizontal_alignment, String vertical_alignment)
	{
		try
		{
			// preparing Files
			File file_bg = new File(FileOperations.imagesDirectory + "\\" + main_image_name + ".png");
			File file_fg = new File(FileOperations.imagesDirectory + "\\" + layered_image_name + ".png");
			
			// preparing BufferedImages
			final BufferedImage main_image;
			final BufferedImage layered_image;
			
			if (file_bg.exists())
				main_image = ImageIO.read(file_bg);
			else
			{
				main_image = missingImageBuffered;
				PopupAlerts.addMessageForMissingImage(main_image_name);
			}
			
			if (file_fg.exists())
				layered_image = ImageIO.read(file_fg);
			else
			{
				layered_image = missingImageBuffered;
				PopupAlerts.addMessageForMissingImage(layered_image_name);
			}
			
			final BufferedImage scaled = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_INT_ARGB); // empty BufferedImage to draw on
			Graphics g = scaled.getGraphics();
			
			// drawing images
			int dot_image_size = 2 * ImageSize / 3;
			int small_image_size = 3 * ImageSize / 5;
			int x_dot = horizontal_alignment.equals("t") ? 0 : (ImageSize - dot_image_size) / (horizontal_alignment.equals("c") ? 2 : 1);
			int y_dot = vertical_alignment.equals("l") ? 0 : (ImageSize - dot_image_size) / (vertical_alignment.equals("c") ? 2 : 1);
			int x = horizontal_alignment.equals("t") ? 0 : (ImageSize - small_image_size) / (horizontal_alignment.equals("c") ? 2 : 1);
			int y = vertical_alignment.equals("l") ? 0 : (ImageSize - small_image_size) / (vertical_alignment.equals("c") ? 2 : 1);
			g.drawImage(main_image, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
			g.drawImage(layeredDotBuffered, x_dot, y_dot, dot_image_size, dot_image_size, null);
			g.drawImage(layered_image, x, y, small_image_size, small_image_size, null);
			
			return new ImageIcon(scaled);
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static void resizeAndCenterRelativeToMainWindow(Container container)
	{
		resizeAndCenterRelativeToMainWindow(container, 1000, 0);
	}
	
	public static void resizeAndCenterRelativeToMainWindow(Container container, int max_height_rel_to_window, int min_height)
	{
		Dimension dim_container = container.getSize();
		Dimension dim_window = MainGui.window.getSize();
		
		if (dim_container.height > dim_window.height + max_height_rel_to_window)
		{
			dim_container.height = dim_window.height + max_height_rel_to_window;
			container.setPreferredSize(dim_container);
		}
		if (dim_container.height < min_height)
		{
			dim_container.height = min_height;
			container.setPreferredSize(dim_container);
		}
		
		int location_x = MainGui.window.getLocation().x + (dim_window.width - dim_container.width) / 2;
		int location_y = MainGui.window.getLocation().y + (dim_window.height - dim_container.height) / 2;
		if (location_y < 0)
			location_y = 0;
		
		container.setLocation(location_x, location_y);
	}
	
	public static JLabel getLeftAlignedNonOpaqueJLabelWithCurrentTextColor(String text)
	{
		JLabel label = new JLabel(text, SwingConstants.LEFT);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setForeground(ColorSettings.currentColorSetting.text);
		label.setOpaque(false);
		return label;
	}
	
	public static JLabel getAlignedNonOpaqueJLabelWithCurrentColors(String text, int alignment)
	{
		JLabel label = new JLabel(text, getSwingAlignment(alignment) );
		label.setAlignmentX(getComponentAlignment(alignment) );
		label.setForeground(ColorSettings.currentColorSetting.text);
		label.setBackground(ColorSettings.getBackgroundColor() );
		label.setOpaque(false);
		return label;
	}
	
	private static int getSwingAlignment(int alignement)
	{
		switch (alignement)
		{
			case LEFT:
				return SwingConstants.LEFT;
			case CENTER:
				return SwingConstants.CENTER;
			case RIGHT:
				return SwingConstants.RIGHT;
			default:
				return -1;
		}
	}
	
	private static float getComponentAlignment(int alignement)
	{
		switch (alignement)
		{
			case LEFT:
				return Component.LEFT_ALIGNMENT;
			case CENTER:
				return Component.CENTER_ALIGNMENT;
			case RIGHT:
				return Component.RIGHT_ALIGNMENT;
			default:
				return -1;
		}
	}
	
	private static BufferedImage loadMissingImageBufferedImage()
	{
		try
		{
			return ImageIO.read(GuiHelper.class.getClassLoader().getResourceAsStream("Missing_image.png"));
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
			return ImageIO.read(GuiHelper.class.getClassLoader().getResource("Layer_dot.png"));
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Layer_dot.png'", false);
			throw new RuntimeException("Error while loading 'Layer_dot.png'");
		} catch (IllegalArgumentException e)
		{
			MainGui.displayErrorAndExit("Error while loading 'Layer_dot.png', file doesn't exist!", true);
			throw new RuntimeException("Error while loading 'Layer_dot.png', file doesn't exist!");
		}
	}
	
	private static ImageIcon getScaledTodoImageIcon()
	{
		try
		{
			return new ImageIcon(ImageIO.read(GuiHelper.class.getClassLoader().getResource("Todo.png")).getScaledInstance(ImageSize, ImageSize, Image.SCALE_DEFAULT));
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
			return new ImageIcon(ImageIO.read(GuiHelper.class.getClassLoader().getResource("Linebreak.png")).getScaledInstance(ImageSize, ImageSize, Image.SCALE_DEFAULT));
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
