import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
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
	
	public int row = 0;
	
	public boolean title_chaged = true;
	public boolean dark_mode = false;
	
	public Color textColor = Color.BLACK;
	public Color borderColor = Color.BLACK;
	public Color backgroundColor = Color.WHITE;
	
	public Set<JLabel> labels = new HashSet<JLabel>();
	public ArrayList<JPanel> subpanels;
	public Set<JPanel> cells = new HashSet<JPanel>();
	
	public Gui(Logic logic)
	{
		this.logic = logic;
		
		JToolBar bar = new JToolBar();
		JButton open = new JButton("Open"), reload = new JButton("Reload"), dark_light_mode = new JButton("Change lighting mode");
		open.addActionListener(logic);
		open.setActionCommand("open");
		reload.addActionListener(logic);
		reload.setActionCommand("reload");
		dark_light_mode.addActionListener(logic);
		dark_light_mode.setActionCommand("change_lighting_mode");
		
		bar.add(open);
		bar.add(reload);
		bar.add(dark_light_mode);
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
		
		main_panel = new JPanel();
		main_panel.setLayout(new BoxLayout(main_panel, BoxLayout.Y_AXIS) );
		main_panel.setBackground(backgroundColor);
		
		subpanels = new ArrayList<JPanel>();
		ArrayList<GridBagConstraints> constraints = new ArrayList<GridBagConstraints>();
		ArrayList<JPanel[][]> content_list = new ArrayList<JPanel[][]>();
		int maxWidth = content[0].length;
		
		if (content != null)
		{
			row = 0;
			
			while (row < content.length)
			{
				JPanel sub_panel = new JPanel();
				sub_panel.setOpaque(false);
				GridBagConstraints gbc = new GridBagConstraints();
				subpanels.add(sub_panel);
				constraints.add(gbc);
				JPanel[][] panels = logic.fillGridBagPanel(sub_panel, gbc);
				content_list.add(panels);				
				main_panel.add(sub_panel);
			}
		}
		
		scrollPane = new JScrollPane(main_panel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBackground(backgroundColor);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		window.add(scrollPane);
		window.pack();
		window.setVisible(true);
		window.repaint();
		
		// determining the maximal width for each column
		int[] maxWidths = new int[maxWidth];
		for (JPanel[][] sub_panel : content_list)
			for (int col = 0; col < maxWidth; col ++)
				if (maxWidths[col] < sub_panel[0][col].getWidth() )
					maxWidths[col] = sub_panel[0][col].getWidth();
		
		// adding a panel with the corresponding width to each column
		for (int subpanel = 0; subpanel < subpanels.size(); subpanel ++)
		{
			JPanel sub_panel = subpanels.get(subpanel);
			GridBagConstraints gbc = constraints.get(subpanel);
			for (int col = 0; col < maxWidth; col ++)
			{
				gbc.gridx = col;
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
	
	public void changeLightingMode(boolean dark_mode)
	{
		backgroundColor = dark_mode ? Color.BLACK      : Color.WHITE;
		textColor       = dark_mode ? Color.LIGHT_GRAY : Color.BLACK;
		borderColor     = dark_mode ? Color.DARK_GRAY  : Color.BLACK;
		
		window.setBackground(backgroundColor);
		scrollPane.setBackground(backgroundColor);
		main_panel.setBackground(backgroundColor);
		
		for (JLabel label : labels) label.setForeground(textColor);
		for (JPanel panel : subpanels)
			if (panel.getBorder() != null)
				((TitledBorder) panel.getBorder() ).setTitleColor(textColor);
		for (JPanel cell : cells)
			cell.setBorder(new MatteBorder( ((MatteBorder) cell.getBorder() ).getBorderInsets(), borderColor) );
	}
	
	public GridBagLayout getSubPanel()
	{
		return null;
	}
}
