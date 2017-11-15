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

public class FileTransferServer
{
	public static void main(String[] args) throws Exception
    {
        //Initialize Sockets
        ServerSocket ssock = new ServerSocket(5000);
        Socket socket = ssock.accept();
        
        File root = new File("C:\\Users\\Connor\\workspace\\CECS327\\src\\serverFiles");
        File[] toSend = root.listFiles();
        String fileNames = "";
        for (File f : toSend)
        	fileNames += f.getName() + "/";
        
        InputStream stream = new ByteArrayInputStream(fileNames.getBytes(StandardCharsets.UTF_8.name()));
        
        BufferedInputStream bis = new BufferedInputStream(stream);
          
        //Get socket's output stream
        OutputStream os = socket.getOutputStream();
                
        //Read File Contents into contents array 
        byte[] contents;
        long fileLength = bis.available();
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
        
        // START
        //System.out.println("before accept");
        socket = ssock.accept();
        contents = new byte[10000];
        
        OutputStream needed = new ByteArrayOutputStream();
        InputStream is = socket.getInputStream();
        //No of bytes read in one read() call
        int bytesRead = 0;
        
        while ((bytesRead = is.read(contents)) != -1)
            needed.write(contents, 0, bytesRead);
        
        String[] splitUp = needed.toString().split("\\|");
        System.out.println(needed.toString());
        String[] filesClientNeeds = splitUp[0].split("/");
        String[] filesServerNeeds = splitUp[1].split("/");
        System.out.println(splitUp[0]);
        System.out.println(splitUp[1]);
        // END
        needed.close();
        socket.close();
        
        // GETTING FILES FROM CLIENT THAT SERVER NEEDS
        for (String snap : filesServerNeeds)
        {
        	 socket = ssock.accept();
             contents = new byte[10000];
             
             //Initialize the FileOutputStream to the output file's full path.
             FileOutputStream fos = new FileOutputStream("C:\\Users\\Connor\\workspace\\CECS327\\src\\serverFiles\\" + snap);
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
        
        // SENDING FILES CLIENT NEEDS
        System.out.println(filesClientNeeds.length);
        for (String shap : filesClientNeeds)
        {
        	socket = ssock.accept();
            contents = new byte[10000];
        	File file = new File("C:\\Users\\Connor\\workspace\\CECS327\\src\\serverFiles\\" + shap);
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
        
        socket.close();
        ssock.close();
        System.out.println("File sent succesfully!");
    }
}