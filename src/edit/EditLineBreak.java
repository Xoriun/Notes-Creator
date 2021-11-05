package edit;

import javax.swing.JLabel;

import gui.ColorSettings;
import gui.GuiHelper;

public class EditLineBreak extends JLabel implements EditPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = 6906813339084384344L;
	
	public EditLineBreak()
	{
		super();
		this.setIcon(GuiHelper.scaledLinebreakImageIcon);
	}
	
	@Override
	public String getString()
	{
		return "\\n";
	}
	
	@Override
	public void setSelectedBorder()
	{
		this.setBorder(GuiHelper.getSelectedBorder() );
	}
	
	@Override
	public void setDefaultBorder()
	{
		this.setBorder(GuiHelper.getEmptyBorder() );
	}
	
	public void updateColorSettings()
	{
		this.setBackground(ColorSettings.getBackgroundColor() );
	}
}
