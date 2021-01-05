import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Logic implements ActionListener
{
	Gui gui;
	public static ArrayList<String[]> content;
	
	public Logic()
	{
		gui = new Gui(this);
		getContentFromFile(gui.fileDirectory + gui.fileName);
		gui.draw(content);
	}
	
	public static void getContentFromFile(String file)
	{
		content = new ArrayList<String[]>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file) );
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File not found!");
		}
		
		String headerS;
		String lineS;
		try {
			if ( (lineS = reader.readLine() ) != null)
				headerS = lineS.substring(3);
			else
			{
				reader.close();
				throw new RuntimeException("Invalid header!");
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error while reading header!");
		}
		
		String[] header = headerS.split(";");
		content.add(header);
		int length = header.length;
		String[] line;
		
		try {
			while ( (lineS = reader.readLine() ) != null)
			{
				line = lineS.split(";");
				if (line.length > length)
				{
					reader.close();
					throw new RuntimeException();
				}
				else if (line.length < length)
				{
					String[] dummy = new String[length];
					for (int i = 0; i < length; i ++)
						if (i < line.length)
							dummy[i]= line[i];
						else
						{
							dummy[i] = "";
						}
					line = dummy;
				}
				
				content.add(line);
			}
			
			reader.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error while reading file!");
		}
	}
	
	public static String getWikiName(String str)
	{
		switch (str)
		{
			case "E_miner": return "Electric_mining_drill";
			case "B_miner": return "Burner_mining_drill";
			case "Gear": return "Iron_gear_wheel";
			case "Belt": return "Transport_belt";
			case "Wire": return "Copper_cable";
			case "Plastic": return "Plastic_bar";
			case "Rail": return "Straight_rail";
			case "Furnace": return "Stone_furnace";
			case "Iron": return "Iron_plate";
			case "Copper": return "Copper_plate";
			case "Steel": return "Stell_plate";
			case "Oil": return "Crude_oil";
			case "Red_science": return "Automation_science_pack";
			case "Green_science": return "Logistic_science_pack";
			case "Blue_science": return "Chemical_science_pack";
			case "Purple_science": return "Productivity_science_pack";
			case "Yellow_science": return "Utility_science_pack";
			case "Assembler": return "Assembling_machine_1";
			case "Power_pole": return "Small_electric_pole";
			case "Gc": return "electronic_circuit";
			default:
				return str;
		}
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		String action = event.getActionCommand();
		//System.out.println(action);
		switch (action)
		{
			case "open":
				gui.getFile();
			case "reload":
				getContentFromFile(gui.fileDirectory + gui.fileName);
				gui.draw(content);
				break;
		}
	}
	
	public static void main(String[] args)
	{
		new Logic();
	}
}
