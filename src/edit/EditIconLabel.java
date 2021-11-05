package edit;

import javax.swing.Icon;
import javax.swing.JLabel;

import gui.ColorSettings;
import gui.GuiHelper;

public class EditIconLabel extends JLabel implements EditPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = -8286203010041183327L;
	
	private Icon icon;
	private String mainImageAbbr = "";
	private String layeredImageAbbr = "";
	private String layeredHorizontalAlignment = "c";
	private String layeredVerticalAlignment = "c";
	
	private EditIconLabel()
	{
		super();
		this.mainImageAbbr = "Transport_belt";
		this.icon = GuiHelper.getScaledImageIconFromAbbreviation(mainImageAbbr);
		this.setIcon(icon);
		this.setAlignmentX(0f);
		this.setBackground(ColorSettings.getBackgroundColor() );
		this.setOpaque(true);
	}
	
	public EditIconLabel(Icon new_icon, String main_image_abbr, String layered_image_abbr, String horizontal_alignment, String vertical_alignment)
	{
		super();
		this.icon = new_icon;
		this.mainImageAbbr = main_image_abbr;
		this.layeredImageAbbr = layered_image_abbr;
		this.layeredHorizontalAlignment = horizontal_alignment;
		this.layeredVerticalAlignment = vertical_alignment;
		this.setIcon(icon);
		this.setAlignmentX(0f);
		this.setBackground(ColorSettings.getBackgroundColor() );
		this.setOpaque(true);
	}
	
	public void updateIcon(String main_image_abbr, String layered_image_abbr, String horizontal_alignment, String vertical_alignment)
	{
		this.mainImageAbbr = main_image_abbr;
		this.layeredImageAbbr = layered_image_abbr;
		this.layeredHorizontalAlignment = horizontal_alignment;
		this.layeredVerticalAlignment = vertical_alignment;
		this.icon = GuiHelper.getScaledLayeredImageFromAbbreviations(mainImageAbbr, layeredImageAbbr, layeredHorizontalAlignment, layeredVerticalAlignment);
		this.setIcon(icon);
	}
	
	public void updateIcon(String main_image_abbr)
	{
		this.mainImageAbbr = main_image_abbr;
		this.layeredImageAbbr = this.layeredHorizontalAlignment = this.layeredVerticalAlignment = "";
		this.icon = GuiHelper.getScaledImageIconFromAbbreviation(mainImageAbbr);
		this.setIcon(icon);
	}
	
	public String getString()
	{
		return "#" + mainImageAbbr + (layeredImageAbbr.equals("") ? "" : ":" + layeredImageAbbr + ":" + layeredHorizontalAlignment + ":" + layeredVerticalAlignment) + "#";
	}
	
	public void setSelectedBorder()
	{
		this.setBorder(GuiHelper.getSelectedBorder() );
	}
	
	public void setDefaultBorder()
	{
		this.setBorder(GuiHelper.getEmptyBorder() );
	}
	
	public static EditIconLabel getEmptyEditIconLabel()
	{
		return new EditIconLabel();
	}
	
	public void updateColorSettings()
	{
		this.setBackground(ColorSettings.getBackgroundColor() );
	}
	
	public String getMainImageAbbr() { return mainImageAbbr; }
	public String getLayeredImageAbbr() { return layeredImageAbbr; }
	public String getLayeredVerticalAlignment() { return layeredVerticalAlignment; }
	public String getLayeredHorizontalAlignment() { return layeredHorizontalAlignment; }
}
