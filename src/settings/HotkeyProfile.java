package settings;

import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HotkeyProfile
{
	public String name;
	private int eventSplitKey = 0;
	private int eventSplitLocation = 0;
	private int eventSplitModifier = 0;
	private int eventResetKey = 0;
	private int eventResetLocation = 0;
	private int eventResetModifier = 0;
	private int eventUndoKey = 0;
	private int eventUndoLocation = 0;
	private int eventUndoModifier = 0;
	private int eventSkipKey = 0;
	private int eventSkipLocation = 0;
	private int eventSkipModifier = 0;
	
	public HotkeyProfile(String name)
	{
		this.name = name;
	}
	
	public HotkeyProfile(Element profile)
	{
		this.name = profile.getAttribute("name");
		
		String[] split = profile.getElementsByTagName("SplitKey").item(0).getTextContent().split(", ");
		if (split.length > 0)
		{
			this.eventSplitKey = getKeyConstant(split[0] );
			this.eventSplitLocation = getLocationConstant(split[0] );
			
			for (int i = 1; i < split.length; i ++)
				eventSplitModifier += getModifierConstant(split[i] );
		}
		
		String[] reset = profile.getElementsByTagName("ResetKey").item(0).getTextContent().split(", ");
		if (reset.length > 0)
		{
			this.eventResetKey = getKeyConstant(reset[0] );
			this.eventResetLocation = getLocationConstant(reset[0] );
			
			for (int i = 1; i < reset.length; i ++)
				eventResetModifier += getModifierConstant(reset[i] );
		}
		
		String[] skip = profile.getElementsByTagName("SkipKey").item(0).getTextContent().split(", ");
		if (skip.length > 0)
		{
			this.eventSkipKey = getKeyConstant(skip[0] );
			this.eventSkipLocation = getLocationConstant(skip[0] );
			
			for (int i = 1; i < skip.length; i ++)
				eventSkipModifier += getModifierConstant(skip[i] );
		}
		
		String[] undo = profile.getElementsByTagName("UndoKey").item(0).getTextContent().split(", ");
		if (undo.length > 0)
		{
			this.eventUndoKey = getKeyConstant(undo[0] );
			this.eventUndoLocation = getLocationConstant(undo[0] );
			
			for (int i = 1; i < undo.length; i ++)
				eventUndoModifier += getModifierConstant(undo[i] );
		}
	}
	
	public HotkeyProfile(Element profile, String current_profile_name)
	{
		this.name = profile.getElementsByTagName("name").item(0).getTextContent();
		
		String[] split = profile.getElementsByTagName("split").item(0).getTextContent().split(",");
		eventSplitKey = Integer.parseInt(split[0] );
		eventSplitLocation = Integer.parseInt(split[1] );
		eventSplitModifier = Integer.parseInt(split[2] );
		
		String[] reset = profile.getElementsByTagName("reset").item(0).getTextContent().split(",");
		eventResetKey = Integer.parseInt(reset[0] );
		eventResetLocation = Integer.parseInt(reset[1] );
		eventResetModifier = Integer.parseInt(reset[2] );
		
		String[] undo = profile.getElementsByTagName("undo").item(0).getTextContent().split(",");
		eventUndoKey = Integer.parseInt(undo[0] );
		eventUndoLocation = Integer.parseInt(undo[1] );
		eventUndoModifier = Integer.parseInt(undo[2] );
		
		String[] skip = profile.getElementsByTagName("skip").item(0).getTextContent().split(",");
		eventSkipKey = Integer.parseInt(skip[0] );
		eventSkipLocation = Integer.parseInt(skip[1] );
		eventSkipModifier = Integer.parseInt(skip[2] );
		
		if (name.equals(current_profile_name) )
			Hotkeys.activeProfile = this;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	public String getHotkeyDisplay(String type)
	{
		switch (type)
		{
			case "Split": return getModString(eventSplitModifier) + getKeyStr(eventSplitKey, eventSplitLocation);
			case "Reset": return getModString(eventResetModifier) + getKeyStr(eventResetKey, eventSplitLocation);
			case "Undo":  return getModString(eventUndoModifier)  + getKeyStr(eventUndoKey,  eventUndoLocation);
			case "Skip":  return getModString(eventSkipModifier)  + getKeyStr(eventSkipKey,  eventSkipLocation);
		}
		throw new RuntimeException("HotkeyProfile.getHotkeyDisplay(String) called with unsupported String \"" + type + "\"!");
	}
	
	private static String getKeyStr(int key, int location)
	{
		String res = "";
		if (location == 4 && key >= 2 && key <= 11)
			res = "NumPad";
		return res + NativeKeyEvent.getKeyText(key);
	}
	
	private static String getModString(int mod)
	{
		String res = NativeInputEvent.getModifiersText(mod);
		return res.isEmpty() ? "" : res.replace("+", ", ") + ", ";
	}
	
	public Element getXMLElement(Document doc)
	{
		Element result = doc.createElement("hotkey-profile");
		
		Element titleElement = doc.createElement("name");
		titleElement.setTextContent(name);
		result.appendChild(titleElement);
		
		Element splitElement = doc.createElement("split");
		splitElement.setTextContent(eventSplitKey + (eventSplitKey == 0 ? "" : "," + eventSplitLocation + "," + eventSplitModifier) );
		result.appendChild(splitElement);
		
		Element resetElement = doc.createElement("reset");
		resetElement.setTextContent(eventResetKey + (eventResetKey == 0 ? "" : "," + eventResetLocation + "," + eventResetModifier) );
		result.appendChild(resetElement);
		
		Element undoElement = doc.createElement("undo");
		undoElement.setTextContent(eventUndoKey + (eventUndoKey == 0 ? "" : "," + eventUndoLocation + "," + eventUndoModifier) );
		result.appendChild(undoElement);
		
		Element skipElement = doc.createElement("skip");
		skipElement.setTextContent(eventSkipKey + (eventSkipKey == 0 ? "" : "," + eventSkipLocation + "," + eventSkipModifier) );
		result.appendChild(skipElement);
		
		return result;
	}
	
	public int getHotkeyMatch(NativeKeyEvent event)
	{
		if (doesHotkeyMatch(event, eventSplitKey, eventSplitLocation, eventSplitModifier) ) return Hotkeys.HK_Split;
		if (doesHotkeyMatch(event, eventResetKey, eventResetLocation, eventResetModifier) ) return Hotkeys.HK_Reset;
		if (doesHotkeyMatch(event, eventUndoKey,  eventUndoLocation,  eventUndoModifier ) ) return Hotkeys.HK_Undo;
		if (doesHotkeyMatch(event, eventSkipKey,  eventSkipLocation,  eventSkipModifier ) ) return Hotkeys.HK_Skip;
		return Hotkeys.HK_Undefined;
	}
	
	private static boolean doesHotkeyMatch(NativeKeyEvent event, int compareToKey, int compareToKeyLocation, int compareToModifier)
	{
		if (event.getKeyCode() != compareToKey)
			return false;
		
		if (event.getKeyLocation() != compareToKeyLocation)
		{
			System.out.println("Location: '" + event.getKeyLocation() + "' instead of '" + compareToKeyLocation + "'");
			return false;
		}
		
		int mask = SpeedrunSettings.workaround_box.isSelected() ? 253 : 255;
		
		int modifiers = event.getModifiers() & mask; // mask for alt, ctrl, shift
		modifiers |= (modifiers << 4) & mask;        // add left to right
		modifiers |= modifiers >> 4;								// add right to left
		
		if (modifiers == compareToModifier)
			return true;
		
		System.out.println("Modifiers '" 	+
						NativeInputEvent.getModifiersText(event.getModifiers()	) + " (" + event.getModifiers() + "), " +
						NativeInputEvent.getModifiersText(modifiers							) + " (" + modifiers 						+ ") " + "' instead of '" +
						NativeInputEvent.getModifiersText(compareToModifier			) + " (" + compareToModifier 		+ ")'"
		);
		return false;
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
			case "ShiftKey": return NativeInputEvent.SHIFT_MASK;
			case "Control":  return NativeInputEvent.CTRL_MASK;
			case "Alt":      return NativeInputEvent.ALT_MASK;
			
			default:
				return 0;
		}
	}
}