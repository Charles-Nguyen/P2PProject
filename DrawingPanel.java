import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.JPanel;

public class DrawingPanel extends JPanel 
implements MouseListener, Runnable
{
	private Thread refresh;
	private static String ipConnectedTo;
	private static String[] filesReceived = new String[0];
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
		g.setColor(Color.YELLOW);
		g.drawString("You are connected to: " + ipConnectedTo, 100, 190);
		int offset = 0;
		for (String file : filesReceived)
		{
			g.drawString("You received: " + file, 100, 290 + offset);
			offset += 30;
		}
	}
	
	public void setIp(String ip)
	{
		ipConnectedTo = ip;
	}
	
	public void setFiles(String[] files)
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