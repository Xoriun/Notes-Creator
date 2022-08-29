package settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gui.GuiHelper;
import gui.MainGui;
import settings.Settings.SettingsTab;

public class ColorSettings extends JPanel implements SettingsTab
{
	/** auto-generated serialVersionUID */
	private static final long serialVersionUID = 8006096909719762398L;
	
	public static ColorSettingProfile[] colorSettingProfiles;
	public static ColorSettingProfile currentColorSetting;
	
	private static JTextField[] customColorFields = new JTextField[9];
	
	private static JRadioButton light_theme_button;
	private static JRadioButton dark_theme_button;
	private static JRadioButton custom_theme_button;
	
	private static JPanel customDisplayPanel;
	private static JLabel customDisplayLabel;

	public ColorSettings(JDialog settings_dialog)
	{
		super();
		
		this.setName("Color theme");
		
		this.setLayout(new BorderLayout() );
		this.setOpaque(false);
		
		JPanel main_panel = new JPanel();
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS) );
		main_panel.setOpaque(false);
		
		JPanel active_color_panel = new JPanel(new GridBagLayout() );
		active_color_panel.setOpaque(false);
		active_color_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Active color") );
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		
		light_theme_button  = new JRadioButton("Light theme");
		light_theme_button.setForeground(ColorSettings.getTextColor() );
		light_theme_button.setBackground(ColorSettings.getBackgroundColor() );
		light_theme_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentColorSetting = colorSettingProfiles[0];
			}
		});
		dark_theme_button   = new JRadioButton("Dark theme");
		dark_theme_button.setForeground(ColorSettings.getTextColor() );
		dark_theme_button.setBackground(ColorSettings.getBackgroundColor() );
		dark_theme_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentColorSetting = colorSettingProfiles[1];
			}
		});
		custom_theme_button = new JRadioButton("Custom theme");
		custom_theme_button.setForeground(ColorSettings.getTextColor() );
		custom_theme_button.setBackground(ColorSettings.getBackgroundColor() );
		custom_theme_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentColorSetting = colorSettingProfiles[2];
			}
		});
		
		ButtonGroup theme_group = new ButtonGroup();
		theme_group.add(light_theme_button);
		theme_group.add(dark_theme_button);
		theme_group.add(custom_theme_button);
		
		active_color_panel.add(light_theme_button, gbc);
		gbc.gridy ++;
		active_color_panel.add(dark_theme_button, gbc);
		gbc.gridy ++;
		active_color_panel.add(custom_theme_button, gbc);
		
		gbc.gridx = 1;
		
		for (int i = 0; i < 3; i ++)
		{
			gbc.gridy = i;
			JPanel display_panel = new JPanel();
			display_panel.setBackground(colorSettingProfiles[i].getBackgroundColor() );
			display_panel.setOpaque(true);
			JLabel display_label = new JLabel(" Content ");
			display_label.setForeground(colorSettingProfiles[i].getTextColor() );
			display_label.setBackground(colorSettingProfiles[i].getBackgroundColor() );
			display_label.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, colorSettingProfiles[i].getBorderColor() ) );
			display_label.setOpaque(true);
			display_panel.add(display_label);
			active_color_panel.add(display_panel, gbc);
			
			if (i == 2)
			{
				customDisplayPanel = display_panel;
				customDisplayLabel = display_label;
			}
		}
		
		main_panel.add(active_color_panel);
		
		JPanel custom_panel = new JPanel(new GridBagLayout() );
		custom_panel.setOpaque(false);
		custom_panel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Custom color") );
		gbc.gridx = gbc.gridy = 0;
		
		gbc.gridx = 1;
		custom_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("R", GuiHelper.CENTER), gbc);
		gbc.gridx = 2;
		custom_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("G", GuiHelper.CENTER), gbc);
		gbc.gridx = 3;
		custom_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("B", GuiHelper.CENTER), gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		custom_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Text color", GuiHelper.LEFT), gbc);
		gbc.gridy = 2;
		custom_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Border color", GuiHelper.LEFT), gbc);
		gbc.gridy = 3;
		custom_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Background color", GuiHelper.LEFT), gbc);
		
		for (int i = 0; i < 9; i ++)
		{
			JTextField field = new JTextField(3);
			field.setBackground(currentColorSetting.getBackgroundColor() );
			field.setForeground(currentColorSetting.getTextColor() );
			customColorFields[i] = field;
			
			gbc.gridx = i%3 + 1;
			gbc.gridy = i/3 + 1;
			custom_panel.add(field, gbc);
			
			field.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e)
				{
					updateCustomDisplayPanel();
					settings_dialog.pack();
				}
				
				@Override
				public void insertUpdate(DocumentEvent e)
				{
					updateCustomDisplayPanel();
					settings_dialog.pack();
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) { }
			});
		}
		
		main_panel.add(custom_panel);
		
		this.add(main_panel, BorderLayout.NORTH);
	}
	
	public static Color getTextColor() { return currentColorSetting.getTextColor(); }
	public static Color getBackgroundColor() { return currentColorSetting.getBackgroundColor(); }
	public static Color getBorderColor() { return currentColorSetting.getBorderColor(); }

	@Override
	public void update()
	{
		switch(currentColorSetting.getName() )
		{
			case "Dark":
				dark_theme_button.setSelected(true);
				break;
			case "Light":
				light_theme_button.setSelected(true);
				break;
			case "Custom":
				custom_theme_button.setSelected(true);
				break;
		}

		for (int i = 0; i < 9; i ++)
		{
			Color color = i/3==0 ? colorSettingProfiles[2].getTextColor() : i/3==1 ? colorSettingProfiles[2].getBorderColor() : colorSettingProfiles[2].getBackgroundColor();
			customColorFields[i].setText("" + (i%3==0 ? color.getRed() : i%3==1 ? color.getGreen() : color.getBlue() ) );
		}
	}

	@Override
	public void save()
	{
		updateCustomColorSettings();
		MainGui.updateLightingSettings();
	}

	@Override
	public void discard()
	{
		//TODO unsaved changes
		
		// nothing to do
	}
	
	private static void updateCustomDisplayPanel()
	{
		//TODO handle incorrect number formats
		
		customDisplayPanel.setBackground(getColor(customColorFields[6], customColorFields[7], customColorFields[8] ) );
		customDisplayLabel.setForeground(getColor(customColorFields[0], customColorFields[1], customColorFields[2] ) );
		customDisplayLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, getColor(customColorFields[3], customColorFields[4], customColorFields[5] ) ) );
		customDisplayLabel.setBackground(getColor(customColorFields[6], customColorFields[7], customColorFields[8] ) );
	}
	
	public static String getCurrentColorSettingName() { return currentColorSetting.getName(); }

	private static void updateCustomColorSettings()
	{
		ColorSettings.colorSettingProfiles[2].update(
				getColor(customColorFields[0], customColorFields[1], customColorFields[2] ),
				getColor(customColorFields[3], customColorFields[4], customColorFields[5] ),
				getColor(customColorFields[6], customColorFields[7], customColorFields[8] ) );
	}
	
	private static Color getColor(JTextField text_red, JTextField text_green, JTextField text_blue)
	{
		int red = getColorInt(text_red);
		int green = getColorInt(text_green);
		int blue = getColorInt(text_blue);
		
		if (red == -1 || green == -1 || blue == -1)
			return Color.RED;
		
		return new Color(red, green, blue);
	}

	private static int getColorInt(JTextField text)
	{
		int res = 0;
		try {
			res = Integer.parseInt(text.getText() );
		} catch (NumberFormatException e) { res = -1;  }
		
		if (res >= 0 && res <= 255)
			text.setBackground(currentColorSetting.getBackgroundColor() );
		else
			text.setBackground(Color.red);
		
		return res;
	}
}
