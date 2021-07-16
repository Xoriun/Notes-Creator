package logic;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Dialog.ModalityType;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
//import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
//import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import gui.ColorSettings;
import gui.MainGui;
import gui.PopupAlerts;
import logic.HotkeyProfile;

public class Hotkeys implements NativeKeyListener
{
	public static ArrayList<HotkeyProfile> profiles = new ArrayList<HotkeyProfile>();
	public static HotkeyProfile activeProfile;
	private static JLabel split = new JLabel();
	private static JLabel reset = new JLabel();
	private static JLabel undo  = new JLabel();
	private static JLabel skip  = new JLabel();
	public static JCheckBox workaround_box = new JCheckBox();;
	
	public static boolean isSpeedrunModeEnabled = false;
	public static int currentSectionIndex = -1;
	
	public static final int HK_Split = 0;
	public static final int HK_Reset = 1;
	public static final int HK_Undo  = 2;
	public static final int HK_Skip  = 3;
	public static final int HK_Undefined = -1;
	
	private static final int SPLIT_SCROLL = 0;
	private static final int SPLIT_HIDE   = 1;
	
	private static int splitType = SPLIT_SCROLL;
	
	public Hotkeys()
	{
		profiles.add(new HotkeyProfile("Undefined") );
		activeProfile = profiles.get(0);
		
		split.setOpaque(false);
		reset.setOpaque(false);
		undo.setOpaque(false);
		skip.setOpaque(false);
	}
	
	@Override
 	public void nativeKeyPressed(NativeKeyEvent e)
	{
		if (isSpeedrunModeEnabled)
		{
			switch (activeProfile.getHotkeyMatch(e))
			{
				case HK_Undefined: return;
				case HK_Split:
				case HK_Skip:
					currentSectionIndex ++;
					if (currentSectionIndex < MainGui.sectionsList.size() )
						if (splitType == SPLIT_SCROLL)
							MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(currentSectionIndex).getScrollLocation() );
						else
						{
							MainGui.mainPanel.removeAll();
							MainGui.mainPanel.add(MainGui.sectionsList.get(currentSectionIndex) );
						}
					break;
				case HK_Reset:
					currentSectionIndex = -1;
					MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(0).getScrollLocation() );
					if (splitType == SPLIT_HIDE)
					{
						MainGui.mainPanel.removeAll();
						for (Section section : MainGui.sectionsList)
							MainGui.mainPanel.add(section );
					}
					break;
				case HK_Undo:
					if (currentSectionIndex >= 0)
						currentSectionIndex --;
					if (currentSectionIndex < 0)
					{
						if (splitType == SPLIT_SCROLL)
							MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(0).getScrollLocation() );
						else
						{
							MainGui.mainPanel.removeAll();
							for (Section section : MainGui.sectionsList)
								MainGui.mainPanel.add(section );
						}
					}
					else
						if (splitType == SPLIT_SCROLL)
							MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(currentSectionIndex).getScrollLocation() );
						else
						{
							MainGui.mainPanel.removeAll();
							MainGui.mainPanel.add(MainGui.sectionsList.get(currentSectionIndex) );
						}
					break;
			}
			if (splitType == SPLIT_HIDE)
			{
				MainGui.keepGuiSize = false;
				MainGui.window.pack();
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
	
	@SuppressWarnings("unchecked")
	public static void showHotkeySettingsWindow()
	{
		showHotkeySettingsWindow((ArrayList<HotkeyProfile>) Hotkeys.profiles.clone() );
	}
	
	public static void showHotkeySettingsWindow(ArrayList<HotkeyProfile> profiles_copy)
	{
		JDialog dialog = new JDialog(MainGui.window);
		dialog.setTitle("Hotkey settings");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		
		// main panel
			JPanel main_panel = new JPanel();
			main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS) );
			main_panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5) );
			main_panel.setBackground(ColorSettings.currentColorSetting.background);
		
		// settings panel
			JPanel settings_panel = new JPanel();
			settings_panel.setLayout(new BoxLayout(settings_panel, BoxLayout.Y_AXIS) );
			settings_panel.setOpaque(false);
			
			// profile combobox
			JPanel profile_panel = new JPanel();
			profile_panel.setOpaque(false);
			profile_panel.add(ColorSettings.getNewJLabelWithCurrentTextColor("Active profile: ") );
			JComboBox<HotkeyProfile> comboBox = new JComboBox<HotkeyProfile>( profiles_copy.toArray(new HotkeyProfile[profiles_copy.size() ] ) );
			comboBox.setSelectedItem(activeProfile);
			comboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					HotkeyProfile profile = profiles_copy.get(comboBox.getSelectedIndex() );
					split.setText(profile.getHotkeyDisplay("Split") );
					reset.setText(profile.getHotkeyDisplay("Reset") );
					undo .setText(profile.getHotkeyDisplay("Undo" ) );
					skip .setText(profile.getHotkeyDisplay("Skip" ) );
				}
			});
			profile_panel.add(comboBox);
			settings_panel.add(profile_panel);
			
			// labels
			HotkeyProfile profile = profiles_copy.get(comboBox.getSelectedIndex() );
			if (profile != null)
			{
				split.setText(profile.getHotkeyDisplay("Split") );
				split.setForeground(ColorSettings.currentColorSetting.text);
				reset.setText(profile.getHotkeyDisplay("Reset") );
				reset.setForeground(ColorSettings.currentColorSetting.text);
				undo .setText(profile.getHotkeyDisplay("Undo" ) );
				undo .setForeground(ColorSettings.currentColorSetting.text);
				skip .setText(profile.getHotkeyDisplay("Skip" ) );
				skip .setForeground(ColorSettings.currentColorSetting.text);
			}
			JPanel split_panel = new JPanel();
			split_panel.setOpaque(false);
			split_panel.add(new JLabel("Split: ") );
			split_panel.add(split);
			settings_panel.add(split_panel);
			JPanel reset_panel = new JPanel();
			reset_panel.setOpaque(false);
			reset_panel.add(new JLabel("Reset: ") );
			reset_panel.add(reset);
			settings_panel.add(reset_panel);
			JPanel undo_panel = new JPanel();
			undo_panel.setOpaque(false);
			undo_panel.add(new JLabel("Undo:  ") );
			undo_panel.add(undo);
			settings_panel.add(undo_panel);
			JPanel skip_panel = new JPanel();
			skip_panel.setOpaque(false);
			skip_panel.add(new JLabel("Skip:  ") );
			skip_panel.add(skip);
			settings_panel.add(skip_panel);
			
			// load Button
			JButton load = new JButton("Load hotkey profiles form LiveSplit");
			load.setAlignmentX(Component.CENTER_ALIGNMENT);
			load.addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					selectLiveSplitFile(profiles_copy);
					dialog.dispose();
					showHotkeySettingsWindow(profiles_copy);
				}
			});
			settings_panel.add(load);
			
			// CTRL workaraound
			JPanel workaraound_panel = new JPanel();
			workaraound_panel.setAlignmentX(Component.CENTER_ALIGNMENT);
			workaraound_panel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(Color.RED, 2),
					"Warnig",
					TitledBorder.CENTER,
					TitledBorder.DEFAULT_POSITION,
					null,
					Color.RED) );
			workaraound_panel.setLayout(new BoxLayout(workaraound_panel, BoxLayout.Y_AXIS) );
			workaraound_panel.setOpaque(false);
			workaraound_panel.add(ColorSettings.getNewJLabelWithCurrentTextColor("Currently, the 'Control' modifier doesn't work porperly!") );
			workaround_box = new JCheckBox("Ignore the 'Control' Modifier entirely! (false by default)", workaround_box.isSelected() );
			workaround_box.setOpaque(false);
			workaround_box.setForeground(ColorSettings.currentColorSetting.text);
			workaraound_panel.add(workaround_box);
			
			settings_panel.add(workaraound_panel);
			
			/**
			// split type
			JPanel splittype_panel = new JPanel();
			//split_panel.setLayout(new BoxLayout(splittype_panel, BoxLayout.Y_AXIS) );
			splittype_panel.setBorder(BorderFactory.createTitledBorder("Split type") );
			
			JRadioButton scroll_radio = new JRadioButton("Scroll to to section");
			scroll_radio.addActionListener(e -> { if (scroll_radio.isSelected() ) splitType = SPLIT_SCROLL; } );
			JRadioButton hide_radio   = new JRadioButton("Hide other sections");
			hide_radio.addActionListener(e -> { if (hide_radio.isSelected() ) splitType = SPLIT_HIDE; } );
			
			ButtonGroup type_group = new ButtonGroup();
			type_group.add(scroll_radio);
			type_group.add(hide_radio);
			
			splittype_panel.add(scroll_radio);
			scroll_radio.setSelected(splitType == SPLIT_SCROLL);
			splittype_panel.add(hide_radio);
			hide_radio.setSelected(splitType == SPLIT_HIDE);
			
			settings_panel.add(splittype_panel);
			*/
		
		// controls panel
			JPanel controls_panel = new JPanel();
			controls_panel.setOpaque(false);
			
			JButton confirm = new JButton("Confim");
			confirm.addActionListener(e -> {
				profiles = profiles_copy;
				activeProfile = profiles_copy.get(comboBox.getSelectedIndex() );
				dialog.dispose();
			});
			JButton cancel  = new JButton("Cancel");
			cancel.addActionListener(e -> { dialog.dispose(); } );
			
			controls_panel.add(confirm);
			controls_panel.add(cancel);
		
		main_panel.add(settings_panel);
		main_panel.add(controls_panel);
		dialog.add(main_panel);
		dialog.pack();
		PopupAlerts.setLocationToCenter(dialog);
		dialog.setVisible(true);
	}
	
	public static void selectLiveSplitFile(ArrayList<HotkeyProfile> new_profiles)
	{
		FileDialog dialog = new FileDialog(MainGui.window, "Select 'LiceSplit.exe'", FileDialog.LOAD);
		PopupAlerts.setLocationToCenter(dialog);
		dialog.setVisible(true);
		String dir = dialog.getDirectory();
		
		if (dir != null)
			importHotkeySettings(dir, new_profiles);
	}

	public static void readHotkeySetting(String[] setting, HotkeyProfile profile)
	{
		String[] keys = setting[1].split(",");
		if (keys == null || keys.length == 0 || keys[0].equals("0") ) // undefined hotkey
			return;
		switch (setting[0] )
		{
			case "Split":
				profile.eventSplitKey      = Integer.valueOf(keys[0] );
				profile.eventSplitLocation = Integer.valueOf(keys[1] );
				for (int i = 2; i < keys.length; i ++)
					profile.eventSplitModifier += Integer.valueOf(keys[i] );
				break;
				
			case "Reset":
				profile.eventResetKey      = Integer.valueOf(keys[0] );
				profile.eventResetLocation = Integer.valueOf(keys[1] );
				for (int i = 2; i < keys.length; i ++)
					profile.eventResetModifier += Integer.valueOf(keys[i] );
				break;
				
			case "Skip":
				profile.eventSkipKey      = Integer.valueOf(keys[0] );
				profile.eventSkipLocation = Integer.valueOf(keys[1] );
				for (int i = 2; i < keys.length; i ++)
					profile.eventSkipModifier += Integer.valueOf(keys[i] );
				break;
				
			case "Undo":
				profile.eventUndoKey      = Integer.valueOf(keys[0] );
				profile.eventUndoLocation = Integer.valueOf(keys[1] );
				for (int i = 2; i < keys.length; i ++)
					profile.eventUndoModifier += Integer.valueOf(keys[i] );
				break;
		}
	}

	public static void importHotkeySettings(String dir_location, ArrayList<HotkeyProfile> new_profiles)
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
	
}
