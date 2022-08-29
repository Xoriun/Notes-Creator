package logic;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edit.EditIconLabel;
import edit.EditPanel;
import edit.EditTextField;
import gui.GuiHelper;
import gui.MainGui;
import settings.AbbreviationSettings;
import settings.ColorSettings;

public class Cell extends JPanel
{
	//auto-generated serialVersionUID
	private static final long serialVersionUID = 1L;
	
	private Row row;
	private int col;
	private boolean topBorder, leftBorder;
	private String contentString = "";
	private String[][] actionsArray = new String[0][0];
	private ArrayList<CellLabel> cellLabels;
	
	Cell(Row row, int col, String cell_str)
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
	
	Cell(Row row, int col, Element cell)
	{
		this.row = row;
		this.col = col;
		this.topBorder = (row.getRowIndex() == 0);
		this.leftBorder = (col == 0);
		this.setOpaque(false);
		cellLabels = new ArrayList<CellLabel>();
		this.addMouseListener(MouseAdapters.actionsAdapter);
		
		NodeList contentNodes = cell.getElementsByTagName("content");
		if (contentNodes.getLength() == 1)
			contentString = contentNodes.item(0).getTextContent();
		
		NodeList actionNodes = cell.getElementsByTagName("action");
		int actions_length = actionNodes.getLength();
		if (actions_length > 0)
		{
			actionsArray = new String[actions_length][];
			for (int action_ind = 0; action_ind < actions_length; action_ind ++)
				actionsArray[action_ind] = new String[] {
						( (Element) actionNodes.item(action_ind) ).getAttribute("command"),
						( (Element) actionNodes.item(action_ind) ).getAttribute("parameter") };
		}

		this.addMouseListener(MouseAdapters.editCellAdapter);
		this.addMouseListener(MouseAdapters.actionsAdapter);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS) );
		fillCellPanel();
		setDefaultBorder();
		
		FileOperations.unsavedChanges = true;
	}
	
	public void updateCell(String cell_str)
	{
		if (cell_str.equals(getCellString() ) ) return;
		
		clearContentAndListeners();
		
		String[] temp = cell_str.split(">>", 2);
		contentString = temp[0];
		if ( temp.length == 1 || temp[1].isEmpty() )
			actionsArray = new String[0][];
		else
		{
			String[] actions = temp[1].replaceAll("write_to_clipboard", "text_to_clipboard").split(Pattern.quote("#") );
			actionsArray = new String[actions.length][];
			for (int i = 0; i < actions.length; i ++)
				actionsArray[i] = actions[i].split(":");
		}

		this.addMouseListener(MouseAdapters.editCellAdapter);
		this.addMouseListener(MouseAdapters.actionsAdapter);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS) );
		fillCellPanel();
		setDefaultBorder();
		
		FileOperations.unsavedChanges = true;
	}
	
	public String getInfo()
	{
		return "Section " + row.getSection().getTitle() + " at [" + row.getRowIndex() + ", " + col + "]";
	}
	
	public ArrayList<CellLabel> getCellLabels() { return cellLabels; }
	public Section getSection() { return row.getSection(); }
	public int getCol() { return col; }
	void increaseCol() { col ++; }
	void decreaseCol() { col --; }
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
	
	void relaodImages()
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
	
	/**
	 * Returns an array of its neighbours in all 4 cardinal directions, ordered as top, bottom, left right.
	 * If this lies at an edge, null is returned for the non-existing neighbours.
	 * @return
	 */
	public Cell[] getNeighbours()
	{
		System.out.println("index: " + row.getRowIndex() + ", rows: " + row.getSection().getRows().size() );
		Cell[] res = new Cell[4];
		res[0] = row.getRowIndex() == 0                      			               ? null : row.getSection().getRows().get(row.getRowIndex() - 1).getCells().get(col);
		res[1] = row.getRowIndex() == row.getSection().numberOfContentRows() - 1 ? null : row.getSection().getRows().get(row.getRowIndex() + 1).getCells().get(col);
		res[2] = col               == 0                         ? null : row.getCells().get(col - 1);
		res[3] = col               == row.getCells().size() - 1 ? null : row.getCells().get(col + 1);
		return res;
	}
	
	public void updateLightingSettings()
	{
		
		this.setBorder(new MatteBorder( ((MatteBorder) this.getBorder() ).getBorderInsets(), ColorSettings.getBorderColor() ) );
		
		for (CellLabel label : cellLabels)
			label.updateLightingSettings();
	}
	
	Element getXMLElement(Document doc)
	{
		Element result = doc.createElement("cell");
		
		if ( !contentString.isEmpty() )
		{
			Element contentElement = doc.createElement("content");
			contentElement.setTextContent(contentString);
			result.appendChild(contentElement);
		}
		
		if (actionsArray.length > 0)
		{
			for (String[] action : actionsArray)
			{
				Element actionElement = doc.createElement("action");
				
				actionElement.setAttribute("command", action[0] );
				actionElement.setAttribute("parameter", actionParameterContainsSensitiveInformation(action[1] ) ? "" : action[1] );
				
				result.appendChild(actionElement);
			}
		}
		
		return result;
	}
	
	private static boolean actionParameterContainsSensitiveInformation(String actionCommand)
	{
		switch (actionCommand)
		{
			case "file_to_clipboard": return true;
			default: return false;
		}
	}
	
	public abstract class CellLabel extends JLabel
	{
		// auto-generated serialVersionUID
		private static final long serialVersionUID = 5537576761542517191L;
		
		private int index;

		private CellLabel(String str, int index)
		{
			super(str);
			this.index = index;
			this.setOpaque(false);
		}
		
		private CellLabel(int index)
		{
			this.index = index;
		}
		
		public abstract EditPanel getEditPanel();
		
		public int getIndex() { return index; }
		public abstract void reloadImage();
		public abstract void updateLightingSettings();
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
		}

		@Override
		public EditPanel getEditPanel()
		{
			return new EditTextField(this.getText() );
		}

		@Override
		public void updateLightingSettings()
		{
			this.setForeground(ColorSettings.getTextColor() );
		}

		@Override
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

		@Override
		public void reloadImage()
		{
			loadImage();
		}
		
		private void loadImage()
		{
			if ( !layeredImageAbbr.isEmpty() )
			{
				String main_image_name    = AbbreviationSettings.getNameFromAbbreviation(mainImageAbbr);
				String layered_image_name = AbbreviationSettings.getNameFromAbbreviation(layeredImageAbbr);
				icon = GuiHelper.getScaledLayeredImage(main_image_name, layered_image_name, horizontalAlignment, verticalAlignment);
				this.setIcon(icon);
			}
			else
			{
				String main_image_name = AbbreviationSettings.getNameFromAbbreviation(mainImageAbbr);
				icon = GuiHelper.getScaledImageIcon(main_image_name);
				this.setIcon(icon);
			}
		}

		@Override
		public EditPanel getEditPanel()
		{
			return new EditIconLabel(icon, mainImageAbbr, layeredImageAbbr, horizontalAlignment, verticalAlignment);
		}

		@Override
		public void updateLightingSettings()
		{
			this.setForeground(ColorSettings.getTextColor() );
		}

		@Override
		public Icon getIcon() { return icon; }
	}
}
