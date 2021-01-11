import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

public class Gui {
	
	Logic logic;
	
	public String fileDirectory = "";
	public String fileName = "";
	public Font font = new Font("Serif", Font.PLAIN, 20);
	
	JFrame window;
	JScrollPane scrollPane;
	JPanel main_panel;
	
	public final static int ImageSize = 30;
	
	public int row = 0;
	
	public boolean title_chaged = true;
	
	public Gui(Logic logic)
	{
		this.logic = logic;
		
		JToolBar bar = new JToolBar();
		//JMenu open = new JMenu("Open"), reload = new JMenu("Reload");
		JButton open = new JButton("Open"), reload = new JButton("Reload");
		open.addActionListener(logic);
		open.setActionCommand("open");
		reload.addActionListener(logic);
		reload.setActionCommand("reload");
		
		bar.add(open);
		bar.add(reload);
		bar.setFloatable(false);
		
		main_panel = new JPanel();
		main_panel.setLayout(new GridLayout() );
		
		scrollPane = new JScrollPane(main_panel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		window = new JFrame();
		window.getContentPane().add(bar, BorderLayout.NORTH);
		window.add(scrollPane);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle("");
		window.pack();
		window.setVisible(true);
		
		getFile();
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
	
	public void draw(String[][] content)
	{
		Dimension old_dimension = scrollPane.getSize();
		window.remove(scrollPane);
		
		JPanel content_pane = new JPanel();
		content_pane.setLayout(new BoxLayout(content_pane, BoxLayout.Y_AXIS) );
		
		ArrayList<JPanel> subpanels = new ArrayList<JPanel>();
		ArrayList<GridBagConstraints> constraints = new ArrayList<GridBagConstraints>();
		ArrayList<JPanel[][]> content_list = new ArrayList<JPanel[][]>();
		int maxWidth = content[0].length;
		
		if (content != null)
		{
			row = 0;
			
			while (row < content.length)
			{
				JPanel sub_panel = new JPanel(); 
				GridBagConstraints gbc = new GridBagConstraints();
				JPanel[][] panels = new JPanel[0][0];
				subpanels.add(sub_panel);
				constraints.add(gbc);
				panels = logic.fillGridBagPanel(sub_panel, gbc, panels);
				content_list.add(panels);				
				content_pane.add(sub_panel);
			}
		}
		
		main_panel = new JPanel();
		main_panel.setLayout(new BorderLayout() );
		JLabel space1 = new JLabel();
		JLabel space2 = new JLabel();
		space1.setPreferredSize(new Dimension(40, 0) );
		space2.setPreferredSize(new Dimension(40, 0) );
		main_panel.add(space1, BorderLayout.WEST);
		main_panel.add(content_pane, BorderLayout.CENTER);
		main_panel.add(space2, BorderLayout.EAST);
		
		scrollPane = new JScrollPane(main_panel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		window.add(scrollPane);
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		int[] maxWidths = new int[maxWidth];
		for (JPanel[][] sub_panel : content_list)
			for (int col = 0; col < maxWidth; col ++)
				if (maxWidths[col] < sub_panel[0][col].getWidth() )
					maxWidths[col] = sub_panel[0][col].getWidth();
		
		for (int subpanel = 0; subpanel < subpanels.size(); subpanel ++)
		{
			JPanel sub_panel = subpanels.get(subpanel);
			GridBagConstraints gbc = constraints.get(subpanel);
			gbc.gridy ++;
			for (int col = 0; col < maxWidth; col ++)
			{
				gbc.gridx = col;
				JLabel label = new JLabel();
				label.setPreferredSize(new Dimension(maxWidths[col], 0) );
				sub_panel.add(label, gbc);
			}
		}
		
		scrollPane.setPreferredSize(title_chaged ? new Dimension(scrollPane.getWidth() + 100, scrollPane.getHeight() ) : old_dimension);
		
		window.pack();
		window.setVisible(true);
		window.repaint();
	}
	
	public GridBagLayout getSubPanel()
	{
		return null;
	}
}
