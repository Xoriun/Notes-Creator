package gui;
import java.awt.Color;

public class ColorSettingProfile
	{
		public String name;
		public Color text;
		public Color border;
		public Color background;
		
		public ColorSettingProfile(String name, Color text, Color border, Color background)
		{
			this.name = name;
			this.text = text;
			this.border = border;
			this.background = background;
		}
	}