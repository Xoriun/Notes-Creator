package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.*;

import edit.*;
import logic.*;

public class MainGui {
	public static Font font = new Font("Serif", Font.PLAIN, 20);
	static Font titleFont = new Font("Serif", Font.PLAIN, 15);
	
	private static SectionManagerDialog sectionManagerDialog;
	public static CellEditDialog cellEditDialog;
	
	public static JFrame window;
	//public static JFrame content_window;
	public static JScrollPane scrollPane;
	public static JPanel mainPanel;
	
	public static boolean inEditMode = false;
	public static boolean keepGuiSize = false;
	public static boolean contentRearraged = false;
	private static int height = 0;
	private static int scrollValue = 0;
	
	public static ArrayList<Section> sectionsList = new ArrayList<Section>();
	
	public static Dimension screensize;
	
	public static String currentVersionTag = "v3.4";
	
	public final static int moveToSectionAbove = -1;
	public final static int removeSection = 0;
	public final static int moveToSectionBelow = 1;
	
	private static void prepareGui()
	{
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		//ColorSettings.selectColorSettings(1); // dark_mode
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout() );
		
		scrollPane = new JScrollPane(mainPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(ColorSettings.getBackgroundColor() );
		colorPanel.setBorder(GuiHelper.getEmptyBorder() );
		
		window = new JFrame() /*{
			private static final long serialVersionUID = -1303115076804508497L;

			@Override
			public Insets getInsets()
			{
				return new Insets(0, 0, 0, 0);
			}
		}//*/;
		window.setJMenuBar(MenuItems.createMenuBar() );
		window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS) );
		window.add(scrollPane);
		//window.setContentPane(colorPanel);
		window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(MouseAdapters.windowOnCloseAdapter);
		window.setTitle("");
		window.pack();
		window.setVisible(true);
		
		/*
		content_window = new JFrame();
		content_window.setUndecorated(true);
		content_window.setLayout(new BoxLayout(content_window.getContentPane(), BoxLayout.Y_AXIS) );
		content_window.add(scrollPane);
		content_window.setTitle("Notes-creator-capture-frame");
		content_window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		content_window.addWindowListener(MouseAdapters.windowOnCloseAdapter);
		content_window.pack();
		content_window.setVisible(true);
		System.out.println("Main:" + window.getInsets() + ", content:" + content_window.getInsets() );
		//*/
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
		//content_window.remove(scrollPane);
		window.setPreferredSize(null);
		//content_window.setPreferredSize(null);
		window.setTitle(FileOperations.getWindowTitle() );
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS) );
		mainPanel.setBackground(ColorSettings.getBackgroundColor() );
		mainPanel.setOpaque(false);
		
		for (Section section : sectionsList)
			mainPanel.add(section);
		
		scrollPane = new JScrollPane(mainPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		//scrollPane.getViewport().setOpaque(false);
		//scrollPane.setOpaque(false);
		scrollPane.getViewport().setBackground(ColorSettings.getBackgroundColor() );
		
		window.add(scrollPane);
		//content_window.add(scrollPane);
		//content_window.setBackground(new Color(0, 0, 0, 0) );
		window.pack();
		//content_window.pack();
		window.setVisible(true);
		//content_window.setVisible(true);
		resizeWindow();
		window.pack();
		//content_window.pack();
		
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
		//content_window.setPreferredSize(new Dimension(width + 100, height) );
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
		//content_window.pack();
		//content_window.setVisible(true);
		//content_window.repaint();
		//content_window.setMaximumSize(screensize);
		
		// determining the maximal width for each column
		Section.maxWidths = new int[FileOperations.numberOfColumns + 2];
		for (Section section : sectionsList)
			section.determineMaxWidth();
		for (Section section : sectionsList)
			section.addSpacingPanels();
		
		scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth() + 100, keepGuiSize ? height : scrollPane.getHeight() ) );
		
		window.pack();
		//content_window.pack();
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
						e.printStackTrace();
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
	
	public static void removeSection(int section_index, int moveContent)
	{
		if (moveContent < -1 || moveContent > 1)
			throw new IllegalArgumentException("moveContent has to be between -1 and 1, got " + moveContent);
		
		if (moveContent == removeSection)
			sectionsList.remove(section_index);
		else
		{
			ArrayList<Row> rows = sectionsList.remove(section_index).getRows();
			rows.remove(rows.size() - 1);
			
			try
			{
				if (moveContent == moveToSectionAbove)
					sectionsList.get(section_index - 1).addRowsAtEnd(rows);
				else if(moveContent == moveToSectionBelow)
					sectionsList.get(section_index).addRowsAtStart(rows); // the section was already removed, so the next section has the same index as the one that was removed.
			}	catch (IndexOutOfBoundsException e)
			{ return; }
		}
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
