package edit;

import java.awt.event.MouseListener;

public interface EditPanel 
{
	public String getString();
	public void setSelectedBorder();
	public void setDefaultBorder();
	public void addMouseListener(MouseListener l);
}
