import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class Gui {
	
	Logic logic;
	
	public String fileDirectory = "";
	public String fileName = "";
	public Font font = new Font("Serif", Font.PLAIN, 20);
	
	JFrame window;
	JScrollPane scrollPane;
	JPanel main_panel;
	
	public final static int ImageSize = 30;
	public static boolean inEditMode = false;
	
	public int row = 0;
	
	public boolean title_chaged = true;
	public ColorSetting[] color_settings;
	public ColorSetting current_custom_color_setting;
	
	public Set<JLabel> labels = new HashSet<JLabel>();
	public ArrayList<JPanel> subpanels;
	public ArrayList<GridBagConstraints> constraints;
	public Set<JPanel> cells = new HashSet<JPanel>();
	public ArrayList<JPanel[][]> contentList;
	public ArrayList<JLabel> addRemoveRowControlsList = new ArrayList<JLabel>();
	
	public Gui(Logic logic, ColorSetting[] settings)
	{
		color_settings = settings;
		current_custom_color_setting = color_settings[1]; // dark_mode
		this.logic = logic;
		
		JMenuBar bar = new JMenuBar();
		JMenu file_menu = new JMenu("File");
		JMenu setting_menu = new JMenu("Settings");
		ButtonGroup lighting_group = new ButtonGroup();
		
		JMenuItem file_open = new JMenuItem("Open");
		JMenuItem file_reload = new JMenuItem("Reload");
		JMenuItem file_edit = new JMenuItem("Edit Mode");
		JMenuItem file_save = new JMenuItem("Save");
		JRadioButtonMenuItem settings_dark_mode  = new JRadioButtonMenuItem("Dark mode");
		JRadioButtonMenuItem settings_light_mode = new JRadioButtonMenuItem("Light mode");
		JRadioButtonMenuItem settings_custom = new JRadioButtonMenuItem("Custom");
		JMenuItem settings_custom_change = new JMenuItem("Modify Custom");
		
		file_open.addActionListener(   e -> { getFile(); refreshView();} );
		file_reload.addActionListener( e -> { title_chaged = false; refreshView(); } );
		file_edit.addActionListener(   e -> { if (inEditMode) disableEditMode(file_edit); else enableEditMode(file_edit); } );
		file_save.addActionListener(   e -> { logic.saveFile(); } );
		
		settings_light_mode.addActionListener(		e -> { current_custom_color_setting = color_settings[0]; updateLightingMode(); } );
		settings_dark_mode.addActionListener(			e -> { current_custom_color_setting = color_settings[1]; updateLightingMode(); } );
		settings_custom.addActionListener(				e -> { current_custom_color_setting = color_settings[2]; updateLightingMode(); } );
		settings_custom_change.addActionListener( e -> { changeCustomLightingSettings(); } );
		
		settings_dark_mode.setSelected(true);
		lighting_group.add(settings_light_mode);
		lighting_group.add(settings_dark_mode);
		lighting_group.add(settings_custom);
		
		
		file_menu.add(file_open);
		file_menu.add(file_reload);
		file_menu.add(file_edit);
		file_menu.add(file_save);
		setting_menu.add(settings_dark_mode);
		setting_menu.add(settings_light_mode);
		setting_menu.add(settings_custom);
		setting_menu.addSeparator();
		setting_menu.add(settings_custom_change);
		
		bar.add(file_menu);
		bar.add(setting_menu);
		
		main_panel = new JPanel();
		main_panel.setLayout(new GridLayout() );
		
		scrollPane = new JScrollPane(main_panel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		window = new JFrame();
		window.setJMenuBar(bar);
		window.add(scrollPane);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(saveBeforeClose() );
		window.setTitle("");
		window.pack();
		window.setVisible(true);
		
		getFile();
	}
	
	public WindowAdapter saveBeforeClose()
	{
		return new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (Logic.unsavedChanges)
				{
					JFrame saveFrame = new JFrame();
					saveFrame.setLayout(new BoxLayout(saveFrame.getContentPane(), BoxLayout.Y_AXIS) );
					JLabel label = new JLabel("There are unsaved changes!");
					label.setAlignmentX(1);
					saveFrame.add(label);
					
					JPanel savePanel = new JPanel(new FlowLayout() );
					savePanel.add(getSaveButton(saveFrame) );
					savePanel.add(getDiscardButton(saveFrame) );
					savePanel.add(getCancelButton(saveFrame) );
					
					saveFrame.add(savePanel);
					saveFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					saveFrame.setTitle("Warning");
					saveFrame.pack();
					saveFrame.setLocationRelativeTo(window);
					saveFrame.setVisible(true);
				}
				else
					exit();
			}
		};
	}
	
	public void exit()
	{
		System.exit(0);
	}
	
	public JButton getSaveButton(JFrame frame)
	{
		JButton save = new JButton("Save and close");
		save.addActionListener(e -> { logic.saveFile(); exit(); } );
		return save;
	}
	
	public JButton getDiscardButton(JFrame frame)
	{
		JButton save = new JButton("Discard changes");
		save.addActionListener(e -> { frame.dispose(); exit(); } );
		return save;
	}
	
	public JButton getCancelButton(JFrame frame)
	{
		JButton save = new JButton("Cancel");
		save.addActionListener(e -> { frame.dispose(); } );
		return save;
	}
	
	public void repaint()
	{
		main_panel.validate();
		window.pack();
		window.repaint();
	}
	
	public void enableEditMode(JMenuItem file_edit)
	{
		inEditMode = true;
		for (JLabel label : addRemoveRowControlsList) label.setForeground(current_custom_color_setting.text);
		file_edit.setText("View Mode");
	}
	
	public void disableEditMode(JMenuItem file_edit)
	{
		inEditMode = false;
		for (JLabel label : addRemoveRowControlsList) label.setForeground(current_custom_color_setting.background);
		file_edit.setText("Edit Mode");
	}
	
	public void refreshView()
	{
		Logic.getContentFromFile(fileDirectory + fileName);
		arrangeContent(Logic.content);
		draw();
	}
	
	public void getFile()
	{
		FileDialog dialog = new FileDialog(new Frame(), "Select File to Open");
		dialog.setMode(FileDialog.LOAD);
		dialog.setVisible(true);
		
		fileDirectory = dialog.getDirectory();
		fileName = dialog.getFile();
		
		String new_title = fileName.replace('_', ' ');
		title_chaged = !new_title.equals(window.getTitle() );
		window.setTitle(new_title);
	}
	
	public void arrangeContent(String[][] content)
	{
		subpanels = new ArrayList<JPanel>();
		constraints = new ArrayList<GridBagConstraints>();
		contentList = new ArrayList<JPanel[][]>();
		
		if (content != null)
		{
			row = 0;
			
			while (row < content.length)
			{
				JPanel sub_panel = new JPanel(new GridBagLayout() );
				sub_panel.setOpaque(false);
				GridBagConstraints gbc = new GridBagConstraints();
				subpanels.add(sub_panel);
				constraints.add(gbc);
				JPanel[][] panels = logic.fillNotesSubPanel(sub_panel, gbc);
				contentList.add(panels);			
			}
		}
	}
	
	public void draw()
	{
		Dimension old_dimension = scrollPane.getSize();
		window.remove(scrollPane);
		
		main_panel = new JPanel();
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS) );
		main_panel.setBackground(current_custom_color_setting.background);
		int maxWidth = Logic.content[0].length;
		
		for (JPanel subPanel : subpanels)
			main_panel.add(subPanel);
		
		scrollPane = new JScrollPane(main_panel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBackground(current_custom_color_setting.background);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
		window.add(scrollPane);
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		// determining the maximal width for each column
		int[] maxWidths = new int[maxWidth];
		for (JPanel[][] sub_panel : contentList)
			for (int col = 0; col < maxWidth; col ++)
				if (maxWidths[col] < sub_panel[0][col + 1].getWidth() )
					maxWidths[col] = sub_panel[0][col + 1].getWidth();
		
		// adding a panel with the corresponding width to each column
		for (int subpanel = 0; subpanel < subpanels.size(); subpanel ++)
		{
			JPanel sub_panel = subpanels.get(subpanel);
			GridBagConstraints gbc = constraints.get(subpanel);
			for (int col = 0; col < maxWidth; col ++)
			{
				gbc.gridx = col + 1;
				JLabel label = new JLabel();
				label.setPreferredSize(new Dimension(maxWidths[col], 0) );
				label.setOpaque(false);
				sub_panel.add(label, gbc);
			}
		}
		
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		scrollPane.setPreferredSize(title_chaged ? new Dimension(scrollPane.getWidth() + 100, scrollPane.getHeight() ) : old_dimension);
		
		window.pack();
		window.setVisible(true);
		window.repaint();
	}
	
	public void changeCustomLightingSettings()
	{
		JDialog options = new JDialog(window);
		options.setModal(true);
		options.setTitle("Modify custom lighting settings");
		
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS) );
		
		
		optionsPanel.add(new JLabel(current_custom_color_setting.name) );
		
		JPanel colorSettings = new JPanel();
		colorSettings.setLayout(new GridBagLayout() );
		logic.fillColorSettingsPane(colorSettings, color_settings[2] );
		optionsPanel.add(colorSettings);
		
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS) );
		
		JButton apply = new JButton("Confirm and apply");
		JButton confirm = new JButton("Confirm");
		JButton cancel = new JButton("Cancel");
		
		apply.addActionListener(e -> {logic.updateCustomColorSettings(options); updateLightingMode(); } );
		confirm.addActionListener(e -> {logic.updateCustomColorSettings(options); } );
		cancel.addActionListener(e -> {options.dispose(); } );
		
		controls.add(apply);
		controls.add(confirm);
		controls.add(cancel);
		
		optionsPanel.add(controls);
		
		options.add(optionsPanel);
		options.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		options.pack();
		
		optionsPanel.setPreferredSize(new Dimension(50 + optionsPanel.getSize().width, optionsPanel.getSize().height) );
		options.pack();
		options.setVisible(true);
		options.repaint();
	}
	
	public void updateLightingMode()
	{
		window.setBackground(current_custom_color_setting.background);
		scrollPane.setBackground(current_custom_color_setting.background);
		main_panel.setBackground(current_custom_color_setting.background);
		
		for (JLabel label : labels                  ) label.setForeground(current_custom_color_setting.text);
		for (JLabel label : addRemoveRowControlsList) label.setForeground(inEditMode ? current_custom_color_setting.text : current_custom_color_setting.background);
		for (JPanel panel : subpanels)
			if (panel.getBorder() != null)
				((TitledBorder) panel.getBorder() ).setTitleColor(current_custom_color_setting.text);
		for (JPanel cell : cells)
			cell.setBorder(new MatteBorder( ((MatteBorder) cell.getBorder() ).getBorderInsets(), current_custom_color_setting.border) );
	}
}
