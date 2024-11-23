import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Client {
    private static int requestCounter = 1; // Initialize requestCounter
    private static final String sourceDir = "srcFiles/";
    private static String fileTransfer = null;

    public static void main(String[] args) {
        MyFrame mf = new MyFrame();
        
        while(true) {
            synchronized (Client.class) {
                try {
                    Client.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(mf.client_port.isEnabled() && mf.client_port.getForeground() == Color.BLACK && !mf.client_port.getText().equals("")){
                (new Thread(() -> {
                    try (DatagramSocket client = new DatagramSocket(Integer.parseInt(mf.client_port.getText()))) {
                        mf.addLeftLabel("Successfully binded to port " + mf.client_port.getText());
                        mf.client_port.setEnabled(false);
                        
                        (new Thread(() -> {
                            
                            while (true) {
                                try {
                                    byte[] dataToReceive = new byte[1024];
                                    DatagramPacket caughtFrame = new DatagramPacket(dataToReceive, 1024);
                                    client.receive(caughtFrame);
                                    String serverMessage = new String(caughtFrame.getData(), 0, caughtFrame.getLength());
                                    System.out.println("Server response: " + serverMessage);
                                    mf.addLeftLabel(serverMessage);
                                    
                                    if(serverMessage.startsWith("FILE-REQ")){
                                        String[] partition = serverMessage.split("\\|");
                                        if(partition.length != 3){
                                            System.out.println("FILE-ERROR| " + requestCounter + serverMessage + partition.length + partition[2]);
                                            continue;
                                        }
                                        
                                        File fileToTransfer = new File("srcFiles/"+partition[2]);

                                        if (!fileToTransfer.exists()) {
                                            String errorMessage = "FILE-ERROR|" + partition[1] + "|The requested file does not exist.";
                                            byte[] errorDataToSend = errorMessage.getBytes();
                                            DatagramPacket errorPacket = new DatagramPacket(errorDataToSend, errorDataToSend.length, caughtFrame.getAddress(), caughtFrame.getPort());
                                            client.send(errorPacket);
                                            continue;
                                        }

                                        (new Thread(() -> {
                                            int port2 = 0;
                                            try (ServerSocket tcpServer = new ServerSocket(0)){
                                                port2 = tcpServer.getLocalPort();
                                                fileTransfer = partition[2];
                                                byte[] dataToSend = ("FILE-CONF|" + partition[1] + "|" + port2).getBytes();
                                                DatagramPacket thrownFrame = new DatagramPacket(dataToSend, dataToSend.length, caughtFrame.getAddress(), caughtFrame.getPort());
                                                client.send(thrownFrame);
                                                mf.addLeftLabel("Successfully binded TCP port " + port2);

                                                char[] chunk = new char[200];
                                                int chunkCounter = 0;
                                                int i = 0;

                                                Socket client2 = tcpServer.accept();
                                                try {
                                                    BufferedReader in = new BufferedReader(new FileReader("srcFiles/"+fileTransfer));
                                                    int data;
                                                    while((data = in.read()) != -1){
                                                        if(i<200){
                                                            chunk[i] = (char) data; // As long as we don't reach the limit of characters of a chunk, we append new characters to the chunk array
                                                            i++;
                                                        }else{ //We have reached the limit of characters of a chunk
                                                            String chunkString = new String(chunk);
                                                            PrintWriter out = new PrintWriter(client2.getOutputStream(), true);
                                                            String message = "FILE|"+requestCounter+"|"+fileTransfer+"|"+chunkCounter+"|"+chunkString+"||END||";
                                                            chunkCounter++;         // In this "else", we reached the limit of characters of a chunk, so we increment this counter
                                                            i = 0;                  // We reset the index, so that we can accomodate the characters of the new chunk
                                                            chunk = new char[200];  // we reset the array representing the chunk to make room for the contents of the next chunk
                                                            chunk[i] = (char) data; // This is added, since we would "lose" a character by exiting the "else"
                                                            i++;
                                                            out.println(message);
                                                            out.flush();
                                                        }
                                                    }
                                                    System.out.println("Out of sending loop");
                                                    if(data == -1){ // if data is equal to "-1", it means we reached the end of the file. We need to send the remaining characters.
                                                        String chunkString = new String(Arrays.copyOfRange(chunk, 0, i+1));
                                                        PrintWriter out = new PrintWriter(client2.getOutputStream(), true);
                                                        String message = "FILE-END|"+requestCounter+"|"+fileTransfer+"|"+chunkCounter+"|"+chunkString+"||END||";
                                                        out.println(message);
                                                        out.flush();
                                                    }
                                                    in.close();
                                                } catch (FileNotFoundException e) { // to send FILE-ERROR
                                                    e.printStackTrace();
                                                    PrintWriter out = new PrintWriter(client2.getOutputStream(), true);
                                                    String message = "FILE-ERROR|"+requestCounter+"|File was not found";
                                                    out.println(message);
                                                    out.flush();
                                                } catch(IOException e1){
                                                    e1.printStackTrace();
                                                    PrintWriter out = new PrintWriter(client2.getOutputStream(), true);
                                                    String message = "FILE-ERROR|"+requestCounter+"|IO exception occurred";
                                                    out.println(message);
                                                    out.flush();
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            
                                        })).start();
                                        
                                    }

                                    if(serverMessage.startsWith("FILE-CONF")){ // Confirmed File-Transfer, so create new thread and begin
                                        String[] partition = serverMessage.split("\\|");
                                        if(partition.length != 3){
                                            System.out.println("FILE-ERROR|" + partition[1] +"|" + serverMessage + partition.length + partition[2]);
                                            continue;
                                        }
                                        (new Thread(() -> {
                                            try {
                                                Socket socket = new Socket(mf.server_ip.getText(), Integer.parseInt(partition[2]));
                                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                                StringBuilder currentMessage = new StringBuilder();
                                                int character;
                                                File FileTemp = new File("FileTemp"); // Temp name for file creation
                                                int Index1 = 0;
                                                while(FileTemp.exists()){
                                                    FileTemp = new File("FileTemp "+"("+ Index1 + ")");
                                                    Index1++;
                                                }

                                                BufferedWriter bw = new BufferedWriter(new FileWriter(FileTemp));
                                                String fileName = null;
                                                while((character = in.read()) != 0){
                                                    currentMessage.append((char) character);
                                                    // Assuming "||END||" is the end-of-message marker
                                                    if (currentMessage.toString().endsWith("||END||")) {
                                                        // Process the complete message
                                                        String completeMessage = currentMessage.substring(0, currentMessage.length() - "||END||".length());
                                                        mf.addLeftLabel(completeMessage);
                                                        String[] partitionMessage = completeMessage.toString().split("\\|");
                                                        fileName = partitionMessage[2];
                                                        bw.write(partitionMessage[4]);
                                                        // Reset StringBuilder for the next message
                                                        currentMessage.setLength(0);
                                                    }
                                                }
                                                while((character = in.read()) != 13){
                                                    currentMessage.append((char) character);
                                                    // Assuming "||END||" is the end-of-message marker
                                                    if (currentMessage.toString().endsWith("||END||")) {
                                                        // Process the complete message
                                                        String completeMessage = currentMessage.substring(0, currentMessage.length() - "||END||".length());
                                                        mf.addLeftLabel(completeMessage);
                                                        String[] partitionMessage = completeMessage.toString().split("\\|");
                                                        fileName = partitionMessage[2];
                                                        bw.write(partitionMessage[4]);
                                                        // Reset StringBuilder for the next message
                                                        currentMessage.setLength(0);
                                                    }
                                                }
                                                bw.flush();
                                                bw.close();

                                                File ActualName = new File(fileName);
                                                int index = 1;
                                                while(ActualName.exists()){
                                                    ActualName = new File(fileName+" ("+ index + ")");
                                                    index++;
                                                }
                                                FileTemp.renameTo(ActualName);
                                                socket.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            
                                        })).start();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                
                            }
                        })).start();
                        while (true) {
                            try {
                                synchronized (Client.class) {
                                    Client.class.wait();
                                }
                                if(mf.server_ip.getForeground() == Color.BLACK && mf.server_port.getForeground() == Color.BLACK){

                                    InetAddress destinationAddress = InetAddress.getByName(mf.server_ip.getText());
                                    String pubTest = mf.text;
                                    String fileNames = "";
                                    StringBuilder missingFiles = new StringBuilder();

                                    if (pubTest.startsWith("PUBLISH")){
                                        String[] partitionMessage = pubTest.split("\\|");
                                        for (int i = 3; i < partitionMessage.length; i++) {
                                            fileNames = partitionMessage[i].endsWith(".txt") ? partitionMessage[i] : partitionMessage[i] + ".txt"; // Enforce .txt extension
                                            Path sourceFilePath = Paths.get(sourceDir, fileNames);
                                            // Check if the file exists
                                            if (!Files.exists(sourceFilePath)) {
                                                missingFiles.append(fileNames); // Add to missing files list
                                                continue;                       // Skip this iteration
                                            }
                                        }
                                        if (!missingFiles.isEmpty()) { // If we have missing files, send a dialog box to the User
                                            String missingFilesString = String.join(", ", missingFiles);
                                            JDialog dialog = new JDialog(mf, "ERROR", true);
                                            JLabel dialogL = new JLabel("Missing Files: " + missingFilesString);
                                            dialog.add(dialogL);
                                            dialog.setPreferredSize(new Dimension(300,200));
                                            dialog.pack();
                                            dialog.setVisible(true);
                                            throw new Exception("The following files were not found" + missingFilesString);
                                        }
                                    }
                                    byte[] dataToSend = mf.text.getBytes();
                                    DatagramPacket thrownFrame = new DatagramPacket(dataToSend, dataToSend.length,
                                            destinationAddress, Integer.parseInt(mf.server_port.getText()));
                                    client.send(thrownFrame);
                                    System.out.println("Client: Frame sent with requestCounter = " + requestCounter);
            
                                    requestCounter++; // Increment requestCounter after each request
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Exception: " + e);
                    }
                    mf.addLeftLabel("UDP Port " + mf.client_port.getText() + " does not work... try again");
                })).start();
            }
        }
    }
}