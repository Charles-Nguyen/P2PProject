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
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * This class deals with synchronizing files as a server with a corresponding machine
 * running the client version.
 * @author Connor Beckett-Lemus and Charles Nguyen
 */
public class FileTransferServer {
	/**
	 * Connects to a machine acting as a client, and coordinates with it so both
	 * machines know which files need to be send to and from each machine.
	 * The files that are synchronized are those in a folder called "Files",
	 * located a directory one level above the source code of this program.
	 * @return The files received from the client.
	 */
	public static String[] connect() {
		try {
		ServerSocket ssock = new ServerSocket(5000);

		File root = new File("Files");
		File[] toSend = root.listFiles();
		String fileNames = "";
		String fileModification = "";
		for (File f : toSend) {
			fileNames += f.getName() + "/";
			fileModification += f.lastModified() + "/";
		}
		String fileData = fileNames + "|" + fileModification;

		sendFileData(ssock, fileData);
		
		String[] splitUp = getMissingFileNames(ssock);
		String[] filesClientNeeds = splitUp[0].split("/");
		String[] filesServerNeeds = splitUp[1].split("/");

		getFilesFromClient(ssock, filesServerNeeds);

		sendFilesToClient(ssock, filesClientNeeds);
		ssock.close();
		return filesServerNeeds;
		}
		catch (IOException e) {
			System.out.println("Problem initializing server socket.");
			return null;
		}
	}
	
	/**
	 * Sends a string containing the names and last modified date of all the files
	 * on the server machine to the client machine.
	 * @param ssock The server socket used for connecting.
	 * @param toSend The file data, formatted as such: The names and the last modified date
	 * are separated with |, then the individual names/dates are separated with /.
	 * Ex: dog.txt/cat.txt/|84274983/12837128/
	 */
	public static void sendFileData(ServerSocket ssock, String toSend) {
		try {
		Socket socket = ssock.accept();
		InputStream stream = new ByteArrayInputStream(
				toSend.getBytes(StandardCharsets.UTF_8.name()));
		BufferedInputStream bis = new BufferedInputStream(stream);
		
		long fileLength = bis.available();
		sendBytes(socket, bis, fileLength);
		bis.close();
		socket.close();
		}
		catch (IOException e) {
			System.out.println("Encoding issue with socket.");
		}
	}
	
	/**
	 * Gets a list from the client of file names that the server and the client
	 * are missing from each other
	 * @param ssock The server socket used to connect with.
	 * @return The list of file names that the server and the client are missing from each other.
	 * The server and client's needed files are separated with |, then the individual files
	 * are separated with /.
	 */
	public static String[] getMissingFileNames(ServerSocket ssock) {
		try {
		Socket socket = ssock.accept();
		byte[] contents = new byte[10000];

		OutputStream needed = new ByteArrayOutputStream();
		InputStream is = socket.getInputStream();
		int bytesRead = 0;

		while ((bytesRead = is.read(contents)) != -1)
			needed.write(contents, 0, bytesRead);
		
		String[] missingFileNames = needed.toString().split("\\|");
		needed.close();
		socket.close();
		return missingFileNames;
		}
		catch (IOException e) {
			System.out.println("Socket issue.");
			return null;
		}
	}
	
	/**
	 * Receives the necessary files from the client.
	 * @param ssock The server socket used for connection.
	 * @param neededFiles The files the server is missing from the client.
	 */
	public static void getFilesFromClient(ServerSocket ssock, String[] neededFiles) {
		try {
		if (neededFiles[0].equals("")) {
			neededFiles = new String[0];
		}
		for (String fileName : neededFiles) {
			Socket socket = ssock.accept();
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
	 * Sends the files to the client that it's missing from the server.
	 * @param ssock The server socket used for connection.
	 * @param neededFiles The list of files the client is missing.
	 */
	public static void sendFilesToClient(ServerSocket ssock, String[] neededFiles) {
		try {
		if (neededFiles[0].equals("")) {
			neededFiles = new String[0];
		}
		for (String fileName : neededFiles) {
			Socket socket = ssock.accept();
			File file = new File("Files\\" + fileName);
			long fileLength = file.length();
			
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);

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