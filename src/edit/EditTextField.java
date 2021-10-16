package edit;

import javax.swing.JTextField;

import gui.ColorSettings;
import gui.GuiHelper;

public class EditTextField extends JTextField implements EditPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = 1009025985436493231L;
	
	public EditTextField(String string)
	{
		super(string);
		this.setAlignmentX(0f);
		this.setOpaque(false);
		this.setBackground(ColorSettings.getBackgroundColor() );
		this.setForeground(ColorSettings.getTextColor() );
		this.setCaretColor(ColorSettings.getTextColor() );
		this.setDefaultBorder();
	}
	
	public String getString()
	{
		return this.getText();
	}
	
	public void setSelectedBorder()
	{
		this.setBorder(GuiHelper.getSelectedBorder() );
	}
	
	public void setDefaultBorder()
	{
		this.setBorder(GuiHelper.getDefaultBorder() );
	}
}
