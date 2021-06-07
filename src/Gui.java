import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class Gui {
	public static Font font = new Font("Serif", Font.PLAIN, 20);
	public static Font titleFont = new Font("Serif", Font.PLAIN, 15);
	
	public static JFrame window;
	public static JScrollPane scrollPane;
	public static JPanel mainPanel;
	private static JDialog sectionManagerDialog;
	private static JPanel sectionManagerPanel;
	
	public final static int ImageSize = 30;
	public static boolean inEditMode = false;
	private static JCheckBoxMenuItem edit_enabled;
	public static JMenu edit_add;
	public static JMenu edit_remove;
	
	public static int row = 0;
	
	public static boolean keepGuiSize = false;
	public static boolean contentRearraged = false;
	private static int height = 0;
	private static int scrollValue = 0;
	public static ColorSetting[] colorSettings;
	public static ColorSetting currentColorSetting;
	
	public static ArrayList<JPanel> sectionPanelsList = new ArrayList<JPanel>();
	public static ArrayList<GridBagConstraints> sectionConstraints = new ArrayList<GridBagConstraints>();
	public static Set<JPanel> cells = new HashSet<JPanel>();
	public static Set<JLabel> sectionLabels = new HashSet<JLabel>();
	public static Set<JLabel> labelsTextcolor = new HashSet<JLabel>();
	public static Set<JLabel> labelsBackgroundcolorTextcolor = new HashSet<JLabel>();
	public static Set<JLabel> labelsHideUnhide = new HashSet<JLabel>(); 
	public static ArrayList<JPanel[][]> cellPanelsList = new ArrayList<JPanel[][]>();
	public static ArrayList<JPanel[]> spacingPanelsList = new ArrayList<JPanel[]>();
	
	@SuppressWarnings("unchecked")
	public static void prepareGui()
	{
		currentColorSetting = colorSettings[1]; // dark_mode
		
		// Menus
		JMenuBar bar = new JMenuBar();
		JMenu file_menu = new JMenu("File");
		JMenu edit_menu = new JMenu("Edit");
		JMenu setting_menu = new JMenu("Settings");
		bar.add(file_menu);
		bar.add(edit_menu);
		bar.add(setting_menu);
		
		// Menu Items
			// File
			JMenuItem file_open    = new JMenuItem("Open");
			JMenuItem file_reload  = new JMenuItem("Reload");
			JMenuItem file_new     = new JMenuItem("New notes");
			JMenuItem file_save    = new JMenuItem("Save");
			JMenuItem file_save_as = new JMenuItem("Save as");
			JMenuItem file_import  = new JMenuItem("Import file");
			JMenuItem file_export  = new JMenuItem("Export file");
			//JMenuItem file_pdf     = new JMenuItem("Export as PDF");
			
			// Edit
			edit_enabled              = new JCheckBoxMenuItem("Edit mode");
			edit_add                  = new JMenu("add Column");
			edit_remove               = new JMenu("remove Column");
			JMenuItem edit_abbr_edit  = new JMenuItem("Abbreviations settings");
			
			// Settings Menu
			JRadioButtonMenuItem settings_dark_mode  = new JRadioButtonMenuItem("Dark mode");
			JRadioButtonMenuItem settings_light_mode = new JRadioButtonMenuItem("Light mode");
			JRadioButtonMenuItem settings_custom     = new JRadioButtonMenuItem("Custom");
			JMenuItem settings_custom_change         = new JMenuItem("Modify Custom");
		
		// Action Listeners
			// File
			file_open   .addActionListener( e -> { FileOperaitons.selectNotesFile(); Logic.readAndDisplayNotes();} );
			file_reload .addActionListener( e -> { keepGuiSize = false; edit_enabled.setSelected(false); inEditMode = false; Logic.readAndDisplayNotes(); } );
			file_new    .addActionListener( e -> { FileOperaitons.createNewFile(); } );
			file_save   .addActionListener( e -> { FileOperaitons.saveFile(); } );
			file_save_as.addActionListener( e -> { FileOperaitons.saveAsFile(); } );
			//file_pdf    .addActionListener( e -> { FileOperaitons.exportAsPdf(); } );
			file_import .addActionListener( e -> { FileOperaitons.importFile(); arrangeContent(); spaceColums(); } );
			file_export .addActionListener( e -> { FileOperaitons.exportFile(); } );
			
			// Edit
			edit_enabled    .addActionListener(e -> { updateEditMode(edit_enabled); } );
			edit_abbr_edit  .addActionListener(e -> { Logic.getAbbreviationSettings(FileOperaitons.fileAbbreviations, (ArrayList<String[]>) Logic.abbreviationsList.clone() ); } );
			
			// Settings
			settings_light_mode   .addActionListener(e -> { currentColorSetting = colorSettings[0]; applyLightingMode(); } );
			settings_dark_mode    .addActionListener(e -> { currentColorSetting = colorSettings[1]; applyLightingMode(); } );
			settings_custom       .addActionListener(e -> { currentColorSetting = colorSettings[2]; applyLightingMode(); } );
			settings_custom_change.addActionListener(e -> { changeCustomLightingSettings(); } );
			
		// Shortcuts
			// File
			file_open   .setAccelerator(KeyStroke.getKeyStroke("control O") );
			file_reload .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0) );
			file_save   .setAccelerator(KeyStroke.getKeyStroke("control S") );
			file_save_as.setAccelerator(KeyStroke.getKeyStroke("control alt S") );
			
		// Filling Menus
			// Fill File Menu
			file_menu.add(file_open);
			file_menu.add(file_reload);
			file_menu.add(file_new);
			file_menu.add(file_save);
			file_menu.add(file_save_as);
			//file_menu.add(file_pdf);
			file_menu.addSeparator();
			file_menu.add(file_import);
			file_menu.add(file_export);
			
			// Edit
			edit_menu.add(edit_enabled);
			edit_menu.add(edit_add);
			edit_menu.add(edit_remove);
			edit_menu.addSeparator();
			edit_menu.add(edit_abbr_edit);
			
			// Settings
			ButtonGroup lighting_group = new ButtonGroup();
			settings_dark_mode.setSelected(true);
			lighting_group.add(settings_light_mode);
			lighting_group.add(settings_dark_mode);
			lighting_group.add(settings_custom);
			
			setting_menu.add(settings_dark_mode);
			setting_menu.add(settings_light_mode);
			setting_menu.add(settings_custom);
			setting_menu.addSeparator();
			setting_menu.add(settings_custom_change);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout() );
		
		scrollPane = new JScrollPane(mainPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.setBackground(currentColorSetting.background);
		//scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		window = new JFrame();
		window.setJMenuBar(bar);
		window.add(scrollPane);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(MouseAdapters.getOnCloseAdapter() );
		window.setTitle("");
		window.pack();
		window.setVisible(true);

		if (FileOperaitons.fileDirectoryNotes.equals("") || FileOperaitons.fileNameNotes.equals("") )
			FileOperaitons.selectNotesFile();
		else
			window.setTitle(FileOperaitons.fileNameNotes.replace('_', ' ') );
		
		sectionManagerDialog = new JDialog(window);
		sectionManagerDialog.setTitle("Section Manager");
		sectionManagerDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		sectionManagerDialog.setVisible(false);
	}
	
	public static void arrangeContent()
	{
		getAddRemoveColumnsMenuItems();
		
		Logic.missingImagesMessage = "";
		
		height = scrollPane.getHeight();
		scrollValue = scrollPane.getVerticalScrollBar().getValue();
		contentRearraged = true;
		
		ArrayList<String> sections_list = new ArrayList<String>();
		ArrayList<Integer> sectionIndices_list = new ArrayList<Integer>();
		
		sectionPanelsList.clear();
		spacingPanelsList.clear();
		sectionConstraints.clear();
		cellPanelsList.clear();
		
		if (Logic.content != null)
		{
			row = 0;
			
			while (row < Logic.content.length)
			{
				JPanel sub_panel = new JPanel(new GridBagLayout() );
				sub_panel.setOpaque(false);
				GridBagConstraints gbc = new GridBagConstraints();
				sectionPanelsList.add(sub_panel);
				sectionConstraints.add(gbc);
				Subsection subsection = Logic.getSubsection(sub_panel, gbc);
				cellPanelsList.add(subsection.content);
				sections_list.add(subsection.title);
				sectionIndices_list.add(subsection.startIndex);
			}
			
			Logic.sections = sections_list.toArray(new String[sections_list.size() ] );
			Logic.sectionIndices = sectionIndices_list.toArray(new Integer[sectionIndices_list.size() ] );
			
			if (inEditMode) updateSectionManagerDialog();
			
			window.remove(scrollPane);
			
			mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS) );
			mainPanel.setBackground(currentColorSetting.background);
			
			for (JPanel sub_panel : sectionPanelsList)
				mainPanel.add(sub_panel);
			
			scrollPane = new JScrollPane(mainPanel,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setBackground(currentColorSetting.background);
				scrollPane.getVerticalScrollBar().setUnitIncrement(16);
			
			window.add(scrollPane);
			window.pack();
			window.setVisible(true);
			window.repaint();
		}
	}

	public static void spaceColums()
	{
		if (!contentRearraged)
		{
			height = scrollPane.getHeight();
			scrollValue = scrollPane.getVerticalScrollBar().getValue();
		}
		
		// resetting all spacing
		if (!spacingPanelsList.isEmpty() )
		{
			for (int section = 0; section < sectionPanelsList.size(); section ++)
				for (int col = 0; col < Logic.maxRowLength + 2; col ++)
					sectionPanelsList.get(section).remove(spacingPanelsList.get(section)[col] );
			spacingPanelsList.clear();
		}
		scrollPane.setPreferredSize(null);
		
		// redrawing so update size
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		// determining the maximal width for each column
		int[] max_widths = new int[Logic.maxRowLength + 2];
		for (JPanel[][] sub_panel : cellPanelsList)
			for (int col = 0; col < Logic.maxRowLength + 2; col ++)
				if (max_widths[col] < sub_panel[0][col].getWidth() )
					max_widths[col] = sub_panel[0][col].getWidth();
		
		// adding a row of JPanel with the determined width for each section
		for (int section_ind = 0; section_ind < sectionPanelsList.size(); section_ind ++)
		{
			JPanel[] panels = new JPanel[Logic.maxRowLength + 2];
			JPanel section = sectionPanelsList.get(section_ind);
			GridBagConstraints gbc = sectionConstraints.get(section_ind);
			gbc.gridy = -2;
			for (int col = 0; col < Logic.maxRowLength + 2; col ++)
			{
				JPanel panel = new JPanel();
				panel.setPreferredSize(new Dimension(max_widths[col], 0) );
				panel.setMaximumSize(new Dimension(max_widths[col], 0) );
				panel.setSize(new Dimension(max_widths[col], 0) );
				panels[col] = panel;
				gbc.gridx = col;
				section.add(panel, gbc);
			}
			spacingPanelsList.add(panels);
		}
		
		scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth() + 100, keepGuiSize ? height : scrollPane.getHeight() ) );
		//scrollPane.setMaximumSize(new Dimension(scrollPane.getWidth() + 100, keepGuiSize ? height : scrollPane.getHeight() ) );
		
		window.pack();
		scrollPane.getVerticalScrollBar().setValue(scrollValue);
		
		keepGuiSize = true;
		contentRearraged = false;
		
		if (!Logic.missingImagesMessage.equals("") )
			showMissingImagesMessage();
		Logic.missingImagesMessage = "";
		Logic.creatMissingImagesMessage = false;
	}
	
	public static void getAddRemoveColumnsMenuItems()
	{
		edit_add.removeAll();
		edit_remove.removeAll();
		
		for (int col = 0; col < Logic.content[0].length; col ++)
		{
			JMenuItem remove = new JMenuItem("Remove " + getNumeral(col + 1) + " column");
			remove.addActionListener(MouseAdapters.removeContentCol(col) );
			edit_remove.add(remove);
			
			JMenuItem add = new JMenuItem(col == 0 ? "Add before 1st column" : "Add between " + getNumeral(col) + " and " + getNumeral(col + 1) + " column");
			add.addActionListener(MouseAdapters.addContentCol(col) );
			edit_add.add(add);
		}
		JMenuItem add = new JMenuItem("Add after " + getNumeral(Logic.content[0].length) + " column");
		add.addActionListener(MouseAdapters.addContentCol(Logic.content[0].length) );
		edit_add.add(add);
	}
	
	public static String getNumeral(int num)
	{
		switch (num) {
			case 1: return "1st";
			case 2: return "2nd";
			case 3: return "3rd";
			default: return num + "th";
		}
	}

	public static void repaint()
	{
		//scrollPane.validate();
		window.pack();
		window.repaint();
	}
	
	public static void showMissingImagesMessage()
	{
		JDialog missing_dialog = new JDialog(window);
		missing_dialog.setModal(true);
		missing_dialog.setTitle("Missing images");
		//missing_dialog.setUndecorated(true);
		
		// main panel
		JPanel missing_panel = new JPanel();
		missing_panel.setLayout(new BoxLayout(missing_panel, BoxLayout.Y_AXIS) );
		missing_panel.setBackground(currentColorSetting.background);
		missing_panel.setBorder(BorderFactory.createLineBorder(currentColorSetting.text, 2) );
		
		// title
			JLabel title = new JLabel("Missing images");
			title.setForeground(currentColorSetting.text);
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			title.setFont(new Font("MonoSpaced", Font.PLAIN, 15) );
			missing_panel.add(title);
		
		// message panel
			JTextArea message = new JTextArea("The following images are missing in your 'Images' folder:" + Logic.missingImagesMessage);
			message.setBackground(currentColorSetting.background);
			message.setForeground(currentColorSetting.text);
			message.setEditable(false);
			JScrollPane scroll_pane = new JScrollPane(message,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll_pane.setBackground(currentColorSetting.background);
			scroll_pane.setBorder(BorderFactory.createEmptyBorder() );
		
		// controls panel
			JPanel controls_panel = new JPanel(new FlowLayout(FlowLayout.CENTER) );
			controls_panel.setBackground(currentColorSetting.background);
			JButton open_folder = new JButton("Open Image folder");
			JButton close = new JButton("OK");
			
			open_folder.addActionListener(e -> {
					try
					{
						Desktop.getDesktop().open(new File("Images\\"));
					} catch (IOException e1)
					{
						System.out.println("Error while opening 'Images' directory!");
					}
				});
			close.addActionListener(e -> { missing_dialog.dispose(); } );
			
			controls_panel.add(open_folder);
			controls_panel.add(close);
		
		// filling panels
		missing_panel.add(scroll_pane);
		missing_panel.add(controls_panel);
		missing_dialog.add(missing_panel);
		
		missing_dialog.pack();
		if (missing_dialog.getHeight() > window.getHeight() - 150)
			missing_dialog.setPreferredSize(new Dimension(missing_dialog.getWidth() + 20, window.getHeight() - 150) );
		missing_dialog.pack();
		setLocation(missing_dialog);
		missing_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		missing_dialog.setVisible(true);
	}

	public static void updateSectionManagerDialog()
	{
		sectionManagerDialog.getContentPane().removeAll();
		
		sectionManagerPanel = new JPanel();
		sectionManagerPanel.setBackground(currentColorSetting.background);
		sectionManagerPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, currentColorSetting.text) );
		JPanel inner_panel = new JPanel(new GridBagLayout() );
		inner_panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 2));
		inner_panel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		int section_index = 0;
		
		while (section_index < Logic.sections.length)
		{
			gbc.gridx = 0;
			inner_panel.add(getAddRemoveSectionControl(section_index, Logic.sectionIndices[section_index], false), gbc);
			
			gbc.gridx = 1;
			JLabel label = new JLabel(Logic.sections[section_index] );
			label.setForeground(currentColorSetting.text);
			label.addMouseListener(MouseAdapters.getSectionTitleEdit(label,Logic.sectionIndices[section_index] ) );
			label.setBorder(BorderFactory.createMatteBorder(section_index == 0 ? 1 : 0, 1, 1, 1, currentColorSetting.border) );
			inner_panel.add(label, gbc);
			sectionLabels.add(label);
			//System.out.println(gbc.gridy + ", " + Logic.sectionIndices[section_index] + ", " + Logic.sections[section_index] );
			
			section_index ++;
			gbc.gridy ++;
		}
		
		gbc.gridx = 0;
		inner_panel.add(getAddRemoveSectionControl(section_index, Logic.content.length, true), gbc);
		
		sectionManagerPanel.add(inner_panel);
		sectionManagerDialog.add(sectionManagerPanel);

		sectionManagerDialog.pack();
		if (sectionManagerDialog.getHeight() > window.getHeight() - 150)
			sectionManagerDialog.setPreferredSize(new Dimension(sectionManagerDialog.getWidth() + 20, window.getHeight() - 150) );
		sectionManagerDialog.pack();
		sectionManagerDialog.setVisible(true);
	}
	
	public static JPanel getAddRemoveSectionControl(int current_section, int current_row, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		//Color color = inEditMode ? currentColorSetting.text : currentColorSetting.background;
		Color color = currentColorSetting.text;
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(MouseAdapters.getAddSectionControl(current_row) );
		
		labelsBackgroundcolorTextcolor.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(color);
			remove.addMouseListener(MouseAdapters.getRemoveSectionControl(current_row) );
			
			labelsBackgroundcolorTextcolor.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	public static void changeCustomLightingSettings()
	{
		JDialog options = new JDialog(window);
		options.setModal(true);
		options.setTitle("Modify custom lighting settings");
		
		JPanel outer_panel = new JPanel();
		outer_panel.setBackground(Gui.currentColorSetting.background);
		outer_panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Gui.currentColorSetting.text) );
		
		JPanel options_panel = new JPanel();
		options_panel.setLayout(new BoxLayout(options_panel, BoxLayout.Y_AXIS) );
		options_panel.setBackground(currentColorSetting.background);
		
		JPanel color_settings_panel = new JPanel();
		color_settings_panel.setLayout(new GridBagLayout() );
		color_settings_panel.setOpaque(false);
		Logic.fillColorSettingsPane(color_settings_panel, colorSettings[2] );
		options_panel.add(color_settings_panel);
		
		JPanel controls = new JPanel();
		controls.setOpaque(false);
		
		JButton apply = new JButton("Confirm and apply");
		JButton confirm = new JButton("Confirm");
		JButton cancel = new JButton("Cancel");
		
		apply.addActionListener(e -> {Logic.updateCustomColorSettings(options); applyLightingMode(); } );
		confirm.addActionListener(e -> {Logic.updateCustomColorSettings(options); } );
		cancel.addActionListener(e -> {options.dispose(); } );
		
		controls.add(apply);
		controls.add(confirm);
		controls.add(cancel);
		
		options_panel.add(controls);
		outer_panel.add(options_panel);
		
		options.add(outer_panel);
		options.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		options.pack();
		
		options.setPreferredSize(new Dimension(50 + options.getSize().width, options.getSize().height) );
		options.pack();
		setLocation(options);
		options.setVisible(true);
		options.repaint();
	}
	
	public static void applyLightingMode()
	{
		// backgrounds
		window.setBackground(currentColorSetting.background);
		scrollPane.setBackground(currentColorSetting.background);
		mainPanel.setBackground(currentColorSetting.background);
		
		// text for cells
		for (JLabel label : labelsTextcolor) label.setForeground(currentColorSetting.text);
		
		// text for add-remove-controls
		for (JLabel label : labelsBackgroundcolorTextcolor) label.setForeground(inEditMode ? currentColorSetting.text : currentColorSetting.background);
		
		// border color for sections
		for (JPanel panel : sectionPanelsList)
			if (panel.getBorder() != null)
				((TitledBorder) panel.getBorder() ).setTitleColor(currentColorSetting.text);
		
		// border for cells
		for (JPanel cell : cells)
			cell.setBorder(new MatteBorder( ((MatteBorder) cell.getBorder() ).getBorderInsets(), currentColorSetting.border) );
		
		// border for section label cells
		for (JLabel label : sectionLabels)
		{
			label.setBorder(new MatteBorder( ((MatteBorder) label.getBorder() ).getBorderInsets(), currentColorSetting.border) );
			label.setForeground(currentColorSetting.text);
		}
		
		// background and border for sectionManagerDialog
		if (sectionManagerPanel!= null)
		{
			sectionManagerPanel.setBackground(currentColorSetting.background);
			sectionManagerPanel.setBorder(new MatteBorder( ((MatteBorder) sectionManagerPanel.getBorder() ).getBorderInsets(), currentColorSetting.text) );
		}
	}
	
	public static void updateEditMode(JCheckBoxMenuItem check_box)
	{
		inEditMode = check_box.isSelected();
		if (inEditMode)
		{
			for (JLabel label : labelsBackgroundcolorTextcolor) label.setForeground(currentColorSetting.text);
			updateSectionManagerDialog();
			setLocation(sectionManagerDialog);
		}
		else
		{
			for (JLabel label : labelsBackgroundcolorTextcolor) label.setForeground(currentColorSetting.background);
			sectionManagerDialog.setVisible(false);
		}
		for (JLabel label : labelsHideUnhide) label.setVisible(inEditMode);
		spaceColums();
	}

	public static void unsavedChangesDialog()
	{
		JDialog save_dialog = new JDialog(window);
		save_dialog.setModal(true);
		save_dialog.setTitle("Warning");
		//save_dialog.setBackground(currentColorSetting.background);
		
		save_dialog.setLayout(new BoxLayout(save_dialog.getContentPane(), BoxLayout.Y_AXIS) );
		JLabel label = new JLabel("There are unsaved changes!");
		label.setAlignmentX(1);
		//label.setBackground(currentColorSetting.background);
		//label.setForeground(currentColorSetting.text);
		save_dialog.add(label);
		
		JButton save_Button = new JButton("Save and close");
		JButton discard_button = new JButton("Discard changes");
		JButton cancel_button = new JButton("Cancel");
		
		save_Button.addActionListener(e -> { FileOperaitons.saveFile(); save_dialog.dispose(); exit(); } );
		discard_button.addActionListener(e -> { save_dialog.dispose(); exit(); } );
		cancel_button.addActionListener(e -> { save_dialog.dispose(); } );
		
		JPanel save_panel = new JPanel(new FlowLayout() );
		save_panel.add(save_Button);
		save_panel.add(discard_button);
		save_panel.add(cancel_button);
		//save_panel.setBackground(currentColorSetting.background);
		
		save_dialog.add(save_panel);
		save_dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		save_dialog.setTitle("Warning");
		save_dialog.pack();
		save_dialog.setLocationRelativeTo(window);
		save_dialog.setVisible(true);
	}

	public static void setLocation(Container container)
	{
		setLocation(container, 1000);
	}
	
	public static void setLocation(Container container, int max_height_rel_to_window)
	{
		Dimension dim_container  = container.getSize();
		Dimension dim_window = window.getSize();
		
		if (dim_container.height > dim_window.height + max_height_rel_to_window)
		{
			dim_container.height = dim_window.height + max_height_rel_to_window;
			container.setPreferredSize(dim_container);
		}
		
		container.setLocation(window.getLocation().x + (dim_window.width - dim_container.width)/2, window.getLocation().y + (dim_window.height - dim_container.height)/2);
	}

	public static void exit()
	{
		FileOperaitons.writeSettingsFile();
		System.exit(0);
	}
}
