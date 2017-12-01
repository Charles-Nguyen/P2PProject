import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class deals with synchronizing files as a client with a corresponding machine
 * running the server version.
 * @author Connor Beckett-Lemus and Charles Nguyen
 */
public class FileTransferClient {
	/**
	 * Connects to a machine acting as a server, and coordinates with it so both
	 * machines know which files need to be send to and from each machine.
	 * The files that are synchronized are those in a folder called "Files",
	 * located a directory one level above the source code of this program.
	 * @return The files received from the server.
	 */
	public static String[] connect(String ip) {
		String fileData = getFileData(ip);
		String[] splitUp = fileData.split("\\|");
		
		String[] serverFileNames = new String[0];
		String[] serverFileDates = new String[0];
		// only splits up the the server's file data if it has any data to send
		// ("|" means there are no server files)
		if (!fileData.toString().equals("|")) {
			// the files and last modified dates are split into parallel arrays
			serverFileNames = splitUp[0].split("/");
			serverFileDates = splitUp[1].split("/");
		}
		
		File root = new File("Files");
		File[] toSend = root.listFiles();
		ArrayList<String> clientFiles = new ArrayList<String>();
		for (File f : toSend)
			clientFiles.add(f.getName());
		
		// determining the files the client needs from the server
		String updatedFilesForServer = "";
		String missingFileNames = "";
		for (int i = 0; i < serverFileNames.length; i++) {
			if (!clientFiles.contains(serverFileNames[i]))
				missingFileNames += serverFileNames[i] + "/";
			else { // the server and client have a matching filename
				File sameName = null;
				// finding that file in the client files
				for (File f : toSend) {
					if (f.getName().equals(serverFileNames[i]))
						sameName = f;
				}
				
				// determines if the files have been modified if they have been last
				// modified more than a minute apart (to account for latency).
				// most recent version is sent to the other machine.
				boolean hasBeenModified = (Math.abs(Long.parseLong(serverFileDates[i]) - sameName.lastModified())) < 20000;
				if (!hasBeenModified) {
					if (Long.parseLong(serverFileDates[i]) > sameName.lastModified())
						missingFileNames += serverFileNames[i] + "/";
					else
						updatedFilesForServer += serverFileNames[i] + "/";
				}
			}
		}
		// determining the files the server needs from the client
		ArrayList<String> serverFiles = new ArrayList<>(
				Arrays.asList(serverFileNames));
		
		// all files after | are what the server is missing from the client
		missingFileNames += "|" + updatedFilesForServer;
		
		for (String n : clientFiles) {
			if (!serverFiles.contains(n)) {
				missingFileNames += n + "/";
			}
		}
		String[] filesServerNeeds = (missingFileNames.split("\\|")[1]).split("/");
		String[] filesClientNeeds = (missingFileNames.split("\\|")[0]).split("/");
		
		sendMissingFileNames(ip, missingFileNames);
		sendFilesToServer(ip, filesServerNeeds);
		getFilesFromServer(ip, filesClientNeeds);
		return filesClientNeeds;
	}
	
	/**
	 * Receives the data (name and last modified date) of all file stored on the server.
	 * @param ip The ip address of the machine you're connected to.
	 * @return The file data, formatted as such: The names and the last modified date
	 * are separated with |, then the individual names/dates are separated with /.
	 * Ex: dog.txt/cat.txt/|84274983/12837128/
	 */
	public static String getFileData(String ip) {
		try {
		Socket socket = new Socket(InetAddress.getByName(ip), 5000);
		byte[] contents = new byte[10000];

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		InputStream is = socket.getInputStream();
		int bytesRead = 0;

		while ((bytesRead = is.read(contents)) != -1)
			stream.write(contents, 0, bytesRead);
		
		String fileData = stream.toString();
		socket.close();
		return fileData;
		}
		catch (IOException e) {
			System.out.println("Socket issue.");
			return null;
		}
	}
	
	/**
	 * Sends to the server the file names that the server and the client are 
	 * missing from each other.
	 * @param ip The ip address of the machine you're connected to.
	 * @param missingFileNames The list of file names that the server and the client are missing from each other.
	 * The server and client's needed files are separated with |, then the individual files
	 * are separated with /.
	 */
	public static void sendMissingFileNames(String ip, String missingFileNames) {
		try {
		Socket socket = new Socket(InetAddress.getByName(ip), 5000);

		InputStream sendBack = new ByteArrayInputStream(
				missingFileNames.getBytes(StandardCharsets.UTF_8.name()));
		BufferedInputStream bis = new BufferedInputStream(sendBack);
		long fileLength = sendBack.available();
		
		sendBytes(socket, bis, fileLength);
		
		bis.close();
		socket.close();
		}
		catch (IOException e) {
			System.out.println("Socket issue.");
		}
	}
	
	/**
	 * Sends all files that the server needs.
	 * @param ip The ip address of the machine you're connected with.
	 * @param neededFiles The files the server is missing from the client.
	 */
	public static void sendFilesToServer(String ip, String[] neededFiles) {
		try {
		if (neededFiles[0].equals(""))
			neededFiles = new String[0];

		for (String fileName : neededFiles) {
			Socket socket = new Socket(InetAddress.getByName(ip), 5000);
			File file = new File("Files\\" + fileName);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			long fileLength = file.length();

			sendBytes(socket, bis, fileLength);
			
			bis.close();
			socket.close();
		}
		}
		catch (IOException e) {
			System.out.println("Socket issue.");
		}
	}
	
	/**
	 * Receives all files the client needs from the server.
	 * @param ip The ip address of the machine you're connected with.
	 * @param neededFiles The files the client is missing from the server.
	 */
	public static void getFilesFromServer(String ip, String[] neededFiles) {
		try {
		if (neededFiles[0].equals(""))
			neededFiles = new String[0];

		for (String fileName : neededFiles) {
			Socket socket = new Socket(InetAddress.getByName(ip), 5000);
			byte[] contents = new byte[10000];

			FileOutputStream fos = new FileOutputStream("Files\\" + fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			InputStream is = socket.getInputStream();

			int bytesRead = 0;

			while ((bytesRead = is.read(contents)) != -1)
				bos.write(contents, 0, bytesRead);

			bos.flush();
			bos.close();
			is.close();
			socket.close();
		}
		}
		catch (IOException e) {
			System.out.println("Socket issue.");
		}
	}
	
	/**
	 * The low-level file transfer that sends files in 10000 byte packets.
	 * @param socket The socket used for connection.
	 * @param bis The buffered input stream used to read the sent data from.
	 * @param fileLength The size of the file being sent.
	 */
	public static void sendBytes(Socket socket, BufferedInputStream bis, long fileLength) {
		try {
		OutputStream os = socket.getOutputStream();
		byte[] contents = new byte[10000];
		long current = 0;

		while (current != fileLength) {
			// current is how many bytes have been sent so far
			int size = 10000;
			if (fileLength - current >= size)
				current += size;
			else { // if there are less than 10000 bytes left to send:
				size = (int) (fileLength - current);
				current = fileLength;
			}
			contents = new byte[size];
			bis.read(contents, 0, size);
			os.write(contents);
		}
		os.flush();
		}
		catch (IOException e) {
			System.out.println("Socket issue.");
		}
	}
}