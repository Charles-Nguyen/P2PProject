import java.awt.*;

import javax.swing.JPanel;

/**
 * The GUI for our project, that displays which IP the user has connected with and what files they received from them.
 * @author Connor Beckett-Lemus and Charles Nguyen
 */
public class DrawingPanel extends JPanel 
implements Runnable
{
	/**
	 * The thread used for refreshing what's shown on the screen.
	 */
	private Thread refresh;
	/**
	 * The ip address of the machine you're currently connected to.
	 */
	private static String ipConnectedTo;
	/**
	 * The files you received from the machine you're currently connected to.
	 */
	private static String[] filesReceived = new String[0];
	/**
	 * The last time filesReceived was modified.
	 */
	private static long lastModified = 0;
	
	/**
	 * Initializes the drawing panel with a black background.
	 */
	public DrawingPanel()
	{
		setBackground(Color.BLACK);
		setFocusable(true);
		refresh = new Thread(this);
		refresh.start();
	}

	/**
	 * The method that repeatedly refreshed the GUI every 50 ms.
	 */
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
	 * Updates the information about who you're connected to and updates the GUI accordingly.
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
	
	/**
	 * Changes the stored ip address to the one you're connected to.
	 * @param ip The ip address you're currently connected to.
	 */
	public void setIp(String ip)
	{
		ipConnectedTo = ip;
	}
	
	/**
	 * Changes the stored file list with the ones you've received.
	 * @param files The files you've just received.
	 */
	public void setFiles(String[] files)
	{
		if (System.currentTimeMillis() > lastModified + 5000) {
			filesReceived = files;
			lastModified = System.currentTimeMillis();
		}
	}
}