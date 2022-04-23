package edit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;
import logic.AddRemoveControl;
import logic.MouseAdapters;

public class SectionManagerDialog extends JDialog
{
	/** Automatically generated secrialVerionUID */
	private static final long serialVersionUID = 4418008182269964040L;
	private static JPanel sectionManagerPanel;
	
	public SectionManagerDialog()
	{
		super(MainGui.window);
		this.setTitle("Section Manager");
		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.setVisible(false);
	}
	
	public void updateSectionManagerDialog()
	{
		this.getContentPane().removeAll();
		
		sectionManagerPanel = new JPanel();
		sectionManagerPanel.setBackground(ColorSettings.getBackgroundColor());
		sectionManagerPanel.setBorder(GuiHelper.getDialogBorder() );
		JPanel inner_panel = new JPanel(new GridBagLayout() );
		inner_panel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		int section_index = 0;
		
		while (section_index < MainGui.sectionsList.size() )
		{
			gbc.gridx = 0;
			inner_panel.add(AddRemoveControl.createAddRemoveSectionControl(section_index), gbc);
			
			gbc.gridx = 1;
			JLabel label = new JLabel(MainGui.sectionsList.get(section_index).getTitle() );
			label.setForeground(ColorSettings.getTextColor() );
			label.addMouseListener(MouseAdapters.getEditSectionTitleAdapter(label, section_index) );
			label.setBorder(GuiHelper.getDefaultBorder(section_index == 0, true) );
			inner_panel.add(label, gbc);
			
			section_index ++;
			gbc.gridy ++;
		}
		
		gbc.gridx = 0;
		inner_panel.add(AddRemoveControl.createAddSectionControl(section_index), gbc);
		
		sectionManagerPanel.add(inner_panel);
		this.add(sectionManagerPanel);

		this.pack();
		if (this.getHeight() > MainGui.screensize.height - 150)
			this.setPreferredSize(new Dimension(this.getWidth() + 20, MainGui.screensize.height - 150) );
		this.pack();
		this.setVisible(true);
	}
	
	public void updateLightingMode()
	{
		//TODO
		updateSectionManagerDialog();
	}
	
	public void updateEditMode()
	{
		this.updateSectionManagerDialog();
		this.setVisible(MainGui.inEditMode);
		this.pack();
	}
}
