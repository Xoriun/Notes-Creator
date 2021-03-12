import javax.swing.JPanel;

public class Subsection
{
	public JPanel[][] content;
	public String title;
	public int startIndex;
	
	public Subsection(JPanel[][] content, String title, int startIndex)
	{
		this.content = content;
		this.title = title;
		this.startIndex = startIndex;
	}
	
	public Subsection(int startIndex)
	{
		this.startIndex = startIndex;
	}
}