package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import edit.CellEditDialog;
import logic.Row;
import logic.Section;

public class ColorSettings
{
	public static ColorSettingProfile[] colorSettingProfiles;
	public static ColorSettingProfile currentColorSetting;
	
	public static Color getTextColor() { return currentColorSetting.getTextColor(); }
	public static Color getBackgroundColor() { return currentColorSetting.getBackgroundColor(); }
	public static Color getBorderColor() { return currentColorSetting.getBorderColor(); }
	public static String getCurrentColorSettingName() { return currentColorSetting.getName(); }
	
	static void selectColorSettings(int index)
	{
		currentColorSetting = colorSettingProfiles[index];
		applyLightingMode();
	}

	private static void fillColorSettingsPane(JPanel options_panel, ColorSettingProfile color_setting)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = gbc.gridy = 0;
		fillColorSettingsRow(options_panel, gbc, color_setting.getTextColor(), "Text");
		fillColorSettingsRow(options_panel, gbc, color_setting.getBorderColor(), "Border");
		fillColorSettingsRow(options_panel, gbc, color_setting.getBackgroundColor(), "Background   ");
	}

	private static void fillColorSettingsRow(JPanel panel, GridBagConstraints gbc, Color color, String name)
	{
		gbc.gridx = 0;
		JLabel label_name = new JLabel(name);
		label_name.setForeground(ColorSettings.getTextColor() );
		label_name.setOpaque(false);
		panel.add(label_name, gbc);
		
		gbc.gridx = 1;
		JLabel label_r = new JLabel(" r ");
		label_r.setForeground(ColorSettings.getTextColor() );
		label_r.setOpaque(false);
		panel.add(label_r, gbc);
		gbc.gridx = 2;
		JTextField text1 = new JTextField("" + color.getRed(), 3);
		text1.setForeground(ColorSettings.getTextColor() );
		text1.setBackground(ColorSettings.getBackgroundColor() );
		text1.setBorder(GuiHelper.getDefaultBorder(name.equals("Text"), true) );
		text1.setName(name + "r");
		panel.add(text1, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		JLabel label_g = new JLabel(" g ");
		label_g.setForeground(ColorSettings.getTextColor() );
		label_g.setOpaque(false);
		panel.add(label_g, gbc);
		gbc.gridx = 2;
		JTextField text2 = new JTextField("" + color.getGreen(), 3);
		text2.setForeground(ColorSettings.getTextColor() );
		text2.setBackground(ColorSettings.getBackgroundColor() );
		text2.setBorder(GuiHelper.getDefaultBorder(false, true) );
		text2.setName(name + "g");
		panel.add(text2, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		JLabel label_b = new JLabel(" b ");
		label_b.setForeground(ColorSettings.getTextColor() );
		label_b.setOpaque(false);
		panel.add(label_b, gbc);
		gbc.gridx = 2;
		JTextField text3 = new JTextField("" + color.getBlue(), 3);
		text3.setForeground(ColorSettings.getTextColor() );
		text3.setBackground(ColorSettings.getBackgroundColor() );
		text3.setBorder(GuiHelper.getDefaultBorder(false, true) );
		text3.setName(name + "b");
		panel.add(text3, gbc);
		gbc.gridy ++;
	}

	private static void updateCustomColorSettings(JDialog options)
	{
		int[][] colors = new int[3][3];
		getAllComponents(options).stream().filter(comp -> comp instanceof JTextField).forEach(text -> {
			String name = text.getName();
			colors[name.startsWith("Text") ? 0 : name.startsWith("Border") ? 1 : 2][name.endsWith("r") ? 0 : name.endsWith("g") ? 1 : 2] = getColorInt( (JTextField) text);
		});
		
		for (int[] row : colors) for (int cell : row) if (cell < 0 || cell > 255) return;
		
		colorSettingProfiles[2].update(new Color(colors[0][0], colors[0][1], colors[0][2]),
				new Color(colors[1][0], colors[1][1], colors[1][2]),
				new Color(colors[2][0], colors[2][1], colors[2][2]) );
		
		options.dispose();
	}

	private static int getColorInt(JTextField text)
	{
		int res = 0;
		try {
			res = Integer.parseInt(text.getText() );
		} catch (NumberFormatException e) { res = -1;  }
		
		if (res < 0 || res > 255)
			text.setBackground(Color.red);
		
		return res;
	}

	static void applyLightingMode()
	{
		// backgrounds
		MainGui.window.setBackground(ColorSettings.getBackgroundColor() );
		MainGui.scrollPane.setBackground(ColorSettings.getBackgroundColor() );
		MainGui.mainPanel.setBackground(ColorSettings.getBackgroundColor() );
		
		// text for cells
		for (JLabel label : MainGui.labelsText) label.setForeground(ColorSettings.getTextColor() );
		
		// text for add-remove-controls
		for (JLabel label : MainGui.labelsTextsHideWhenNotInEdit) label.setForeground(MainGui.inEditMode ? ColorSettings.getTextColor()  : ColorSettings.getBackgroundColor() );
		
		// border color for sections
		for (Section section : MainGui.sectionsList)
			if (section.getBorder() != null)
				((TitledBorder) section.getBorder() ).setTitleColor(ColorSettings.getTextColor() );
		
		// border for cells
		for (Section section : MainGui.sectionsList)
			for (Row row : section.getRows() )
				for (JPanel cell : row.getCells() )
					if (cell.getBorder() != null)
						cell.setBorder(new MatteBorder( ((MatteBorder) cell.getBorder() ).getBorderInsets(), ColorSettings.getBorderColor() ) );
		
		// border for section label cells
		for (JLabel label : MainGui.sectionLabels)
		{
			label.setBorder(new MatteBorder( ((MatteBorder) label.getBorder() ).getBorderInsets(), ColorSettings.getBorderColor() ) );
			label.setForeground(ColorSettings.getTextColor() );
		}
		
		// background and border for sectionManagerDialog
		if (edit.SectionManagerDialog.sectionManagerPanel!= null)
		{
			edit.SectionManagerDialog.sectionManagerPanel.setBackground(ColorSettings.getBackgroundColor() );
			edit.SectionManagerDialog.sectionManagerPanel.setBorder(new MatteBorder( ((MatteBorder) edit.SectionManagerDialog.sectionManagerPanel.getBorder() ).getBorderInsets(), ColorSettings.getTextColor() ) );
		}
		
		// unpdate CellEditDialog
		CellEditDialog.updateColorSettings();
	}
	
	static void changeCustomLightingSettings()
	{
		JDialog options = new JDialog(MainGui.window);
		options.setModal(true);
		options.setTitle("Modify custom lighting settings");
		
		JPanel outer_panel = new JPanel();
		outer_panel.setBackground(ColorSettings.getBackgroundColor() );
		outer_panel.setBorder(GuiHelper.getDefaultBorder() );
		
		JPanel options_panel = new JPanel();
		options_panel.setLayout(new BoxLayout(options_panel, BoxLayout.Y_AXIS) );
		options_panel.setBackground(ColorSettings.getBackgroundColor() );
		
		JPanel color_settings_panel = new JPanel();
		color_settings_panel.setLayout(new GridBagLayout() );
		color_settings_panel.setOpaque(false);
		fillColorSettingsPane(color_settings_panel, colorSettingProfiles[2] );
		options_panel.add(color_settings_panel);
		
		JPanel controls = new JPanel();
		controls.setOpaque(false);
		
		JButton apply = new JButton("Confirm and apply");
		JButton confirm = new JButton("Confirm");
		JButton cancel = new JButton("Cancel");
		
		apply.addActionListener(e -> {updateCustomColorSettings(options); MenuItems.settings_custom.setSelected(true); selectColorSettings(2); } );
		confirm.addActionListener(e -> {updateCustomColorSettings(options); } );
		cancel.addActionListener(e -> {options.dispose(); } );
		
		controls.add(apply);
		controls.add(confirm);
		controls.add(cancel);
		
		options_panel.add(controls);
		outer_panel.add(options_panel);
		
		options.add(outer_panel);
		options.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		options.pack();
		
		options.setPreferredSize(new Dimension(50 + options.getSize().width, options.getSize().height) );
		options.pack();
		GuiHelper.resizeAndCenterRelativeToMainWindow(options);
		options.setVisible(true);
		options.repaint();
	}

	private static List<Component> getAllComponents(final Container c)
	{
	  List<Component> compList = new ArrayList<Component>();
	  for (Component comp : c.getComponents()) {
	      compList.add(comp);
	      if (comp instanceof Container)
	          compList.addAll(getAllComponents((Container) comp));
	  }
	  return compList;
	}
	
}
