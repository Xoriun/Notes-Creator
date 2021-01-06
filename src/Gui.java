import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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
		
		window.setTitle(fileName.substring(0, fileName.lastIndexOf('.') ).replace('_', ' ') );
	}
	
	public void draw(ArrayList<String[]> content)
	{
		window.remove(scrollPane);
		
		int rows = content.size();
		int cols = content.get(0).length;
		
		main_panel = new JPanel();
		main_panel.setLayout(new GridLayout(rows, cols) );
		
		for (String[] row : content)
			for (String cell : row)
				main_panel.add(getCell(cell) );
		
//		String url = Gui.class.getResource("").toString().substring(6);
//		System.out.println(url);
//		main_panel.add(new JLabel(new ImageIcon("Images\\Destroyed-icon.png") ) );
		
		
		scrollPane = new JScrollPane(main_panel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		window.add(scrollPane);
		window.pack();
		window.setVisible(true);
		window.repaint();
	}
	
	public JPanel getCell(String cell)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS) );
		String[] cells = cell.split("#");
		boolean text = true;
		for (String str : cells)
		{
			if (text)
			{
				JLabel label = new JLabel(str);
				label.setFont(font);
				panel.add(label);
			}
			else
			{
				File file = new File("Images\\" + Logic.getWikiName(str) + ".png");
				panel.add(new JLabel(new ImageIcon(new ImageIcon("Images\\" + (file.exists() ? Logic.getWikiName(str) : "Destroyed-icon") + ".png").getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT) ) ) );
			}
			text = !text;
		}
		
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK) );
		return panel;
	}
}
