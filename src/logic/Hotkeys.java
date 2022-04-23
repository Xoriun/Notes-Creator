package logic;
import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import gui.GuiHelper;
import gui.MainGui;
import logic.HotkeyProfile;

public class Hotkeys implements NativeKeyListener
{
	protected static ArrayList<HotkeyProfile> profiles = new ArrayList<HotkeyProfile>();
	protected static HotkeyProfile activeProfile;
	
	protected static final int HK_Split = 0;
	protected static final int HK_Reset = 1;
	protected static final int HK_Undo  = 2;
	protected static final int HK_Skip  = 3;
	protected static final int HK_Undefined = -1;
	
	private static boolean listenForHotkeys = false;
	
	public Hotkeys()
	{
		
	}
	
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
		
		GlobalScreen.addNativeKeyListener(new Hotkeys() );
	}
	
	public static void shutDownHotkeys()
	{
		Logger.getLogger(GlobalScreen.class.getPackage().getName() ).setLevel(Level.OFF);
		try {
			GlobalScreen.unregisterNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem unregistering the native hook.");
			System.err.println(ex.getMessage());
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
					SpeedRunMode.startOrSplit();
					break;
				case HK_Skip:
					SpeedRunMode.skip();
					break;
				case HK_Reset:
					SpeedRunMode.reset();
					break;
				case HK_Undo:
					SpeedRunMode.undoSplit();
					break;
			}
		}
	}

	@Override
	public void nativeKeyReleased(NativeKeyEvent e)
	{
		//System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode() ) );
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent e)
	{
		//System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(e.getKeyCode() ) );
	}
	
	static void selectLiveSplitFile(ArrayList<HotkeyProfile> new_profiles)
	{
		FileDialog dialog = new FileDialog(MainGui.window, "Select 'LiveSplit.exe'", FileDialog.LOAD);
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
		String dir = dialog.getDirectory();
		
		if (dir != null)
			importHotkeySettings(dir, new_profiles);
	}

	private static void importHotkeySettings(String dir_location, ArrayList<HotkeyProfile> new_profiles)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(dir_location + "settings.cfg") );
			
			String line = "";
			while (!line.contains("<HotkeyProfiles>") )
			{
				line = reader.readLine();
				if (line == null)
				{
					reader.close();
					return;
				}
			}
			
			new_profiles.clear();
			// next line will be the declaration of the first hotkey profile
			
			while (true)
			{
				if ( (line = reader.readLine() ) == null)
				{
					reader.close();
					return;
				}
				
				// last profile was read
				if (line.contains("</HotkeyProfiles>") )
				{
					reader.close();
					return;
				}
				
				// line looks like <HotkeyProfile name="__name__">
				HotkeyProfile profile = new HotkeyProfile(line.split("\"")[1] );
				
				// Split
				line = reader.readLine();
				String[] split_arr = line.substring(line.indexOf('>') + 1, line.lastIndexOf('<') ).split(", ");
				for (int i = 0; i < split_arr.length; i ++)
				{
					String key = split_arr[i];
					if (i == 0)
					{
						profile.eventSplitKey      = getKeyConstant(key);
						profile.eventSplitLocation = getLocationConstant(key);
						profile.eventSplitModifier = 0;
					}
					else
						profile.eventSplitModifier += getModifierConstant(key);
				}
				
				// Reset
				line = reader.readLine();
				String[] reset_arr = line.substring(line.indexOf('>') + 1, line.lastIndexOf('<') ).split(", ");
				for (int i = 0; i < reset_arr.length; i ++)
				{
					String key = reset_arr[i];
					if (i == 0)
					{
						profile.eventResetKey      = getKeyConstant(key);
						profile.eventResetLocation = getLocationConstant(key);
						profile.eventResetModifier = 0;
					}
					else
						profile.eventResetModifier += getModifierConstant(key);
				}
				
				// Skip
				line = reader.readLine();
				String[] skip_arr = line.substring(line.indexOf('>') + 1, line.lastIndexOf('<') ).split(", ");
				for (int i = 0; i < skip_arr.length; i ++)
				{
					String key = skip_arr[i];
					if (i == 0)
					{
						profile.eventSkipKey      = getKeyConstant(key);
						profile.eventSkipLocation = getLocationConstant(key);
						profile.eventSkipModifier = 0;
					}
					else
						profile.eventSkipModifier += getModifierConstant(key);
				}
				
				// Undo
				line = reader.readLine();
				String[] undo_arr = line.substring(line.indexOf('>') + 1, line.lastIndexOf('<') ).split(", ");
				for (int i = 0; i < undo_arr.length; i ++)
				{
					String key = undo_arr[i];
					if (i == 0)
					{
						profile.eventUndoKey      = getKeyConstant(key);
						profile.eventUndoLocation = getLocationConstant(key);
						profile.eventUndoModifier = 0;
					}
					else
						profile.eventUndoModifier += getModifierConstant(key);
				}
				
				new_profiles.add(profile);
				
				// skip the rest of the profiles settings
				while ( (line = reader.readLine() ) != null)
					if (line.contains("</HotkeyProfile>") )
						break;

				// next line will be the declaration of the next hotkey profile
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO: handle exception
		}
	}
	
	private static int getKeyConstant(String imported_str)
	{
		switch (imported_str)
		{
			// letters
			case "A": return NativeKeyEvent.VC_A;
			case "B": return NativeKeyEvent.VC_B;
			case "C": return NativeKeyEvent.VC_C;
			case "D": return NativeKeyEvent.VC_D;
			case "E": return NativeKeyEvent.VC_E;
			case "F": return NativeKeyEvent.VC_F;
			case "G": return NativeKeyEvent.VC_G;
			case "H": return NativeKeyEvent.VC_H;
			case "I": return NativeKeyEvent.VC_I;
			case "J": return NativeKeyEvent.VC_J;
			case "K": return NativeKeyEvent.VC_K;
			case "L": return NativeKeyEvent.VC_L;
			case "M": return NativeKeyEvent.VC_M;
			case "N": return NativeKeyEvent.VC_N;
			case "O": return NativeKeyEvent.VC_O;
			case "P": return NativeKeyEvent.VC_P;
			case "Q": return NativeKeyEvent.VC_Q;
			case "R": return NativeKeyEvent.VC_R;
			case "S": return NativeKeyEvent.VC_S;
			case "T": return NativeKeyEvent.VC_T;
			case "U": return NativeKeyEvent.VC_U;
			case "V": return NativeKeyEvent.VC_V;
			case "W": return NativeKeyEvent.VC_W;
			case "X": return NativeKeyEvent.VC_X;
			case "Y": return NativeKeyEvent.VC_Y;
			case "Z": return NativeKeyEvent.VC_Z;
			// Numbers
			case "D1": case "NumPad1": return NativeKeyEvent.VC_1;
			case "D2": case "NumPad2": return NativeKeyEvent.VC_2;
			case "D3": case "NumPad3": return NativeKeyEvent.VC_3;
			case "D4": case "NumPad4": return NativeKeyEvent.VC_4;
			case "D5": case "NumPad5": return NativeKeyEvent.VC_5;
			case "D6": case "NumPad6": return NativeKeyEvent.VC_6;
			case "D7": case "NumPad7": return NativeKeyEvent.VC_7;
			case "D8": case "NumPad8": return NativeKeyEvent.VC_8;
			case "D9": case "NumPad9": return NativeKeyEvent.VC_9;
			case "D0": case "NumPad0": return NativeKeyEvent.VC_0;
			// F-keys
			case "F1":  return NativeKeyEvent.VC_F1;
			case "F2":  return NativeKeyEvent.VC_F2;
			case "F3":  return NativeKeyEvent.VC_F3;
			case "F4":  return NativeKeyEvent.VC_F4;
			case "F5":  return NativeKeyEvent.VC_F5;
			case "F6":  return NativeKeyEvent.VC_F6;
			case "F7":  return NativeKeyEvent.VC_F7;
			case "F8":  return NativeKeyEvent.VC_F8;
			case "F9":  return NativeKeyEvent.VC_F9;
			case "F10": return NativeKeyEvent.VC_F10;
			case "F11": return NativeKeyEvent.VC_F11;
			case "F12": return NativeKeyEvent.VC_F12;
			// special keys
			case "Oemtilde":        return NativeKeyEvent.VC_UNDEFINED;
			case "Capital":         return NativeKeyEvent.VC_CAPS_LOCK;
			case "ShiftKey":        return NativeKeyEvent.VC_SHIFT;
			case "ControlKey":      return NativeKeyEvent.VC_CONTROL;
			case "LWin":            return NativeKeyEvent.VC_ALT;
			case "RWin":            return NativeKeyEvent.VC_ALT;
			case "Space":           return NativeKeyEvent.VC_SPACE;
			case "Apps":            return NativeKeyEvent.VC_UNDEFINED;
			case "Return":          return NativeKeyEvent.VC_ENTER;
			case "Back":            return NativeKeyEvent.VC_BACKSPACE;
			case "Oemplus":         return NativeKeyEvent.VC_EQUALS;
			case "OemMinus":        return NativeKeyEvent.VC_MINUS;
			case "OemOpenBrackets": return NativeKeyEvent.VC_OPEN_BRACKET;
			case "Oem6":            return NativeKeyEvent.VC_CLOSE_BRACKET;
			case "Oem1":            return NativeKeyEvent.VC_SEMICOLON;
			case "Oem7":            return NativeKeyEvent.VC_QUOTE;
			case "Oem5":            return NativeKeyEvent.VC_BACK_SLASH;
			case "Oemcomma":        return NativeKeyEvent.VC_COMMA;
			case "OemPeriod":       return NativeKeyEvent.VC_PERIOD;
			case "OemQuestion":     return NativeKeyEvent.VC_SLASH;
		
			default:
				return NativeKeyEvent.VC_UNDEFINED;
		}
	}
	
	private static int getLocationConstant(String imported_str)
	{
		if (imported_str.contains("NumPad") ) return 4;
		switch (imported_str)
		{
			// LiveSplit doesn't differentiate between left and right keys
			case "ShiftKey": return -1;
			case "ControlKey": return -1;
			case "Menu": return -1;
			case "Enter": return -1;
			
			case "LWin": return 2;
			case "Rwin": return 3;
			
			case "Decimal": return 4;
			case "Add": return 4;
			case "Subtract": return 4;
			case "Multiply": return 4;
			case "Devide": return 4;
			case "NumLock": return 4;
			case "Left": return 4;
			case "Up": return 4;
			case "Right": return 4;
			case "Down": return 4;
			case "Insert": return 4;
			case "Delete": return 4;
			case "Start": return 4;
			case "Home": return 4;
			case "PageUp": return 4;
			case "Next": return 4;
			
			default:
				return 1;
		}
	}
	
	private static int getModifierConstant(String imported_str)
	{
		switch (imported_str)
		{
			case "ShiftKey":   return NativeKeyEvent.SHIFT_MASK;
			case "Control": return NativeKeyEvent.CTRL_MASK;
			case "Alt":       return NativeKeyEvent.ALT_MASK;
			
			default:
				return 0;
		}
	}
	
	static String getActiveHotkeyProfileName() { return activeProfile == null ? "" : activeProfile.name; }
}
