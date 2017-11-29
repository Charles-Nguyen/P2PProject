import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class FileTransferServer {
	public static void connect() throws Exception {
		// Initialize Sockets
		ServerSocket ssock = new ServerSocket(5000);
		Socket socket = ssock.accept();

		File root = new File("Files");
		File[] toSend = root.listFiles();
		String fileNames = "";
		String fileModification = "";
		for (File f : toSend)
		{
			fileNames += f.getName() + "/";
			fileModification += f.lastModified() + "/";
		}
		String fileData = fileNames + "|" + fileModification;
		System.out.println("server sends to client initially: " + fileData);

		InputStream stream = new ByteArrayInputStream(
				fileData.getBytes(StandardCharsets.UTF_8.name()));

		BufferedInputStream bis = new BufferedInputStream(stream);

		// Get socket's output stream
		OutputStream os = socket.getOutputStream();

		// Read File Contents into contents array
		byte[] contents;
		long fileLength = bis.available();
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
		}
		os.flush();
		bis.close();
		socket.close();

		// START
		socket = ssock.accept();
		contents = new byte[10000];

		OutputStream needed = new ByteArrayOutputStream();
		InputStream is = socket.getInputStream();
		// No of bytes read in one read() call
		int bytesRead = 0;

		while ((bytesRead = is.read(contents)) != -1)
			needed.write(contents, 0, bytesRead);

		System.out.println("Raw version:" + needed);
		String[] splitUp = needed.toString().split("\\|");
		// System.out.println(needed.toString());
		String[] filesClientNeeds = splitUp[0].split("/");
		String[] filesServerNeeds = splitUp[1].split("/");
		//System.out.println("This is the raw version:" + splitUp[0]);
		// System.out.println(splitUp[1]);
		// END
		needed.close();
		socket.close();

		// GETTING FILES FROM CLIENT THAT SERVER NEEDS
		System.out.println("Files server needs:");
		if (filesServerNeeds[0].equals(""))
		{
			filesServerNeeds = new String[0];
		}
		for (String fileName : filesServerNeeds) {
			System.out.println(fileName);
			socket = ssock.accept();
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

		// SENDING FILES CLIENT NEEDS
		System.out.println("Files client needs:");
		if (filesClientNeeds[0].equals(""))
		{
			filesClientNeeds = new String[0];
		}
		for (String fileName : filesClientNeeds) {
			System.out.println(fileName + "is length " + fileName.length());
			socket = ssock.accept();
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

		socket.close();
		ssock.close();
		System.out.println("Files synched succesfully!");
	}
}