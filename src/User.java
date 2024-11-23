import java.util.HashSet;
import java.util.Set;

public class User {
    private String name;
    private String ipAddress;
    private String udpSocket;
    private Set<String> files;

    public User (){
        this.files = new HashSet<>();
    }

    public User(String name, String ipAddress, String udpSocket) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.udpSocket = udpSocket;
        this.files = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUdpSocket() {
        return udpSocket;
    }

    public void setUdpSocket(String udpSocket) {
        this.udpSocket = udpSocket;
    }

    public Set<String> getFiles() {
        return files;
    }

    public void setFiles(Set<String> files) {
        this.files = files;
    }

    public void addFile(String fileName) {
        this.files.add(fileName);
    }

    public void removeFile(String fileName) {
        this.files.remove(fileName);
    }


    @Override
    public String toString() {
        return "User{ " +
                "Name: '" + name + '\'' +
                ", IPAddress: '" + ipAddress + '\'' +
                ", UDPSocket: '" + udpSocket + '\'' +
                ", Files: " + files +
                '}';
    }
}
