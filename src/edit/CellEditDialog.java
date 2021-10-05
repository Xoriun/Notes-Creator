package edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import gui.MainGui;
import logic.Cell;
import logic.FileOperations;
import logic.MouseAdapters;

public class CellEditDialog
{
	public static JDialog cellEditDialog;

	private static Cell selectedCell;
	
	private static JPanel mainPanel;
	private static JTabbedPane tabbedPanel;
	private static JPanel editSimple;
	private static JPanel editFancy;
	private static JPanel actionsPanel;
	
	private static JButton confirmButton;
	
	public static void initializeCellEditDialog()
	{
		cellEditDialog = new JDialog(MainGui.window);
		cellEditDialog.setTitle("Cell Manager");
		cellEditDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		cellEditDialog.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent e)
				{
					hideEditDialog();
				}
			}
		);
		
		mainPanel = new JPanel(new BorderLayout() );
		JPanel content_panel = new JPanel(new BorderLayout() );
		
		tabbedPanel = new JTabbedPane();
		tabbedPanel.setBorder(BorderFactory.createTitledBorder("Content") );
		
		editSimple = new JPanel();
		editFancy = new JPanel();
		editSimple.setName("Simple");
		editFancy.setName("Fancy");
		tabbedPanel.add(editSimple);
		tabbedPanel.add(editFancy);
		
		actionsPanel = new JPanel();
		actionsPanel.setBorder(BorderFactory.createTitledBorder("Actions") );
		actionsPanel.setPreferredSize(new Dimension(1000, 150) );
		
		content_panel.add(tabbedPanel,  BorderLayout.CENTER);
		content_panel.add(actionsPanel, BorderLayout.PAGE_END);
		
		JPanel control_panel = new JPanel();
		confirmButton = new JButton("Confirm");
		JButton cancel_button = new JButton("Cancel");
		
		confirmButton.setEnabled(false);
		confirmButton.addActionListener(e -> {saveCell(); hideEditDialog(); } );
		cancel_button.addActionListener(e -> {hideEditDialog(); } );
		
		control_panel.add(confirmButton);
		control_panel.add(cancel_button);
		
		mainPanel.add(content_panel, BorderLayout.CENTER);
		mainPanel.add(control_panel, BorderLayout.PAGE_END);
		mainPanel.setPreferredSize(new Dimension(300, 500) );
		
		cellEditDialog.add(mainPanel);
		cellEditDialog.pack();
		cellEditDialog.setVisible(false);
	}
	
	private static void saveCell()
	{
		String new_content_string;
		if (tabbedPanel.getSelectedIndex() == 0)
			new_content_string = getContentStringSimple();
		else
			new_content_string = getContentStringFancy();
		
		String new_action_string = getActionsString();
		
		
		if ( !(new_content_string + ">>" + new_action_string).equals(selectedCell.getContentString() ) )
		{
			// resetting cell
			selectedCell.clearContentAndListeners();
			
			// updating content of section
			selectedCell.setContentString(new_content_string + ">>" + new_action_string); 
			
			// refilling cell
			selectedCell.fillCellPanel(new_content_string);
			if (!new_action_string.isEmpty() )
				for (String action_str : new_action_string.split("#", -1) )
					selectedCell.addMouseListener(MouseAdapters.getCellActionAdapter(action_str) );
			selectedCell.addMouseListener(MouseAdapters.getEditCellAdapter(selectedCell) );
			
			// reorganizing gui
			MainGui.spaceColums();
			FileOperations.unsavedChanges = true;
		}
	}
	
	private static String getContentStringSimple()
	{
		return "1";
	}
	
	private static String getContentStringFancy()
	{
		return "2";
	}
	
	private static String getActionsString()
	{
		return "";
	}
	
	private static void hideEditDialog()
	{
		if (selectedCell != null)
		{
			selectedCell.setDefaultBorder();
			selectedCell = null;
		}
		cellEditDialog.setVisible(false); 
	}
	
	public static void processCell(Cell cell)
	{
		if (selectedCell != null)
			selectedCell.setDefaultBorder();
		selectedCell = cell;
		selectedCell.setSelectedBorder();
		
		cellEditDialog.pack();
		cellEditDialog.setVisible(true);
		confirmButton.setEnabled(true);
}
	}
