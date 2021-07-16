package logic;
import org.jnativehook.keyboard.NativeKeyEvent;

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
		
		int mask = Hotkeys.workaround_box.isSelected() ? 253 : 255;
		
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