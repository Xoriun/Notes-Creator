package gui;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.w3c.dom.*;

import gui.MainGui;
import logic.FileOperations;

public class Abbreviations
{
	private static JDialog dialog;
	
	private static ArrayList<String[]> abbreviationsList = new ArrayList<String[]>();
	private static String abbr_location_copy;
	private static String images_location_copy;
	
	private static JPanel abbrListPanel;

	public static void showAbbreviationSettingsDialog()
	{
		abbr_location_copy = FileOperations.fileAbbreviations;
		images_location_copy = FileOperations.imagesDirectory;
		
		dialog = new JDialog(MainGui.window);
		dialog.setModal(true);
		dialog.setTitle("Edit abbreviations");
		dialog.setMinimumSize(new Dimension(400, 200) );
		
		JPanel outer_panel = new JPanel();
		outer_panel.setLayout(new BoxLayout(outer_panel, BoxLayout.Y_AXIS) );
		outer_panel.setBackground(gui.ColorSettings.getBackgroundColor() );
		outer_panel.setBorder(GuiHelper.getDialogBorder() );
		
		JPanel settings_panel = new JPanel();
		settings_panel.setLayout(new BoxLayout(settings_panel, BoxLayout.Y_AXIS) );
		settings_panel.setOpaque(false);
		settings_panel.setBorder(GuiHelper.getSpacingBorder(5) );
		
	  // image_dir panel
			JPanel image_dir_panel = new JPanel();
			image_dir_panel.setLayout(new BoxLayout(image_dir_panel, BoxLayout.X_AXIS) );
			image_dir_panel.setOpaque(false);
			image_dir_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Image directory") );
			
			// image_dir location
			JPanel current_image_dir_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			current_image_dir_panel.setOpaque(false);
			JLabel current_image_dir = new JLabel("Selected image directory: " + images_location_copy);
			current_image_dir.setForeground(ColorSettings.getTextColor() );
			current_image_dir_panel.add(current_image_dir);
			
			// image_dir file
			JPanel image_dir_actions_panel = new JPanel();
			image_dir_actions_panel.setOpaque(false);
			JButton image_dir_select = new JButton("Select image directory");
			image_dir_select.addActionListener(e -> {
				images_location_copy = FileOperations.selectImageDirectory();
				current_image_dir.setText("Selected image directory: " + images_location_copy);
				dialog.pack();
			} );
			image_dir_actions_panel.add(image_dir_select);
	    
			// fill cells
			image_dir_panel.add(current_image_dir_panel);
			image_dir_panel.add(image_dir_actions_panel);
			
			settings_panel.add(image_dir_panel);
		
	  // file panel
			JPanel file_panel = new JPanel();
			file_panel.setLayout(new BoxLayout(file_panel, BoxLayout.Y_AXIS) );
			file_panel.setOpaque(false);
			file_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Abbreviations file") );
			
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
				abbr_location_copy = FileOperations.selectAbbreviationsFile();
				current_file.setText("Selected abbreviations file: " + abbr_location_copy);
				abbrListPanel.removeAll();
				
				// abbreviationsList list panel
				GridBagConstraints gbc = new GridBagConstraints();
				Dimension min_dim = new Dimension(150,20);
				ArrayList<JTextField[]> textfield_list = new ArrayList<JTextField[]>();
				
				gbc.gridy = 0;
				for (String[] abbr : FileOperations.readAbbriviationsFile(abbr_location_copy) )
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
						abbrListPanel.add(textfield, gbc);
					}
					textfield_list.add(row);
					JLabel remove = new JLabel(" - ");
					remove.setOpaque(false);
					remove.setForeground(ColorSettings.getTextColor() );
					remove.addMouseListener(new MouseInputAdapter() {
						@Override
						public void mouseClicked(MouseEvent e)
						{
							textfield_list.remove(row);
							abbrListPanel.remove(row[0] );
							abbrListPanel.remove(row[1] );
							dialog.pack();
						}
					} );
					gbc.gridx = 3;
					abbrListPanel.add(remove, gbc);
					gbc.gridy ++;
				}
			} );
			file_new.addActionListener(e -> {
				abbr_location_copy = "";
				current_file.setText("Selected abbreviations file: " + abbr_location_copy);
				abbrListPanel.removeAll();
			} );
			file_remove.addActionListener(e -> {
				abbr_location_copy = "";
				current_file.setText("Selected abbreviations file: " + abbr_location_copy);
				abbrListPanel.removeAll();
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
			abbr_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Abbreviations") );
			JButton abbr_add_button = new JButton("Add abbreviation");
			
			// abbreviationsList list panel
			abbrListPanel = new JPanel(new GridBagLayout() );
			abbrListPanel.setBackground(ColorSettings.getBackgroundColor() );
			GridBagConstraints gbc = new GridBagConstraints();
			Dimension min_dim = new Dimension(150,20);
			ArrayList<JTextField[]> textfield_list = new ArrayList<JTextField[]>();
			
			gbc.gridy = 0;
			for (String[] abbr : abbreviationsList)
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
					abbrListPanel.add(textfield, gbc);
				}
				textfield_list.add(row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(ColorSettings.getTextColor() );
				remove.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e)
					{
						textfield_list.remove(row);
						abbrListPanel.remove(row[0] );
						abbrListPanel.remove(row[1] );
						abbrListPanel.remove(remove);
						dialog.pack();
					}
				} );
				gbc.gridx = 3;
				abbrListPanel.add(remove, gbc);
				gbc.gridy ++;
			}
			
			// abbreviation add button
			JPanel abbr_add_panel = new JPanel();
			abbr_add_panel.setOpaque(false);
			abbr_add_button.addActionListener(e -> {
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
					abbrListPanel.add(textfield, gbc);
				}
				textfield_list.add(new_row);
				JLabel remove = new JLabel(" - ");
				remove.setOpaque(false);
				remove.setForeground(ColorSettings.getTextColor() );
				remove.addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e)
					{
						textfield_list.remove(new_row);
						abbrListPanel.remove(new_row[0] );
						abbrListPanel.remove(new_row[1] );
						dialog.pack();
					}
				} );
				gbc.gridx = 3;
				abbrListPanel.add(remove, gbc);
				gbc.gridy ++;
				dialog.pack();
				dialog.repaint();
			} );
			abbr_add_panel.add(abbr_add_button);
			
			// fill cells
			JScrollPane abbr_scroll_pane = new JScrollPane(abbrListPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			abbr_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);
			abbr_scroll_pane.setBackground(ColorSettings.getBackgroundColor() );
			abbr_scroll_pane.setBorder(null);
			abbr_panel.add(abbr_scroll_pane);
			abbr_panel.add(Box.createVerticalGlue() );
			abbr_panel.add(abbr_add_panel);
			
			settings_panel.add(abbr_panel);
		
		// controls panel
			JPanel controls_panel = new JPanel();
			controls_panel.setOpaque(false);
			
			JButton confirm = new JButton("Confirm");
			JButton cancel  = new JButton("Cancel");
			
			confirm.addActionListener(e -> {
				dialog.dispose();
				FileOperations.unsavedChanges = true;
				PopupAlerts.createMissingImagesMessage = true;
				setAbbreviationsList(getAbbreviationsListFromTextfields(textfield_list) );
				FileOperations.imagesDirectory = images_location_copy;
				FileOperations.fileAbbreviations = abbr_location_copy;
				FileOperations.saveAbbereviationsFile();
				MainGui.reloadImages();
				closeDialog();
			} );
			cancel .addActionListener(e -> { closeDialog(); } );
		
			controls_panel.add(confirm);
			controls_panel.add(cancel);
			
			settings_panel.add(controls_panel);
			
		outer_panel.add(settings_panel);
		dialog.add(outer_panel);
		dialog.pack();
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog, -200, dialog.getHeight() - (abbr_scroll_pane.getHeight() / 2) );
		dialog.pack();
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	public static void closeDialog()
	{
		abbr_location_copy = images_location_copy = null;
		dialog.dispose();
	}
	
	public static void setAbbreviationsList(ArrayList<String[]> abbreviations_list)
	{
		abbreviationsList = abbreviations_list;
		sortAbbreviationList();
	}
	
	private static void sortAbbreviationList()
	{
		abbreviationsList = abbreviationsList.stream().sorted( (a,b) -> {return a[0].compareTo(b[0] ); } ).collect(Collectors.toCollection(ArrayList::new) );
	}
	
	private static ArrayList<String[]> getAbbreviationsListFromTextfields(ArrayList<JTextField[]> textfields)
	{
		return textfields.stream().map(row -> new String[] {row[0].getText(), row[1].getText() } ).collect(Collectors.toCollection(ArrayList::new) );
	}
	
	public static Stream<String> getStreamOfAbbreviations()
	{
		return abbreviationsList.stream().map(e -> e[0] );
	}
	
	public static Stream<String> getStreamOfCompleteNames()
	{
		return abbreviationsList.stream().map(e -> e[1] );
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
	
	public static Element getAbbreviationsElement(Document doc)
	{
		Element abbreviations_element = doc.createElement("abbreviations");
		
		for (String[] abbr : abbreviationsList)
		{
			Element abbr_element = doc.createElement("abbreviation");
			abbreviations_element.appendChild(abbr_element);
			
			Element abbr_short_element = doc.createElement("abbr-short");
			abbr_short_element.setTextContent(abbr[0] );
			abbr_element.appendChild(abbr_short_element);
			
			Element abbr_long_element = doc.createElement("abbr-long");
			abbr_long_element.setTextContent(abbr[1] );
			abbr_element.appendChild(abbr_long_element);
		}
		
		return abbreviations_element;
	}
	
	public static void parseAbbreviationselement(Element abbreviaitons_element)
	{
		abbreviationsList.clear();
		
		NodeList abbreviation_nodes = abbreviaitons_element.getElementsByTagName("abbreviation");
		for (int i = 0; i < abbreviation_nodes.getLength(); i ++)
		{
			Element abbr = (Element) abbreviation_nodes.item(i);
			abbreviationsList.add(new String[] {abbr.getElementsByTagName("abbr-short").item(0).getTextContent(), abbr.getElementsByTagName("abbr-long").item(0).getTextContent()} );
		}
		
		sortAbbreviationList();
	}
}