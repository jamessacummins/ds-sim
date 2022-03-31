import java.net.*;
import java.io.*;
import java.util.ArrayList;


class Client {

    // state variables
    public static ArrayList<String> serverMessageList = new ArrayList<String>();
    public static String[] jobn = new String[7];
    public static String[] data = new String[3];

    //this method writes a messages to an output stream then reads from the server back
    public static void writeThenRead(String message, BufferedReader reader, DataOutputStream dataOutputStream){
        try{
            System.out.println("Client says: " + message);
            dataOutputStream.write(message.concat("\n").getBytes());
            dataOutputStream.flush();
            while(reader.ready()){
                serverMessageList.add(reader.readLine());
                System.out.println("The server says: " + serverMessageList.get(serverMessageList.size()-1));
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    };

    public static void main(String[] args) {
        System.out.println("Client3 is up and running!"); 

        try{
            int port;

            // if a command variable is provided for port use that else use 6666
            if(args.length == 1){
                port = Integer.parseInt(args[0]);
                System.out.println("running on port " + port);
            } else { port = 6666; };
            Socket socket = new Socket("localhost", port);


            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writeThenRead("HELO",reader,dataOutputStream);
            writeThenRead("AUTH James",reader,dataOutputStream);
            writeThenRead("REDY",reader,dataOutputStream);
            writeThenRead("GETS Capable 3 700 3800",reader,dataOutputStream);
            writeThenRead("OK",reader,dataOutputStream);
            writeThenRead("OK",reader,dataOutputStream);
            writeThenRead("QUIT",reader,dataOutputStream);
            
            dataOutputStream.close();
            reader.close();
            socket.close();

        }
        
        catch(Exception e) {
            System.out.println(e);
        }
    };
};

class Server {
    String type;
    int limit;
    double hourlyRate;
    int cores;
    int memory;
    int disk;
    int bootupTime;
}
