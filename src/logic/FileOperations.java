package logic;
import java.awt.Color;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import gui.GuiHelper;
import gui.MainGui;
import gui.PopupAlerts;
import settings.AbbreviationSettings;
import settings.ColorSettingProfile;
import settings.ColorSettings;
import settings.HotkeyProfile;
import settings.Hotkeys;
import settings.SpeedrunSettings;

public class FileOperations
{
	private static String fileNotesDirectory = "";
	private static String fileNotesName = "";
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
		
		MainGui.keepWindowHeight = MainGui.keepScrollPosition = false;
	}
	
	public static String getWindowTitle()
	{
		return fileNotesName.replace("_", " ");
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
	
	public static void setDefaulSettings()
	{
		ColorSettings.colorSettingProfiles = new ColorSettingProfile[] {
				new ColorSettingProfile("Light" , Color.BLACK, Color.BLACK, Color.WHITE),
				new ColorSettingProfile("Dark"  , Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK),
				new ColorSettingProfile("Custom", Color.BLACK, Color.WHITE, Color.WHITE)
		};
		ColorSettings.currentColorSetting = ColorSettings.colorSettingProfiles[1];
		SpeedrunSettings.workaround_box.setSelected(true);
		Hotkeys.profiles.clear();
		Hotkeys.profiles.add(Hotkeys.EMPTY_HOTKEY_PROFILE);
		Hotkeys.activeProfile = Hotkeys.EMPTY_HOTKEY_PROFILE;
	}
	
	public static void readSettingsFile()
	{
		setDefaulSettings();
		
		try
		{
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = documentBuilder.parse(new File("settings.xml") );
			
			Element settings = (Element) doc.getChildNodes().item(0);
			
			String last_notes_file = settings.getElementsByTagName("last-notes-file").item(0).getTextContent();
			int split = last_notes_file.lastIndexOf('\\');
			fileNotesDirectory = last_notes_file.substring(0, split + 1);
			fileNotesName = last_notes_file.substring(split + 1);
			
			// color settings
			Element color_settings_element = (Element) settings.getElementsByTagName("color-settings").item(0);
			String current_color_profile = color_settings_element.getAttribute("current");
			
			NodeList color_profile_elements_list = color_settings_element.getElementsByTagName("color-profile");
			ArrayList<ColorSettingProfile> color_profiles_list = new ArrayList<ColorSettingProfile>();
			for (int i = 0; i < color_profile_elements_list.getLength(); i ++)
				color_profiles_list.add(new ColorSettingProfile( (Element) color_profile_elements_list.item(i), current_color_profile) );
			ColorSettings.colorSettingProfiles = color_profiles_list.toArray(new ColorSettingProfile[color_profiles_list.size() ] );
			
			// hotkey settings
			Element hotkey_settings_element = (Element) settings.getElementsByTagName("hotkey-settings").item(0);
			SpeedrunSettings.workaround_box.setSelected(Boolean.parseBoolean(hotkey_settings_element.getAttribute("ctrl-workaround") ) );
			String current_hotlkey_profile = hotkey_settings_element.getAttribute("current");
			
			NodeList hotkey_profile_elements_list = hotkey_settings_element.getElementsByTagName("hotkey-profile");
			Hotkeys.profiles.clear();
			Hotkeys.profiles.add(Hotkeys.EMPTY_HOTKEY_PROFILE);
			for (int i = 0; i < hotkey_profile_elements_list.getLength(); i ++)
				Hotkeys.profiles.add(new HotkeyProfile( (Element) hotkey_profile_elements_list.item(i), current_hotlkey_profile) );
			
		}
		catch (FileNotFoundException e)
		{
			MainGui.displayErrorAndExit("FileNotFoundException while reading settings file.\nDoes the file exists?", false, false);
		} catch (SAXParseException e)
		{
			MainGui.displayErrorAndExit("ParsingException while reading settings file.\nDid you convert your settings file using version 3.0?", false, false);
		} catch (SAXException e1)
		{
			MainGui.displayErrorAndExit("Exception while reading settings file.\nDid you convert your settings file using version 3.0?", false, false);
		} catch (IOException e1)
		{
			MainGui.displayErrorAndExit("IOException while reading settings file.", false, true);
		} catch (ParserConfigurationException e)
		{
			MainGui.displayErrorAndExit("ConfigurationError while reading settings file.", false, true);
		}
	}
	
	public static void readNotesFile()
	{
		PopupAlerts.createMissingImagesMessage = true;
		MainGui.reset();
		numberOfColumns = 0;
		  
		try
		{
			File notes_file = new File(fileNotesDirectory + fileNotesName);
			while ( !notes_file.exists() )
			{
				selectNotesFile();
				notes_file = new File(fileNotesDirectory + fileNotesName);
			}
			
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = documentBuilder.parse(new File(fileNotesDirectory + fileNotesName) );
			
			parseNotesElement( (Element) doc.getChildNodes().item(0) );
			
		} catch (SAXParseException e)
		{
			MainGui.displayErrorAndExit("ParsingException while reading notes file.\nIf you want to open an old notes file (.txt instead of .xml) use version 3.0, 'Open old' and save.", false, false);
		} catch (SAXException e1)
		{
			MainGui.displayErrorAndExit("Exception while reading notes file.\nDid you convert your settings file using version 3.0?", false, false);
		} catch (IOException e1)
		{
			MainGui.displayErrorAndExit("IOException while reading notes file.", false, true);
		} catch (ParserConfigurationException e)
		{
			MainGui.displayErrorAndExit("ConfigurationError while reading notes file.", false, true);
		}
	}
	
	private static void parseNotesElement(Element notes)
	{
		NodeList image_list = notes.getElementsByTagName("images-directory");
		NodeList abbrs_list = notes.getElementsByTagName("abbreviations-file");
		if (image_list.getLength() > 0)
			imagesDirectory = image_list.item(0).getTextContent();
		if (abbrs_list.getLength() > 0)
		{
			fileAbbreviations = abbrs_list.item(0).getTextContent();
			AbbreviationSettings.setAbbreviationsList(readAbbriviationsFile() );
		}
		
		for (int sections_index = 0; sections_index < notes.getElementsByTagName("section").getLength(); sections_index ++ )
			MainGui.sectionsList.add(new Section( (Element) notes.getElementsByTagName("section").item(sections_index) ) );
	}
	
	public static void writeSettingsFile()
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try (FileOutputStream output = new FileOutputStream("settings.xml") )
		{
			docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			Element settingsElement = doc.createElement("settings");
			doc.appendChild(settingsElement);
			
			// last opened notes file
			Element notesFilElement = doc.createElement("last-notes-file");
			notesFilElement.setTextContent(fileNotesDirectory + fileNotesName);
			settingsElement.appendChild(notesFilElement);
			
			// color settings
			Element colorsettingsElement = doc.createElement("color-settings");
			colorsettingsElement.setAttribute("current", ColorSettings.getCurrentColorSettingName() );
			
			for (ColorSettingProfile profile : ColorSettings.colorSettingProfiles)
				colorsettingsElement.appendChild(profile.getXMLElement(doc) );
			
			settingsElement.appendChild(colorsettingsElement);
			
			// hotkey settings
			Element hotkeysettingsElement = doc.createElement("hotkey-settings");
			
			hotkeysettingsElement.setAttribute("current", Hotkeys.getActiveHotkeyProfileName() );
			hotkeysettingsElement.setAttribute("ctrl-workaround", "" + SpeedrunSettings.workaround_box.isSelected() );
			
			for (int i = 1; i < Hotkeys.profiles.size(); i ++)
				hotkeysettingsElement.appendChild(Hotkeys.profiles.get(i).getXMLElement(doc) );
			
			settingsElement.appendChild(hotkeysettingsElement);
			
			// write dom document to a file
			Transformer transformer = TransformerFactory.newInstance().newTransformer();

		  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		  transformer.transform(new DOMSource(doc), new StreamResult(output) );
		  
		  unsavedChanges = false;
		}
		catch (TransformerConfigurationException e1)
		{
			MainGui.displayErrorAndExit("ConfigurationError while writing settings file.", false, true);
		} catch (TransformerException e)
		{
			MainGui.displayErrorAndExit("TransformerError while writing settings file.", false, true);
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("IOException while writing settings file.", false, true);
		} catch (ParserConfigurationException e2)
		{
			MainGui.displayErrorAndExit("ConfigurationError while writing settings file.", false, true);
		}
	}
	
	public static void createNewFile()
	{
		fileNotesDirectory = "";
		fileNotesName = "new_notes";
		MainGui.reset();
		numberOfColumns = 2;
		
		MainGui.keepWindowHeight = MainGui.keepScrollPosition = false;
		
		Section section = new Section("section 1");
		MainGui.sectionsList.add(section);
		
		MainGui.arrangeContent();
		unsavedChanges = true;
		MainGui.spaceColums();
	}
	
	public static String selectImageDirectory()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Sslect a new Image Directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		
		if (chooser.showOpenDialog(MainGui.settings) != JFileChooser.APPROVE_OPTION)
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
	
	private static ArrayList<String[]> readAbbriviationsFile()
	{
		return readAbbriviationsFile(fileAbbreviations);
	}
	
	public static ArrayList<String[]> readAbbriviationsFile(String location)
	{
		try (BufferedReader reader = new BufferedReader(new FileReader(location) ) )
		{
			String line_string;
			String[] line_arr;
			ArrayList<String[]> abbreviations_list = new ArrayList<String[]>();
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
			
			return abbreviations_list;
		} catch (FileNotFoundException ef)
		{
			return new ArrayList<String[]>();
		} catch (RuntimeException er)
		{
			return new ArrayList<String[]>();
		} catch (Exception e)
		{
			return new ArrayList<String[]>();
		}
	}
	
	public static void saveAbbereviationsFile()
	{
		if (fileAbbreviations == null)
			return;

		try (PrintWriter out = new PrintWriter(fileAbbreviations) )
		{
			out.print(AbbreviationSettings.getAbbreviationSaveString() );
		} catch (FileNotFoundException e)
		{
			fileAbbreviations = null;
		}
}
	
	public static void saveAsFile()
	{
		FileDialog dialog = new FileDialog(MainGui.window, "Save as", FileDialog.SAVE);
		dialog.setFile(".xml");
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
	
	private static Element getNotesElement(Document doc, boolean include_flie_locations)
	{
		// root element
		Element notesElement = doc.createElement("notes");
		
		if (include_flie_locations)
		{
			// image directory
			Element imagesElement = doc.createElement("images-directory");
			imagesElement.setTextContent(imagesDirectory);
			notesElement.appendChild(imagesElement);
			
			// abbr file
			Element abbreviationsElement = doc.createElement("abbreviations-file");
			abbreviationsElement.setTextContent(fileAbbreviations);
			notesElement.appendChild(abbreviationsElement);
		}
		
		// section
		for (Section section : MainGui.sectionsList)
			notesElement.appendChild(section.getXMLElement(doc) );
		
		return notesElement;
	}
	
	public static void saveFile()
	{
		saveAbbereviationsFile();
		
		if (fileNotesDirectory == null || fileNotesName == null)
			saveAsFile();
		else
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			try (FileOutputStream output = new FileOutputStream(fileNotesDirectory + fileNotesName) )
			{
				docBuilder = docFactory.newDocumentBuilder();
				
				// root elements
				Document doc = docBuilder.newDocument();
				doc.appendChild(getNotesElement(doc, true) );
				
				// write dom document to a file
				Transformer transformer = TransformerFactory.newInstance().newTransformer();

			  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			  transformer.transform(new DOMSource(doc), new StreamResult(output) );
			  
			  unsavedChanges = false;
			}
			catch (TransformerConfigurationException e1)
			{
				MainGui.displayErrorAndExit("ConfigurationError while writing notes file.", false, true);
			} catch (TransformerException e)
			{
				MainGui.displayErrorAndExit("TransformerError while writing notes file.", false, true);
			} catch (IOException e)
			{
				MainGui.displayErrorAndExit("IOException while writing notes file.", false, true);
			} catch (ParserConfigurationException e2)
			{
				MainGui.displayErrorAndExit("ConfigurationError while writing notes file.", false, true);
			}
		}
	}
	
	public static void importFile()
	{
		// TODO what to do with unsaved changes? 
		
		PopupAlerts.createMissingImagesMessage = true;
		MainGui.reset();
		numberOfColumns = 0;
		
		MainGui.keepWindowHeight = MainGui.keepScrollPosition = false;
		
		// selecting which file to import
		FileDialog dialog = new FileDialog(MainGui.window, "Select File to Import");
		dialog.setMode(FileDialog.LOAD);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getDirectory() == null)
			return;
		
		try
		{			
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = documentBuilder.parse(new File(dialog.getDirectory() + dialog.getFile() ) );
		
			fileNotesDirectory = dialog.getDirectory();
			fileNotesName = dialog.getFile();
			
			// selecting where to save the notes file
			fileNotesName = fileNotesName.replace("_export", "");
			int split = fileNotesName.lastIndexOf('.');
			fileNotesName = fileNotesName.substring(0, split) + "_import.xml";
			fileAbbreviations = fileNotesDirectory + fileNotesName.substring(0, split) + "_abbr.txt";
			
			Element export_element = (Element) doc.getChildNodes().item(0);
			
			AbbreviationSettings.parseAbbreviationselement( (Element) export_element.getElementsByTagName("abbreviations").item(0) );
			parseNotesElement( (Element) export_element.getElementsByTagName("notes").item(0) );
			
		} catch (SAXParseException e)
		{
			MainGui.displayErrorAndExit("ParsingException while reading import file.\nDid you convert your settings file using version 3.0?", false, false);
		} catch (SAXException e1)
		{
			MainGui.displayErrorAndExit("Exception while reading import file.\nDid you convert your settings file using version 3.0?", false, false);
		} catch (IOException e1)
		{
			MainGui.displayErrorAndExit("IOException while reading import file.", false, true);
		} catch (ParserConfigurationException e)
		{
			MainGui.displayErrorAndExit("ConfigurationError while reading import file.", false, true);
		}
		
		MainGui.arrangeContent();
		MainGui.spaceColums();
		FileOperations.unsavedChanges = true;
	}
	
	public static void exportFile()
	{
		String[] file_name_split = fileNotesName.split(Pattern.quote(".") );
		FileDialog dialog = new FileDialog(MainGui.window, "Select export loaction", FileDialog.SAVE);
		dialog.setDirectory(fileNotesDirectory);
		dialog.setFile(file_name_split[0] + "_export.xml");
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		
		if (dialog.getFile() == null)
			return;
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try (FileOutputStream output = new FileOutputStream(dialog.getDirectory() + dialog.getFile() ) )
		{
			docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			Element export_element = doc.createElement("export");
			doc.appendChild(export_element);
			
			export_element.appendChild(getNotesElement(doc, false) );
			export_element.appendChild(AbbreviationSettings.getAbbreviationsElement(doc) );
			
			// write dom document to a file
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(doc), new StreamResult(output) );
			
			unsavedChanges = false;
		}
		catch (TransformerConfigurationException e1)
		{
			MainGui.displayErrorAndExit("ConfigurationError while writing export file.", false, true);
		} catch (TransformerException e)
		{
			MainGui.displayErrorAndExit("TransformerError while writing export file.", false, true);
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("IOException while writing export file.", false, true);
		} catch (ParserConfigurationException e2)
		{
			MainGui.displayErrorAndExit("ConfigurationError while writing export file.", false, true);
		}
	}
}