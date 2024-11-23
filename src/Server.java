import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;


public class Server {
    private static final Lock lock = new ReentrantLock();
    private static final String CSV_PATH = "registered_users.csv";
    private static ArrayList<User> registeredUsers = new ArrayList<>();
    private static Set<String> connectedUsers = new HashSet<>();
    private static long requestCounter = 0;
    private static String responseToClient;
    private static Timer timer = new Timer();
    private static File file = new File(CSV_PATH);
    private static long lastModifiedTime = file.lastModified();

    public static void main(String[] args) {
        try {
            // Check if the CSV file exists
            File csvFile = new File(CSV_PATH);
            if (!csvFile.exists()) {
                // File does not exist, create it
                FileWriter fileWriter = new FileWriter(csvFile);
                // Write the header row for the CSV file
                fileWriter.append("Name, IP Address, UDP Socket, Files\n");
                fileWriter.flush();
                fileWriter.close();
                System.out.println("Created new CSV file for registered users.");
            } else {
                System.out.println("CSV file for registered users already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("An error occurred while checking or creating the CSV file.");
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a socket number for the server: ");
        final int PORT = scanner.nextInt();
        scanner.close();
        readCSV(); // Update Registered User list
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            updatePeriodic(serverSocket);
            System.out.println("Server operational on port: " + PORT);

            Boolean loopEnable = true;
            System.out.println("Server: out of loop");
            do {
                System.out.println("Server: in loop");
                byte[] dataReceived = new byte[1024];
                DatagramPacket caughtFrame = new DatagramPacket(dataReceived, dataReceived.length);
                serverSocket.receive(caughtFrame);
                System.out.println("Server: caught Frame");
                System.out.println((caughtFrame.getAddress()).getHostAddress()+" "+caughtFrame.getPort());
                connectedUsers.add((caughtFrame.getAddress()).getHostAddress()+" "+caughtFrame.getPort());
                //connectedUsers.add(new User("", ""+(caughtFrame.getAddress()).getHostAddress(), ""+caughtFrame.getPort()));
                new Thread(new ClientRequestHandler(serverSocket, caughtFrame)).start();

            } while (loopEnable);
        } catch (Exception e) {
            System.out.println("Server exception " + e);
        }
    }
    

    private static class ClientRequestHandler implements Runnable {
        private final DatagramSocket socket;
        private final DatagramPacket packet;

        public ClientRequestHandler(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.packet = packet;
        }

        @Override
        public void run() {
            lock.lock();
            try {
                requestCounter++;
            } finally {
                lock.unlock();
            }

            // Process the request
            String clientMessage = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + clientMessage + " | Request #" + requestCounter);
            String[] tokens = partitionMessage(clientMessage);
            String response = processRequest(tokens);

            // Send the response back to the client
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
            try {
                socket.send(responsePacket);
                System.out.println("Server: Frame sent");
                for (User registeredUser : registeredUsers) {
                    System.out.println(registeredUser.toString());
                }

                if (file.lastModified() != lastModifiedTime) {
                    updatePeriodic(socket);
                    lastModifiedTime = file.lastModified();
                }
            } catch (IOException e) {
                System.out.println("Error sending response: " + e.getMessage());
            }
            System.out.println("Response sent: " + response);
        }


        private String[] partitionMessage(String message) {
            return message.split("\\|");
        }


        private String processRequest(String[] tokens) {
            String command = tokens[0].toUpperCase();
            switch (command) {
                case "REGISTER": {
                    Boolean registered = userRegistrationCheck(tokens[2]); // Verify for Unused Username and Socket number
                    Boolean socketUsed = userSocketCheck(tokens[3], tokens[4]);
                    responseToClient = registerUser(registered, socketUsed, tokens);
                    return responseToClient;
                }
                case "DE-REGISTER": {
                    Boolean registered = userRegistrationCheck(tokens[2]);
                    responseToClient = deregisterUser(registered, tokens);
                    return responseToClient;
                }
                case "PUBLISH": {
                    Boolean registered = userRegistrationCheck(tokens[2]);
                    responseToClient = publishFile(registered, tokens);
                    return responseToClient;
                }
                case "REMOVE": {
                    Boolean registered = userRegistrationCheck(tokens[2]);
                    responseToClient = removeFile(registered, tokens);
                    return responseToClient;
                }
                case "UPDATE-CONTACT": {
                    Boolean registered = userRegistrationCheck(tokens[2]);
                    Boolean socketUsed = userSocketCheck(tokens[3], tokens[4]);
                    responseToClient = updateUser(registered, socketUsed, tokens);
                    return responseToClient;
                }

                default: {
                    System.out.println("There was an error whilst processing the request");
                    responseToClient = "ERROR";
                    return responseToClient;
                }
            }
        }
    }

        
    private static void readCSV() {
        Path csvPath = Paths.get(CSV_PATH);
        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line = br.readLine(); // Skip the header line
    
            while ((line = br.readLine()) != null) {
                String[] userDetails = line.split(",", -1); // Split all, include trailing empty strings
    
                if (userDetails.length >= 4) {
                    String name = userDetails[0].trim();
                    String ipAddress = userDetails[1].trim();
                    String udpSocket = userDetails[2].trim();
                    User user = new User(name, ipAddress, udpSocket);
    
                    // Assuming files are listed after the UDP socket and separated by "|"
                    if (userDetails.length > 3) {
                        String[] files = userDetails[3].split("\\|");
                        for (String file : files) {
                            if (!file.trim().isEmpty()) {
                                user.addFile(file.trim());
                            }
                        }
                    }
                    registeredUsers.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the CSV file: " + e.getMessage());
        }
    }    


    private static void writeToCSV(ArrayList<User> registeredUsers) {
        lock.lock();
        try {
            String tempFilePath = "registered_users_temp.csv"; // Temporary file
            File tempFile = new File(tempFilePath);
            try (FileWriter writer = new FileWriter(tempFile, false)) {
                writer.write("Name, IP Address, UDP Socket, Files\n");
                for (User user : registeredUsers) {
                    String files = String.join("|", user.getFiles()); // Join files with |
                    writer.write(user.getName() + ", " + user.getIpAddress() + ", " + user.getUdpSocket() + ", " + files + "\n");
                }
                writer.flush();
            } catch (IOException e) {
                System.out.println("Error writing to temporary CSV: " + e.getMessage());
                return; // Exit the method if writing failed
            }
    
            // Rename temp file to actual CSV file path, effectively replacing it
            File actualFile = new File(CSV_PATH);
            if (actualFile.delete()) { // Delete the actual file
                if (!tempFile.renameTo(actualFile)) { // Rename temp file to actual file name
                    System.out.println("Error renaming temporary file to actual CSV path");
                }
            } else {
                System.out.println("Error deleting actual CSV file");
            }
        } finally {
            lock.unlock();
        }
    }
    

    private static String registerUser(Boolean registered, Boolean socketUsed, String[] tokens) {
        if (!registered) {
            if (!socketUsed) {
            User user = new User(tokens[2], tokens[3], tokens[4]);
            lock.lock();
            try {
                registeredUsers.add(user); // Add User, if User provides a registered UserName and valid Socket number
            }  finally {
                lock.unlock();
            }
            writeToCSV(registeredUsers); // Update CSV
            return ("REGISTERED | " + requestCounter);
            } else {
                return ("REGISTER-DENIED | " + requestCounter + " | Socket already taken");
            }
        } else {
            return ("REGISTER-DENIED | " + requestCounter + " | Name already taken");
        }
    }
    

    private static String deregisterUser(Boolean registered, String[] tokens) {
        if (registered) {
            for (User registeredUser : registeredUsers) {
                if (registeredUser.getName().equals(tokens[2])) {
                    lock.lock();
                    try {
                        registeredUsers.remove(registeredUser); // Remove User, if User provides a registered UserName
                    } finally {
                        lock.unlock();
                    }
                    writeToCSV(registeredUsers); // Update CSV
                    break;
                }
            }
            return ("DE-REGISTERED | " + requestCounter);
        } else {
            return ("DE-REGISTER-DENIED | " + requestCounter + " | Name does not exist");
        }
    }


    private static String updateUser(Boolean registered, Boolean socketUsed, String[] tokens) {
        if (registered) {
            if (!socketUsed) {
                lock.lock();
                try {
                for (User registeredUser : registeredUsers) {
                    if (registeredUser.getName().equals(tokens[2])) {
                        registeredUser.setIpAddress(tokens[3]);
                        registeredUser.setUdpSocket(tokens[4]);
                        writeToCSV(registeredUsers); // Update CSV, if User provides a registered UserName and valid Socket number
                        break;
                    }
                }
                } finally {
                lock.unlock();
                }
            } else {
                return ("UPDATE-DENIED | " + requestCounter + " | Socket already taken");
            }
            return ("UPDATE-CONFIRMED | " + requestCounter + " | " + tokens[2] + " | " + tokens[3] + " | " + tokens[4]);
        } else {
            return ("UPDATE-DENIED | " + requestCounter + " | " + tokens[2] + " | Name does not exists");
        }
    }


    private static String publishFile(boolean registered, String[] tokens) { // File check is done on the client side
        String userName = tokens[2];
        if (!registered) {
            return "PUBLISH-DENIED | " + requestCounter + " | Name does not exist";
        }
        lock.lock();
        try {
            try {
                User user = findUserByName(userName);
                
                for (int i = 3; i < tokens.length; i++) {
                    String fileName = tokens[i].endsWith(".txt") ? tokens[i] : tokens[i] + ".txt"; // Enforce .txt extension
                    user.addFile(fileName); // Add the file(s) the appropriate User
                }

                writeToCSV(registeredUsers);
                return "PUBLISHED | " + requestCounter;
                
            } catch (Exception e) {
                return "PUBLISH-DENIED | " + requestCounter + " | Error publishing file(s): " + e.getMessage();
            }
        } finally {
            lock.unlock();
        }
    }
    

    private static String removeFile(boolean registered, String[] tokens) {
        if (!registered) {
            return "REMOVE-DENIED | " + requestCounter + " | Name does not exist";
        }
        lock.lock();
        try {
            User user = findUserByName(tokens[2]);
            StringBuilder response = new StringBuilder();
            for (int i = 3; i < tokens.length; i++) {
                String fileName = tokens[i].endsWith(".txt") ? tokens[i] : tokens[i] + ".txt"; // Enforce .txt extension
                if (user.getFiles().contains(fileName)) { // Check if the file exists (on the CSV) before attempting to remove
                    user.removeFile(fileName);
                    response.append("REMOVED | ").append(requestCounter);
                } else {
                    response.append(" REMOVE-DENIED | ").append(requestCounter).append(" | File(s) not found: ").append(fileName); // This method is to remove the file(s) that are present even if some are not
                }
            }
            writeToCSV(registeredUsers);
            return response.toString();
        } finally {
            lock.unlock();
        }
    }


    private static User findUserByName(String name) { // Scan the list of users to find a matching username
        lock.lock();
        try {
            for (User registeredUser : registeredUsers) {
                if (registeredUser.getName().equals(name)) {
                    return registeredUser;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }


    private static Boolean userRegistrationCheck(String name) { //  Checks whether a given username is already in use, returns false if not
        lock.lock();
        try {
            for (User registeredUser : registeredUsers) {
                if (registeredUser.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    
    private static Boolean userSocketCheck(String ipAddress, String socketString) { // Checks whether the IP and UDP socket are valid
        lock.lock();
        try {
            for (User registeredUser : registeredUsers) {
                if (registeredUser.getIpAddress().equals(ipAddress)) {
                    if (registeredUser.getUdpSocket().equals(socketString)) {
                        return true;
                    }
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }


    private static void updatePeriodic(DatagramSocket server) {
        timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Read the entire CSV file into a String
                    String CSVContent = new String(Files.readAllBytes(Paths.get(CSV_PATH)), StandardCharsets.UTF_8);
                    // Preparing the message to send to clients.
                    String updateMessage = "UPDATE | " + CSVContent;
    
                    byte[] dataToSend = updateMessage.getBytes(StandardCharsets.UTF_8);
                    
                    System.out.println("Number of connected users: "+connectedUsers.size());
                    for (String s : connectedUsers) {
                        String[] user = s.split(" ");
                        InetAddress destinationAddress = InetAddress.getByName(user[0]);
                        int udpPort = Integer.parseInt(user[1]);
                        DatagramPacket updatePacket = new DatagramPacket(dataToSend, dataToSend.length, destinationAddress, udpPort);
                        server.send(updatePacket);
                    }

                } catch (IOException e) {
                    System.out.println("Exception in updatePeriodic: " + e.getMessage());
                }
                System.out.println("Sent update");
            }
        }, 0, 300000); // Period: run every 5 minutes (300,000 milliseconds)   
    }
    
}
