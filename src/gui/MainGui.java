package gui;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import logic.FileOperations;
import logic.Hotkeys;
import logic.MouseAdapters;
import logic.Section;

public class MainGui {
	public static Font font = new Font("Serif", Font.PLAIN, 20);
	public static Font titleFont = new Font("Serif", Font.PLAIN, 15);
	
	public static JFrame window;
	public static JScrollPane scrollPane;
	public static JPanel mainPanel;
	private static JDialog sectionManagerDialog;
	static JPanel sectionManagerPanel;
	
	public final static int ImageSize = 30;
	public static boolean inEditMode = false;
	public static boolean keepGuiSize = false;
	public static boolean contentRearraged = false;
	private static int height = 0;
	public static int scrollValue = 0;
	public static Set<JLabel> sectionLabels = new HashSet<JLabel>();
	public static Set<JLabel> labelsTextcolor = new HashSet<JLabel>();
	public static Set<JLabel> labelsBackgroundcolorTextcolor = new HashSet<JLabel>();
	public static Set<JLabel> labelsHideUnhide = new HashSet<JLabel>(); 
	
	public static ArrayList<Section> sectionsList = new ArrayList<Section>();
	
	private static Dimension screensize;
	
	public static void prepareGui()
	{
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		ColorSettings.currentColorSetting = ColorSettings.colorSettingProfiles[1]; // dark_mode
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout() );
		
		scrollPane = new JScrollPane(mainPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		window = new JFrame();
		window.setJMenuBar(MenuItems.createMenuBar() );
		window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS) );
		window.add(scrollPane);
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(MouseAdapters.getWindowOnCloseAdapter() );
		window.setTitle("");
		window.pack();
		window.setMaximumSize(screensize);
		window.setVisible(true);

		if (FileOperations.fileDirectoryNotes.equals("") || FileOperations.fileNameNotes.equals("") )
			FileOperations.selectNotesFile();
		else
			window.setTitle(FileOperations.fileNameNotes.replace('_', ' ') );
		
		sectionManagerDialog = new JDialog(window);
		sectionManagerDialog.setTitle("Section Manager");
		sectionManagerDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		sectionManagerDialog.setVisible(false);
	}
	
	public static void arrangeContent()
	{
		MenuItems.getAddRemoveColumnsMenuItems();
		
		PopupAlerts.missingImagesMessage = "";
		
		height = scrollPane.getHeight();
		scrollValue = scrollPane.getVerticalScrollBar().getValue();
		contentRearraged = true;
		
		if (inEditMode) updateSectionManagerDialog();
		
		window.remove(scrollPane);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS) );
		mainPanel.setBackground(ColorSettings.currentColorSetting.background);
		
		for (Section section : sectionsList)
			mainPanel.add(section);
		
		scrollPane = new JScrollPane(mainPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		window.add(scrollPane);
		window.pack();
		window.setVisible(true);
		window.repaint();
		window.setMaximumSize(screensize);
		
		sectionsList.forEach(e -> e.setLocation() );
	}

	public static void spaceColums()
	{
		if (!contentRearraged)
		{
			height = scrollPane.getHeight();
			scrollValue = scrollPane.getVerticalScrollBar().getValue();
		}
		
		// resetting all spacing
		for (Section section : sectionsList)
			section.removeSpacingPanels();
		scrollPane.setPreferredSize(null);
		
		// redrawing to update size
		window.pack();
		window.setVisible(true);
		window.repaint();
		window.setMaximumSize(screensize);
		
		// determining the maximal width for each column
		Section.maxWidths = new int[FileOperations.numberOfColumns + 2];
		for (Section section : sectionsList)
			section.determineMaxWidth();
		for (Section section : sectionsList)
			section.addSpacingPanels();
		
		scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth() + 100, keepGuiSize ? height : scrollPane.getHeight() ) );
		
		window.pack();
		scrollPane.getVerticalScrollBar().setValue(scrollValue);
		
		keepGuiSize = true;
		contentRearraged = false;
		
		if (!PopupAlerts.missingImagesMessage.equals("") )
			PopupAlerts.showMissingImagesMessage();
		PopupAlerts.missingImagesMessage = "";
		PopupAlerts.creatMissingImagesMessage = false;
	}
	
	public static void updateEditMode(JCheckBoxMenuItem check_box)
	{
		inEditMode = check_box.isSelected();
		if (inEditMode)
		{
			for (JLabel label : labelsBackgroundcolorTextcolor) label.setForeground(ColorSettings.currentColorSetting.text);
			updateSectionManagerDialog();
			PopupAlerts.setLocationToCenter(sectionManagerDialog);
		}
		else
		{
			for (JLabel label : labelsBackgroundcolorTextcolor) label.setForeground(ColorSettings.currentColorSetting.background);
			sectionManagerDialog.setVisible(false);
		}
		for (JLabel label : labelsHideUnhide) label.setVisible(inEditMode);
		spaceColums();
	}

	public static void updateSectionManagerDialog()
	{
		sectionManagerDialog.getContentPane().removeAll();
		
		sectionManagerPanel = new JPanel();
		sectionManagerPanel.setBackground(ColorSettings.currentColorSetting.background);
		sectionManagerPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, ColorSettings.currentColorSetting.text) );
		JPanel inner_panel = new JPanel(new GridBagLayout() );
		inner_panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 2));
		inner_panel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		int section_index = 0;
		
		while (section_index < sectionsList.size() )
		{
			gbc.gridx = 0;
			inner_panel.add(getAddRemoveSectionControl(section_index, false), gbc);
			
			gbc.gridx = 1;
			JLabel label = new JLabel(sectionsList.get(section_index).getTitle() );
			label.setForeground(ColorSettings.currentColorSetting.text);
			label.addMouseListener(MouseAdapters.getEditSectionTitleAdapter(label, section_index) );
			label.setBorder(BorderFactory.createMatteBorder(section_index == 0 ? 1 : 0, 1, 1, 1, ColorSettings.currentColorSetting.border) );
			inner_panel.add(label, gbc);
			sectionLabels.add(label);
			//System.out.println(gbc.gridy + ", " + Abbreviations.sectionIndices[section_index] + ", " + Abbreviations.sections[section_index] );
			
			section_index ++;
			gbc.gridy ++;
		}
		
		gbc.gridx = 0;
		inner_panel.add(getAddRemoveSectionControl(section_index, true), gbc);
		
		sectionManagerPanel.add(inner_panel);
		sectionManagerDialog.add(sectionManagerPanel);

		sectionManagerDialog.pack();
		if (sectionManagerDialog.getHeight() > window.getHeight() - 150)
			sectionManagerDialog.setPreferredSize(new Dimension(sectionManagerDialog.getWidth() + 20, window.getHeight() - 150) );
		sectionManagerDialog.pack();
		sectionManagerDialog.setVisible(true);
	}
	
	public static JPanel getAddRemoveSectionControl(int current_section_index, boolean only_add)
	{
		JPanel control = new JPanel(new GridLayout(only_add ? 1 : 2, 2) );
		
		JLabel add = new JLabel(" + ");
		add.setForeground(ColorSettings.currentColorSetting.text);
		add.addMouseListener(MouseAdapters.getAddSectionAdapter(current_section_index) );
		
		labelsBackgroundcolorTextcolor.add(add);
		control.add(new JLabel());
		control.add(add);
		
		if (! only_add)
		{
			JLabel remove = new JLabel(" - ");
			remove.setForeground(ColorSettings.currentColorSetting.text);
			remove.addMouseListener(MouseAdapters.getRemoveSectionAdapter(current_section_index) );
			
			labelsBackgroundcolorTextcolor.add(remove);
			control.add(remove);
			control.add(new JLabel());
		}
		
		control.setOpaque(false);
		return control;
	}
	
	public static void main(String[] args)
	{
		Logger.getLogger(GlobalScreen.class.getPackage().getName() ).setLevel(Level.OFF);
		
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
	
			System.exit(1);
		}
		
		GlobalScreen.addNativeKeyListener(new Hotkeys() );
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName() );
				} catch (Exception e) { }
				
				FileOperations.readSettingsFile();
				prepareGui();
				readAndDisplayNotes();
			}
		});
	}

	public static void readAndDisplayNotes()
	{
		FileOperations.readNotesFile();
		arrangeContent();
		spaceColums();
	}

	public static void exit()
	{
		FileOperations.writeSettingsFile();
		System.exit(0);
	}
}
