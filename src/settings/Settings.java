package settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.*;

import gui.GuiHelper;

public class Settings extends JDialog
{
	/** automatically generated UUID */
	private static final long serialVersionUID = 8624602459372136529L;
	
	private static ArrayList<SettingsTab> settingsTabsList = new ArrayList<SettingsTab>();
	
	public Settings(Window owner)
	{
		super(owner, "Settings", Dialog.DEFAULT_MODALITY_TYPE);
		
		JPanel mainPanel = new JPanel(new BorderLayout() );
		mainPanel.setBorder(GuiHelper.getDialogBorder() );
		mainPanel.setBackground(ColorSettings.getBackgroundColor() );
		
		JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		tabbedPane.setOpaque(true);
		tabbedPane.setBackground(ColorSettings.getBackgroundColor() );
		tabbedPane.setForeground(ColorSettings.getTextColor() );
		tabbedPane.setBorder(GuiHelper.getSpacingBorder(5) );
		
		settingsTabsList.add(new AbbreviationSettings(this) );
		settingsTabsList.add(new ColorSettings(this) );
		settingsTabsList.add(new SpeedrunSettings(this) );
		
		for (SettingsTab tab : settingsTabsList)
			tabbedPane.add( (Component) tab);
		
		for (int i = 0; i < tabbedPane.getTabCount(); i++)
			tabbedPane.setBackgroundAt(i, ColorSettings.getBackgroundColor() );
		
		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		
		
		JPanel controlsPanel = new JPanel();
		controlsPanel.setOpaque(false);
		controlsPanel.add(new JButton(new AbstractAction("Confirm") {
			/** automatically generated UUID */
			private static final long serialVersionUID = 8196630786304618587L;
			@Override public void actionPerformed(ActionEvent e) { saveSettings(); }
		}) );
		controlsPanel.add(new JButton(new AbstractAction("Cancel") {
			/** automatically generated UUID */
			private static final long serialVersionUID = 1535767868700988924L;
			@Override public void actionPerformed(ActionEvent e) { discardSettings(); }
		}) );
		mainPanel.add(controlsPanel, BorderLayout.SOUTH);
		
		this.add(mainPanel);
		
		this.setPreferredSize(new Dimension(500, 800) );
	}
	
	private void saveSettings()
	{
		for (SettingsTab tab : settingsTabsList)
			tab.save();
		
		this.setVisible(false);
	}
	
	private void discardSettings()
	{
		for (SettingsTab tab : settingsTabsList)
			tab.discard();
		
		this.setVisible(false);
	}
	
	public void showSettings()
	{
		for (SettingsTab tab : settingsTabsList)
			tab.update();
		
		this.pack();
		GuiHelper.resizeAndCenterRelativeToMainWindow(this);
		this.setVisible(true);
	}
	
	public interface SettingsTab
	{
		public void update();
		public void save();
		public void discard();
	}
}
