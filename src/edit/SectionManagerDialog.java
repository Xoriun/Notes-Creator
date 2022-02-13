package edit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import gui.AddRemoveControl;
import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;
import logic.MouseAdapters;

public class SectionManagerDialog
{
	public static JDialog sectionManagerDialog;
	public static JPanel sectionManagerPanel;
	
	public static void initializeSectionDialog()
	{
		sectionManagerDialog = new JDialog(MainGui.window);
		sectionManagerDialog.setTitle("Section Manager");
		sectionManagerDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		sectionManagerDialog.setVisible(false);
	}
	
	public static void updateSectionManagerDialog()
	{
		sectionManagerDialog.getContentPane().removeAll();
		
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
			MainGui.sectionLabels.add(label);
			
			section_index ++;
			gbc.gridy ++;
		}
		
		gbc.gridx = 0;
		inner_panel.add(AddRemoveControl.createAddSectionControl(section_index), gbc);
		
		sectionManagerPanel.add(inner_panel);
		sectionManagerDialog.add(sectionManagerPanel);

		sectionManagerDialog.pack();
		if (sectionManagerDialog.getHeight() > MainGui.screensize.height - 150)
			sectionManagerDialog.setPreferredSize(new Dimension(sectionManagerDialog.getWidth() + 20, MainGui.screensize.height - 150) );
		sectionManagerDialog.pack();
		sectionManagerDialog.setVisible(true);
	}
}
