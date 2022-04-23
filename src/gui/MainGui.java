package gui;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
	static Font titleFont = new Font("Serif", Font.PLAIN, 15);
	
	private static SectionManagerDialog sectionManagerDialog;
	public static CellEditDialog cellEditDialog;
	
	public static JFrame window;
	public static JScrollPane scrollPane;
	public static JPanel mainPanel;
	
	public static boolean inEditMode = false;
	public static boolean keepGuiSize = false;
	public static boolean contentRearraged = false;
	private static int height = 0;
	private static int scrollValue = 0;
	
	public static ArrayList<Section> sectionsList = new ArrayList<Section>();
	
	public static Dimension screensize;
	
	public static String currentVersionTag = "v3.1";
	
	private static void prepareGui()
	{
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		//ColorSettings.selectColorSettings(1); // dark_mode
		
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
	}
	
	public static void arrangeContent()
	{
		MenuItems.getAddRemoveColumnsMenuItems();
		
		PopupAlerts.resetMissingImagesMessage();
		
		height = scrollPane.getHeight();
		scrollValue = scrollPane.getVerticalScrollBar().getValue();
		contentRearraged = true;
		
		if (inEditMode) sectionManagerDialog.updateSectionManagerDialog();
		
		window.remove(scrollPane);
		window.setPreferredSize(null);
		window.setTitle(FileOperations.getWindowTitle() );
		
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
	
	static void reloadImages()
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
					FileOperations.setDefaulSettings();
					prepareGui();
					FileOperations.readSettingsFile();
					sectionManagerDialog = new SectionManagerDialog();
					cellEditDialog = new CellEditDialog();
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
		if (fatal)
		{
			dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e)
				{
					dialog.dispose();
					exit();
				}
			});
		}
		
		JPanel error_panel = new JPanel();
		error_panel.setLayout(new BoxLayout(error_panel, BoxLayout.PAGE_AXIS) );
		error_panel.setBorder(GuiHelper.getDialogBorder() );
		error_panel.setOpaque(true);
		error_panel.setBackground(ColorSettings.getBackgroundColor() );
		
		for (String line : error_text.split(Pattern.quote("\n") ) )
			error_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors(line, GuiHelper.LEFT) );
		if (fatal)
			error_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("The programm will exit!", GuiHelper.LEFT) );
		
		dialog.add(error_panel);
		dialog.pack();
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
	}
	
	public static void addSection(int section_index, Section new_section)
	{
		sectionsList.add(section_index, new Section("new section") );
		arrangeContent();
		spaceColums();
		sectionManagerDialog.updateSectionManagerDialog();
	}
	
	public static void removeSection(int section_index)
	{
		arrangeContent();
		spaceColums();
		sectionManagerDialog.updateSectionManagerDialog();
	}
	
	public static void renameSection(int section_index, String new_title)
	{
		sectionsList.get(section_index).setTitle(new_title);
		arrangeContent();
		spaceColums();
		sectionManagerDialog.updateSectionManagerDialog();
	}

	static void readAndDisplayNotes()
	{
		FileOperations.readNotesFile();
		arrangeContent();
		spaceColums();
		FileOperations.unsavedChanges = false;
	}

	public static void exit()
	{
		FileOperations.writeSettingsFile();
		LiveSplitConnection.shotDownAPI();
		Hotkeys.shutDownHotkeys();
		while(!LiveSplitConnection.readyToExit() );
		System.exit(0);
	}
	
	public static void updateLightingSettings()
	{
		window.setBackground(ColorSettings.getBackgroundColor() );
		scrollPane.setBackground(ColorSettings.getBackgroundColor() );
		mainPanel.setBackground(ColorSettings.getBackgroundColor() );
		
		sectionManagerDialog.updateLightingMode();
		
		cellEditDialog.updateLightingSettings();
		
		for (Section section : sectionsList)
			section.updateLightingSettings();
	}

	static void updateEditMode(JCheckBoxMenuItem check_box)
	{
		inEditMode = check_box.isSelected();
		for (Section section : sectionsList)
			section.updateEditMode();
		
		sectionManagerDialog.updateEditMode();
		
		if (inEditMode)
		{
		}
		else
		{
			cellEditDialog.hideEditDialog();
		}
		
		spaceColums();
	}
	
	public static void reset()
	{
		sectionsList.clear();
		
		mainPanel.removeAll();
	}
}
