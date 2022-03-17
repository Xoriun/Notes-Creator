package logic;
import java.awt.Color;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.JFileChooser;

import gui.Abbreviations;
import gui.ColorSettingProfile;
import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;
import gui.PopupAlerts;

public class FileOperations
{
	public static String fileNotesDirectory = "";
	public static String fileNotesName = "";
	public static String fileAbbreviations = "";
	public static String imagesDirectory = "";
	
	public static int numberOfColumns;
	public static boolean unsavedChanges = false;
	
	public static void selectNotesFile()
	{
		FileDialog dialog = new FileDialog(MainGui.window, "Select File to Open");
		dialog.setMode(FileDialog.LOAD);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return;
		
		fileNotesDirectory = dialog.getDirectory();
		fileNotesName = dialog.getFile();
		
		String new_title = fileNotesName.replace('_', ' ');
		MainGui.keepGuiSize = false;
		MainGui.window.setTitle(new_title);
	}
	
	public static Stream<String> getStreamOfNamesOfImagesInImagesDirectory()
	{
		return Stream.of(new File(imagesDirectory).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith(".png");
			}
		}) ).map(e -> e.substring(0, e.length() - 4) );
	}
	
	private static void setDefaulSettings()
	{
		ColorSettings.colorSettingProfiles = new ColorSettingProfile[] {
				new ColorSettingProfile("Light" , Color.BLACK, Color.BLACK, Color.WHITE),
				new ColorSettingProfile("Dark"  , Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK),
				new ColorSettingProfile("Custom", Color.WHITE, Color.WHITE, Color.WHITE)
		};
		SpeedRunMode.workaround_box.setSelected(true);
		Hotkeys.profiles.clear();
		Hotkeys.activeProfile = null;
	}
	
	public static void readSettingsFile()
	{
		setDefaulSettings();
		
		File settings = new File("settings.txt");
		new File("Images\\").mkdir(); //  creates Images directory if it does not exist
		
		try {
			// no settings file
			if ( !settings.createNewFile() )
			{
				BufferedReader reader;
				try {
					reader = new BufferedReader(new FileReader(settings) );
				} catch (FileNotFoundException e) {
					throw new RuntimeException("Settings File not found!");
				}
				
				String line;
				int[][] colors = new int[3][3];
				
				// last used notes-file
				if (null != (line = reader.readLine() ) ) // last notes-file
				{
					int split = line.lastIndexOf('\\');
					fileNotesDirectory = line.substring(0, split + 1);
					fileNotesName = line.substring(split + 1);
				}
				
				// color settings
				ArrayList<ColorSettingProfile> colors_list = new ArrayList<ColorSettingProfile>();
				while ( (line = reader.readLine() ) != null)
				{
					String color_setting_name = line;
					if ( !line.equals("Light") && !line.equals("Dark") && !line.equals("Custom") ) // All color settings are read
						break;
					
					for (int i = 0; i < 3 && (line = reader.readLine() ) != null; i ++)
					{
						colors[i] = Stream.of(line.split(":") ).mapToInt(Integer::parseInt).toArray();
						
						if (i == 2)
							colors_list.add(new ColorSettingProfile(color_setting_name,	new Color(colors[0][0], colors[0][1], colors[0][2] ),
									new Color(colors[1][0], colors[1][1], colors[1][2] ),
									new Color(colors[2][0], colors[2][1], colors[2][2] )
									) );
					}
				}
				if (colors_list.size() > 0)
					ColorSettings.colorSettingProfiles = colors_list.toArray(new ColorSettingProfile[colors_list.size() ] );
				
				// hotkey settings
				String workaraound_activated = line;
				if (workaraound_activated != null && !workaraound_activated.isEmpty() )
					SpeedRunMode.workaround_box.setSelected(Boolean.parseBoolean(workaraound_activated.split(":")[1] ) );
				
				String activeProfile = line = reader.readLine();
				
				if (activeProfile != null)
				{
					Hotkeys.profiles.clear();
					
					// reading
					while (null != (line = reader.readLine() ) )
					{				
						HotkeyProfile profile = new HotkeyProfile(line);
						
						for (int i = 0; i < 4; i ++)
							if (null != (line = reader.readLine() ) && !line.isEmpty() )
								Hotkeys.readHotkeySetting(line.split(":"), profile);
						
						Hotkeys.profiles.add(profile);
					}
					
					// setting active profile
					for (HotkeyProfile profile : Hotkeys.profiles)
						if (profile.name.equals(activeProfile) )
						{
							Hotkeys.activeProfile = profile;
							break;
						}
				}
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readNotesFile()
	{
		PopupAlerts.createMissingImagesMessage = true;
		MainGui.reset();
		numberOfColumns = 0;
		
		BufferedReader reader = null;
		while (reader == null)
		{
			try {
				reader = new BufferedReader(new FileReader(fileNotesDirectory + fileNotesName) );
			} catch (FileNotFoundException e) {
				selectNotesFile();
			}
		}
		
		String line_string;
		try {
			line_string = reader.readLine();
			
			// Checking for abbreviations file
			if (line_string.startsWith("***image_directory;") )
			{
				imagesDirectory = line_string.split(";")[1];
				line_string = reader.readLine();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading header!");
		}
		
		try {
			// Checking for abbreviations file
			if (line_string.startsWith("***abbreviations_file;") )
			{
				fileAbbreviations = line_string.split(";")[1];
				Abbreviations.setAbbreviationsList(readAbbriviationsFile() );
				line_string = reader.readLine();
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while reading header!");
		}
		
		String title_pattern = "---.*---.*";
		Section current_section;
		try
		{
			while (line_string != null)
			{
				current_section = new Section(line_string);
				MainGui.sectionsList.add(current_section);
				
				line_string = reader.readLine();
				
				while (line_string != null && !line_string.matches(title_pattern))
				{
					current_section.addRow(line_string);
					line_string = reader.readLine();
				}
				
				current_section.addEmptyRow();
				current_section.fillPanel();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void writeSettingsFile()
	{
		try
		{
			FileWriter writer = new FileWriter(new File("settings.txt"), false);
			writer.write(fileNotesDirectory + fileNotesName + '\n');
			for (ColorSettingProfile color : ColorSettings.colorSettingProfiles)
			{
				writer.write(color.name + "\n" +
						color.text.getRed() +       ":" + color.text.getGreen() +       ":" + color.text.getBlue() + "\n" +
						color.border.getRed() +     ":" + color.border.getGreen() +     ":" + color.border.getBlue() + "\n" +
						color.background.getRed() + ":" + color.background.getGreen() + ":" + color.background.getBlue()  + "\n");
			}
			writer.write("WorkaroundActivated:" + SpeedRunMode.workaround_box.isSelected() + "\n");
			if (Hotkeys.profiles.size() > 0)
			{
				if (Hotkeys.activeProfile != null)
					writer.write(Hotkeys.activeProfile.name);
				for (HotkeyProfile profile : Hotkeys.profiles)
					writer.write("\n" + profile.getHotkeySettingsString() );
			}
			writer.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void createNewFile()
	{
		fileNotesDirectory = fileNotesName = null;
		MainGui.reset();
		
		Section section = new Section("section 1");
		section.addRow("new;file");
		MainGui.sectionsList.add(section);
		numberOfColumns = 2;
		
		MainGui.arrangeContent();
		MainGui.contentRearraged = false; // to reset the height of the window
		unsavedChanges = true;
		MainGui.spaceColums();
	}
	
	public static String newAbbreviationsFile(ArrayList<String[]> abbreviations_list)
	{
		saveAbbereviationsFile();
		
		FileDialog dialog = new FileDialog(MainGui.window, "Select locations for new abbreviation file");
		dialog.setFile("\\abbreviation_new.txt");
		dialog.setMode(FileDialog.SAVE);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return "";
		
		abbreviations_list.clear();
		return dialog.getDirectory() + dialog.getFile();
	}
	
	public static String selectImageDirectory()
	{
		saveAbbereviationsFile();
		
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("choosertitle");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		
		if (chooser.showOpenDialog(MainGui.window) != JFileChooser.APPROVE_OPTION)
			return null;
		
		return chooser.getSelectedFile().toString();
	}
	
	public static String selectAbbreviationsFile()
	{
		saveAbbereviationsFile();
		
		FileDialog dialog = new FileDialog(MainGui.window, "Select abbreviation file to load");
		dialog.setMode(FileDialog.LOAD);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return null;
		
		return dialog.getDirectory() + dialog.getFile();
	}
	
	public static ArrayList<String[]> readAbbriviationsFile()
	{
		return readAbbriviationsFile(fileAbbreviations);
	}
	
	public static ArrayList<String[]> readAbbriviationsFile(String location)
	{
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(location) );
		} catch (FileNotFoundException ef) {
			return new ArrayList<String[]>();
		} catch (RuntimeException er)
		{
			return new ArrayList<String[]>();
		}
		
		String line_string;
		String[] line_arr;
		ArrayList<String[]> abbreviations_list = new ArrayList<String[]>();
		try {
			while( (line_string = reader.readLine() ) != null)
			{
				if ( !line_string.contains(":") )
					continue;
				
				line_arr = line_string.split(":");
				
				switch (line_arr.length)
				{
					case 0:
						abbreviations_list.add(new String[] {"", ""} );
						break;
					case 1:
						if (line_string.startsWith(":") )
							abbreviations_list.add(new String[] {"", line_arr[0] } );
						else
							abbreviations_list.add(new String[] {line_arr[0], ""} );
						break;
					case 2:
						abbreviations_list.add(line_arr);
						break;
					default:
						continue;
				}
			}
		} catch (Exception e) {
			try { reader.close(); } catch (IOException e1) { }
			return new ArrayList<String[]>(
					);
		}
		
		try { reader.close(); } catch (IOException e) {	}
		
		return abbreviations_list;
	}
	
	public static void saveAbbereviationsFile()
	{
		if (fileAbbreviations == null)
			return;
		else
		{
			try (PrintWriter out = new PrintWriter(fileAbbreviations) )
			{
				out.print(Abbreviations.getAbbreviationSaveString() );
			} catch (FileNotFoundException e)
			{
				fileAbbreviations = null;
			}
		}
	}
	
	public static void saveAsFile()
	{
		FileDialog dialog = new FileDialog(MainGui.window, "Save as", FileDialog.SAVE);
		dialog.setFile(".txt");
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return;
		fileNotesDirectory = dialog.getDirectory();
		fileNotesName = dialog.getFile();
		
		String new_title = fileNotesName.replace('_', ' ');
		MainGui.window.setTitle(new_title);
		
		saveFile();
	}
	
	public static void saveFile()
	{
		saveAbbereviationsFile();
		
		if (fileNotesDirectory == null || fileNotesName == null)
			saveAsFile();
		else
		{
			try (PrintWriter out = new PrintWriter(fileNotesDirectory + fileNotesName) )
			{
				if (imagesDirectory != null)
					out.println("***image_directory;" + imagesDirectory);
				if (fileAbbreviations != null)
					out.println("***abbreviations_file;" + fileAbbreviations);
				for (Section section : MainGui.sectionsList)
					out.println(section.getSaveString() );
			} catch (FileNotFoundException e)
			{
				// TODO
			} finally {
				unsavedChanges = false;
			}
		}
	}
	
	public static void importFile()
	{
		// selecting which file to import
		FileDialog dialog = new FileDialog(MainGui.window, "Select File to Import");
		dialog.setMode(FileDialog.LOAD);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return;
		
		String import_dir = dialog.getDirectory();
		String import_name = dialog.getFile();
		
		// creating reader for importing
		BufferedReader reader;
		try
		{
			reader = new BufferedReader(new FileReader(import_dir + import_name) );
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException("Error while importing file: Import file at '" + import_dir + import_name + "' was not found!");
		}
		
		// selecting where to save the notes file
		import_name = import_name.replace("_export", "");
		String[] file_name_split = import_name.split(Pattern.quote(".") );
		
		String file = null;
		dialog = new FileDialog(MainGui.window, "Save notes file", FileDialog.SAVE);
		dialog.setDirectory(import_dir);
		dialog.setFile(file_name_split[0] + "_import." + file_name_split[1]);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getFile() == null)
		{
			try { reader.close(); } catch (IOException e1) { }
			return;
		}
		
		file = dialog.getFile();
		
		fileNotesDirectory = dialog.getDirectory();
		fileNotesName = dialog.getFile();
		
		// creating reading notes file
		String line;
		ArrayList<String> content = new ArrayList<String>();
		try
		{
			while ( (line = reader.readLine()) != null)
			{
				if (line.equals("***ABBREVIATIONS***") )
					break;
				
				content.add(line);
			}
		} catch (IOException e)
		{
			try { reader.close(); } catch (IOException e1) { }
			throw new RuntimeException("Error while importing file: creating notes writer!");
		}
		
		// if there are abbreviations: creating writer for abbreviations file, writing abbreviations file
		if (line != null && line.equals("***ABBREVIATIONS***") )
		{
			String import_name_abbr = file_name_split[0] + "_abbreviations." + file_name_split[1];
			dialog = new FileDialog(MainGui.window, "Save abbreviations file", FileDialog.SAVE);
			dialog.setDirectory(import_dir);
			dialog.setFile(import_name_abbr);
			GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
			dialog.setVisible(true);
			file = dialog.getFile();
			if (file == null)
			{
				try { reader.close(); } catch (IOException e1) { }
				return;
			}
			
			String import_dir_abbr = dialog.getDirectory();
			import_name_abbr = dialog.getFile();
			if (content.get(0).startsWith("***abbreviations_file;") )
				content.set(0, "***abbreviations_file;" + import_dir_abbr + import_name_abbr);
			
			PrintWriter writer_abbreviations;
			try
			{
				writer_abbreviations = new PrintWriter(new File(import_dir_abbr + import_name_abbr) );
			} catch (FileNotFoundException e)
			{
				try { reader.close(); } catch (IOException e1) { }
				throw new RuntimeException("Error while importing file: creating abbreviations writer!");
			}
			
			try
			{
				while ( (line = reader.readLine()) != null)
					writer_abbreviations.println(line);
				writer_abbreviations.close();
				reader.close();
			} catch (IOException e)
			{
				throw new RuntimeException("Error while importing file: writing!");
			}
			
			Abbreviations.setAbbreviationsList(readAbbriviationsFile(import_dir_abbr + import_name_abbr) );
		}
		
		
		PrintWriter writer_notes;
		try
		{
			writer_notes = new PrintWriter(new File(fileNotesDirectory + fileNotesName) );
			for (String content_line : content)
				writer_notes.println(content_line);
			writer_notes.close();
		} catch (IOException e)
		{
			try { reader.close(); } catch (IOException e1) { }
			throw new RuntimeException("Error while importing file: creating notes writer!");
		}
		readNotesFile();
	}
	
	public static void exportFile()
	{
		if (unsavedChanges)
			saveFile();
		
		// creating notes reader
		BufferedReader reader_notes;
		try
		{
			reader_notes = new BufferedReader(new FileReader(fileNotesDirectory + fileNotesName) );
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException("Error while exporting file: Notes file at '" + fileNotesDirectory + fileNotesName + "' was not found!");
		}
		
		// creating abbreviations reader
		BufferedReader reader_abbreviations = null;
		if (fileAbbreviations != null && !fileAbbreviations.isEmpty() )
			try
		{
				reader_abbreviations = new BufferedReader(new FileReader(fileAbbreviations) );
		} catch (FileNotFoundException e)
		{
			try { reader_notes.close(); } catch (IOException e1) { }
			throw new RuntimeException("Error while exporting file: Notes file at '" + fileAbbreviations + "' was not found!");
		}
		
		// creating writer
		PrintWriter writer;
		String[] file_name_split = fileNotesName.split(Pattern.quote(".") );
		
		FileDialog dialog = new FileDialog(MainGui.window, "Save notes file", FileDialog.SAVE);
		dialog.setDirectory(fileNotesDirectory);
		dialog.setFile(file_name_split[0] + "_export." + file_name_split[1]);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getFile() == null)
		{
			try { reader_abbreviations.close(); reader_notes.close(); } catch (IOException e1) { }
			return;
		}
		
		try
		{
			writer = new PrintWriter(new File(dialog.getFile() ) );
		} catch (IOException e)
		{
			try { reader_notes.close(); reader_abbreviations.close(); } catch (Exception e1) { }
			throw new RuntimeException("Error while exporting file: creating writer!");
		}
		
		// writing
		String line;
		try
		{
			while ( (line = reader_notes.readLine()) != null)
				writer.println(line);
			reader_notes.close();
			
			if (fileAbbreviations != null && !fileAbbreviations.isEmpty() )
			{
				String first_abbr = reader_abbreviations.readLine();
				if (first_abbr != null && !first_abbr.isEmpty() )
				{
					writer.println("***ABBREVIATIONS***\n" + first_abbr);
					while ( (line = reader_abbreviations.readLine()) != null)
						writer.println(line);
				}
				reader_abbreviations.close();
			}
			writer.close();
		} catch (IOException e)
		{
			throw new RuntimeException("Error while exporting file: writing!");
		}
	}
}