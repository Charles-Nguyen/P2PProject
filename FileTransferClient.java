import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class FileTransferClient {
	public static String[] connect(String ip) throws Exception {
		// Initialize socket
		Socket socket = new Socket(InetAddress.getByName(ip), 5000);
		byte[] contents = new byte[10000];

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		InputStream is = socket.getInputStream();
		// No of bytes read in one read() call
		int bytesRead = 0;

		while ((bytesRead = is.read(contents)) != -1)
			stream.write(contents, 0, bytesRead);
		
		String[] fileData = stream.toString().split("\\|");
		String[] serverFileNames = new String[0];
		String[] serverFileDates = new String[0];
		if (!stream.toString().equals("|")) {
			serverFileNames = fileData[0].split("/");
			serverFileDates = fileData[1].split("/");
		}
		File root = new File("Files");
		File[] toSend = root.listFiles();
		ArrayList<String> Files = new ArrayList<String>();
		for (File f : toSend)
			Files.add(f.getName());
		
		String filesServerNeeds = "";
		// determining the files the client needs from the server
		String neededFiles = "";
		for (int i = 0; i < serverFileNames.length; i++) {
			if (!Files.contains(serverFileNames[i])) {
				neededFiles += serverFileNames[i] + "/";
			}
			else
			{
				File sameName = null;
				for (File f : toSend) {
					if (f.getName().equals(serverFileNames[i]))
					{
						sameName = f;
					}
				}
				
				if (Long.parseLong(serverFileDates[i]) > sameName.lastModified()) {
					neededFiles += serverFileNames[i] + "/";
				}
				else
				{
					filesServerNeeds += serverFileNames[i] + "/";
				}
			}
		}
		// determining the files the server needs from the client
		ArrayList<String> serverFiles2 = new ArrayList<>(
				Arrays.asList(serverFileNames));
		neededFiles += "|" + filesServerNeeds;
		for (String n : Files) {
			if (!serverFiles2.contains(n)) {
				neededFiles += n + "/";
			}
		}
		System.out.println(neededFiles);
		String[] serverNeedsFiles = (neededFiles.split("\\|")[1]).split("/");

		stream.close();
		is.close();
		socket.close();

		socket = new Socket(InetAddress.getByName(ip), 5000);

		// START SENDING BACK LIST OF FILES CLIENT NEEDS
		InputStream sendBack = new ByteArrayInputStream(
				neededFiles.getBytes(StandardCharsets.UTF_8.name()));
		BufferedInputStream bis = new BufferedInputStream(sendBack);

		OutputStream os = socket.getOutputStream();

		long fileLength = sendBack.available();
		long current = 0;

		while (current != fileLength) {
			int size = 10000;
			if (fileLength - current >= size)
				current += size;
			else {
				size = (int) (fileLength - current);
				current = fileLength;
			}
			contents = new byte[size];
			bis.read(contents, 0, size);
			os.write(contents);
			//System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete! ");
		}

		os.flush();
		bis.close();
		socket.close();

		// END

		// SENDING FILES THAT SERVER NEEDS
		if (serverNeedsFiles[0].equals(""))
		{
			serverNeedsFiles = new String[0];
		}
		for (String fileName : serverNeedsFiles) {
			socket = new Socket(InetAddress.getByName(ip), 5000);
			contents = new byte[10000];
			File file = new File("Files\\" + fileName);
			FileInputStream fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);

			os = socket.getOutputStream();

			fileLength = file.length();
			current = 0;

			while (current != fileLength) {
				int size = 10000;
				if (fileLength - current >= size)
					current += size;
				else {
					size = (int) (fileLength - current);
					current = fileLength;
				}
				contents = new byte[size];
				bis.read(contents, 0, size);
				os.write(contents);
				//System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete!");
			}
			os.flush();
			bis.close();
			socket.close();
		}

		// START RECEIVING FILES NEEDED FROM SERVER
		String[] receivedFiles = (neededFiles.split("\\|")[0]).split("/");
		if (receivedFiles[0].equals(""))
		{
			receivedFiles = new String[0];
		}
		for (String fileName : receivedFiles) {
			socket = new Socket(InetAddress.getByName(ip), 5000);
			contents = new byte[10000];

			// Initialize the FileOutputStream to the output file's full path.
			FileOutputStream fos = new FileOutputStream("Files\\" + fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			is = socket.getInputStream();

			// No of bytes read in one read() call
			bytesRead = 0;

			while ((bytesRead = is.read(contents)) != -1)
				bos.write(contents, 0, bytesRead);

			bos.flush();
			bos.close();
			is.close();
			socket.close();
		}

		System.out.println("Files synched successfully!");
		return receivedFiles;
	}

}