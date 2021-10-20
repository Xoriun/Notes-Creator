package edit;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

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
			inner_panel.add(getAddRemoveSectionControl(section_index, false), gbc);
			
			gbc.gridx = 1;
			JLabel label = new JLabel(MainGui.sectionsList.get(section_index).getTitle() );
			label.setForeground(ColorSettings.getTextColor() );
			label.addMouseListener(MouseAdapters.getEditSectionTitleAdapter(label, section_index) );
			label.setBorder(GuiHelper.getDefaultBorder(section_index == 0, true) );
			inner_panel.add(label, gbc);
			MainGui.sectionLabels.add(label);
			//System.out.println(gbc.gridy + ", " + Abbreviations.sectionIndices[section_index] + ", " + Abbreviations.sections[section_index] );
			
			section_index ++;
			gbc.gridy ++;
		}
		
		gbc.gridx = 0;
		inner_panel.add(getAddRemoveSectionControl(section_index, true), gbc);
		
		sectionManagerPanel.add(inner_panel);
		sectionManagerDialog.add(sectionManagerPanel);

		sectionManagerDialog.pack();
		if (sectionManagerDialog.getHeight() > MainGui.window.getHeight() - 150)
			sectionManagerDialog.setPreferredSize(new Dimension(sectionManagerDialog.getWidth() + 20, MainGui.window.getHeight() - 150) );
		sectionManagerDialog.pack();
		sectionManagerDialog.setVisible(true);
	}
	
	public static JPanel getAddRemoveSectionControl(int current_section_index, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		
		JLabel add = new JLabel(" + ");
		add.setForeground(ColorSettings.getTextColor() );
		add.addMouseListener(MouseAdapters.getAddSectionAdapter(current_section_index) );
		
		MainGui.labelsTextsHideWhenNotInEdit.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(ColorSettings.getTextColor() );
			remove.addMouseListener(MouseAdapters.getRemoveSectionAdapter(current_section_index) );
			
			MainGui.labelsTextsHideWhenNotInEdit.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
}