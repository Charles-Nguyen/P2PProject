import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.JPanel;

public class DrawingPanel extends JPanel 
implements MouseListener, Runnable
{
	private Thread refresh;
	private String ipConnectedTo;
	private String filesReceived;
	public DrawingPanel()
	{
		setBackground(Color.BLACK);
		addMouseListener(this);
		setFocusable(true);
		refresh = new Thread(this);
		refresh.start();
	}

	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				repaint();
				Thread.sleep(50);
			}
		}
		catch (InterruptedException e)
		{
			System.out.println("Thread interrupted.");
		}
	}
	/**
	 * Updates positional values of all objects and draws the game accordingly.
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		//g.drawString("TIME: " + (TIME_LIMIT - timeElapsed), 100, 590);
	}
	
	public void setIp(String ip)
	{
		ipConnectedTo = ip;
	}
	
	public void setFiles(String files)
	{
		filesReceived = files;
	}
	
	/**
	 * When a click is made in the gameframe, the hunter fires a bullet.
	 * @param e The mouse click that triggers this method.
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
//		int clickX = e.getX();
//		int clickY = e.getY();
//		if (GAMEFRAME.contains(new Point(clickX, clickY)))
//		{
//			hunter.fireBullet();
//		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}