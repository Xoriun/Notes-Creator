import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class Gui {
	public static Font font = new Font("Serif", Font.PLAIN, 20);
	
	public static JFrame window;
	public static JScrollPane scrollPane;
	public static JPanel mainPanel;
	private static JDialog sectionManagerDialog;
	
	public final static int ImageSize = 30;
	public static boolean inEditMode = false;
	
	public static int row = 0;
	
	public static boolean keepGuiSize = false;
	public static boolean contentRearraged = false;
	private static int height = 0;
	private static int scrollValue = 0;
	public static int columns = 0;
	public static ColorSetting[] colorSettings;
	public static ColorSetting currentColorSetting;
	
	public static ArrayList<JPanel> sectionPanelsList;
	public static ArrayList<GridBagConstraints> sectionConstraints;
	public static Set<JPanel> cells = new HashSet<JPanel>();
	public static Set<JLabel> labelsTextcolor = new HashSet<JLabel>();
	public static Set<JLabel> labelsBackgroundcolorTextcolor = new HashSet<JLabel>();
	public static Set<JLabel> labelsHideUnhide = new HashSet<JLabel>(); 
	public static ArrayList<JPanel[][]> cellPanelsList;
	public static ArrayList<JPanel[]> spacingPanelsList = new ArrayList<JPanel[]>();
	
	public static void prepareGui()
	{
		currentColorSetting = colorSettings[1]; // dark_mode
		
		// Menus
		JMenuBar bar = new JMenuBar();
		JMenu file_menu = new JMenu("File");
		JMenu setting_menu = new JMenu("Settings");
		bar.add(file_menu);
		bar.add(setting_menu);
		
		// File Menu
		JMenuItem file_open   = new JMenuItem("Open");
		JMenuItem file_reload = new JMenuItem("Reload");
		JMenuItem file_edit   = new JMenuItem("Edit Mode");
		JMenuItem file_save   = new JMenuItem("Save");
		//JMenuItem file_pdf    = new JMenuItem("Export as PDF");
		
		// File actions
		file_open  .addActionListener( e -> { FileOperaitons.getFile(); Logic.readAndDisplayNotes();} );
		file_reload.addActionListener( e -> { keepGuiSize = false; Logic.readAndDisplayNotes(); } );
		file_save  .addActionListener( e -> { FileOperaitons.saveFile(); } );
		file_edit  .addActionListener( e -> { if (inEditMode) disableEditMode(file_edit); else enableEditMode(file_edit); } );
		//file_pdf   .addActionListener( e -> { FileOperaitons.exportAsPdf(); } );
		
		// Fill File Menu
		file_menu.add(file_open);
		file_menu.add(file_reload);
		file_menu.add(file_edit);
		file_menu.add(file_save);
		//file_menu.add(file_pdf);
		
		// Settings Menu
		JRadioButtonMenuItem settings_dark_mode  = new JRadioButtonMenuItem("Dark mode");
		JRadioButtonMenuItem settings_light_mode = new JRadioButtonMenuItem("Light mode");
		JRadioButtonMenuItem settings_custom = new JRadioButtonMenuItem("Custom");
		JMenuItem settings_custom_change = new JMenuItem("Modify Custom");
		
		// Settings actions
		settings_light_mode.addActionListener(		e -> { currentColorSetting = colorSettings[0]; applyLightingMode(); } );
		settings_dark_mode.addActionListener(			e -> { currentColorSetting = colorSettings[1]; applyLightingMode(); } );
		settings_custom.addActionListener(				e -> { currentColorSetting = colorSettings[2]; applyLightingMode(); } );
		settings_custom_change.addActionListener( e -> { changeCustomLightingSettings(); } );
		
		ButtonGroup lighting_group = new ButtonGroup();
		settings_dark_mode.setSelected(true);
		lighting_group.add(settings_light_mode);
		lighting_group.add(settings_dark_mode);
		lighting_group.add(settings_custom);
		
		// Fill Settings Menu
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

		if (FileOperaitons.fileDirectory.equals("") || FileOperaitons.fileName.equals("") )
			FileOperaitons.getFile();
		else
			window.setTitle(FileOperaitons.fileName.replace('_', ' ') );
		
		sectionManagerDialog = new JDialog(window);
		sectionManagerDialog.setTitle("Section Manager");
		sectionManagerDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		sectionManagerDialog.setVisible(false);
	}
	
	public static void arrangeContent()
	{
		height = scrollPane.getHeight();
		scrollValue = scrollPane.getVerticalScrollBar().getValue();
		contentRearraged = true;
		
		ArrayList<String> sections_list = new ArrayList<String>();
		ArrayList<Integer> sectionIndices_list = new ArrayList<Integer>();
		
		sectionPanelsList = new ArrayList<JPanel>();
		sectionConstraints = new ArrayList<GridBagConstraints>();
		cellPanelsList = new ArrayList<JPanel[][]>();
		
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
				for (int col = 0; col < columns; col ++)
					sectionPanelsList.get(section).remove(spacingPanelsList.get(section)[col] );
			spacingPanelsList.clear();
		}
		scrollPane.setPreferredSize(null);
		
		// redrawing so update size
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		// determining the maximal width for each column
		int[] max_widths = new int[columns];
		for (JPanel[][] sub_panel : cellPanelsList)
			for (int col = 0; col < columns; col ++)
				if (max_widths[col] < sub_panel[0][col].getWidth() )
					max_widths[col] = sub_panel[0][col].getWidth();
		
		// adding a row of JPanel with the determined width for each section
		for (int section_ind = 0; section_ind < sectionPanelsList.size(); section_ind ++)
		{
			JPanel[] panels = new JPanel[columns];
			JPanel section = sectionPanelsList.get(section_ind);
			GridBagConstraints gbc = sectionConstraints.get(section_ind);
			gbc.gridy = -2;
			for (int col = 0; col < columns; col ++)
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
	}

	public static void repaint()
	{
		//scrollPane.validate();
		window.pack();
		window.repaint();
	}

	public static void updateSectionManagerDialog()
	{
		sectionManagerDialog.getContentPane().removeAll();
		
		JPanel section_manager_panel = new JPanel(new GridBagLayout() );
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		int section_index = 0;
		
		while (section_index < Logic.sections.length)
		{
			gbc.gridx = 0;
			section_manager_panel.add(getAddRemoveSectionControl(section_index, Logic.sectionIndices[section_index], false), gbc);
			
			gbc.gridx = 1;
			JLabel label = new JLabel(Logic.sections[section_index] );
			label.addMouseListener(MouseAdapters.getSectionTitleEdit(label,Logic.sectionIndices[section_index] ) );
			label.setBorder(BorderFactory.createMatteBorder(section_index == 0 ? 1 : 0, 1, 1, 1, colorSettings[0].border) );
			section_manager_panel.add(label, gbc);
			//System.out.println(gbc.gridy + ", " + Logic.sectionIndices[section_index] + ", " + Logic.sections[section_index] );
			
			section_index ++;
			gbc.gridy ++;
		}
		
		gbc.gridx = 0;
		section_manager_panel.add(getAddRemoveSectionControl(section_index, Logic.content.length, true), gbc);
		
		sectionManagerDialog.add(section_manager_panel);

		sectionManagerDialog.pack();
		sectionManagerDialog.setVisible(true);
	}
	
	public static JPanel getAddRemoveSectionControl(int current_section, int current_row, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		//Color color = inEditMode ? currentColorSetting.text : currentColorSetting.background;
		Color color = colorSettings[0].text;
		
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
		
		JPanel options_panel = new JPanel();
		options_panel.setLayout(new BoxLayout(options_panel, BoxLayout.Y_AXIS) );
		
		
		options_panel.add(new JLabel(currentColorSetting.name) );
		
		JPanel color_settings_panel = new JPanel();
		color_settings_panel.setLayout(new GridBagLayout() );
		Logic.fillColorSettingsPane(color_settings_panel, colorSettings[2] );
		options_panel.add(color_settings_panel);
		
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS) );
		
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
		
		options.add(options_panel);
		options.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		options.pack();
		
		options_panel.setPreferredSize(new Dimension(50 + options_panel.getSize().width, options_panel.getSize().height) );
		options.pack();
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
	}

	public static void enableEditMode(JMenuItem file_edit)
	{
		inEditMode = true;
		for (JLabel label : labelsBackgroundcolorTextcolor) label.setForeground(currentColorSetting.text);
		for (JLabel label : labelsHideUnhide) label.setVisible(true);
		file_edit.setText("View Mode");
		updateSectionManagerDialog();
		spaceColums();
	}

	public static void disableEditMode(JMenuItem file_edit)
	{
		inEditMode = false;
		for (JLabel label : labelsBackgroundcolorTextcolor) label.setForeground(currentColorSetting.background);
		for (JLabel label : labelsHideUnhide) label.setVisible(false);
		file_edit.setText("Edit Mode");
		sectionManagerDialog.setVisible(false);
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

	public static void exit()
	{
		FileOperaitons.updateSettingsFile();
		System.exit(0);
	}
}
