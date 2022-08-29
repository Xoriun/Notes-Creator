package settings;
import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gui.GuiHelper;
import gui.MainGui;
import settings.HotkeyProfile;

public class Hotkeys implements NativeKeyListener
{
	public static ArrayList<HotkeyProfile> profiles = new ArrayList<HotkeyProfile>();
	public static HotkeyProfile activeProfile;
	
	protected static final int HK_Split = 0;
	protected static final int HK_Reset = 1;
	protected static final int HK_Undo  = 2;
	protected static final int HK_Skip  = 3;
	protected static final int HK_Undefined = -1;
	
	private static boolean listenForHotkeys = false;
	
	private static final Hotkeys HOTKEYS = new Hotkeys();
	
	public static final EmptyHotkeyProfile EMPTY_HOTKEY_PROFILE = HOTKEYS.new EmptyHotkeyProfile();
	
	public Hotkeys()
	{ }
	
	public static void initializeHotkeys()
	{
		Logger.getLogger(GlobalScreen.class.getPackage().getName() ).setLevel(Level.OFF);
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
		}
		
		GlobalScreen.addNativeKeyListener(HOTKEYS);
	}
	
	public static void shutDownHotkeys()
	{
		Logger.getLogger(GlobalScreen.class.getPackage().getName() ).setLevel(Level.OFF);
		try {
			GlobalScreen.unregisterNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem unregistering the native hook.");
			System.err.println(ex.getMessage() );
		}
	}
	
	static void startListeningForHotkeys()
	{
		listenForHotkeys = true;
	}
	
	static void stopListeningForHotkeys()
	{
		listenForHotkeys = false;
	}
	
	@Override
 	public void nativeKeyPressed(NativeKeyEvent e)
	{
		if (listenForHotkeys)
		{
			switch (activeProfile.getHotkeyMatch(e) )
			{
				case HK_Undefined: return;
				case HK_Split:
					SpeedrunSettings.startOrSplit();
					break;
				case HK_Skip:
					SpeedrunSettings.skip();
					break;
				case HK_Reset:
					SpeedrunSettings.reset();
					break;
				case HK_Undo:
					SpeedrunSettings.undoSplit();
					break;
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e)
	{
		// not used
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e)
	{
		// not used
	}
	
	static void loadHotkeyProfilesFromLiveSplitSettings(ArrayList<HotkeyProfile> new_profiles)
	{
		FileDialog dialog = new FileDialog(MainGui.window, "Select 'LiveSplit.exe'", FileDialog.LOAD);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		String dir = dialog.getDirectory();
		
		if (dir == null)
			return;
		
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(dir + "settings.cfg") );
			NodeList settings = doc.getElementsByTagName("Settings");
			
			new_profiles.add(EMPTY_HOTKEY_PROFILE);
			
			NodeList hotkey_profiles = ( (Element) settings.item(0) ).getElementsByTagName("HotkeyProfiles").item(0).getChildNodes();
			for (int i = 0; i < hotkey_profiles.getLength(); i ++)
			{
				Node node = hotkey_profiles.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE)
					continue;
				
				new_profiles.add(new HotkeyProfile( (Element) node) );
			}
		} catch (SAXException e)
		{
			MainGui.displayErrorAndExit("SAXException while reading Liveplit settings file. Did you select the executable LiveSplit.exe?", false, false);
		} catch (IOException e)
		{
			MainGui.displayErrorAndExit("IOException while reading Liveplit settings file.", false, true);
		} catch (ParserConfigurationException e)
		{
			MainGui.displayErrorAndExit("ConfigurationException while reading Liveplit settings file.", false, true);
		} catch (Exception e)
		{
			MainGui.displayErrorAndExit("Unhandled Exception while reading Liveplit settings file.", false, true);
		}
	}
	
	public static String getActiveHotkeyProfileName() { return activeProfile == null ? "" : activeProfile.name; }
	
	private class EmptyHotkeyProfile extends HotkeyProfile
	{
		public EmptyHotkeyProfile()
		{
			super("<not set>");
		}

		@Override
		public String getHotkeyDisplay(String type)
		{
			return "<not set>";
		}

		@Override
		public Element getXMLElement(Document doc)
		{
			return null;
		}
	}
}
