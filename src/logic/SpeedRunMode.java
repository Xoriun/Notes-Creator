package logic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;

public class SpeedRunMode
{
	/**
	 * Determines whether of not the program is in speedrun mode or not.
	 * This enables splits as well as actions.
	 */
	public static boolean speedrunModeEnabled = false;
	
	/**
	 * Whether or not to use Events from the LiveSplitAPI Component instead of separate hotkeys.
	 * Default is true. 
	 */
	public static boolean useLiveSplitAPI = true;
	
	/**
	 * The index of the currently selected Section. Negative values indicate that the run was reset or has been started.
	 */
	private static int currentSectionIndex = -1;
	
	private static final int SPLIT_SCROLL = 0;
	private static final int SPLIT_HIDE   = 1;
	
	private static int splitType = SPLIT_SCROLL;
	
	static JLabel split;
	static JLabel reset;
	static JLabel undo;
	static JLabel skip;
	public static JCheckBox workaround_box = new JCheckBox();
	
	public static void updateSpeedRunMode(boolean speedrun_mode_enabled)
	{
		speedrunModeEnabled = speedrun_mode_enabled;
		updateSpeedRunMode();
	}
	
	public static void updateSpeedRunMode()
	{
		if (useLiveSplitAPI)
		{
			Hotkeys.stopListeningForHotkeys();
			if (speedrunModeEnabled)
				LiveSplitConnection.startLiveSplitCommunication();
			else
				LiveSplitConnection.holdLiveSplitCommunication();
		}
		else
		{
			LiveSplitConnection.holdLiveSplitCommunication();
			if (speedrunModeEnabled)
				Hotkeys.startListeningForHotkeys();
			else
				Hotkeys.stopListeningForHotkeys();
		}
	}
	
	public static void startOrSplit()
	{
		if (currentSectionIndex >= 0)
			split();
		else
			start();
	}
	
	public static void start()
	{
		currentSectionIndex = 0;
			if (splitType == SPLIT_SCROLL)
				MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(currentSectionIndex).getScrollLocation() );
			else
			{
				MainGui.mainPanel.removeAll();
				MainGui.mainPanel.add(MainGui.sectionsList.get(currentSectionIndex) );
			}
	}
	
	public static void split()
	{
		currentSectionIndex ++;
		if (currentSectionIndex < MainGui.sectionsList.size() )
			if (splitType == SPLIT_SCROLL)
				MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(currentSectionIndex).getScrollLocation() );
			else
			{
				MainGui.mainPanel.removeAll();
				MainGui.mainPanel.add(MainGui.sectionsList.get(currentSectionIndex) );
			}
	}
	
	public static void skip()
	{
		if (currentSectionIndex < 0)
			return;
		currentSectionIndex ++;
		if (currentSectionIndex < MainGui.sectionsList.size() )
			if (splitType == SPLIT_SCROLL)
				MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(currentSectionIndex).getScrollLocation() );
			else
			{
				MainGui.mainPanel.removeAll();
				MainGui.mainPanel.add(MainGui.sectionsList.get(currentSectionIndex) );
			}
	}
	
	public static void reset()
	{
		currentSectionIndex = -1;
		MainGui.scrollPane.getVerticalScrollBar().setValue(MainGui.sectionsList.get(0).getScrollLocation() );
		if (splitType == SPLIT_HIDE)
		{
			MainGui.mainPanel.removeAll();
			for (Section section : MainGui.sectionsList)
				MainGui.mainPanel.add(section );
		}
	}
	
	public static void undoSplit()
	{
		// undoSplit does nothing when the first split is currently running or the run hasn't started yet
		if (currentSectionIndex <= 0)
			return;
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
	}

	@SuppressWarnings("unchecked")
	public static void showSpeedrunSettingsWindow()
	{
		showSpeedrunSettingsWindow((ArrayList<HotkeyProfile>) Hotkeys.profiles.clone() );
	}

	public static void showSpeedrunSettingsWindow(ArrayList<HotkeyProfile> profiles_copy)
	{
		JDialog dialog = new JDialog(MainGui.window);
		dialog.setTitle("Hotkey settings");
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		
		// main panel
			JPanel main_panel = new JPanel();
			main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS) );
			main_panel.setBorder(GuiHelper.getDialogBorder() );
			main_panel.setBackground(ColorSettings.getBackgroundColor() );
		
		// settings panel
			JPanel settings_panel = new JPanel();
			settings_panel.setLayout(new BoxLayout(settings_panel, BoxLayout.Y_AXIS) );
			settings_panel.setOpaque(false);
			
			// hotkeys or LiveSplitAPI
			JPanel live_panel = new JPanel();
			live_panel.setOpaque(false);
			live_panel.add(GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor("Use LiveSplitAPI or separate hotkeys?") );
			JRadioButton live_radio_button = new JRadioButton("LiveSplit", useLiveSplitAPI);
			live_radio_button.setOpaque(false);
			live_radio_button.setForeground(ColorSettings.getTextColor() );
			JRadioButton hotkeys_button = new JRadioButton("Hotkeys", !useLiveSplitAPI);
			hotkeys_button.setOpaque(false);
			hotkeys_button.setForeground(ColorSettings.getTextColor() );
			ButtonGroup live_button_group = new ButtonGroup();
			live_button_group.add(live_radio_button);
			live_button_group.add(hotkeys_button);
			live_panel.add(live_radio_button);
			live_panel.add(hotkeys_button);
			settings_panel.add(live_panel);
			
			
			// profile combobox
			JPanel profile_panel = new JPanel();
			profile_panel.setOpaque(false);
			profile_panel.add(GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor("Active profile: ") );
			JComboBox<HotkeyProfile> comboBox = new JComboBox<HotkeyProfile>( profiles_copy.toArray(new HotkeyProfile[profiles_copy.size() ] ) );
			comboBox.setSelectedItem(Hotkeys.activeProfile);
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
				split = GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor(profile.getHotkeyDisplay("Split") );
				reset = GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor(profile.getHotkeyDisplay("Reset") );
				undo = GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor(profile.getHotkeyDisplay("Undo") );
				skip = GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor(profile.getHotkeyDisplay("Skip") );
			}
			JPanel split_display_panel = new JPanel(new GridLayout(4, 2) );
			split_display_panel.setOpaque(false);
			split_display_panel.add(GuiHelper.getRightAlignedNonOpaqueJLabelWithCurrentTextColor("Split:  ") );
			split_display_panel.add(split);
			split_display_panel.add(GuiHelper.getRightAlignedNonOpaqueJLabelWithCurrentTextColor("Reset:  ") );
			split_display_panel.add(reset);
			split_display_panel.add(GuiHelper.getRightAlignedNonOpaqueJLabelWithCurrentTextColor("Undo:  ") );
			split_display_panel.add(undo);
			split_display_panel.add(GuiHelper.getRightAlignedNonOpaqueJLabelWithCurrentTextColor("Skip:  ") );
			split_display_panel.add(skip);
			settings_panel.add(split_display_panel);
			
			// load Button
			JButton load = new JButton("Load hotkey profiles form LiveSplit");
			load.setAlignmentX(Component.CENTER_ALIGNMENT);
			load.addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					Hotkeys.selectLiveSplitFile(profiles_copy);
					dialog.dispose();
					showSpeedrunSettingsWindow(profiles_copy);
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
			workaraound_panel.add(GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor("Currently, the 'Control' modifier doesn't work porperly!") );
			workaround_box = new JCheckBox("Ignore the 'Control' Modifier entirely! (false by default)", workaround_box.isSelected() );
			workaround_box.setOpaque(false);
			workaround_box.setForeground(ColorSettings.getTextColor() );
			workaraound_panel.add(workaround_box);
			
			settings_panel.add(workaraound_panel);
			
			/*
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
				Hotkeys.profiles = profiles_copy;
				Hotkeys.activeProfile = profiles_copy.get(comboBox.getSelectedIndex() );
				useLiveSplitAPI = live_radio_button.isSelected();
				updateSpeedRunMode();
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
		GuiHelper.resizeAndCenterRelativeToMainWindow(dialog);
		dialog.setVisible(true);
	}
	
}
