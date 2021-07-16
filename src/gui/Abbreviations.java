package gui;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
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
	public static void getAbbreviationSettings(String abbr_location_copy, ArrayList<String[]> abbr_list_copy)
	{
		JDialog abbreviations_dialog = new JDialog(MainGui.window);
		abbreviations_dialog.setModal(true);
		abbreviations_dialog.setTitle("Edit abbreviations");
		abbreviations_dialog.setMinimumSize(new Dimension(400, 200) );
		
		JPanel outer_panel = new JPanel();
		outer_panel.setLayout(new BoxLayout(outer_panel, BoxLayout.Y_AXIS) );
		outer_panel.setBackground(gui.ColorSettings.currentColorSetting.background);
		outer_panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, ColorSettings.currentColorSetting.text) );
		
		JPanel settings_panel = new JPanel();
		settings_panel.setLayout(new BoxLayout(settings_panel, BoxLayout.Y_AXIS) );
		settings_panel.setOpaque(false);
		settings_panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 2));
		
	  // file panel
			JPanel file_panel = new JPanel();
			file_panel.setLayout(new BoxLayout(file_panel, BoxLayout.Y_AXIS) );
			file_panel.setOpaque(false);
			
			// file location
			JPanel current_file_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			current_file_panel.setOpaque(false);
			JLabel current_file = new JLabel("Selected abbreviations file: " + abbr_location_copy);
			current_file.setForeground(ColorSettings.currentColorSetting.text);
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
			abbr_list_panel.setBackground(ColorSettings.currentColorSetting.background);
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
					textfield.setBorder(BorderFactory.createMatteBorder(gbc.gridy == 0 ? 1 : 0, 1, 1, i, ColorSettings.currentColorSetting.border) );
					textfield.setPreferredSize(min_dim);
					row[i] = textfield;
					textfield.setOpaque(false);
					textfield.setForeground(ColorSettings.currentColorSetting.text);
					abbr_list_panel.add(textfield, gbc);
				}
				textfield_list.add(row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(ColorSettings.currentColorSetting.text);
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
					textfield.setBorder(BorderFactory.createMatteBorder(gbc.gridy == 0 ? 1 : 0, 1, 1, i, ColorSettings.currentColorSetting.border) );
					textfield.setPreferredSize(min_dim);
					new_row[i] = textfield;
					textfield.setOpaque(false);
					textfield.setForeground(ColorSettings.currentColorSetting.text);
					abbr_list_panel.add(textfield, gbc);
				}
				textfield_list.add(new_row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(ColorSettings.currentColorSetting.text);
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
			abbr_scroll_pane.setBackground(ColorSettings.currentColorSetting.background);
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
				Abbreviations.abbreviationsList = getAbbreviationsListFromTextfiles(textfield_list);
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
		PopupAlerts.setLocationToCenter(abbreviations_dialog, -200);
		abbreviations_dialog.pack();
		abbreviations_dialog.setVisible(true);
		abbreviations_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public static ArrayList<String[]> getAbbreviationsListFromTextfiles(ArrayList<JTextField[]> textfields)
	{
		return textfields.stream().map(row -> new String[] {row[0].getText(), row[1].getText() } ).collect(Collectors.toCollection(ArrayList::new) );
	}
	
	public static String getAbbreviation(String str)
	{
		if (Abbreviations.abbreviationsList != null)
			for (String[] abbr : Abbreviations.abbreviationsList)
				if (str.equals(abbr[0]) )
					return abbr[1];
		return str;
	}

	public static ArrayList<String[]> abbreviationsList = new ArrayList<String[]>();
	
	/*
	public MouseInputAdapter getTextEdit(JLabel label)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				String newText = JOptionPane.showInputDialog(null, "Set the text!", label.getText() );
				if (newText != null) label.setText(newText);
			}
		};
	}
	//*/
	
	/* public MouseInputAdapter getIconEdit(File mainFile, File secondaryFile)
	{
		return new MouseInputAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				JPanel mainPanel = new JPanel(new GridBagLayout() );
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridy = 0;
				
				// main Icon
				gbc.gridx = 0;
				mainPanel.add(new JLabel("Main Icon:"), gbc);
				gbc.gridx ++;
				mainPanel.add(new JLabel(mainFile.getName() ) );
				gbc.gridx ++;
				mainPanel.add(new JLabel(new ImageIcon(new ImageIcon(mainFile.exists() ? mainFile.getAbsolutePath() : "Images\\Destroyed-icon.png").getImage().getScaledInstance(MainGui.ImageSize, MainGui.ImageSize, Image.SCALE_DEFAULT) ) ) );
				gbc.gridx ++;
				mainPanel.add(new JButton("Change"), gbc);
				
				// checkbox secondary Icon
				gbc.gridy ++;
				gbc.gridx = 0;
				mainPanel.add(new JLabel("Use secondary Icon"), gbc);
				gbc.gridx ++;
				JCheckBox checkBox = new JCheckBox();
				mainPanel.add(checkBox, gbc);
				
				// secondary Icon
				gbc.gridy ++;
				gbc.gridx = 0;
				JLabel secLabel = new JLabel("Secundary Icon:");
				mainPanel.add(secLabel, gbc);
				gbc.gridx ++;
				JLabel secFileName = new JLabel(secondaryFile == null ? "" : secondaryFile.getName() );
				mainPanel.add(secFileName);
				gbc.gridx ++;
				JLabel secIcon = secondaryFile == null ? new JLabel() : new JLabel(new ImageIcon(new ImageIcon(secondaryFile.exists() ? secondaryFile.getAbsolutePath() : "Images\\Destroyed-icon.png").getImage().getScaledInstance(MainGui.ImageSize, MainGui.ImageSize, Image.SCALE_DEFAULT) ) );
				mainPanel.add(secIcon);
				gbc.gridx ++;
				JButton secButton = new JButton("Change");
				mainPanel.add(secButton, gbc);
				// secButton.addActionListener( e -> { secondaryFile = getIconFile(secondaryFile); } );
				
				// secondary Icon location
				gbc.gridy ++;
				gbc.gridx = 0;
				JLabel secLocationLabel = new JLabel("Location of secondary Icon");
				mainPanel.add(secLocationLabel, gbc);
				gbc.gridx ++;
				JComboBox<String> secVert = new JComboBox<String>( new String[] {"top", "center", "buttom"} );
				mainPanel.add(secVert, gbc);
				gbc.gridx ++;
				JComboBox<String> secHor = new JComboBox<String>( new String[] {"left", "center", "right"} );
				mainPanel.add(secHor, gbc);
				
				// confirm
				gbc.gridy ++;
				gbc.gridx = 0;
				gbc.gridwidth = 4;
				JButton confirm = new JButton("Confirm");
				JButton cancel = new JButton("Cancel");
				JPanel confirmPanel = new JPanel();
				confirmPanel.add(confirm);
				confirmPanel.add(cancel);
				mainPanel.add(confirmPanel, gbc);
				
				JDialog iconSelect = new JDialog();
				iconSelect.setModalityType(ModalityType.APPLICATION_MODAL);
				iconSelect.add(mainPanel);
				iconSelect.pack();
				iconSelect.setVisible(true);
			}
		};
	}
	*/
	
	/* public static File getIconFile(File old_file)
	{
		FileDialog dialog = new FileDialog(new Frame(), "Select File to Open");
    dialog.setMode(FileDialog.LOAD);
    dialog.setVisible(true);
    if (dialog.getFile() != null) return new File(dialog.getFile() );
    else return old_file;
	}*/
}
