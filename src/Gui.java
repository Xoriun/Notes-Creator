import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
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
	
	public final static int ImageSize = 30;
	
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
				main_panel.add(getCellPanel(cell.replace("->", "⇨") ) );
		
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
	
	public JPanel getCellPanel(String cell)
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
				if (str.contains(":") )
				{
					// Layered Images
					String[] images = str.split(":");
					if (images.length != 4) throw new RuntimeException("Error while parsing layered images: " + str + "! There have to be 2 images and 2 poition tags (t/b/c and l/r/c)");
					try
					{
						// preparing Files
						File file_bG = new File("Images\\" + Logic.getWikiName(images[0] ) + ".png");
						File file_fG = new File("Images\\" + Logic.getWikiName(images[1] ) + ".png");
						File error = new File("Images\\Destroyed-icon.png");
						
						// preparing BufferedImages
						final BufferedImage backGround = ImageIO.read(file_bG.exists() ? file_bG : error);
						final BufferedImage foreGround = ImageIO.read(file_fG.exists() ? file_fG : error);
						final BufferedImage layerDot = ImageIO.read(new File("Images\\Layer_dot.png")); // Black layer between images for better visibility
						final BufferedImage scaled = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_INT_ARGB); // empty BufferedImage to draw on
						Graphics g = scaled.getGraphics();
						
						// drawing images
						g.drawImage(backGround, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
						int smallImageSize = 2 * ImageSize / 3;
						int x = images[2].equals("t") ? 0 : (ImageSize - smallImageSize) / (images[2].equals("c") ? 2 : 1);
						int y = images[3].equals("l") ? 0 : (ImageSize - smallImageSize) / (images[3].equals("c") ? 2 : 1);
						g.drawImage(layerDot, x-1, y-1, smallImageSize+2, smallImageSize+2, null);
						g.drawImage(foreGround, x, y, smallImageSize, smallImageSize, null);
						
						
						panel.add(new JLabel(new ImageIcon(scaled) ) );
					} catch (MalformedURLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					// normal Image
					File file = new File("Images\\" + Logic.getWikiName(str) + ".png");
					panel.add(new JLabel(new ImageIcon(new ImageIcon("Images\\" + (file.exists() ? Logic.getWikiName(str) : "Destroyed-icon") + ".png").getImage().getScaledInstance(ImageSize, ImageSize, Image.SCALE_DEFAULT) ) ) );
				}
			}
			text = !text;
		}
		
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK) );
		return panel;
	}
}
