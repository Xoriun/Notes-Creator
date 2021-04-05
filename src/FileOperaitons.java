import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
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

public class FileOperaitons
{
	public static String fileDirectory = "";
	public static String fileName = "";
	
	public static void getFile()
	{
		FileDialog dialog = new FileDialog(new Frame(), "Select File to Open");
		dialog.setMode(FileDialog.LOAD);
		dialog.setVisible(true);
		
		fileDirectory = dialog.getDirectory();
		fileName = dialog.getFile();
		
		String new_title = fileName.replace('_', ' ');
		Gui.titleChaged = !new_title.equals(Gui.window.getTitle() );
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
				updateSettingsFile();
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
					fileDirectory = line.substring(0, split + 1);
					fileName = line.substring(split + 1);
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
	
	public static void getContentFromFile()
	{
		ArrayList<String[]> content_list = new ArrayList<String[]>();
		ArrayList<String> todo_list = new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileDirectory + fileName) );
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!");
		}
		
		String header_string;
		String line_string;
		try {
			if ( (line_string = reader.readLine() ) != null)
				header_string = line_string.substring(line_string.startsWith("ï") ? 3 : 0);
			else
			{
				reader.close();
				throw new RuntimeException("Invalid header!");
			}
		} catch (IOException e) {
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
	
	public static void updateSettingsFile()
	{
		try
		{
			FileWriter writer = new FileWriter(new File("settings.txt"), false);
			writer.write(fileDirectory + fileName + '\n');
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
	
	public static void saveFile()
	{
		try (PrintWriter out = new PrintWriter(fileDirectory + fileName) )
		{
			for (int i = 0; i < Logic.content.length; i ++)
			{
				String rowStr = "";
				for (String cell : Logic.content[i])
					rowStr += cell + ";";
				out.println(rowStr.substring(0, rowStr.length() - 1) + (Logic.todoList[i].equals("") ? "" : ("||" + Logic.todoList[i] ) ) );
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} finally {
			Logic.unsavedChanges = false;
		}
	}
	
	public static void exportAsPdf()
	{
		//TODO
	}
}