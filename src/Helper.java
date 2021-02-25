public class Helper
{
	public static String getWikiName(String str)
	{
		switch (str)
		{
			case "Belt": return "Transport_belt";
			case "Ug_belt": return "Underground_belt";
			case "Ug_pipe": return "Pipe_to_ground";
			 
			case "Red_inserter": return "Long-handed_inserter";
			
			case "Power_pole": return "Small_electric_pole";
			case "Medium_power_pole": return "Medium_electric_pole";
			
			case "Rail": return "Straight_rail";
			
			case "E_miner": return "Electric_mining_drill";
			case "B_miner": return "Burner_mining_drill";
			
			case "Furnace": return "Stone_furnace";
			
			case "Assembler": return "Assembling_machine_1";
			case "Assembler_2": return "Assembling_machine_2";
			case "Refinery": return "Oil_refinery";
			
			case "Iron": return "Iron_plate";
			case "Copper": return "Copper_plate";
			case "Steel": return "Steel_plate";
			case "Plastic": return "Plastic_bar";
			
			case "Wire": return "Copper_cable";
			case "Gear": return "Iron_gear_wheel";
			case "Gc": return "Electronic_circuit";
			case "Red_circuit": return "Advanced_circuit";
			case "Engine": return "Engine_unit";
			case "Red_engine": return "Electric_engine_unit";
			case "Frame": case "Robot_frame": return "Flying_robot_frame";
			
			case "Oil": return "Crude_oil";
			case "Petroleum": return "Petroleum_gas";
			case "Advanced_oil": return "Advanced_oil_processing";
			case "Lub": return "Lubricant";
			
			case "Red_science": return "Automation_science_pack";
			case "Green_science": return "Logistic_science_pack";
			case "Blue_science": return "Chemical_science_pack";
			case "Purple_science": return "Productivity_science_pack";
			case "Yellow_science": return "Utility_science_pack";
			
			case "R_logistics": return "Logistics_(research)";
			case "R_fast_inserter": return "Fast_inserter_(research)";
			case "R_steel_axe": return "Steel_axe_(research)";
			default:
				return str;
		}
	}
}


