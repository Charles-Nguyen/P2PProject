import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.*;

/**
 * This file is what handles discovery, and delegates the file transferring to
 * FileTransferServer/FileTransferClient files after connecting users.
 * @author Connor Beckett-Lemus and Charles Nguyen
 */
public class PortBroadcast extends JFrame implements Runnable {
	/**
	 * A list of IP addresses this machine has synchronized files with.
	 */
	private static HashSet<String> ipsSynched = new HashSet<String>();
	/**
	 * The drawing panel to send data to.
	 */
	private static DrawingPanel panel;
	
	/**
	 * The entry point of our program. It initializes the GUI, starts a second
	 * thread to act as a client, and then continues on acting as a server.
	 * If another machine acting as a client reaches it, it starts synchronizing
	 * its files with it, and lets the GUI know who it's interacting with.
	 */
	public static void main(String[] args) {
		PortBroadcast f = new PortBroadcast();
		f.setTitle("File Synchronization");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		Thread clientThread = new Thread(new PortBroadcast());
		clientThread.start();
		while (true) {
			try {
			ServerSocket ssock = new ServerSocket(4997);
			Socket sock = ssock.accept();
			panel.setIp(sock.getInetAddress().toString());
			String[] filesReceived = FileTransferServer.connect();
			panel.setFiles(filesReceived);
			ipsSynched.add(sock.getInetAddress().toString());
			sock.close();
			ssock.close();
			}
			catch (IOException e) {
				System.out.println("Socket issue.");
			}
		}
	}
	
	/**
	 * Initializes the drawing panel and adds it to the content pane.
	 */
	public PortBroadcast()
	{
		setBounds(100, 100, 800, 640);
		panel = new DrawingPanel();
		getContentPane().add(panel);
	}

	/**
	 * The client thread. It tries to reach any connected IP address on the network.
	 * If it can successfully reach it (and hasn't already synched with it before),
	 * it starts synchronizing files with that IP, acting as the client, and lets
	 * the GUI know who it's interacting with.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				ArrayList<String> ipsToReach = getIps();
				for (String ip : ipsToReach) {
					if (!ipsSynched.contains("/" + ip))
					{
						Socket sock = new Socket(InetAddress.getByName(ip), 4997);
						panel.setIp(sock.getInetAddress().toString());
						String[] filesReceived = FileTransferClient.connect(ip);
						panel.setFiles(filesReceived);
						ipsSynched.add(sock.getInetAddress().toString());
						sock.close();
					}
				}
			} catch (ConnectException e) {
				System.out.println("Timed out");
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Retrieves the ARP table and parses it for the connected IP addresses.
	 * @return An ArrayList of all the other IP addresses connected to the network.
	 */
	public static ArrayList<String> getIps() {
		try {
		Scanner s = new Scanner(Runtime.getRuntime().exec("arp -a")
				.getInputStream());
		ArrayList<String> ipsToReach = new ArrayList<String>();
		while (s.hasNextLine()) {
			String currentLine = s.nextLine();
			if (!currentLine.equals("")) {
				Scanner checkLine = new Scanner(currentLine);
				String ip = checkLine.next();
				if (ip.contains("192.168.43") && !ip.equals("192.168.43.1")
						&& !ip.equals("192.168.43.255")) {
					ipsToReach.add(ip);
				}
				checkLine.close();
			}
		}
		s.close();
		return ipsToReach;
		}
		catch (IOException e) {
			System.out.println("Problem executing arp -a command.");
			return null;
		}
	}
}