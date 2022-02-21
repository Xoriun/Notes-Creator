package logic;

import java.awt.Component;
import java.awt.event.MouseListener;
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

public class Cell extends JPanel
{
	//auto-generated serialVersionUID
	private static final long serialVersionUID = 1L;
	
	private Row row;
	private int col;
	private boolean topBorder, leftBorder;
	private String contentString;
	private String[][] actionsArray;
	private ArrayList<CellLabel> cellLabels;
	
	public Cell(Row row, String cell_str, int col)
	{
		this.row = row;
		this.col = col;
		this.topBorder = (row.getRowIndex() == 0);
		this.leftBorder = (col == 0);
		this.setOpaque(false);
		cellLabels = new ArrayList<CellLabel>();
		this.addMouseListener(MouseAdapters.actionsAdapter);
		
		updateCell(cell_str);
	}
	
	public void updateCell(String cell_str)
	{
		clearContentAndListeners();
		
		if (cell_str.contains(">>") )
		{
			String[] temp = cell_str.split(">>", 2);
			contentString = temp[0];
			String[] actions = temp[1].replaceAll("write_to_clipboard", "text_to_clipboard").split(Pattern.quote("#") );
			actionsArray = new String[actions.length][];
			for (int i = 0; i < actions.length; i ++)
				actionsArray[i] = actions[i].split(":");
		}
		else
		{
			contentString = cell_str;
			actionsArray = new String[0][];
		}

		this.addMouseListener(MouseAdapters.editCellAdapter);
		this.addMouseListener(MouseAdapters.actionsAdapter);
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
	public String getCellString() { return contentString + (actionsArray.length == 0 ? "" : ">>" + getActionString() ); }
	public String getContentString() { return contentString; }
	public String getActionString()
	{
		String res = "";
		for (String[] action : actionsArray)
			res += "#" + action[0] + ":" + action[1];
		return res.substring(1);
	}
	public String[][] getActionsArray() { return actionsArray; }
	
	public void setDefaultBorder()
	{
		this.setBorder(GuiHelper.getDefaultBorder(topBorder, leftBorder) );
	}
	
	public void relaodImages()
	{
		for (CellLabel icon_label : cellLabels) icon_label.reloadImage();
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
		public abstract void reloadImage();
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
		
		public void reloadImage() {}
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
			}
			else
			{
				mainImageAbbr = str;
				layeredImageAbbr = horizontalAlignment = verticalAlignment = "";
			}
			loadImage();
		}
		
		public void reloadImage()
		{
			loadImage();
		}
		
		private void loadImage()
		{
			if ( !layeredImageAbbr.isEmpty() )
			{
				String main_image_name    = Abbreviations.getNameFromAbbreviation(mainImageAbbr);
				String layered_image_name = Abbreviations.getNameFromAbbreviation(layeredImageAbbr);
				icon = GuiHelper.getScaledLayeredImage(main_image_name, layered_image_name, horizontalAlignment, verticalAlignment);
				this.setIcon(icon);
			}
			else
			{
				String main_image_name = Abbreviations.getNameFromAbbreviation(mainImageAbbr);
				icon = GuiHelper.getScaledImageIcon(main_image_name);
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
