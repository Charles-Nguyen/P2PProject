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


public class FileTransferClient
{
	public static void main(String[] args) throws Exception
    {   
        //Initialize socket
        Socket socket = new Socket(InetAddress.getByName("localhost"), 5000);
        byte[] contents = new byte[10000];
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        InputStream is = socket.getInputStream();
        //No of bytes read in one read() call
        int bytesRead = 0;
        
        while ((bytesRead = is.read(contents)) != -1)
            stream.write(contents, 0, bytesRead);
        
        System.out.println(stream);
        String[] serverFiles = stream.toString().split("/");
        File root = new File("clientFiles");
        File[] toSend = root.listFiles();
        ArrayList<String> clientFiles = new ArrayList<String>();
        for (File f : toSend)
        	clientFiles.add(f.getName());
        
        String neededFiles = "";
        for (String n : serverFiles)
        {
        	if (!clientFiles.contains(n))
        	{
        		neededFiles += n + "/";
        	}
        }
        
        ArrayList<String> serverFiles2 = new ArrayList<>(Arrays.asList(serverFiles));
        neededFiles += "|";
        for (String n : clientFiles)
        {
        	if (!serverFiles2.contains(n))
        	{
        		neededFiles += n + "/";
        	}
        }
        System.out.println(neededFiles);
        String[] serverNeedsFiles = (neededFiles.split("\\|")[1]).split("/");
        
        stream.close();
        is.close();
        socket.close();
        
        socket = new Socket(InetAddress.getByName("localhost"), 5000);
        
        // START SENDING BACK LIST OF FILES CLIENT NEEDS
        InputStream sendBack = new ByteArrayInputStream(neededFiles.getBytes(StandardCharsets.UTF_8.name()));
        BufferedInputStream bis = new BufferedInputStream(sendBack);
        
        OutputStream os = socket.getOutputStream();

        long fileLength = sendBack.available();
        long current = 0;

        while (current != fileLength)
        {
            int size = 10000;
            if (fileLength - current >= size)
                current += size;
            else
            {
                size = (int)(fileLength - current); 
                current = fileLength;
            }
            contents = new byte[size];
            bis.read(contents, 0, size);
            os.write(contents);
            System.out.print("Sending file ... " + (current * 100) / fileLength + "% complete! ");
        }
        
        os.flush();
        bis.close();
        socket.close();
        
        // END
        
        // SENDING FILES THAT SERVER NEEDS
        for (String shop : serverNeedsFiles)
        {
        	socket = new Socket(InetAddress.getByName("localhost"), 5000);
            contents = new byte[10000];
        	File file = new File("clientFiles\\" + shop);
            FileInputStream fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis); 
            
            os = socket.getOutputStream();
            
            fileLength = file.length(); 
            current = 0;
            
            while (current != fileLength)
            { 
                int size = 10000;
                if (fileLength - current >= size)
                    current += size;    
                else
                { 
                    size = (int)(fileLength - current); 
                    current = fileLength;
                } 
                contents = new byte[size]; 
                bis.read(contents, 0, size); 
                os.write(contents);
                System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
            }
            os.flush();
            bis.close();
            socket.close();
        }
        
        //START RECEIVING FILES NEEDED FROM SERVER
        String[] receivedFiles = (neededFiles.split("\\|")[0]).split("/");
        System.out.println(receivedFiles.length);
        for (String slap : receivedFiles)
        {
        	 System.out.println(slap);
        	 socket = new Socket(InetAddress.getByName("localhost"), 5000);
             contents = new byte[10000];
             
             //Initialize the FileOutputStream to the output file's full path.
             FileOutputStream fos = new FileOutputStream("clientFiles\\" + slap);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             is = socket.getInputStream();
             
             //No of bytes read in one read() call
             bytesRead = 0; 
             
             while((bytesRead = is.read(contents)) != -1)
                 bos.write(contents, 0, bytesRead); 
             
             bos.flush();
             bos.close();
             is.close();
             socket.close(); 
        }
        
        System.out.println("File saved successfully!");
    }
}