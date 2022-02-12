package logic;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;

public class Updates
{
	public static void checkForUpdates(boolean displayIfCurrent)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				String version = null;
				try
				{
					HttpURLConnection con = (HttpURLConnection) new URL("https://api.github.com/repos/Xoriun/Notes-Creator/releases/latest").openConnection();
					con.setRequestMethod("GET");
					BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream() ) );
					String line = in.readLine();
					in.close();
					
					int index_1 = line.indexOf("\"tag_name\":\"") + 12;
					int index_2 = line.indexOf("\",", index_1);
					
					version = line.substring(index_1, index_2);
				}
				catch (Exception e) { }
				
				if (!displayIfCurrent && MainGui.currentVersionTag.equals(version) ) return;
				
				JDialog dialog = new JDialog(MainGui.window);
				dialog.setTitle("Check for updates");
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS) );
				panel.setBorder(GuiHelper.getDialogBorder() );
				panel.setBackground(ColorSettings.getBackgroundColor() );
				
				String text;
				if (version == null)
					text = "An error occured while getting information from GitHub!";
				else if (version.equals(MainGui.currentVersionTag) )
					text = "You have the current version!";
				else
					text = "A new version (" + version + ") is availabe on GitHub!";
				
				JLabel label = new JLabel(text);
				label.setForeground(ColorSettings.getTextColor() );
				label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10) );
				label.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(label);
				
				JButton confirm = new JButton("Confrim");
				confirm.addActionListener(action -> { dialog.dispose(); } );
				confirm.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(confirm);
				
				dialog.add(panel);
				dialog.pack();
				GuiHelper.setLocationToCenter(dialog);
				dialog.setVisible(true);
			}
		});
	}
}
