package logic;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edit.EditIconLabel;
import edit.EditPanel;
import edit.EditTextField;
import gui.Abbreviations;
import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;
import gui.PopupAlerts;

public class Cell extends JPanel
{
	//auto-generated serialVersionUID
	private static final long serialVersionUID = 1L;
	
	private Row row;
	private int col;
	private boolean topBorder, leftBorder;
	private String contentString;
	private String actionsString;
	private ArrayList<CellLabel> cellLabels;
	
	public Cell(Row row, String cell_str, int col)
	{
		this.row = row;
		this.col = col;
		this.topBorder = (row.getRowIndex() == 0);
		this.leftBorder = (col == 0);
		this.setOpaque(false);
		cellLabels = new ArrayList<CellLabel>();
		
		updateCell(cell_str);
	}
	
	public void updateCell(String cell_str)
	{
		clearContentAndListeners();
		
		if (cell_str.contains(">>") )
		{
			String[] temp = cell_str.split(">>", 2);
			contentString = temp[0];
			actionsString = temp[1].replaceAll("write_to_clipboard", "text_to_clipboard"); // legacy
		}
		else
		{
			contentString = cell_str;
			actionsString = "";
		}

		this.addMouseListener(MouseAdapters.editCellAdapter);
		if (!actionsString.isEmpty() )
			for (String action : actionsString.split(Pattern.quote("#") ) )
				this.addMouseListener(MouseAdapters.getCellActionAdapter(action) );
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS) );
		fillCellPanel();
		setDefaultBorder();
	}
	
	public String getInfo()
	{
		return "Section " + row.getSection().getTitle() + " at [" + row.getRowIndex() + ", " + col + "]";
	}
	
	public ArrayList<CellLabel> getCellLabels() { return cellLabels; }
	public Section getSection() { return row.getSection(); }
	public int getCol() { return col; }
	public void increaseCol() { col ++; }
	public void decreaseCol() { col --; }
	public int getRow() { return row.getRowIndex(); }
	public String getCellString() { return contentString + (actionsString.isEmpty() ? "" : ">>" + actionsString); }
	public String getContentString() { return contentString; }
	public String getActionString() { return actionsString; }
	
	public void setDefaultBorder()
	{
		this.setBorder(GuiHelper.getDefaultBorder(topBorder, leftBorder) );
	}
	
	public void setSelectedBorder()
	{
		this.setBorder(GuiHelper.getSelectedBorder() );
	}
	
	private void clearContentAndListeners()
	{
		this.removeAll();
		cellLabels.clear();
		for (MouseListener listener : this.getMouseListeners() )
			this.removeMouseListener(listener);
	}
	
	private void fillCellPanel()
	{
		for (String row : contentString.replace("->", "⇨").split(Pattern.quote("\\n") ) )
		{
			JPanel horizontal_panel = new JPanel();
			horizontal_panel.setLayout(new BoxLayout(horizontal_panel, BoxLayout.X_AXIS) );
			horizontal_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
			boolean text = true;
			CellLabel cell_label;
			for (String str : row.split("#") )
			{
				if (text)
					cell_label = new TextLabel(str, cellLabels.size() );
				else // Icon
					cell_label = new IconLabel(str, cellLabels.size() );
				horizontal_panel.add(cell_label);
				cellLabels.add(cell_label);
				text = !text;
			}
			horizontal_panel.setOpaque(false);
			this.add(horizontal_panel);
		}
	}
	
	public abstract class CellLabel extends JLabel
	{
		// auto-generated serialVersionUID
		private static final long serialVersionUID = 5537576761542517191L;
		
		private int index;

		public CellLabel(String str, int index)
		{
			super(str);
			this.index = index;
		}
		
		public CellLabel(int index)
		{
			this.index = index;
		}
		
		public abstract EditPanel getEditPanel();
		
		public int getIndex() { return index; }
	}
	
	private class TextLabel extends CellLabel
	{
		// auto-generated serialVersionUID
		private static final long serialVersionUID = 162996911984988275L;
		
		public TextLabel(String str, int index)
		{
			super(str, index);
			this.setFont(MainGui.font);
			this.setForeground(ColorSettings.getTextColor() );
			MainGui.labelsText.add(this);
		}
		
		public EditPanel getEditPanel()
		{
			return new EditTextField(this.getText() );
		}
	}
	
	private class IconLabel extends CellLabel
	{
		// auto-generated serialVersionUID
		private static final long serialVersionUID = 5976606087766686091L;
		
		private String mainImageAbbr = "";
		private String layeredImageAbbr = "";
		private String horizontalAlignment = "";
		private String verticalAlignment = "";
		
		private Icon icon;

		public IconLabel(String str, int index)
		{
			super(index);
			if (str.contains(":") )
			{
				// Layered Images
				String[] images = str.split(":");
				if (images.length != 4) throw new RuntimeException("Error while parsing layered images: " + str + "! There have to be 2 images and 2 poition tags (t/b/c and l/r/c)");
				
				mainImageAbbr       = images[0];
				layeredImageAbbr    = images[1];
				horizontalAlignment = images[2];
				verticalAlignment   = images[3];
				
				String main_image_name    = Abbreviations.getNameFromAbbreviation(mainImageAbbr);
				String layered_image_name = Abbreviations.getNameFromAbbreviation(layeredImageAbbr);
				
				if ( PopupAlerts.creatMissingImagesMessage && ! new File("Images\\" + main_image_name + ".png").exists() )
				{
					String new_message = main_image_name + ".png" + (mainImageAbbr.equals(main_image_name) ? "" : " (" + mainImageAbbr + ")");
					if (!PopupAlerts.missingImagesMessage.contains(new_message) )
						PopupAlerts.missingImagesMessage += "\n" + new_message;
				}
				if ( PopupAlerts.creatMissingImagesMessage && ! new File("Images\\" + layered_image_name + ".png").exists() )
				{
					String new_message = layered_image_name + ".png" + (layeredImageAbbr.equals(layered_image_name) ? "" : " (" + layeredImageAbbr + ")");
					if (!PopupAlerts.missingImagesMessage.contains(new_message) )
						PopupAlerts.missingImagesMessage += "\n" + new_message;
				}
				
				icon = GuiHelper.getScaledLayeredImage(mainImageAbbr, layeredImageAbbr, horizontalAlignment, verticalAlignment);
				this.setIcon(icon);
			}
			else
			{
				mainImageAbbr = str;
				String main_image_name = Abbreviations.getNameFromAbbreviation(mainImageAbbr);
				
				if ( ! new File("Images\\" + main_image_name + ".png").exists() )
				{
					if (PopupAlerts.creatMissingImagesMessage)
					{
						String new_message = main_image_name + ".png" + (mainImageAbbr.equals(main_image_name) ? "" : " (" + mainImageAbbr + ")");
						if (!PopupAlerts.missingImagesMessage.contains(new_message) )
							PopupAlerts.missingImagesMessage += "\n" + new_message;
					}
				}
				
				icon = GuiHelper.getScaledImageIcon(mainImageAbbr);
				this.setIcon(icon);
			}
		}
		
		public EditPanel getEditPanel()
		{
			return new EditIconLabel(icon, mainImageAbbr, layeredImageAbbr, verticalAlignment, horizontalAlignment);
		}
		
		public Icon getIcon() { return icon; }
	}
}
