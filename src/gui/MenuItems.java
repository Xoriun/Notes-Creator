package gui;

import java.awt.Desktop;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import logic.FileOperations;
import logic.MouseAdapters;
import logic.SpeedRunMode;
import logic.Updates;

public class MenuItems
{
	static JCheckBoxMenuItem edit_enabled;
	public static JMenu edit_add;
	public static JMenu edit_remove;
	
	public static JMenuBar createMenuBar()
	{
		// Menus
		JMenuBar bar = new JMenuBar();
		JMenu menu_file     = new JMenu("File");
		JMenu menu_edit     = new JMenu("Edit");
		JMenu menu_speedrun = new JMenu("Speedrun");
		JMenu menu_setting  = new JMenu("Settings");
		JMenu menu_about    = new JMenu("About");
		bar.add(menu_file);
		bar.add(menu_edit);
		bar.add(menu_speedrun);
		bar.add(menu_setting);
		bar.add(menu_about);
		
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
			
			// Speedrun
			JCheckBoxMenuItem speedrun_enabled  = new JCheckBoxMenuItem("Enable Speedruning mode");
			JMenuItem speedrun_settings = new JMenuItem("Speedrun settings");
			
			// Settings Menu
			JRadioButtonMenuItem settings_dark_mode  = new JRadioButtonMenuItem("Dark mode");
			JRadioButtonMenuItem settings_light_mode = new JRadioButtonMenuItem("Light mode");
			JRadioButtonMenuItem settings_custom     = new JRadioButtonMenuItem("Custom");
			JMenuItem settings_custom_change         = new JMenuItem("Modify Custom");
			
			// About
			JMenuItem about_github = new JMenuItem("Github page");
			JMenuItem about_update = new JMenuItem("Check for updates");
		
		// Action Listeners
			// File
			file_open   .addActionListener( e -> { FileOperations.selectNotesFile(); MainGui.readAndDisplayNotes();} );
			file_reload .addActionListener( e -> { MainGui.keepGuiSize = false; edit_enabled.setSelected(false); MainGui.inEditMode = false; MainGui.readAndDisplayNotes(); } );
			file_new    .addActionListener( e -> { FileOperations.createNewFile(); } );
			file_save   .addActionListener( e -> { FileOperations.saveFile(); } );
			file_save_as.addActionListener( e -> { FileOperations.saveAsFile(); } );
			//file_pdf    .addActionListener( e -> { FileOperations.exportAsPdf(); } );
			file_import .addActionListener( e -> { FileOperations.importFile(); MainGui.arrangeContent(); MainGui.spaceColums(); } );
			file_export .addActionListener( e -> { FileOperations.exportFile(); } );
			
			// Edit
			edit_enabled    .addActionListener(e -> { MainGui.updateEditMode(edit_enabled); } );
			edit_abbr_edit  .addActionListener(e -> { Abbreviations.showAbbreviationSettingsDialog(); } );
			
			// Speedrun
			speedrun_enabled .addActionListener(e -> { SpeedRunMode.updateSpeedRunMode(speedrun_enabled.isSelected() ); } );
			speedrun_settings.addActionListener(e -> { SpeedRunMode.showSpeedrunSettingsWindow(); } );
			
			// Settings
			settings_light_mode   .addActionListener(e -> { ColorSettings.selectColorSettings(0); ColorSettings.applyLightingMode(); } );
			settings_dark_mode    .addActionListener(e -> { ColorSettings.selectColorSettings(1); ColorSettings.applyLightingMode(); } );
			settings_custom       .addActionListener(e -> { ColorSettings.selectColorSettings(2); ColorSettings.applyLightingMode(); } );
			settings_custom_change.addActionListener(e -> { ColorSettings.changeCustomLightingSettings(); } );
			
			// About
			about_github.addActionListener(action -> {
				try {
	        Desktop.getDesktop().browse(new URL("https://github.com/Xoriun/Notes-Creator").toURI() );
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
			});
			about_update.addActionListener(action -> { Updates.checkForUpdates(true); });
			
		// Shortcuts
			file_open   .setAccelerator(KeyStroke.getKeyStroke("control O") );
			file_reload .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0) );
			file_save   .setAccelerator(KeyStroke.getKeyStroke("control S") );
			file_save_as.setAccelerator(KeyStroke.getKeyStroke("control alt S") );
			
		// Filling Menus
			// Fill File Menu
			menu_file.add(file_open);
			menu_file.add(file_reload);
			menu_file.add(file_new);
			menu_file.add(file_save);
			menu_file.add(file_save_as);
			//file_menu.add(file_pdf);
			menu_file.addSeparator();
			menu_file.add(file_import);
			menu_file.add(file_export);
			
			// Edit
			menu_edit.add(edit_enabled);
			menu_edit.add(edit_add);
			menu_edit.add(edit_remove);
			menu_edit.addSeparator();
			menu_edit.add(edit_abbr_edit);
			
			// Speedrun
			menu_speedrun.add(speedrun_enabled);
			menu_speedrun.add(speedrun_settings);
			
			// Settings
			ButtonGroup lighting_group = new ButtonGroup();
			settings_dark_mode.setSelected(true);
			lighting_group.add(settings_light_mode);
			lighting_group.add(settings_dark_mode);
			lighting_group.add(settings_custom);
			
			menu_setting.add(settings_dark_mode);
			menu_setting.add(settings_light_mode);
			menu_setting.add(settings_custom);
			menu_setting.addSeparator();
			menu_setting.add(settings_custom_change);
			
			// About
			menu_about.add(about_github);
			menu_about.add(about_update);
			
		return bar;
	}
	
	public static void getAddRemoveColumnsMenuItems()
	{
		edit_add.removeAll();
		edit_remove.removeAll();
		
		for (int col = 0; col < FileOperations.numberOfColumns; col ++)
		{
			JMenuItem remove = new JMenuItem("Remove " + getNumeral(col + 1) + " column");
			remove.addActionListener(MouseAdapters.getRemoveContentColAdapter(col) );
			edit_remove.add(remove);
			
			JMenuItem add = new JMenuItem(col == 0 ? "Add before 1st column" : "Add between " + getNumeral(col) + " and " + getNumeral(col + 1) + " column");
			add.addActionListener(MouseAdapters.getAddContentColAdapter(col) );
			edit_add.add(add);
		}
		
		JMenuItem add = new JMenuItem("Add after " + getNumeral(FileOperations.numberOfColumns) + " column");
		add.addActionListener(MouseAdapters.getAddContentColAdapter(FileOperations.numberOfColumns) );
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
	
}
