package settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;

import gui.GuiHelper;
import gui.MainGui;
import logic.LiveSplitConnection;
import logic.Section;
import settings.Settings.SettingsTab;

public class SpeedrunSettings extends JPanel implements SettingsTab
{
	/** auto-generated serialVersionUID */
	private static final long serialVersionUID = 437403026369975780L;
	
	/**
	 * Determines whether of not the program is in speedrun mode or not.
	 * This enables splits as well as actions.
	 */
	public static boolean speedrunModeEnabled = false;
	
	/**
	 * Whether or not to use Events from the LiveSplitAPI Component instead of separate hotkeys.
	 * Default is true. 
	 */
	private static boolean useLiveSplitAPI = true;
	
	/**
	 * The index of the currently selected Section. Negative values indicate that the run was reset or has been started.
	 */
	private static int currentSectionIndex = -1;
	
	private static final int SPLIT_SCROLL = 0;
	private static final int SPLIT_HIDE   = 1;
	
	private static int splitType = SPLIT_SCROLL;
	
	private static JRadioButton buttonLivesplit;
	private static JRadioButton buttonHotkeys;
	
	private static JLabel split;
	private static JLabel reset;
	private static JLabel undo;
	private static JLabel skip;
	public static JCheckBox workaround_box = new JCheckBox();
	
	private static ArrayList<HotkeyProfile> profiles_copy;
	
	private static JComboBox<HotkeyProfile> profileComboBox = new JComboBox<HotkeyProfile>();

	public SpeedrunSettings(JDialog settings_dialog)
	{
		super();
		
		this.setName("Speedrun");
		
		this.setLayout(new BorderLayout() );
		this.setOpaque(false);
		
	// settings panel
		JPanel main_panel = new JPanel();
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS) );
		main_panel.setOpaque(false);
		
		// hotkeys or LiveSplitAPI
		JPanel live_panel = new JPanel();
		live_panel.setOpaque(false);
		live_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Use LiveSplitAPI or separate hotkeys?", GuiHelper.LEFT) );
		buttonLivesplit = new JRadioButton("LiveSplit");
		buttonLivesplit.setOpaque(false);
		buttonLivesplit.setForeground(ColorSettings.getTextColor() );
		buttonHotkeys = new JRadioButton("Hotkeys");
		buttonHotkeys.setOpaque(false);
		buttonHotkeys.setForeground(ColorSettings.getTextColor() );
		ButtonGroup live_button_group = new ButtonGroup();
		live_button_group.add(buttonLivesplit);
		live_button_group.add(buttonHotkeys);
		live_panel.add(buttonLivesplit);
		live_panel.add(buttonHotkeys);
		main_panel.add(live_panel);
		
		
		// profile combobox
		JPanel profile_panel = new JPanel();
		profile_panel.setOpaque(false);
		profile_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Active profile: ", GuiHelper.LEFT) );
		profileComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action)
			{
				HotkeyProfile profile = profiles_copy.get(profileComboBox.getSelectedIndex() );
				split.setText(profile.getHotkeyDisplay("Split") );
				reset.setText(profile.getHotkeyDisplay("Reset") );
				undo .setText(profile.getHotkeyDisplay("Undo" ) );
				skip .setText(profile.getHotkeyDisplay("Skip" ) );
				Hotkeys.activeProfile = profile;
			}
		});
		profile_panel.add(profileComboBox);
		main_panel.add(profile_panel);
		
		// labels
		
		split = GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("", GuiHelper.LEFT);
		reset = GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("", GuiHelper.LEFT);
		undo  = GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("", GuiHelper.LEFT);
		skip  = GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("", GuiHelper.LEFT);
			
		JPanel split_display_panel = new JPanel(new GridLayout(4, 2) );
		split_display_panel.setOpaque(false);
		split_display_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Split:  ", GuiHelper.RIGHT) );
		split_display_panel.add(split);
		split_display_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Reset:  ", GuiHelper.RIGHT) );
		split_display_panel.add(reset);
		split_display_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Undo:  ",  GuiHelper.RIGHT) );
		split_display_panel.add(undo);
		split_display_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Skip:  ",  GuiHelper.RIGHT) );
		split_display_panel.add(skip);
		main_panel.add(split_display_panel);
		
		// load Button
		JButton load = new JButton("Load hotkey profiles form LiveSplit");
		load.setAlignmentX(Component.CENTER_ALIGNMENT);
		load.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				Hotkeys.loadHotkeyProfilesFromLiveSplitSettings(profiles_copy);
				update();
			}
		});
		main_panel.add(load);
		
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
		workaraound_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Currently, the 'Control' modifier doesn't work porperly!", GuiHelper.LEFT) );
		workaround_box = new JCheckBox("Ignore the 'Control' Modifier entirely! (false by default)");
		workaround_box.setOpaque(false);
		workaround_box.setForeground(ColorSettings.getTextColor() );
		workaraound_panel.add(workaround_box);
		
		main_panel.add(workaraound_panel);
		
		this.add(main_panel, BorderLayout.NORTH);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void update()
	{
		if (useLiveSplitAPI)
			buttonLivesplit.setSelected(true);
		else
			buttonHotkeys.setSelected(true);
		
		if (profiles_copy == null || profiles_copy.isEmpty() )
			profiles_copy = (ArrayList<HotkeyProfile>) Hotkeys.profiles.clone();
		
		profileComboBox.setModel(new DefaultComboBoxModel<>(profiles_copy.toArray(new HotkeyProfile[profiles_copy.size() ] ) ) );
		profileComboBox.setSelectedItem(Hotkeys.activeProfile);

		HotkeyProfile profile = (HotkeyProfile) profileComboBox.getSelectedItem();
		
		split.setText(profile.getHotkeyDisplay("Split") );
		reset.setText(profile.getHotkeyDisplay("Reset") );
		undo .setText(profile.getHotkeyDisplay("Undo") );
		skip .setText(profile.getHotkeyDisplay("Skip") );
		
		workaround_box = new JCheckBox("Ignore the 'Control' Modifier entirely! (false by default)", workaround_box.isSelected() );
	}
	
	@Override
	public void save()
	{
		Hotkeys.profiles = profiles_copy;
	}
	
	@Override
	public void discard()
	{
		profiles_copy.clear();
	}
	
	public static void updateSpeedRunMode(boolean speedrun_mode_enabled)
	{
		speedrunModeEnabled = speedrun_mode_enabled;
		updateSpeedRunMode();
	}
	
	private static void updateSpeedRunMode()
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
		if (currentSectionIndex >= MainGui.sectionsList.size() )
			return;
		
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
		if (currentSectionIndex >= MainGui.sectionsList.size() )
			return;
		
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
}
