package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import logic.Section;

public class ColorSettings
{
	public static ColorSettingProfile[] colorSettingProfiles;
	public static ColorSettingProfile currentColorSetting;
	
	public static JLabel getNewJLabelWithCurrentTextColor(String text)
	{
		JLabel label = new JLabel(text);
		label.setForeground(currentColorSetting.text);
		return label;
	}

	public static void fillColorSettingsPane(JPanel options_panel, ColorSettingProfile color_setting)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = gbc.gridy = 0;
		fillColorSettingsRow(options_panel, gbc, color_setting.text, "Text");
		fillColorSettingsRow(options_panel, gbc, color_setting.border, "Border");
		fillColorSettingsRow(options_panel, gbc, color_setting.background, "Background   ");
	}

	public static void fillColorSettingsRow(JPanel panel, GridBagConstraints gbc, Color color, String name)
	{
		
		gbc.gridx = 0;
		JLabel label_name = new JLabel(name);
		label_name.setForeground(currentColorSetting.text);
		label_name.setOpaque(false);
		panel.add(label_name, gbc);
		
		gbc.gridx = 1;
		JLabel label_r = new JLabel(" r ");
		label_r.setForeground(currentColorSetting.text);
		label_r.setOpaque(false);
		panel.add(label_r, gbc);
		gbc.gridx = 2;
		JTextField text1 = new JTextField("" + color.getRed() );
		text1.setForeground(currentColorSetting.text);
		text1.setBackground(currentColorSetting.background);
		text1.setBorder(BorderFactory.createMatteBorder(name.equals("Text") ? 1 : 0, 1, 1, 1, currentColorSetting.border) );
		text1.setName(name + "r");
		panel.add(text1, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		JLabel label_g = new JLabel(" g ");
		label_g.setForeground(currentColorSetting.text);
		label_g.setOpaque(false);
		panel.add(label_g, gbc);
		gbc.gridx = 2;
		JTextField text2 = new JTextField("" + color.getGreen() );
		text2.setForeground(currentColorSetting.text);
		text2.setBackground(currentColorSetting.background);
		text2.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, currentColorSetting.border) );
		text2.setName(name + "g");
		panel.add(text2, gbc);
		gbc.gridy ++;
		
		gbc.gridx = 1;
		JLabel label_b = new JLabel(" b ");
		label_b.setForeground(currentColorSetting.text);
		label_b.setOpaque(false);
		panel.add(label_b, gbc);
		gbc.gridx = 2;
		JTextField text3 = new JTextField("" + color.getBlue() );
		text3.setForeground(currentColorSetting.text);
		text3.setBackground(currentColorSetting.background);
		text3.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, currentColorSetting.border) );
		text3.setName(name + "b");
		panel.add(text3, gbc);
		gbc.gridy ++;
	}

	public static void updateCustomColorSettings(JDialog options)
	{
		int[][] colors = new int[3][3];
		ColorSettings.getAllComponents(options).stream().filter(comp -> comp instanceof TextField).forEach(text -> 
		{
			text.setBackground(Color.white);
			String name = text.getName();
			colors[name.startsWith("Text") ? 0 : name.startsWith("Border") ? 1 : 2][name.endsWith("r") ? 0 : name.endsWith("g") ? 1 : 2] = getColorInt( (TextField) text);
		});
		
		for (int[] row : colors) for (int cell : row) if (cell < 0 || cell > 255) return;
		
		colorSettingProfiles[2].text 			= new Color(colors[0][0], colors[0][1], colors[0][2]);
		colorSettingProfiles[2].border		  = new Color(colors[1][0], colors[1][1], colors[1][2]);
		colorSettingProfiles[2].background = new Color(colors[2][0], colors[2][1], colors[2][2]);
		currentColorSetting = colorSettingProfiles[2];
		
		options.dispose();
	}

	public static int getColorInt(TextField text)
	{
		int res = 0;
		try {
			res = Integer.parseInt( ( (TextField) text).getText() );
		} catch (NumberFormatException e) { res = -1;  }
		
		if (res < 0 || res > 255)
			text.setBackground(Color.red);
		
		return res;
	}

	public static void applyLightingMode()
	{
		// backgrounds
		MainGui.window.setBackground(currentColorSetting.background);
		MainGui.scrollPane.setBackground(currentColorSetting.background);
		MainGui.mainPanel.setBackground(currentColorSetting.background);
		
		// text for cells
		for (JLabel label : MainGui.labelsTextcolor) label.setForeground(currentColorSetting.text);
		
		// text for add-remove-controls
		for (JLabel label : MainGui.labelsBackgroundcolorTextcolor) label.setForeground(MainGui.inEditMode ? currentColorSetting.text : currentColorSetting.background);
		
		// border color for sections
		for (Section section : MainGui.sectionsList)
			if (section.getBorder() != null)
				((TitledBorder) section.getBorder() ).setTitleColor(currentColorSetting.text);
		
		// border for cells
		for (Section section : MainGui.sectionsList)
			for (JPanel[] cell_row : section.getCells() )
				for (JPanel cell : cell_row)
					if (cell.getBorder() != null)
						cell.setBorder(new MatteBorder( ((MatteBorder) cell.getBorder() ).getBorderInsets(), currentColorSetting.border) );
		
		// border for section label cells
		for (JLabel label : MainGui.sectionLabels)
		{
			label.setBorder(new MatteBorder( ((MatteBorder) label.getBorder() ).getBorderInsets(), currentColorSetting.border) );
			label.setForeground(currentColorSetting.text);
		}
		
		// background and border for sectionManagerDialog
		if (MainGui.sectionManagerPanel!= null)
		{
			MainGui.sectionManagerPanel.setBackground(currentColorSetting.background);
			MainGui.sectionManagerPanel.setBorder(new MatteBorder( ((MatteBorder) MainGui.sectionManagerPanel.getBorder() ).getBorderInsets(), currentColorSetting.text) );
		}
	}
	
	public static void changeCustomLightingSettings()
	{
		JDialog options = new JDialog(MainGui.window);
		options.setModal(true);
		options.setTitle("Modify custom lighting settings");
		
		JPanel outer_panel = new JPanel();
		outer_panel.setBackground(currentColorSetting.background);
		outer_panel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, currentColorSetting.text) );
		
		JPanel options_panel = new JPanel();
		options_panel.setLayout(new BoxLayout(options_panel, BoxLayout.Y_AXIS) );
		options_panel.setBackground(currentColorSetting.background);
		
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
		
		apply.addActionListener(e -> {updateCustomColorSettings(options); applyLightingMode(); } );
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
		PopupAlerts.setLocationToCenter(options);
		options.setVisible(true);
		options.repaint();
	}

	public static List<Component> getAllComponents(final Container c)
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
