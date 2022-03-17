package edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;

import gui.Abbreviations;
import gui.ColorSettings;
import gui.GuiHelper;
import gui.MainGui;
import logic.Cell;
import logic.FileOperations;
import logic.Cell.CellLabel;

public class CellEditDialog
{
	public static JDialog cellEditDialog;

	private static Cell selectedCell;
	private static EditPanel selectedCellPanel;
	private static ArrayList<EditPanel> editPanels = new ArrayList<EditPanel>();
	
	private static JPanel contentPanel;
	private static JPanel actionsPanel;
	
	private static JPanel mainPanel;
	private static JPanel contentScrollPanel;
	private static JPanel actionsScrollPanel;
	
	private static JPanel contentEditLabelPanel;
	private static JPanel actionsEditPanel;
	
	private static JButton buttonSplitAtCursor;
	private static JButton buttonEditIcon;
	
	private static ArrayList<JComboBox<String> > actionComboboxList = new ArrayList<JComboBox<String> >();
	private static ArrayList<JTextField> actionTextfieldList = new ArrayList<JTextField>();
	
	private static String[] possibleActionsArray = new String[] {"", "text_to_clipboard", "file_to_clipboard"}; 
	
	public static boolean iconEditEditorOpen = false;
	
	private static String[] abbreviationsAndFileNamesList;
	
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
		
		
		mainPanel 															= new JPanel();	// main panel for the JDialog
			JPanel cell_panel 										= new JPanel();		// panel that contains the content and action editing of the cell
				contentPanel 												= new JPanel();			// panel that contains the content editing (text and icons) of the cell
					JScrollPane cellLabel_scroll_pane	= new JScrollPane(		// ScrollPane for the content edit
											JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
						contentScrollPanel 							= new JPanel();					// panel within the scroll panel (its only purpose is to add a BorderLayout so the textboxes don't stretch vertically)
							contentEditLabelPanel 				= new JPanel();						// panel for the EditPanels which represent the different text and icon labels
					JPanel control_cellLabel_panel		= new JPanel();				// panel for the edit buttons
				actionsPanel 												= new JPanel();			// panel that contains the action editing of the cell
					JScrollPane actions_scroll_pane		= new JScrollPane(		// ScrollPane for the actions edit
											JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
											JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
						actionsScrollPanel 							= new JPanel();					// panel within the scroll panel (its only purpose is to add a BorderLayout so the textboxes don't stretch vertically)
							actionsEditPanel							= new JPanel();						// panel for the actions edit
			JPanel main_control_panel 						= new JPanel();		// panel for the main control buttons

		mainPanel.setLayout(new BorderLayout() );
		mainPanel.setBackground(ColorSettings.getBackgroundColor() );
		mainPanel.setPreferredSize(new Dimension(300, 500) );
		mainPanel.setBorder(GuiHelper.getDialogBorder() );
		mainPanel.add(cell_panel, BorderLayout.CENTER);
		mainPanel.add(main_control_panel, BorderLayout.PAGE_END);
			
		cell_panel.setLayout(new BorderLayout() );
		cell_panel.setOpaque(false);
		cell_panel.add(contentPanel,  BorderLayout.CENTER);
		cell_panel.add(actionsPanel, BorderLayout.PAGE_END);
		
		contentPanel.setLayout(new BorderLayout() );
		contentPanel.setOpaque(false);
		contentPanel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Content") );
		contentPanel.add(cellLabel_scroll_pane, BorderLayout.CENTER);
		contentPanel.add(control_cellLabel_panel, BorderLayout.LINE_END);
		
		cellLabel_scroll_pane.setOpaque(false);
		cellLabel_scroll_pane.setBorder(GuiHelper.getEmptyBorder() );
		cellLabel_scroll_pane.getViewport().add(contentScrollPanel);
		cellLabel_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);
		
		contentScrollPanel.setLayout(new BorderLayout() );
		contentScrollPanel.setBackground(ColorSettings.getBackgroundColor() );
		contentScrollPanel.add(contentEditLabelPanel, BorderLayout.PAGE_START);
		
		contentEditLabelPanel.setLayout(new BoxLayout(contentEditLabelPanel, BoxLayout.Y_AXIS) );
		contentEditLabelPanel.setOpaque(false);
		
		control_cellLabel_panel.setLayout(new BoxLayout(control_cellLabel_panel, BoxLayout.Y_AXIS) );
		control_cellLabel_panel.setOpaque(false);
		
		buttonSplitAtCursor = new JButton("Split at cursor");
		buttonSplitAtCursor.setAlignmentX(0.5f);
		buttonSplitAtCursor.addActionListener(getSplitAtCurserListener() );
		control_cellLabel_panel.add(buttonSplitAtCursor);
		
		buttonEditIcon = new JButton("Edit icon");
		buttonEditIcon.setAlignmentX(0.5f);
		buttonEditIcon.addActionListener(getIconEditListener() );
		control_cellLabel_panel.add(buttonEditIcon);
		
		JButton button_remove = new JButton("Remove");
		button_remove.setAlignmentX(0.5f);
		button_remove.addActionListener(getRemoveEditPanelListener() );
		control_cellLabel_panel.add(button_remove);
		
		JButton button_add_above = new JButton("Add above");
		button_add_above.setAlignmentX(0.5f);
		button_add_above.addActionListener(getAddAboveListener() );
		control_cellLabel_panel.add(button_add_above);
		
		JButton button_add_below = new JButton("Add below");
		button_add_below.setAlignmentX(0.5f);
		button_add_below.addActionListener(getAddBelowPanelListener() );
		control_cellLabel_panel.add(button_add_below);
		
		JButton button_add__linebreak_below = new JButton("Add Linebreak");
		button_add__linebreak_below.setAlignmentX(0.5f);
		button_add__linebreak_below.addActionListener(getAddLineBreakBelowListener() );
		control_cellLabel_panel.add(button_add__linebreak_below);
		
		actionsPanel.setLayout(new BorderLayout() );
		actionsPanel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Actions") );
		actionsPanel.setPreferredSize(new Dimension(1000, 150) );
		actionsPanel.setOpaque(false);
		actionsPanel.add(actions_scroll_pane, BorderLayout.CENTER);
		
		actions_scroll_pane.setOpaque(false);
		actions_scroll_pane.setBorder(GuiHelper.getEmptyBorder() );
		actions_scroll_pane.getViewport().add(actionsScrollPanel);
		actions_scroll_pane.getVerticalScrollBar().setUnitIncrement(16);
		
		actionsScrollPanel.setLayout(new FlowLayout() );
		actionsScrollPanel.setBackground(ColorSettings.getBackgroundColor() );
		actionsScrollPanel.add(actionsEditPanel, BorderLayout.PAGE_START);
		
		actionsEditPanel.setLayout(new GridBagLayout() );
		actionsEditPanel.setOpaque(false);
		actionsEditPanel.setAlignmentX(0f);
		
		// control panel with the cancel/confirm buttons
		main_control_panel.setOpaque(false);
		
		JButton confirmButton = new JButton("Confirm");
		JButton cancel_button = new JButton("Cancel");
		
		confirmButton.addActionListener(e -> {saveCell(); hideEditDialog(); } );
		cancel_button.addActionListener(e -> {hideEditDialog(); } );
		
		main_control_panel.add(confirmButton);
		main_control_panel.add(cancel_button);
		
		cellEditDialog.add(mainPanel);
		cellEditDialog.pack();
		cellEditDialog.setVisible(false);
	}
	
	public static void updateColorSettings()
	{
		contentPanel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Content") );
		actionsPanel.setBorder(GuiHelper.getTitledBorderWithCorrectTextColor("Actions") );
		
		mainPanel.setBackground(ColorSettings.getBackgroundColor() );
		contentScrollPanel.setBackground(ColorSettings.getBackgroundColor() );
		actionsScrollPanel.setBackground(ColorSettings.getBackgroundColor() );
		
		updateEditPanels();
		updateActionsEdit();
	}
	
	private static void saveCell()
	{
		String new_content_string = "";
		for (EditPanel edit_panel: editPanels)
			new_content_string += edit_panel.getString();
		
		String new_action_string = getActionsString();
		String new_cell_string = new_content_string + (new_action_string.isEmpty() ? "" : ">>" + new_action_string);
		
		if ( !new_cell_string.equals(selectedCell.getCellString() ) )
		{
			// updating content of section
			selectedCell.updateCell(new_cell_string); 
			
			// reorganizing gui
			MainGui.spaceColums();
			FileOperations.unsavedChanges = true;
		}
	}
	
	private static String getActionsString()
	{
		String res = "";
		for (int i = 0; i < actionComboboxList.size(); i ++)
		{
			if (actionComboboxList.get(i).getSelectedItem().equals("") ) continue;
			
			res += actionComboboxList.get(i).getSelectedItem() + ":" + actionTextfieldList.get(i).getText() + "#";
		}
		return res.equals("") ? res : res.substring(0, res.length() - 1);
	}
	
	public static void hideEditDialog()
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
		if (iconEditEditorOpen)
			return;
		
		// cell
		if (selectedCell != null)
			selectedCell.setDefaultBorder();
		selectedCell = cell;
		selectedCell.setSelectedBorder();
		
		// EditLabels
		selectedCellPanel = null;
		buttonSplitAtCursor.setEnabled(false);
		buttonEditIcon.setEnabled(false);
		
		contentEditLabelPanel.removeAll();
		editPanels.clear();
		
		for (CellLabel cell_label : cell.getCellLabels() )
		{
			EditPanel edit_panel = cell_label.getEditPanel();
			addMosueLisetenerToEditPanel(edit_panel);
			editPanels.add(edit_panel);
		}
		
		int index = 0;
		String[] lines = cell.getCellString().split(Pattern.quote("\\n") );
		for (int i = 0; i < lines.length - 1; i ++) // skip last line, no linebreak needed there
		{
			String line = lines[i];
			index += line.split("#").length;
			EditLineBreak lb = new EditLineBreak();
			addMosueLisetenerToEditPanel(lb);
			editPanels.add(index, lb);
			index ++;
		}
		
		// Actions
		actionsEditPanel.removeAll();
		actionComboboxList.clear();
		actionTextfieldList.clear();
		for (String[] action : cell.getActionsArray() )
		{
			JComboBox<String> comboBox = new JComboBox<String>(possibleActionsArray);
			comboBox.setSelectedItem(action[0] );
			JTextField textField = new JTextField(action[1], 15);
			
			actionComboboxList.add(comboBox);
			actionTextfieldList.add(textField);
		}
		
		updateEditPanels();
		updateActionsEdit();
		
		processEditPanel(editPanels.get(0) );
		
		// Dialog
		cellEditDialog.setTitle(cell.getInfo() );
		cellEditDialog.pack();
		cellEditDialog.setVisible(true);
	}
	
	private static void updateActionsEdit()
	{
		actionsEditPanel.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gbc.gridy = 0;
		for (int index = 0; index < actionComboboxList.size(); index ++)
		{
			JLabel remove_label = new JLabel(" - ");
			remove_label.setForeground(ColorSettings.getTextColor() );
			remove_label.setOpaque(false);
			remove_label.addMouseListener(new MouseAdapter() {
				final int index = gbc.gridy;
				@Override
				public void mouseClicked(MouseEvent me)
				{
					actionComboboxList.remove(index);
					actionTextfieldList.remove(index);
					updateActionsEdit();
				}
			});
			
			gbc.gridx = 0;
			actionsEditPanel.add(remove_label, gbc);
			gbc.gridx ++;
			actionsEditPanel.add(actionComboboxList.get(index), gbc);
			gbc.gridx ++;
			actionsEditPanel.add(actionTextfieldList.get(index), gbc);
			
			gbc.gridy ++;
		}
		
		gbc.gridx = 1;
		JLabel add_label = new JLabel("+");
		add_label.setForeground(ColorSettings.getTextColor() );
		add_label.setOpaque(false);
		add_label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				actionComboboxList.add(new JComboBox<String>(possibleActionsArray) );
				actionTextfieldList.add(new JTextField(15) );
				updateActionsEdit();
			}
		});
		actionsEditPanel.add(add_label, gbc);
		
		cellEditDialog.pack();
	}
	
	private static void addMosueLisetenerToEditPanel(EditPanel edit_panel)
	{
		edit_panel.addMouseListener(new MouseInputAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				processEditPanel(edit_panel);
			}
		});
	}
	
	private static void processEditPanel(EditPanel edit_panel)
	{
		if (iconEditEditorOpen)
			return;
		
		if (selectedCellPanel != null)
			selectedCellPanel.setDefaultBorder();
		selectedCellPanel = edit_panel;
		edit_panel.setSelectedBorder();
		
		buttonSplitAtCursor.setEnabled(selectedCellPanel instanceof EditTextField);
		buttonEditIcon.setEnabled(selectedCellPanel instanceof EditIconLabel);
	}
	
	private static void updateEditPanels()
	{
		contentEditLabelPanel.removeAll();
		for (EditPanel edit_panel : editPanels)
		{
			edit_panel.updateColorSettings();
			contentEditLabelPanel.add( (Component) edit_panel);
		}
		cellEditDialog.pack();
	}
	
	private static ActionListener getSplitAtCurserListener()
	{
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int caret_position = ( (EditTextField) selectedCellPanel).getCaretPosition();
				String content = ( (EditTextField) selectedCellPanel).getText();
				if (caret_position > 0 && caret_position < content.length() )
				{
					int selected_index = editPanels.indexOf(selectedCellPanel);
					editPanels.remove(selectedCellPanel);
					
					EditTextField new_edit_text_field_1 = new EditTextField(content.substring(0, caret_position) );
					EditTextField new_edit_text_field_2 = new EditTextField(content.substring(caret_position) );
					
					addMosueLisetenerToEditPanel(new_edit_text_field_1);
					addMosueLisetenerToEditPanel(new_edit_text_field_2);
					
					editPanels.add(selected_index, new_edit_text_field_1);
					editPanels.add(selected_index + 1, new_edit_text_field_2);
					
					selectedCellPanel = new_edit_text_field_1;
					
					updateEditPanels();
				}
			}
		};
	}
	
	private static ActionListener getIconEditListener()
	{
		return new ActionListener() {
			
			String main_image_abbr;
			String layered_image_abbr;
			
			JLabel main_image_label;
			JLabel layered_image_label;
			
			Dimension combobox_main_dim, combobox_layered_dim;
			
			private void closeDialog(JDialog dialog)
			{
				iconEditEditorOpen = false;
				dialog.dispose();
			}
			
			@Override
			public void actionPerformed(ActionEvent ae)
			{
				updateAbbreviationsAndFileNamesList();
				
				iconEditEditorOpen = true;
				
				main_image_abbr    = ( (EditIconLabel) selectedCellPanel).getMainImageAbbr();
				layered_image_abbr = ( (EditIconLabel) selectedCellPanel).getLayeredImageAbbr();
				
				JDialog icon_dialog = new JDialog(CellEditDialog.cellEditDialog, "Edit cell icon");
				icon_dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				icon_dialog.addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { closeDialog(icon_dialog); } } );
				
				JPanel main_panel = new JPanel();
				main_panel.setBackground(ColorSettings.getBackgroundColor() );
				main_panel.setBorder(GuiHelper.getDialogBorder() );
				icon_dialog.add(main_panel);
				
				JPanel inner_panel = new JPanel(new BorderLayout() );
				inner_panel.setOpaque(false);
				inner_panel.setBorder(GuiHelper.getSpacingBorder(5) );
				main_panel.add(inner_panel);
				
				JPanel content_panel = new JPanel(new GridBagLayout() );
				content_panel.setOpaque(false);
				inner_panel.add(content_panel, BorderLayout.CENTER);
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 2, 2);
				gbc.fill = GridBagConstraints.BOTH;
				
				// main image
					
					//label
					gbc.gridy = gbc.gridx = 0;
					content_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Main image", GuiHelper.LEFT), gbc);
					
					//image
					gbc.gridx ++;
					main_image_label = new JLabel(GuiHelper.getScaledImageIconFromAbbreviation(main_image_abbr) );
					content_panel.add(main_image_label, gbc);
					
					//edit panel
					gbc.gridx ++;
					JPanel main_image_edit_panel = new JPanel();
					main_image_edit_panel.setLayout(new GridBagLayout() );
					main_image_edit_panel.setOpaque(false);
					GridBagConstraints gbc_main = new GridBagConstraints();
					gbc_main.insets = new Insets(2, 2, 2, 2);
					gbc_main.fill = GridBagConstraints.HORIZONTAL;
					gbc_main.gridx = gbc_main.gridy = 0;
					
					main_image_edit_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("List:", GuiHelper.LEFT), gbc_main);
					
					gbc_main.gridx ++;
					JComboBox<String> combobox_main = new JComboBox<String>(abbreviationsAndFileNamesList);
					combobox_main.setSelectedItem(getLowercaseStringWithFirstCharCapitablized(main_image_abbr) );
					main_image_edit_panel.add(combobox_main, gbc_main);

					gbc_main.gridy ++;
					gbc_main.gridx = 0;
					main_image_edit_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Filter:", GuiHelper.LEFT), gbc_main);
					
					gbc_main.gridx ++;
					JTextField filter_textFiled_main = new JTextField(); 
					main_image_edit_panel.add(filter_textFiled_main, gbc_main);
					
					combobox_main.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e)
						{
							main_image_abbr = (String) combobox_main.getSelectedItem();
							main_image_label.setIcon(GuiHelper.getScaledImageIconFromAbbreviation(main_image_abbr) );
							icon_dialog.pack();
				    }
					});
					filter_textFiled_main.getDocument().addDocumentListener(new DocumentListener() {
						@Override public void removeUpdate(DocumentEvent e) { update(); }
						@Override public void insertUpdate(DocumentEvent e) { update(); }
						@Override public void changedUpdate(DocumentEvent e) { update(); }
						
						private void update()
						{
							combobox_main.setModel(new DefaultComboBoxModel<>(getFilteredAbbreviationsAndFileNamesList(filter_textFiled_main.getText() ) ) );
							combobox_main.setPreferredSize(combobox_main_dim);
						}
					});
					
					content_panel.add(main_image_edit_panel, gbc);
				
				// layered?
					
					//label
					gbc.gridy ++;
					gbc.gridx = 0;
					content_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Layered image", GuiHelper.LEFT), gbc);
					
					//check box
					gbc.gridx ++;
					JCheckBox checkbox_layered = new JCheckBox();
					checkbox_layered.setSelected( ! layered_image_abbr.equals("") );
					checkbox_layered.setOpaque(false);
					content_panel.add(checkbox_layered, gbc);
				
				// layered image
					
					//label
					gbc.gridy ++;
					gbc.gridx = 0;
					content_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Layered image", GuiHelper.LEFT), gbc);
					
					//image
					gbc.gridx ++;
					layered_image_label = layered_image_abbr.isEmpty() ? new JLabel() : new JLabel(GuiHelper.getScaledImageIconFromAbbreviation(layered_image_abbr) );
					content_panel.add(layered_image_label, gbc);
					
					//edit pane;
					
					gbc.gridx ++;
					JPanel layered_image_edit_panel = new JPanel();
					layered_image_edit_panel.setLayout(new GridBagLayout() );
					layered_image_edit_panel.setOpaque(false);
					GridBagConstraints gbc_layered = new GridBagConstraints();
					gbc_layered.insets = new Insets(2, 2, 2, 2);
					gbc_layered.fill = GridBagConstraints.HORIZONTAL;
					gbc_layered.gridx = gbc_layered.gridy = 0;
					
					layered_image_edit_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("List:", GuiHelper.LEFT), gbc_layered);
					
					gbc_layered.gridx ++;
					JComboBox<String> combobox_layered = new JComboBox<String>(abbreviationsAndFileNamesList);
					combobox_layered.setSelectedItem(getLowercaseStringWithFirstCharCapitablized(layered_image_abbr) );
					layered_image_edit_panel.add(combobox_layered, gbc_layered);
					
					gbc_layered.gridx = 0;
					gbc_layered.gridy ++;
					layered_image_edit_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Filter:", GuiHelper.LEFT), gbc_layered);
					
					gbc_layered.gridx ++;
					JTextField filter_textFiled_layered = new JTextField();
					layered_image_edit_panel.add(filter_textFiled_layered, gbc_layered);
					
					content_panel.add(layered_image_edit_panel, gbc);
					
					combobox_layered.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e)
						{
							layered_image_abbr = (String) combobox_layered.getSelectedItem();
							content_panel.remove(layered_image_label);
							layered_image_label = new JLabel(GuiHelper.getScaledImageIconFromAbbreviation(layered_image_abbr) );
							gbc.gridx = 1;
							gbc.gridy = 2;
							content_panel.add(layered_image_label, gbc);
							icon_dialog.pack();
						}
					});
					filter_textFiled_layered.getDocument().addDocumentListener(new DocumentListener() {
						@Override public void removeUpdate(DocumentEvent e) { update(); }
						@Override public void insertUpdate(DocumentEvent e) { update(); }
						@Override public void changedUpdate(DocumentEvent e) { update(); }
						
						private void update()
						{
							combobox_layered.setModel(new DefaultComboBoxModel<>(getFilteredAbbreviationsAndFileNamesList(filter_textFiled_layered.getText() ) ) );
							combobox_layered.setPreferredSize(combobox_layered_dim);
						}
					});
				
				// horizontal alignment
				gbc.gridy ++;
				gbc.gridx = 0;
				content_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Horizontal alignment", GuiHelper.LEFT), gbc);
				
				gbc.gridx ++;
				JComboBox<String> dropdown_horizontal = new JComboBox<String>(new String[] {"left", "center", "right"} );
				dropdown_horizontal.setSelectedItem( ( (EditIconLabel) selectedCellPanel).getLayeredHorizontalAlignment() );
				gbc.fill = GridBagConstraints.HORIZONTAL;
				content_panel.add(dropdown_horizontal, gbc);
				gbc.fill = GridBagConstraints.BOTH;
				
				// vertical alignment
				gbc.gridy ++;
				gbc.gridx = 0;
				content_panel.add(GuiHelper.getAlignedNonOpaqueJLabelWithCurrentColors("Vertical alignment", GuiHelper.LEFT), gbc);
				
				gbc.gridx ++;
				JComboBox<String> dropdown_vertical = new JComboBox<String>(new String[] {"top", "center", "bottom"} );
				dropdown_vertical.setSelectedItem( ( (EditIconLabel) selectedCellPanel).getLayeredVerticalAlignment() );
				gbc.fill = GridBagConstraints.HORIZONTAL;
				content_panel.add(dropdown_vertical, gbc);
				gbc.fill = GridBagConstraints.BOTH;
				
				// controls
				JPanel control_panel = new JPanel();
				control_panel.setOpaque(false);
				inner_panel.add(control_panel, BorderLayout.PAGE_END);
				
				JButton button_confirm = new JButton("Confirm");
				control_panel.add(button_confirm);
				button_confirm.addActionListener(e -> {
					if (checkbox_layered.isSelected() )
						( (EditIconLabel) selectedCellPanel).updateIcon(main_image_abbr, layered_image_abbr, (String) dropdown_vertical.getSelectedItem(), (String) dropdown_horizontal.getSelectedItem() );
					else
						( (EditIconLabel) selectedCellPanel).updateIcon(main_image_abbr);
					CellEditDialog.cellEditDialog.pack();
					closeDialog(icon_dialog);
				});
				
				JButton button_cancel = new JButton("Cancel");
				control_panel.add(button_cancel);
				button_cancel.addActionListener(e -> closeDialog(icon_dialog) );
				
				icon_dialog.pack();
				icon_dialog.setVisible(true);
				
				combobox_main_dim    = combobox_main.getSize();
				combobox_layered_dim = combobox_layered.getSize();
			}
		};
	}
	
	private static ActionListener getRemoveEditPanelListener()
	{
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (selectedCellPanel == null)
					return;
				
				int selected_index = editPanels.indexOf(selectedCellPanel);
				
				editPanels.remove(selectedCellPanel);
				
				if (selected_index >= editPanels.size() )
					selected_index = editPanels.size() - 1;
				
				selectedCellPanel = editPanels.get(selected_index);
				
				updateEditPanels();
			}
		};
	}
	
	private static ActionListener getAddAboveListener()
	{
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (selectedCellPanel == null)
					return;
				
				int selected_index = editPanels.indexOf(selectedCellPanel);
				
				EditPanel new_panel = selectedCellPanel instanceof EditTextField ? EditIconLabel.getEmptyEditIconLabel() : new EditTextField("");
				addMosueLisetenerToEditPanel(new_panel);
				editPanels.add(selected_index, new_panel);
				updateEditPanels();
			}
		};
	}
	
	private static ActionListener getAddBelowPanelListener()
	{
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (selectedCellPanel == null)
					return;
				
				int selected_index = editPanels.indexOf(selectedCellPanel);
				
				EditPanel new_panel = selectedCellPanel instanceof EditTextField ? EditIconLabel.getEmptyEditIconLabel() : new EditTextField("");
				addMosueLisetenerToEditPanel(new_panel);
				editPanels.add(selected_index + 1, new_panel);
				updateEditPanels();
			}
		};
	}
	
	private static ActionListener getAddLineBreakBelowListener()
	{
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (selectedCellPanel == null)
					return;
				
				int selected_index = editPanels.indexOf(selectedCellPanel);
				
				EditPanel new_panel = new EditLineBreak();
				addMosueLisetenerToEditPanel(new_panel);
				editPanels.add(selected_index + 1, new_panel);
				updateEditPanels();
			}
		};
	}
	
	public static String getLowercaseStringWithFirstCharCapitablized(String str)
	{
		return str.length() == 0 ? str : str.length() == 1 ? str.toUpperCase() : Character.toUpperCase(str.charAt(0) ) + str.substring(1).toLowerCase();
	}
	
	private static void updateAbbreviationsAndFileNamesList()
	{
		ArrayList<String> completeNames = Abbreviations.getStreamOfCompleteNames().collect(Collectors.toCollection(ArrayList::new) );
		abbreviationsAndFileNamesList = Stream.concat(
					Stream.concat(Stream.of(""), Abbreviations.getStreamOfAbbreviations() ), 
					FileOperations.getStreamOfNamesOfImagesInImagesDirectory()
							.filter(e -> ! completeNames.contains(e) )
							.map(e -> getLowercaseStringWithFirstCharCapitablized(e) )
				)
				.map(e -> getLowercaseStringWithFirstCharCapitablized(e) )
				.sorted( (e,f) -> e.compareTo(f) )
				.toArray(String[]::new);
	}
	
	private static String[] getFilteredAbbreviationsAndFileNamesList(String filter)
	{
		return Stream.of(abbreviationsAndFileNamesList)
				.map(e -> e.toLowerCase() )
				.filter(e -> e.contains(filter.toLowerCase() ) ).
				sorted( (String e, String f) -> {
					boolean eStartsWith = e.startsWith(filter);
					boolean fStartsWith = f.startsWith(filter);
					if (eStartsWith && !fStartsWith) return -1;
					else if ( !eStartsWith && fStartsWith) return 1;
					else return e.compareTo(f);
				} )
				.map(e -> getLowercaseStringWithFirstCharCapitablized(e) )
				.toArray(String[]::new);
	}
}