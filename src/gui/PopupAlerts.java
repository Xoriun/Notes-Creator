package gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

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
	//private static ArrayList<String> missingImagesSet = new ArrayList<String>(100);
	private static SortedSet<String> missingImagesSet = new TreeSet<String>();
	public static boolean createMissingImagesMessage = false;
	
	public static void showMissingImagesMessageIfNonEmpty()
	{
		if ( !missingImagesSet.isEmpty() )
			showMissingImagesMessage();
	}
	
	public static void resetMissingImagesMessage()
	{
		missingImagesSet.clear();
	}
	
	public static void addMessageForMissingImage(String image_name)
	{
		if (createMissingImagesMessage && !missingImagesSet.contains(image_name) )
			missingImagesSet.add(image_name);
	}
	
	public static void showMissingImagesMessage()
	{
		JDialog missing_dialog = new JDialog(MainGui.window);
		missing_dialog.setModal(true);
		missing_dialog.setTitle("Missing images");
		
		// main panel
		JPanel missing_panel = new JPanel();
		missing_panel.setLayout(new BoxLayout(missing_panel, BoxLayout.Y_AXIS) );
		missing_panel.setBackground(ColorSettings.getBackgroundColor() );
		missing_panel.setBorder(GuiHelper.getDialogBorder() );
		
		// title
			JLabel title = new JLabel("Missing images");
			title.setForeground(ColorSettings.getTextColor() );
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			title.setFont(new Font("MonoSpaced", Font.PLAIN, 15) );
			missing_panel.add(title);
		
		// message panel
			String text = "The following images are missing in your 'Images' folder:";
			for (String message : missingImagesSet)
				text += '\n' + message + ".pmg";
			JTextArea text_area = new JTextArea(text);
			text_area.setBackground(ColorSettings.getBackgroundColor() );
			text_area.setForeground(ColorSettings.getTextColor() );
			text_area.setEditable(false);
			JScrollPane scroll_pane = new JScrollPane(text_area,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll_pane.setBackground(ColorSettings.getBackgroundColor() );
			scroll_pane.setBorder(GuiHelper.getEmptyBorder() );
		
		// controls panel
			JPanel controls_panel = new JPanel(new FlowLayout(FlowLayout.CENTER) );
			controls_panel.setBackground(ColorSettings.getBackgroundColor() );
			JButton open_folder = new JButton("Open Image folder");
			JButton close = new JButton("OK");
			
			open_folder.addActionListener(e -> {
					try
					{
						Desktop.getDesktop().open(new File(FileOperations.imagesDirectory) );
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
		GuiHelper.resizeAndCenterRelativeToMainWindow(missing_dialog);
		missing_dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		missing_dialog.setVisible(true);
	}
	
	public static void errorDialog(String error)
	{
		JDialog error_dialog = new JDialog(MainGui.window);
		error_dialog.setModal(true);
		error_dialog.setTitle("Warning");
		
		JPanel main_panel = new JPanel();
		main_panel.setBorder(GuiHelper.getDialogBorder() );
		main_panel.setBackground(ColorSettings.getBackgroundColor() );
		
		JPanel inner_panel = new JPanel();
		inner_panel.setBorder(GuiHelper.getSpacingBorder(5) );
		inner_panel.setOpaque(false);
		inner_panel.setLayout(new BoxLayout(inner_panel, BoxLayout.Y_AXIS) );
		
		JButton save_button = new JButton("OK");
		save_button.setAlignmentX(Component.CENTER_ALIGNMENT);
		save_button.addActionListener(e -> error_dialog.dispose() );
		
		inner_panel.add(GuiHelper.getCenteredNonOpaqueJLabelWithCurrentTextColor("An error occured: " + error) );
		inner_panel.add(save_button);
		
		main_panel.add(inner_panel);
		
		error_dialog.add(main_panel);
		error_dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		error_dialog.setTitle("Warning");
		error_dialog.pack();
		error_dialog.setLocationRelativeTo(MainGui.window);
		error_dialog.setVisible(true);
	}
	
	public static void unsavedChangesDialog()
	{
		JDialog save_dialog = new JDialog(MainGui.window);
		save_dialog.setModal(true);
		save_dialog.setTitle("Warning");
		save_dialog.setBackground(ColorSettings.getBackgroundColor() );
		
		JPanel main_panel = new JPanel();
		main_panel.setBorder(GuiHelper.getDialogBorder() );
		main_panel.setBackground(ColorSettings.getBackgroundColor() );
		
		JPanel inner_panel = new JPanel();
		inner_panel.setBorder(GuiHelper.getSpacingBorder(5) );
		inner_panel.setOpaque(false);
		inner_panel.setLayout(new BoxLayout(inner_panel, BoxLayout.Y_AXIS) );
		
		JButton save_Button = new JButton("Save and close");
		JButton discard_button = new JButton("Discard changes");
		JButton cancel_button = new JButton("Cancel");
		
		save_Button.addActionListener(e -> { FileOperations.saveFile(); save_dialog.dispose(); MainGui.exit(); } );
		discard_button.addActionListener(e -> { save_dialog.dispose(); MainGui.exit(); } );
		cancel_button.addActionListener(e -> { save_dialog.dispose(); } );
		
		JPanel controls_panel = new JPanel(new FlowLayout() );
		controls_panel.setOpaque(false);
		controls_panel.add(save_Button);
		controls_panel.add(discard_button);
		controls_panel.add(cancel_button);
		
		inner_panel.add(GuiHelper.getLeftAlignedNonOpaqueJLabelWithCurrentTextColor("There are unsaved changes!") );
		inner_panel.add(controls_panel);
		
		main_panel.add(inner_panel);
		
		save_dialog.add(main_panel);
		save_dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		save_dialog.setTitle("Warning");
		save_dialog.pack();
		save_dialog.setLocationRelativeTo(MainGui.window);
		save_dialog.setVisible(true);
	}
}
