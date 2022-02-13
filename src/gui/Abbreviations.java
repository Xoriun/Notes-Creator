package gui;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MouseInputAdapter;

import gui.MainGui;
import logic.FileOperations;

public class Abbreviations
{
	private static ArrayList<String[]> abbreviationsList = new ArrayList<String[]>();
	
	@SuppressWarnings("unchecked")
	public static void editAbbreviationSettings()
	{
		getAbbreviationSettings(FileOperations.fileAbbreviations, (ArrayList<String[]>) abbreviationsList.clone() );
	}

	private static void getAbbreviationSettings(String abbr_location_copy, ArrayList<String[]> abbr_list_copy)
	{
		JDialog abbreviations_dialog = new JDialog(MainGui.window);
		abbreviations_dialog.setModal(true);
		abbreviations_dialog.setTitle("Edit abbreviations");
		abbreviations_dialog.setMinimumSize(new Dimension(400, 200) );
		
		JPanel outer_panel = new JPanel();
		outer_panel.setLayout(new BoxLayout(outer_panel, BoxLayout.Y_AXIS) );
		outer_panel.setBackground(gui.ColorSettings.getBackgroundColor() );
		outer_panel.setBorder(GuiHelper.getDialogBorder() );
		
		JPanel settings_panel = new JPanel();
		settings_panel.setLayout(new BoxLayout(settings_panel, BoxLayout.Y_AXIS) );
		settings_panel.setOpaque(false);
		
	  // file panel
			JPanel file_panel = new JPanel();
			file_panel.setLayout(new BoxLayout(file_panel, BoxLayout.Y_AXIS) );
			file_panel.setOpaque(false);
			
			// file location
			JPanel current_file_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			current_file_panel.setOpaque(false);
			JLabel current_file = new JLabel("Selected abbreviations file: " + abbr_location_copy);
			current_file.setForeground(ColorSettings.getTextColor() );
			current_file_panel.add(current_file);
			
			// load file
			JPanel file_actions_panel = new JPanel();
			file_actions_panel.setOpaque(false);
			JButton file_select = new JButton("Select abbreviations file");
			JButton file_new    = new JButton("New abbreviations file");
			JButton file_remove = new JButton("Remove abbreviations file");
			file_select.addActionListener(e -> {
				String new_abbr_loaction = FileOperations.selectAbbreviationsFile();
				abbreviations_dialog.dispose();
				ArrayList<String[]> new_abbr_list = FileOperations.readAbbriviationsFile(new_abbr_loaction);
				if (!new_abbr_list.isEmpty() )
					getAbbreviationSettings(new_abbr_loaction, new_abbr_list);
			} );
			file_new.addActionListener(e -> {
				abbreviations_dialog.dispose();
				getAbbreviationSettings(FileOperations.newAbbreviationsFile(abbr_list_copy), abbr_list_copy);
			} );
			file_remove.addActionListener(e -> {
				abbreviations_dialog.dispose();
				getAbbreviationSettings("", new ArrayList<String[]>() );
			} );
			file_actions_panel.add(file_select);
			file_actions_panel.add(file_new);
			file_actions_panel.add(file_remove);
	    
			// fill cells
			file_panel.add(current_file_panel);
			file_panel.add(file_actions_panel);
			
			settings_panel.add(file_panel);
		
		// abbreviationsList panel
			JPanel abbr_panel = new JPanel();
			abbr_panel.setLayout(new BoxLayout(abbr_panel, BoxLayout.Y_AXIS) );
			abbr_panel.setOpaque(false);
			JButton abbr_add_button = new JButton("Add abbreviation");
			
			// abbreviationsList list panel
			JPanel abbr_list_panel = new JPanel(new GridBagLayout() );
			abbr_list_panel.setBackground(ColorSettings.getBackgroundColor() );
			GridBagConstraints gbc = new GridBagConstraints();
			Dimension min_dim = new Dimension(150,20);
			ArrayList<JTextField[]> textfield_list = new ArrayList<JTextField[]>();
			
			gbc.gridy = 0;
			for (String[] abbr : abbr_list_copy)
			{
				JTextField[] row = new JTextField[2];
				for (int i = 0; i < 2; i ++)
				{
					gbc.gridx = i;
					JTextField textfield = new JTextField(abbr[i] );
					textfield.setBorder(GuiHelper.getDefaultBorder(gbc.gridy == 0, gbc.gridx == 0) );
					textfield.setPreferredSize(min_dim);
					row[i] = textfield;
					textfield.setOpaque(false);
					textfield.setForeground(ColorSettings.getTextColor() );
					abbr_list_panel.add(textfield, gbc);
				}
				textfield_list.add(row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(ColorSettings.getTextColor() );
				remove.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e)
					{
						abbreviations_dialog.dispose();
						textfield_list.remove(row);
						abbr_list_copy.remove(abbr);
						getAbbreviationSettings(abbr_location_copy, getAbbreviationsListFromTextfiles(textfield_list) );
					}
				} );
				gbc.gridx = 3;
				abbr_list_panel.add(remove, gbc);
				gbc.gridy ++;
			}
			
			// abbreviation add button
			JPanel abbr_add_panel = new JPanel();
			abbr_add_panel.setOpaque(false);
			abbr_add_button.addActionListener(e -> {
				String[] new_abbr = new String[] {"",""};
				abbr_list_copy.add(new_abbr);
				JTextField[] new_row = new JTextField[2];
				for (int i = 0; i < 2; i ++)
				{
					gbc.gridx = i;
					JTextField textfield = new JTextField("");
					textfield.setBorder(GuiHelper.getDefaultBorder(gbc.gridy == 0, gbc.gridx == 0) );
					textfield.setPreferredSize(min_dim);
					new_row[i] = textfield;
					textfield.setOpaque(false);
					textfield.setForeground(ColorSettings.getTextColor() );
					abbr_list_panel.add(textfield, gbc);
				}
				textfield_list.add(new_row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(ColorSettings.getTextColor() );
				remove.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e)
					{
						abbreviations_dialog.dispose();
						textfield_list.remove(new_row);
						abbr_list_copy.remove(new_abbr);
						getAbbreviationSettings(abbr_location_copy, getAbbreviationsListFromTextfiles(textfield_list) );
					}
				} );
				gbc.gridx = 3;
				abbr_list_panel.add(remove, gbc);
				gbc.gridy ++;
				abbreviations_dialog.pack();
				abbreviations_dialog.repaint();
			} );
			abbr_add_panel.add(abbr_add_button);
			
			// fill cells
			JScrollPane abbr_scroll_pane = new JScrollPane(abbr_list_panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			abbr_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);
			abbr_scroll_pane.setBackground(ColorSettings.getBackgroundColor() );
			abbr_scroll_pane.setBorder(null);
			abbr_panel.add(abbr_scroll_pane);
			abbr_panel.add(abbr_add_panel);
			
			settings_panel.add(abbr_panel);
		
		// controls panel
			JPanel controls_panel = new JPanel();
			controls_panel.setOpaque(false);
			
			JButton confirm = new JButton("Confirm");
			JButton cancel  = new JButton("Cancel");
			
			confirm.addActionListener(e -> {
				abbreviations_dialog.dispose();
				FileOperations.unsavedChanges = true;
				PopupAlerts.creatMissingImagesMessage = true;
				setAbbreviationsList(getAbbreviationsListFromTextfiles(textfield_list) );
				FileOperations.fileAbbreviations = abbr_location_copy;
				FileOperations.saveAbbereviationsFile();
				MainGui.arrangeContent();
				MainGui.spaceColums();
			} );
			cancel .addActionListener(e -> { abbreviations_dialog.dispose(); } );
		
			controls_panel.add(confirm);
			controls_panel.add(cancel);
			
			settings_panel.add(controls_panel);
			
		outer_panel.add(settings_panel);
		abbreviations_dialog.add(outer_panel);
		abbreviations_dialog.pack();
		GuiHelper.setLocationToCenter(abbreviations_dialog, -200);
		abbreviations_dialog.pack();
		abbreviations_dialog.setVisible(true);
		abbreviations_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public static void setAbbreviationsList(ArrayList<String[]> abbreviations_list)
	{
		abbreviationsList = abbreviations_list.stream().sorted( (a,b) -> {return a[0].compareTo(b[0] ); } ).collect(Collectors.toCollection(ArrayList::new) );
	}
	
	public static ArrayList<String[]> getAbbreviationsListFromTextfiles(ArrayList<JTextField[]> textfields)
	{
		return textfields.stream().map(row -> new String[] {row[0].getText(), row[1].getText() } ).collect(Collectors.toCollection(ArrayList::new) );
	}
	
	public static String[] getArrayOfAbbreviations()
	{
		return Stream.concat(Stream.of(""), abbreviationsList.stream().map(e -> e[0] ) ).toArray(String[]::new);
	}
	
	public static String getNameFromAbbreviation(String str)
	{
		if (abbreviationsList != null)
			for (String[] abbr : abbreviationsList)
				if (str.equals(abbr[0]) )
					return abbr[1];
		return str;
	}
	
	public static String getAbbreviationSaveString()
	{
		String res = "";
		for (String[] abbr : abbreviationsList)
			res += abbr[0] + ":" + abbr[1] + "\n";
		return res;
	}
}