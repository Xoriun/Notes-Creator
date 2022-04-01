package gui;
import java.awt.Color;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ColorSettingProfile
	{
		private String name;
		private Color text;
		private Color border;
		private Color background;
		
		public ColorSettingProfile(String name, Color text, Color border, Color background)
		{
			this.name = name;
			this.text = text;
			this.border = border;
			this.background = background;
		}
		
		public ColorSettingProfile(Element profile, String current_profile_name)
		{
			this.name = profile.getElementsByTagName("name").item(0).getTextContent();
			this.text = new Color(Integer.parseInt(profile.getElementsByTagName("text").item(0).getTextContent() ) );
			this.border = new Color(Integer.parseInt(profile.getElementsByTagName("border").item(0).getTextContent() ) );
			this.background = new Color(Integer.parseInt(profile.getElementsByTagName("background").item(0).getTextContent() ) );
			
			if (name.equals(current_profile_name) )
				ColorSettings.currentColorSetting = this;
		}
		
		public void update(Color text, Color border, Color background)
		{
			this.text = text;
			this.border = border;
			this.background = background;
		}
		
		public String getName() { return name; }
		public Color getTextColor() { return text; }
		public Color getBorderColor() { return border; }
		public Color getBackgroundColor() { return background; }
		
		public Element getXMLElement(Document doc)
		{
			Element result = doc.createElement("color-profile");
			
			Element titleElement = doc.createElement("name");
			titleElement.setTextContent(name);
			result.appendChild(titleElement);
			
			Element textElement = doc.createElement("text");
			textElement.setTextContent("" + text.getRGB() );
			result.appendChild(textElement);
			
			Element borderElement = doc.createElement("border");
			borderElement.setTextContent("" + border.getRGB() );
			result.appendChild(borderElement);
			
			Element backgroundElement = doc.createElement("background");
			backgroundElement.setTextContent("" + background.getRGB() );
			result.appendChild(backgroundElement);
			
			return result;
		}
	}