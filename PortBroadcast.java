import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import javax.swing.*;

public class PortBroadcast extends JFrame implements Runnable {
	public static HashSet<String> ipsSynched = new HashSet<String>();
	private static DrawingPanel panel;
	public static void main(String[] args) throws Exception {
		PortBroadcast f = new PortBroadcast();
		f.setTitle("File Synchronization");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		Thread clientThread = new Thread(new PortBroadcast());
		clientThread.start();
		while (true) {
			ServerSocket ssock = new ServerSocket(4997);
			Socket sock = ssock.accept();
			panel.setIp(sock.getInetAddress().toString());
			FileTransferServer.connect();
			System.out.println("ip server saves: " + sock.getInetAddress().toString());
			ipsSynched.add(sock.getInetAddress().toString());
			sock.close();
			ssock.close();
		}
	}
	
	public PortBroadcast()
	{
		setBounds(100, 100, 800, 640);
		panel = new DrawingPanel();
		getContentPane().add(panel);
	}

	@Override
	public void run() {
		while (true) {
			try {
				ArrayList<String> ipsToReach = getIps();
				for (String ip : ipsToReach) {
					System.out.println(ipsSynched);
					if (!ipsSynched.contains("/" + ip))
					{
						Socket sock = new Socket(InetAddress.getByName(ip), 4997);
						panel.setIp(sock.getInetAddress().toString());
						String[] filesReceived = FileTransferClient.connect(ip);
						panel.setFiles(filesReceived);
						System.out.println("ip client saves: " + sock.getInetAddress().toString());
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

	public static ArrayList<String> getIps() throws Exception {
		Scanner s = new Scanner(Runtime.getRuntime().exec("arp -a")
				.getInputStream());
		boolean relevantIpsLeft = true;
		ArrayList<String> ipsToReach = new ArrayList<String>();
		while (s.hasNextLine()/* relevantIpsLeft */) {
			String currentLine = s.nextLine();
			// System.out.println(currentLine);
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
}