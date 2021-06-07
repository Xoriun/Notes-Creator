import java.awt.Color;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Stream;

//import javax.imageio.ImageIO;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.SwingUtilities;

public class FileOperaitons
{
	public static String fileDirectoryNotes = "";
	public static String fileNameNotes = "";
	public static String fileAbbreviations = "";
	
	public static void selectNotesFile()
	{
		FileDialog dialog = new FileDialog(Gui.window, "Select File to Open");
		dialog.setMode(FileDialog.LOAD);
		Gui.setLocation(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return;
		
		fileDirectoryNotes = dialog.getDirectory();
		fileNameNotes = dialog.getFile();
		
		String new_title = fileNameNotes.replace('_', ' ');
		Gui.keepGuiSize = false;
		Gui.window.setTitle(new_title);
	}

	public static void readSettingsFile()
	{
		File settings = new File("settings.txt");
		
		try {
			// no settings file
			if (settings.createNewFile() )
			{
				ColorSetting[] res = new ColorSetting[] { new ColorSetting("Light" , Color.BLACK, Color.BLACK, Color.WHITE),
																									new ColorSetting("Dark"  , Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK),
																									new ColorSetting("Custom", Color.WHITE, Color.WHITE, Color.WHITE)
																									};
				Gui.colorSettings = res;
				writeSettingsFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(settings) );
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!");
		}
		
		ArrayList<ColorSetting> colors_list = new ArrayList<ColorSetting>();
		String line, name = "";
		int[][] colors = new int[3][3];
		try {
			for (int i = 0; (line = reader.readLine() ) != null; i++ )
			{
				if (i == 0 && !line.equals("Light") ) // last file
				{
					int split = line.lastIndexOf('\\');
					fileDirectoryNotes = line.substring(0, split + 1);
					fileNameNotes = line.substring(split + 1);
					i = 0;
					line = reader.readLine();
					if (line == null) break;
				}
				
				if (i%4 == 0) name = line;
				else colors[i%4-1] = Stream.of(line.split(":") ).mapToInt(Integer::parseInt).toArray();
				if (i%4 == 3)
					colors_list.add(new ColorSetting(name,	new Color(colors[0][0], colors[0][1], colors[0][2] ),
																									new Color(colors[1][0], colors[1][1], colors[1][2] ),
																									new Color(colors[2][0], colors[2][1], colors[2][2] )
																									) );
			}
			
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while reading settings file!");
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("Error while reading settings file!");
		}
		
		Gui.colorSettings = colors_list.toArray(new ColorSetting[colors_list.size() ] );
	}
	
	public static void readNotesFile()
	{
		Logic.creatMissingImagesMessage = true;
		
		ArrayList<String[]> content_list = new ArrayList<String[]>();
		ArrayList<String> todo_list = new ArrayList<String>();
		BufferedReader reader = null;
		while (reader == null)
		{
			try {
				reader = new BufferedReader(new FileReader(fileDirectoryNotes + fileNameNotes) );
			} catch (FileNotFoundException e) {
				selectNotesFile();
			}
		}
		
		String header_string;
		String line_string;
		try {
			line_string = reader.readLine();
			if (line_string.startsWith("***abbreviations_file;") )
			{
				fileAbbreviations = line_string.split(";")[1];
				Logic.abbreviationsList = readAbbriviationsFile();
				line_string = reader.readLine();
			}
			header_string = line_string.substring(line_string.startsWith("�") ? 3 : 0);
		} catch (Exception e) {
			throw new RuntimeException("Error while reading header!");
		}
		
		String[] header = header_string.split(Pattern.quote("||"), -1);
		String[] header_content = header[0].split(";");
		Logic.maxRowLength = header_content.length;
		content_list.add(header_content);
		todo_list.add(header_string.contains("||") ? header[1] : "");
		
		String empty_line = "";
		for (int i = 1; i < Logic.maxRowLength; i ++) empty_line += ";";
		
		String[] line;
		String[] line_content;
		
		int row = 0;
		
		try {
			while ( (line_string = reader.readLine() ) != null)
			{
				row ++;
				if (empty_line.contains(line_string) ) continue;
				
				// splitting line_string into cells without the todo-part
				line = line_string.split(Pattern.quote("||"), -1);
				line_content = line[0].split(";", -1); 
				
				if (line_content.length > Logic.maxRowLength) // checking number of cells
				{
					reader.close();
					throw new RuntimeException("The maximum number of cells was exeeded in row " + row + "! Maximum is " + Logic.maxRowLength + " but was " + line.length);
				}
				else if (line_content.length < Logic.maxRowLength) // appending cells if necessary
				{
					String[] dummy = new String[Logic.maxRowLength];
					for (int i = 0; i < Logic.maxRowLength; i ++)
						if (i < line_content.length)
							dummy[i] = line_content[i];
						else
							dummy[i] = "";
					line_content = dummy;
				}
				
				content_list.add(line_content);
				todo_list.add(line_string.contains("||") ? line[1] : "");
			}
			
			Logic.content = content_list.toArray(new String[content_list.size() ][Logic.maxRowLength] );
			Logic.todoList = todo_list.toArray(new String[todo_list.size() ] );
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Error while reading file!");
		}
	}
	
	public static void writeSettingsFile()
	{
		try
		{
			FileWriter writer = new FileWriter(new File("settings.txt"), false);
			writer.write(fileDirectoryNotes + fileNameNotes + '\n');
			for (ColorSetting color : Gui.colorSettings)
			{
				writer.write(color.name + "\n" +
										color.text.getRed() +       ":" + color.text.getGreen() +       ":" + color.text.getBlue() + "\n" +
										color.border.getRed() +     ":" + color.border.getGreen() +     ":" + color.border.getBlue() + "\n" +
										color.background.getRed() + ":" + color.background.getGreen() + ":" + color.background.getBlue()  + "\n");
			}
			writer.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void createNewFile()
	{
		fileDirectoryNotes = fileNameNotes = null;
		Logic.content = new String[][] {{"---section 1---"," "},{"new","file"}};
		Logic.maxRowLength = 2;
		Gui.arrangeContent();
		Gui.contentRearraged = false; // to reset the height of the window
		Gui.spaceColums();
	}
	
	public static String newAbbreviationsFile(ArrayList<String[]> abbreviations_list)
	{
		saveAbbereviationsFile();
		
		FileDialog dialog = new FileDialog(Gui.window, "Select locations for new abbreviation file");
		dialog.setFile("\\abbreviation_new.txt");
		dialog.setMode(FileDialog.SAVE);
		Gui.setLocation(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return "";
		
		abbreviations_list.clear();
		return dialog.getDirectory() + dialog.getFile();
	}
	
	public static String selectAbbreviationsFile()
	{
		saveAbbereviationsFile();
		
		FileDialog dialog = new FileDialog(Gui.window, "Select abbreviation file to load");
		dialog.setMode(FileDialog.LOAD);
		Gui.setLocation(dialog);
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
				for (String[] abbreviation : Logic.abbreviationsList)
					out.println(abbreviation[0] + ":" + abbreviation[1] );
			} catch (FileNotFoundException e)
			{
				fileAbbreviations = null;
			}
		}
	}
	
	public static void saveAsFile()
	{
		FileDialog dialog = new FileDialog(Gui.window, "Save as", FileDialog.SAVE);
		dialog.setFile(".txt");
		Gui.setLocation(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return;
		fileDirectoryNotes = dialog.getDirectory();
		fileNameNotes = dialog.getFile();
		
		String new_title = fileNameNotes.replace('_', ' ');
		Gui.window.setTitle(new_title);
		
		saveFile();
	}
	
	public static void saveFile()
	{
		saveAbbereviationsFile();
		
		if (fileDirectoryNotes == null || fileNameNotes == null)
			saveAsFile();
		else
		{
			try (PrintWriter out = new PrintWriter(fileDirectoryNotes + fileNameNotes) )
			{
				if (fileAbbreviations != null)
					out.println("***abbreviations_file;" + fileAbbreviations);
				for (int i = 0; i < Logic.content.length; i ++)
				{
					String rowStr = "";
					for (String cell : Logic.content[i])
						rowStr += cell + ";";
					out.println(rowStr.substring(0, rowStr.length() - 1) + (Logic.todoList[i].equals("") ? "" : ("||" + Logic.todoList[i] ) ) );
				}
			} catch (FileNotFoundException e)
			{
				// TODO
			} finally {
				Logic.unsavedChanges = false;
			}
		}
	}
	
	public static void importFile()
	{
		// selecting which file to import
		FileDialog dialog = new FileDialog(Gui.window, "Select File to Import");
		dialog.setMode(FileDialog.LOAD);
		Gui.setLocation(dialog);
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
		dialog = new FileDialog(Gui.window, "Save notes file", FileDialog.SAVE);
		dialog.setDirectory(import_dir);
		dialog.setFile(file_name_split[0] + "_import." + file_name_split[1]);
		Gui.setLocation(dialog);
		dialog.setVisible(true);
		
		if (dialog.getFile() == null)
		{
			try { reader.close(); } catch (IOException e1) { }
			return;
		}
		
		file = dialog.getFile();

		fileDirectoryNotes = dialog.getDirectory();
		fileNameNotes = dialog.getFile();
		
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
			dialog = new FileDialog(Gui.window, "Save abbreviations file", FileDialog.SAVE);
			dialog.setDirectory(import_dir);
			dialog.setFile(import_name_abbr);
			Gui.setLocation(dialog);
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
			
			Logic.abbreviationsList = readAbbriviationsFile(import_dir_abbr + import_name_abbr);
		}
		

		PrintWriter writer_notes;
		try
		{
			writer_notes = new PrintWriter(new File(fileDirectoryNotes + fileNameNotes) );
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
		if (Logic.unsavedChanges)
			saveFile();
		
		// creating notes reader
		BufferedReader reader_notes;
		try
		{
			reader_notes = new BufferedReader(new FileReader(fileDirectoryNotes + fileNameNotes) );
		} catch (FileNotFoundException e)
		{
			throw new RuntimeException("Error while exporting file: Notes file at '" + fileDirectoryNotes + fileNameNotes + "' was not found!");
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
		String[] file_name_split = fileNameNotes.split(Pattern.quote(".") );
		
		FileDialog dialog = new FileDialog(Gui.window, "Save notes file", FileDialog.SAVE);
		dialog.setDirectory(fileDirectoryNotes);
		dialog.setFile(file_name_split[0] + "_export." + file_name_split[1]);
		Gui.setLocation(dialog);
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
	
	/** public static void exportAsPdf()
	{
		int width  = Gui.sectionPanelsList.get(0).getWidth();
		int height = 0, current_height = 0;
		for (JPanel panel : Gui.sectionPanelsList)
			height += panel.getHeight();
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		JFrame buffer_window = new JFrame();
		
		//Gui.scrollPane.printAll(image.getGraphics());
		for (JPanel panel : Gui.sectionPanelsList)
		{
			SwingUtilities.paintComponent(image.getGraphics(), panel, buffer_window.getContentPane(), 0, current_height, panel.getWidth(), panel.getHeight() );
			current_height += panel.getHeight();
		}
		System.out.println("test");
		
		try {
			ImageIO.write(image, "png", new File("test.png") );
		} catch (IOException e) {
			//Handle exception
		}
	}
	*/
}