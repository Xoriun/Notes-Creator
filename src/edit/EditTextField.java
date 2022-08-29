package edit;

import javax.swing.JTextField;

import gui.GuiHelper;
import settings.ColorSettings;

public class EditTextField extends JTextField implements EditPanel
{
	// auto-generated serialVersionUID
	private static final long serialVersionUID = 1009025985436493231L;
	
	public EditTextField(String string)
	{
		super(string);
		this.setAlignmentX(0f);
		this.setOpaque(false);
		this.setDefaultBorder();
		updateColorSettings();
	}
	
	@Override
	public String getString()
	{
		return this.getText();
	}

	@Override
	public void setSelectedBorder()
	{
		this.setBorder(GuiHelper.getSelectedBorder() );
	}

	@Override
	public void setDefaultBorder()
	{
		this.setBorder(GuiHelper.getDefaultBorder() );
	}

	@Override
	public void updateColorSettings()
	{
		this.setBackground(ColorSettings.getBackgroundColor() );
		this.setForeground(ColorSettings.getTextColor() );
		this.setDefaultBorder();
		this.setCaretColor(ColorSettings.getTextColor() );
	}
}
