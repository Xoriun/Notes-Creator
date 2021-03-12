import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

public class Gui {
	
	public static String fileDirectory = "";
	public static String fileName = "";
	public static Font font = new Font("Serif", Font.PLAIN, 20);
	
	private static JFrame window;
	private static JScrollPane scrollPane;
	private static JPanel mainPanel;
	private static JDialog sectionManagerDialog;
	
	public final static int ImageSize = 30;
	public static boolean inEditMode = false;
	
	public static int row = 0;
	
	public static boolean titleChaged = true;
	public static ColorSetting[] colorSettings;
	public static ColorSetting currentColorSetting;
	
	public static Set<JLabel> labels = new HashSet<JLabel>();
	public static ArrayList<JPanel> sectionPanelsList;
	public static ArrayList<GridBagConstraints> sectionConstraints;
	public static Set<JPanel> cells = new HashSet<JPanel>();
	public static ArrayList<JPanel[][]> cellPanelsList;
	public static ArrayList<JLabel> addRemoveRowControlsList = new ArrayList<JLabel>();
	
	public Gui()
	{
		colorSettings = Logic.getColorSettings();
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
		
		// File actions
		file_open.addActionListener(   e -> { getFile(); refreshView();} );
		file_reload.addActionListener( e -> { titleChaged = false; refreshView(); } );
		file_save.addActionListener(   e -> { Logic.saveFile(); } );
		file_edit.addActionListener(   e -> { if (inEditMode) disableEditMode(file_edit); else enableEditMode(file_edit); } );
		
		// Fill File Menu
		file_menu.add(file_open);
		file_menu.add(file_reload);
		file_menu.add(file_edit);
		file_menu.add(file_save);
		
		// Settings Menu
		JRadioButtonMenuItem settings_dark_mode  = new JRadioButtonMenuItem("Dark mode");
		JRadioButtonMenuItem settings_light_mode = new JRadioButtonMenuItem("Light mode");
		JRadioButtonMenuItem settings_custom = new JRadioButtonMenuItem("Custom");
		JMenuItem settings_custom_change = new JMenuItem("Modify Custom");
		
		// Settings actions
		settings_light_mode.addActionListener(		e -> { currentColorSetting = colorSettings[0]; updateLightingMode(); } );
		settings_dark_mode.addActionListener(			e -> { currentColorSetting = colorSettings[1]; updateLightingMode(); } );
		settings_custom.addActionListener(				e -> { currentColorSetting = colorSettings[2]; updateLightingMode(); } );
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
		window.addWindowListener(unsavedChangesDialog() );
		window.setTitle("");
		window.pack();
		window.setVisible(true);
		
		getFile();
		
		sectionManagerDialog = new JDialog(window);
		sectionManagerDialog.setTitle("Section Manager");
		sectionManagerDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		sectionManagerDialog.setVisible(false);
	}
	
	public static WindowAdapter unsavedChangesDialog()
	{
		return new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (Logic.unsavedChanges)
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
					
					JPanel save_panel = new JPanel(new FlowLayout() );
					save_panel.add(getSaveButton(save_dialog) );
					save_panel.add(getDiscardButton(save_dialog) );
					save_panel.add(getCancelButton(save_dialog) );
					//save_panel.setBackground(currentColorSetting.background);
					
					save_dialog.add(save_panel);
					save_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					save_dialog.setTitle("Warning");
					save_dialog.pack();
					save_dialog.setLocationRelativeTo(window);
					save_dialog.setVisible(true);
				}
				else
					exit();
			}
		};
	}
	
	public static void updateSectionManagerDialog()
	{
		sectionManagerDialog.getContentPane().removeAll();
		
		JPanel section_manager_panel = new JPanel(new GridBagLayout() );
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		int section = 0;
		
		while (section < Logic.sections.length)
		{
			gbc.gridx = 0;
			section_manager_panel.add(getAddRemoveSectionControl(section, Logic.sectionIndices[section], false), gbc);
			
			gbc.gridx = 1;
			JLabel label = new JLabel(Logic.sections[section] );
			label.addMouseListener(getSectionTitleEdit(label,Logic.sectionIndices[section] ) );
			label.setBorder(BorderFactory.createMatteBorder(section == 0 ? 1 : 0, 1, 1, 1, colorSettings[0].border) );
			section_manager_panel.add(label, gbc);
			System.out.println(gbc.gridy + ", " + Logic.sectionIndices[section] + ", " + Logic.sections[section] );
			
			section ++;
			gbc.gridy ++;
		}
		
		gbc.gridx = 0;
		section_manager_panel.add(getAddRemoveSectionControl(section, Logic.content.length, true), gbc);
		
		sectionManagerDialog.add(section_manager_panel);

		sectionManagerDialog.pack();
		sectionManagerDialog.setVisible(true);
	}
	
	public static MouseInputAdapter getSectionTitleEdit(JLabel label, int current_row)
	{
		return new MouseInputAdapter() {
			@Override
      public void mouseClicked(MouseEvent e)
			{
				String newText = JOptionPane.showInputDialog(null, "Set the section title!", label.getText() );
				if (newText != null)
				{
					label.setText(newText);
					Logic.content[current_row][0] = "---" + newText + "---";
					arrangeContent(Logic.content);
					draw();
					updateSectionManagerDialog();
				}
			}
		};
	}
	
	public static JPanel getAddRemoveSectionControl(int current_section, int current_row, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		//Color color = inEditMode ? currentColorSetting.text : currentColorSetting.background;
		Color color = colorSettings[0].text;
		
		JLabel add = new JLabel(" + ");
		add.setForeground(color);
		add.addMouseListener(new MouseInputAdapter() {
																@Override
																public void mouseClicked(MouseEvent e)
																{
																	if (inEditMode)
																	{
																		Logic.unsavedChanges = true;
																		Logic.addContentLine(current_row);
																		Logic.content[current_row][0] = "---New section---";
																		arrangeContent(Logic.content);
																		draw();
																		updateSectionManagerDialog();
																	}
																} } );
		
		addRemoveRowControlsList.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(color);
			remove.addMouseListener(new MouseInputAdapter() {
																	@Override
																	public void mouseClicked(MouseEvent e)
																	{
																		if (inEditMode)
																		{
																			Logic.unsavedChanges = true;
																			Logic.removeContentLine(current_row);
																			arrangeContent(Logic.content);
																			draw();
																			updateSectionManagerDialog();
																		}
																	} } );
			
			addRemoveRowControlsList.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	public static void exit()
	{
		System.exit(0);
	}
	
	public static JButton getSaveButton(JDialog frame)
	{
		JButton save_Button = new JButton("Save and close");
		save_Button.addActionListener(e -> { Logic.saveFile(); frame.dispose(); exit(); } );
		return save_Button;
	}
	
	public static JButton getDiscardButton(JDialog frame)
	{
		JButton discard_button = new JButton("Discard changes");
		discard_button.addActionListener(e -> { frame.dispose(); exit(); } );
		return discard_button;
	}
	
	public static JButton getCancelButton(JDialog frame)
	{
		JButton cancel_button = new JButton("Cancel");
		cancel_button.addActionListener(e -> { frame.dispose(); } );
		return cancel_button;
	}
	
	public static void repaint()
	{
		mainPanel.validate();
		window.pack();
		window.repaint();
	}
	
	public static void enableEditMode(JMenuItem file_edit)
	{
		inEditMode = true;
		for (JLabel label : addRemoveRowControlsList) label.setForeground(currentColorSetting.text);
		file_edit.setText("View Mode");
		updateSectionManagerDialog();
	}
	
	public static void disableEditMode(JMenuItem file_edit)
	{
		inEditMode = false;
		for (JLabel label : addRemoveRowControlsList) label.setForeground(currentColorSetting.background);
		file_edit.setText("Edit Mode");
		sectionManagerDialog.setVisible(false);
	}
	
	public void refreshView()
	{
		Logic.getContentFromFile(fileDirectory + fileName);
		arrangeContent(Logic.content);
		draw();
	}
	
	public static void getFile()
	{
		FileDialog dialog = new FileDialog(new Frame(), "Select File to Open");
		dialog.setMode(FileDialog.LOAD);
		dialog.setVisible(true);
		
		fileDirectory = dialog.getDirectory();
		fileName = dialog.getFile();
		
		String new_title = fileName.replace('_', ' ');
		titleChaged = !new_title.equals(window.getTitle() );
		window.setTitle(new_title);
	}
	
	public static void arrangeContent(String[][] content)
	{
		ArrayList<String> sections_list = new ArrayList<String>();
		ArrayList<Integer> sectionIndices_list = new ArrayList<Integer>();
		
		sectionPanelsList = new ArrayList<JPanel>();
		sectionConstraints = new ArrayList<GridBagConstraints>();
		cellPanelsList = new ArrayList<JPanel[][]>();
		
		if (content != null)
		{
			row = 0;
			
			while (row < content.length)
			{
				JPanel sub_panel = new JPanel(new GridBagLayout() );
				sub_panel.setOpaque(false);
				GridBagConstraints gbc = new GridBagConstraints();
				sectionPanelsList.add(sub_panel);
				sectionConstraints.add(gbc);
				Subsection subsection = Logic.getSubsection(sub_panel, gbc);
				cellPanelsList.add(subsection.content);
				if (!subsection.title.equals("") )
				{
					sections_list.add(subsection.title);
					sectionIndices_list.add(subsection.startIndex);
				}
			}
			
			Logic.sections = sections_list.toArray(new String[sections_list.size() ] );
			Logic.sectionIndices = sectionIndices_list.toArray(new Integer[sectionIndices_list.size() ] );
			for (int i = 0; i < Logic.sections.length; i ++)
				System.out.println("Section '" + Logic.sections[i] + "' starts at index " + Logic.sectionIndices[i] );
			System.out.println();
			
			if (inEditMode) updateSectionManagerDialog();
		}
	}
	
	public static void draw()
	{
		Dimension old_dimension = scrollPane.getSize();
		window.remove(scrollPane);
		//scrollPane.remove(mainPanel);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS) );
		mainPanel.setBackground(currentColorSetting.background);
		int max_width = Logic.content[0].length;
		
		for (JPanel sub_panel : sectionPanelsList)
			mainPanel.add(sub_panel);
		
		//scrollPane.add(mainPanel);
		//**
		scrollPane = new JScrollPane(mainPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setBackground(currentColorSetting.background);
			scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		//*/
		
		window.add(scrollPane);
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		// determining the maximal width for each column
		int[] max_widths = new int[max_width];
		for (JPanel[][] sub_panel : cellPanelsList)
			for (int col = 0; col < max_width; col ++)
				if (max_widths[col] < sub_panel[0][col + 1].getWidth() )
					max_widths[col] = sub_panel[0][col + 1].getWidth();
		
		// adding a panel with the corresponding width to each column
		for (int sub_panel_index = 0; sub_panel_index < sectionPanelsList.size(); sub_panel_index ++)
		{
			JPanel sub_panel = sectionPanelsList.get(sub_panel_index);
			GridBagConstraints gbc = sectionConstraints.get(sub_panel_index);
			for (int col = 0; col < max_width; col ++)
			{
				gbc.gridx = col + 1;
				JLabel label = new JLabel();
				label.setPreferredSize(new Dimension(max_widths[col], 0) );
				label.setOpaque(false);
				sub_panel.add(label, gbc);
			}
		}
		
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		scrollPane.setPreferredSize(titleChaged ? new Dimension(scrollPane.getWidth() + 100, scrollPane.getHeight() ) : old_dimension);
		//scrollPane.setSize(titleChaged ? new Dimension(scrollPane.getWidth() + 100, scrollPane.getHeight() ) : old_dimension);
		
		window.pack();
		window.setVisible(true);
		window.repaint();
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
		
		apply.addActionListener(e -> {Logic.updateCustomColorSettings(options); updateLightingMode(); } );
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
	
	public static void updateLightingMode()
	{
		window.setBackground(currentColorSetting.background);
		scrollPane.setBackground(currentColorSetting.background);
		mainPanel.setBackground(currentColorSetting.background);
		
		for (JLabel label : labels                  ) label.setForeground(currentColorSetting.text);
		for (JLabel label : addRemoveRowControlsList) label.setForeground(inEditMode ? currentColorSetting.text : currentColorSetting.background);
		for (JPanel panel : sectionPanelsList)
			if (panel.getBorder() != null)
				((TitledBorder) panel.getBorder() ).setTitleColor(currentColorSetting.text);
		for (JPanel cell : cells)
			cell.setBorder(new MatteBorder( ((MatteBorder) cell.getBorder() ).getBorderInsets(), currentColorSetting.border) );
	}
}
