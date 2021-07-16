package gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import logic.FileOperations;

public class PopupAlerts
{
	public static String missingImagesMessage = "";
	public static boolean creatMissingImagesMessage = false;
	
	public static void showMissingImagesMessage()
	{
		JDialog missing_dialog = new JDialog(MainGui.window);
		missing_dialog.setModal(true);
		missing_dialog.setTitle("Missing images");
		//missing_dialog.setUndecorated(true);
		
		// main panel
		JPanel missing_panel = new JPanel();
		missing_panel.setLayout(new BoxLayout(missing_panel, BoxLayout.Y_AXIS) );
		missing_panel.setBackground(ColorSettings.currentColorSetting.background);
		missing_panel.setBorder(BorderFactory.createLineBorder(ColorSettings.currentColorSetting.text, 2) );
		
		// title
			JLabel title = new JLabel("Missing images");
			title.setForeground(ColorSettings.currentColorSetting.text);
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			title.setFont(new Font("MonoSpaced", Font.PLAIN, 15) );
			missing_panel.add(title);
		
		// message panel
			JTextArea message = new JTextArea("The following images are missing in your 'Images' folder:" + missingImagesMessage);
			message.setBackground(ColorSettings.currentColorSetting.background);
			message.setForeground(ColorSettings.currentColorSetting.text);
			message.setEditable(false);
			JScrollPane scroll_pane = new JScrollPane(message,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll_pane.setBackground(ColorSettings.currentColorSetting.background);
			scroll_pane.setBorder(BorderFactory.createEmptyBorder() );
		
		// controls panel
			JPanel controls_panel = new JPanel(new FlowLayout(FlowLayout.CENTER) );
			controls_panel.setBackground(ColorSettings.currentColorSetting.background);
			JButton open_folder = new JButton("Open Image folder");
			JButton close = new JButton("OK");
			
			open_folder.addActionListener(e -> {
					try
					{
						Desktop.getDesktop().open(new File("Images\\"));
					} catch (IOException e1)
					{
						System.out.println("Error while opening 'Images' directory!");
					}
				});
			close.addActionListener(e -> { missing_dialog.dispose(); } );
			
			controls_panel.add(open_folder);
			controls_panel.add(close);
		
		// filling cells
		missing_panel.add(scroll_pane);
		missing_panel.add(controls_panel);
		missing_dialog.add(missing_panel);
		
		missing_dialog.pack();
		if (missing_dialog.getHeight() > MainGui.window.getHeight() - 150)
			missing_dialog.setPreferredSize(new Dimension(missing_dialog.getWidth() + 20, MainGui.window.getHeight() - 150) );
		missing_dialog.pack();
		setLocationToCenter(missing_dialog);
		missing_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		missing_dialog.setVisible(true);
	}
	public static void unsavedChangesDialog()
	{
		JDialog save_dialog = new JDialog(MainGui.window);
		save_dialog.setModal(true);
		save_dialog.setTitle("Warning");
		//save_dialog.setBackground(currentColorSetting.background);
		
		save_dialog.setLayout(new BoxLayout(save_dialog.getContentPane(), BoxLayout.Y_AXIS) );
		JLabel label = new JLabel("There are unsaved changes!");
		label.setAlignmentX(1);
		//label.setBackground(currentColorSetting.background);
		//label.setForeground(currentColorSetting.text);
		save_dialog.add(label);
		
		JButton save_Button = new JButton("Save and close");
		JButton discard_button = new JButton("Discard changes");
		JButton cancel_button = new JButton("Cancel");
		
		save_Button.addActionListener(e -> { FileOperations.saveFile(); save_dialog.dispose(); MainGui.exit(); } );
		discard_button.addActionListener(e -> { save_dialog.dispose(); MainGui.exit(); } );
		cancel_button.addActionListener(e -> { save_dialog.dispose(); } );
		
		JPanel save_panel = new JPanel(new FlowLayout() );
		save_panel.add(save_Button);
		save_panel.add(discard_button);
		save_panel.add(cancel_button);
		//save_panel.setBackground(currentColorSetting.background);
		
		save_dialog.add(save_panel);
		save_dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		save_dialog.setTitle("Warning");
		save_dialog.pack();
		save_dialog.setLocationRelativeTo(MainGui.window);
		save_dialog.setVisible(true);
	}
	public static void setLocationToCenter(Container container)
	{
		setLocationToCenter(container, 1000);
	}
	public static void setLocationToCenter(Container container, int max_height_rel_to_window)
	{
		Dimension dim_container  = container.getSize();
		Dimension dim_window = MainGui.window.getSize();
		
		if (dim_container.height > dim_window.height + max_height_rel_to_window)
		{
			dim_container.height = dim_window.height + max_height_rel_to_window;
			container.setPreferredSize(dim_container);
		}
		
		container.setLocation(MainGui.window.getLocation().x + (dim_window.width - dim_container.width)/2, MainGui.window.getLocation().y + (dim_window.height - dim_container.height)/2);
	}
	
}
