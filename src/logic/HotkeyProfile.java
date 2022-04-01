package logic;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class HotkeyProfile
{
	public String name;
	public int eventSplitKey = 0;
	public int eventSplitLocation = 0;
	public int eventSplitModifier = 0;
	public int eventResetKey = 0;
	public int eventResetLocation = 0;
	public int eventResetModifier = 0;
	public int eventUndoKey = 0;
	public int eventUndoLocation = 0;
	public int eventUndoModifier = 0;
	public int eventSkipKey = 0;
	public int eventSkipLocation = 0;
	public int eventSkipModifier = 0;
	
	public HotkeyProfile(String name)
	{
		this.name = name;
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
		String res = NativeKeyEvent.getModifiersText(mod);
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
	
	public String getHotkeySettingsString()
	{
		String res = name;
		res += "\nSplit:" + eventSplitKey;
		if (eventSplitKey != 0)
			res += "," + eventSplitLocation + "," + eventSplitModifier;
	
		res += "\nReset:" + eventResetKey;
		if (eventResetKey != 0)
			res += "," + eventResetLocation + "," + eventResetModifier;
	
		res += "\nUndo:" + eventUndoKey;
		if (eventUndoKey != 0)
			res += "," + eventUndoLocation + "," + eventUndoModifier;
	
		res += "\nSkip:" + eventSkipKey;
		if (eventSkipKey != 0)
			res += "," + eventSkipLocation + "," + eventSkipModifier;
		
		return res;
	}
	
	public int getHotkeyMatch(NativeKeyEvent event)
	{
		if (doesHotkeyMatch(event, eventSplitKey, eventSplitLocation, eventSplitModifier) ) return Hotkeys.HK_Split;
		if (doesHotkeyMatch(event, eventResetKey, eventResetLocation, eventResetModifier) ) return Hotkeys.HK_Reset;
		if (doesHotkeyMatch(event, eventUndoKey,  eventUndoLocation,  eventUndoModifier ) ) return Hotkeys.HK_Undo;
		if (doesHotkeyMatch(event, eventSkipKey,  eventSkipLocation,  eventSkipModifier ) ) return Hotkeys.HK_Skip;
		return Hotkeys.HK_Undefined;
	}
	
	private boolean doesHotkeyMatch(NativeKeyEvent event, int compareToKey, int compareToKeyLocation, int compareToModifier)
	{
		if (event.getKeyCode() != compareToKey)
			return false;
		
		if (event.getKeyLocation() != compareToKeyLocation)
		{
			System.out.println("Location: '" + event.getKeyLocation() + "' instead of '" + compareToKeyLocation + "'");
			return false;
		}
		
		int mask = SpeedRunMode.workaround_box.isSelected() ? 253 : 255;
		
		int modifiers = event.getModifiers() & mask; // mask for alt, ctrl, shift
		modifiers |= (modifiers << 4) & mask;        // add left to right
		modifiers |= modifiers >> 4;								// add right to left
		
		if (modifiers == compareToModifier)
			return true;
		else
		{
			System.out.println("Modifiers '" 	+ NativeKeyEvent.getModifiersText(event.getModifiers() ) + " (" + event.getModifiers() + "), " +
																					NativeKeyEvent.getModifiersText(modifiers) + " (" + modifiers + ") " + "' instead of '" +
																					NativeKeyEvent.getModifiersText(compareToModifier) + " (" + compareToModifier + ")'");
			return false;
		}
	}
}