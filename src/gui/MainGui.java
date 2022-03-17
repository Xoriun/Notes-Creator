package gui;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

import edit.CellEditDialog;
import edit.SectionManagerDialog;
import logic.FileOperations;
import logic.Hotkeys;
import logic.LiveSplitConnection;
import logic.MouseAdapters;
import logic.Section;
import logic.Updates;

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
	
	public static Dimension screensize;
	
	public static String currentVersionTag = "v2.11";
	
	public static void prepareGui()
	{
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		ColorSettings.selectColorSettings(1); // dark_mode
		
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
		window.addWindowListener(MouseAdapters.windowOnCloseAdapter);
		window.setTitle("");
		window.pack();
		window.setVisible(true);

		if (FileOperations.fileNotesDirectory.equals("") || FileOperations.fileNotesName.equals("") )
			FileOperations.selectNotesFile();
		else
			window.setTitle(FileOperations.fileNotesName.replace('_', ' ') );
		
		SectionManagerDialog.initializeSectionDialog();
		CellEditDialog.initializeCellEditDialog();
	}
	
	public static void arrangeContent()
	{
		MenuItems.getAddRemoveColumnsMenuItems();
		
		PopupAlerts.resetMissingImagesMessage();
		
		height = scrollPane.getHeight();
		scrollValue = scrollPane.getVerticalScrollBar().getValue();
		contentRearraged = true;
		
		if (inEditMode) SectionManagerDialog.updateSectionManagerDialog();
		
		window.remove(scrollPane);
		window.setPreferredSize(null);
		
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
		resizeWindow();
		window.pack();
		
		sectionsList.forEach(section -> section.setLocation() );
	}
	
	public static void reloadImages()
	{
		PopupAlerts.createMissingImagesMessage = true;
		PopupAlerts.resetMissingImagesMessage();
		
		for (Section section : sectionsList)
			section.reloadImages();
		
		PopupAlerts.showMissingImagesMessageIfNonEmpty();
		PopupAlerts.createMissingImagesMessage = false;
	}
	
	private static void resizeWindow()
	{
		int height = (int) (screensize.height*0.9), width = (int) (screensize.width*0.9);
		if (window.getHeight() < height)
			height = window.getHeight();
		if (window.getWidth() < width)
			width = window.getWidth();
		window.setPreferredSize(new Dimension(width + 100, height) );
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
		
		PopupAlerts.showMissingImagesMessageIfNonEmpty();
		PopupAlerts.resetMissingImagesMessage();
		PopupAlerts.createMissingImagesMessage = false;
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				LiveSplitConnection.initializeLiveSplitconnection();
				Hotkeys.initializeHotkeys();
				
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName() );
				} catch (Exception e) { }
				
				try {
					FileOperations.readSettingsFile();
					prepareGui();
					readAndDisplayNotes();
				} catch (Exception e) {
					try
					{
						e.printStackTrace(new PrintStream(new File("log.txt") ) );
					} catch (FileNotFoundException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				Updates.checkForUpdates(false);
			}
		});
	}
	
	public static void displayErrorAndExit(String error_text, boolean fatal)
	{
		JDialog dialog = new JDialog(window, fatal ? "A fatal" : "An" + " error occured", true);
		
		JPanel error_panel = new JPanel();
		error_panel.setBorder(GuiHelper.getDialogBorder() );
		error_panel.setOpaque(false);
		error_panel.setBackground(ColorSettings.getBackgroundColor() );
		dialog.add(error_panel);
		
		JLabel error_label = GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors(error_text, GuiHelper.LEFT);
		error_panel.add(error_label);
		
		dialog.pack();
		dialog.setVisible(true);
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
		LiveSplitConnection.shotDownAPI();
		while(!LiveSplitConnection.readyToExit() );
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
			CellEditDialog.hideEditDialog();
		}
		for (JLabel label : MainGui.labelsIconsHideWhenNotInEdit) label.setVisible(MainGui.inEditMode);
		MainGui.spaceColums();
	}
	
	public static void reset()
	{
		sectionsList.clear();
		sectionLabels.clear();
		labelsText.clear();
		labelsTextsHideWhenNotInEdit.clear();
		labelsIconsHideWhenNotInEdit.clear();
		
		mainPanel.removeAll();
	}
}
