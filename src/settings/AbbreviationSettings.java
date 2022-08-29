package settings;

import java.awt.BorderLayout;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MouseInputAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import gui.GuiHelper;
import gui.MainGui;
import gui.PopupAlerts;
import gui.WrapLayout;
import logic.FileOperations;
import settings.Settings.SettingsTab;

public class AbbreviationSettings extends JPanel implements SettingsTab
{
	/** auto-generated serialVersionUID */
	private static final long serialVersionUID = 5949447282978690686L;
	
	private static ArrayList<String[]> abbreviationsList = new ArrayList<String[]>();
	
	private static String abbr_location_copy = "";
	private static String images_location_copy = "";
	
	private static JPanel abbrListPanel = new JPanel(new GridBagLayout() );
	
	private static JLabel current_image_dir = new JLabel("Selected image directory: " + images_location_copy);
	private static JLabel current_abbr_file = new JLabel("Selected abbreviations file: " + abbr_location_copy);
	private static ArrayList<JTextField[]> textfieldList = new ArrayList<JTextField[]>();
	
	private static GridBagConstraints gbc = new GridBagConstraints();

	AbbreviationSettings(JDialog settings_dialog)
	{
		super();
		
		this.setName("Abbreviations");
		
		this.setLayout(new BorderLayout() );
		this.setOpaque(false);
		this.setBorder(GuiHelper.getSpacingBorder(5) );
		
		// north panel
		JPanel north_panel = new JPanel();
		north_panel.setLayout(new BoxLayout(north_panel, BoxLayout.Y_AXIS) );
		north_panel.setOpaque(false);
		
	  // image_directory panel
		JPanel image_dir_panel = new JPanel();
		image_dir_panel.setLayout(new BoxLayout(image_dir_panel, BoxLayout.Y_AXIS) );
		image_dir_panel.setOpaque(false);
		image_dir_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Image directory") );
		
		// Label
		JPanel dir_panel = new JPanel(new FlowLayout(FlowLayout.LEFT) );
		dir_panel.setOpaque(false);
		current_image_dir.setForeground(ColorSettings.getTextColor() );
		dir_panel.add(current_image_dir);
		image_dir_panel.add(dir_panel);
		
		// Button
		JPanel button_panel = new JPanel();
		button_panel.setOpaque(false);
		JButton image_dir_select = new JButton("Select image directory");
		image_dir_select.addActionListener(e -> {
			String new_image_location = FileOperations.selectImageDirectory();
			if (new_image_location == null || new_image_location.equals(images_location_copy) )
				return;
			
			FileOperations.unsavedChanges = true;
			images_location_copy = new_image_location; 
			current_image_dir.setText("Selected image directory: " + images_location_copy);
		} );
    button_panel.add(image_dir_select);
		image_dir_panel.add(button_panel);
		
		
		north_panel.add(image_dir_panel);
		
	  // abbr_file panel
		JPanel file_panel = new JPanel();
		file_panel.setLayout(new BoxLayout(file_panel, BoxLayout.Y_AXIS) );
		file_panel.setOpaque(false);
		file_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Abbreviations file") );
		
		// Label
		JPanel current_file_panel = new JPanel(new FlowLayout(FlowLayout.LEFT) );
		current_file_panel.setOpaque(false);
		
		current_abbr_file.setForeground(ColorSettings.getTextColor() );
		current_file_panel.add(current_abbr_file);
		
		file_panel.add(current_file_panel);
		
		// Buttons
		JPanel file_actions_panel = new JPanel(new WrapLayout() );
		file_actions_panel.setOpaque(false);
		JButton file_select = new JButton("Select abbreviations file");
		JButton file_new    = new JButton("New abbreviations file");
		JButton file_remove = new JButton("Remove abbreviations file");
		file_select.addActionListener(e -> {
			String new_abbr_loaction = FileOperations.selectAbbreviationsFile();
			if (new_abbr_loaction == null || new_abbr_loaction.equals(abbr_location_copy) )
				return;
			
			FileOperations.unsavedChanges = true;
			abbr_location_copy = new_abbr_loaction; 
			fillAbbrList();
			settings_dialog.pack();
		} );
		file_new.addActionListener(e -> {
			abbr_location_copy = "";
			fillAbbrList();
			settings_dialog.pack();
		} );
		file_remove.addActionListener(e -> {
			abbr_location_copy = "";
			fillAbbrList();
			settings_dialog.pack();
		} );
		file_actions_panel.add(file_select);
		file_actions_panel.add(file_new);
		file_actions_panel.add(file_remove);
    
		file_panel.add(file_actions_panel);
		
		north_panel.add(file_panel);
		
		this.add(north_panel, BorderLayout.NORTH);
		
		// abbreviationsList panel
		JPanel abbr_panel = new JPanel();
		abbr_panel.setLayout(new BorderLayout() );
		abbr_panel.setOpaque(false);
		abbr_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Abbreviations List") );
		
		// abbreviationsList list panel
		abbrListPanel.setBackground(ColorSettings.getBackgroundColor() );
		JScrollPane abbr_scroll_pane = new JScrollPane(
				abbrListPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
		abbr_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);
		abbr_scroll_pane.setBackground(ColorSettings.getBackgroundColor() );
		abbr_scroll_pane.setBorder(GuiHelper.getEmptyBorder() );
		
		abbr_panel.add(abbr_scroll_pane, BorderLayout.CENTER);
		
		// abbreviation add button
		JPanel abbr_add_panel = new JPanel();
		abbr_add_panel.setOpaque(false);
		gbc.gridx = gbc.gridy = 0;
		JButton abbr_add_button = new JButton("Add abbreviation");
		abbr_add_button.addActionListener(e -> {
			addAbbreviation();
			settings_dialog.pack();
			abbr_scroll_pane.getVerticalScrollBar().setValue(abbr_scroll_pane.getVerticalScrollBar().getMaximum() );
		} );
		abbr_add_panel.add(abbr_add_button);
		
		abbr_panel.add(abbr_add_panel, BorderLayout.SOUTH);
		
		this.add(abbr_panel, BorderLayout.CENTER);
	}

	@Override
	public void update()
	{
		abbr_location_copy = FileOperations.fileAbbreviations;
		images_location_copy = FileOperations.imagesDirectory;

		current_image_dir.setText("Selected image directory: " + images_location_copy);
		current_abbr_file.setText("Selected abbreviations file: " + abbr_location_copy);
		
		fillAbbrList();
	}

	@Override
	public void save()
	{
		PopupAlerts.createMissingImagesMessage = true;
		abbreviationsList = getSortedAbbreviationsListFromTextfields(textfieldList);
		FileOperations.imagesDirectory = images_location_copy;
		FileOperations.fileAbbreviations = abbr_location_copy;
		FileOperations.saveAbbereviationsFile();
		MainGui.reloadImages();
	}
	
	private static ArrayList<String[]> getSortedAbbreviationsListFromTextfields(ArrayList<JTextField[]> textfields)
	{
		return textfields.stream()
				.map(row -> new String[] {row[0].getText(), row[1].getText() } )
				.sorted( (a,b) -> {return a[0].compareTo(b[0] ); } )
				.collect(Collectors.toCollection(ArrayList::new) );
	}

	@Override
	public void discard()
	{
		// nothing to do
	}
	
	private void fillAbbrList()
	{
		current_abbr_file.setText("Selected abbreviations file: " + abbr_location_copy);
		abbrListPanel.removeAll();
		textfieldList.clear();
		
		if (abbr_location_copy.equals("") )
			return;
		
		gbc.gridy = 0;
		for (String[] abbr : FileOperations.readAbbriviationsFile(abbr_location_copy) )
			addAbbrevation(abbr);
	}
	
	private void addAbbreviation()
	{
		addAbbrevation(new String[] {"", ""} );
	}
	
	private void addAbbrevation(String[] abbr)
	{
		JTextField[] row = new JTextField[2];
		for (int i = 0; i < 2; i ++)
		{
			gbc.gridx = i;
			AbbrTexField textfield = new AbbrTexField(abbr[i], gbc);
			row[i] = textfield;
			abbrListPanel.add(textfield, gbc);
		}
		textfieldList.add(row);
		JLabel remove = new JLabel(" - ");
		remove.setOpaque(false);
		remove.setForeground(ColorSettings.getTextColor() );
		remove.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				textfieldList.remove(row);
				abbrListPanel.remove(row[0] );
				abbrListPanel.remove(row[1] );
				abbrListPanel.remove(remove);
				MainGui.settings.pack();
			}
		} );
		gbc.gridx = 3;
		abbrListPanel.add(remove, gbc);
		gbc.gridy ++;
	}
	
	public static String getAbbreviationSaveString()
	{
		String res = "";
		for (String[] abbr : abbreviationsList)
			res += abbr[0] + ":" + abbr[1] + "\n";
		return res;
	}
	
	public static void setAbbreviationsList(ArrayList<String[]> abbreviations_list)
	{
		abbreviationsList = abbreviations_list;
	}
	
	public static String getNameFromAbbreviation(String str)
	{
		if (abbreviationsList == null)
			return str;
		
		for (String[] abbr : abbreviationsList)
			if (str.equals(abbr[0]) )
				return abbr[1];
		
		return str;
	}
	
	public static Stream<String> getStreamOfAbbreviations()
	{
		return abbreviationsList.stream().map(e -> e[0] );
	}
	
	public static Stream<String> getStreamOfCompleteNames()
	{
		return abbreviationsList.stream().map(e -> e[1] );
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
	
	private class AbbrTexField extends JTextField
	{
		/** automatically generated UUID */
		private static final long serialVersionUID = -9056673685126009912L;
		
		private AbbrTexField(String content, GridBagConstraints gbc)
		{
			super(content);
			
			this.setBorder(GuiHelper.getDefaultBorder(gbc.gridy == 0, gbc.gridx == 0) );
			this.setPreferredSize(new Dimension(150, 20) );
			this.setOpaque(false);
			this.setForeground(ColorSettings.getTextColor() );
		}
	}
}