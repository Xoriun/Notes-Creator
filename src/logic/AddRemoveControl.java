package logic;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;

public class AddRemoveControl extends JPanel
{
	// automatically generated serialVersionUID
	private static final long serialVersionUID = -110941854879740768L;
	private Row row;
	private int sectionIndex;
	private JLabel add, remove;
	
	private AddRemoveControl(Row row, int section_index, boolean only_add)
	{
		super(new GridLayout(only_add ? 1 : 2, 2) );
		
		this.row = row;
		this.sectionIndex = section_index;
		
		add = GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors(" + ", GuiHelper.CENTER);
		add.setForeground(MainGui.inEditMode ? ColorSettings.getTextColor() : ColorSettings.getBackgroundColor() );
		add.addMouseListener(row == null ? MouseAdapters.addSectionAdapter : MouseAdapters.addRowAdapter);
		
		this.add(new JLabel());
		this.add(add);
		
		if (! only_add)
		{
			remove = GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors(" - ", GuiHelper.CENTER);
			remove.setForeground(MainGui.inEditMode ? ColorSettings.getTextColor() : ColorSettings.getBackgroundColor() );
			remove.addMouseListener(row == null ? MouseAdapters.removeSectionAdapter : MouseAdapters.removeRowAdapter);
			
			this.add(remove);
			this.add(new JLabel());
		}
		
		this.setOpaque(false);
	}
	
	static AddRemoveControl createAddRemoveRowControl(Row row)
	{
		return new AddRemoveControl(row, -1, false);
	}
	
	static AddRemoveControl createAddRowControl(Row row)
	{
		return new AddRemoveControl(row, -1, true);
	}
	
	public static AddRemoveControl createAddRemoveSectionControl(int section_index)
	{
		return new AddRemoveControl(null, section_index, false);
	}
	
	public static AddRemoveControl createAddSectionControl(int section_index)
	{
		return new AddRemoveControl(null, section_index, true);
	}
	
	public void updateLightingMode()
	{
		add.setForeground(MainGui.inEditMode ? ColorSettings.getTextColor() : ColorSettings.getBackgroundColor() );
		if (remove != null)
			remove.setForeground(MainGui.inEditMode ? ColorSettings.getTextColor() : ColorSettings.getBackgroundColor() );
	}
	
	public void updateEditMode()
	{
		this.updateLightingMode();
	}
	
	public Row getRow() { return row; }
	public int getSectionIndex() { return sectionIndex; }
}