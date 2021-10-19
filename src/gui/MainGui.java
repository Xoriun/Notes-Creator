package gui;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
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

import edit.CellEditDialog;
import edit.SectionManagerDialog;
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
	
	public static boolean inEditMode = false;
	public static boolean keepGuiSize = false;
	public static boolean contentRearraged = false;
	private static int height = 0;
	public static int scrollValue = 0;
	public static Set<JLabel> sectionLabels = new HashSet<JLabel>();
	public static Set<JLabel> labelsText = new HashSet<JLabel>();
	public static Set<JLabel> labelsTextsHideWhenNotInEdit = new HashSet<JLabel>();
	public static Set<JLabel> labelsIconsHideWhenNotInEdit = new HashSet<JLabel>();
	
	public static ArrayList<Section> sectionsList = new ArrayList<Section>();
	
	private static Dimension screensize;
	
	public static String currentVersionTag = "v2.1";
	
	public static void prepareGui()
	{
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		ColorSettings.selectColorSettings(1); // dark_mode
		UIManager.put("Panel.opaque", Boolean.valueOf(false) );
		
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
		
		SectionManagerDialog.initializeSectionDialog();
		CellEditDialog.initializeCellEditDialog();
	}
	
	public static void arrangeContent()
	{
		MenuItems.getAddRemoveColumnsMenuItems();
		
		PopupAlerts.missingImagesMessage = "";
		
		height = scrollPane.getHeight();
		scrollValue = scrollPane.getVerticalScrollBar().getValue();
		contentRearraged = true;
		
		if (inEditMode) SectionManagerDialog.updateSectionManagerDialog();
		
		window.remove(scrollPane);
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS) );
		mainPanel.setBackground(ColorSettings.getBackgroundColor() );
		
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

	public static void updateEditMode(JCheckBoxMenuItem check_box)
	{
		MainGui.inEditMode = check_box.isSelected();
		if (MainGui.inEditMode)
		{
			for (JLabel label : MainGui.labelsTextsHideWhenNotInEdit) label.setForeground(ColorSettings.getTextColor() );
			SectionManagerDialog.updateSectionManagerDialog();
		}
		else
		{
			for (JLabel label : MainGui.labelsTextsHideWhenNotInEdit) label.setForeground(ColorSettings.getBackgroundColor() );
			SectionManagerDialog.sectionManagerDialog.setVisible(false);
		}
		for (JLabel label : MainGui.labelsIconsHideWhenNotInEdit) label.setVisible(MainGui.inEditMode);
		MainGui.spaceColums();
	}
}
