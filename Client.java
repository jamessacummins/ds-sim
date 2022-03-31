import java.net.*;
import java.io.*;
import java.util.ArrayList;


class Client {

    // state variables


    public ArrayList<String> serverMessageList = new ArrayList<String>();
    public String[] jobn = new String[7];
    public String[] data = new String[3];

    public String test;

    public BufferedReader reader;
    public DataOutputStream dataOutputStream;
    
    public static void main(String[] args) {
        
        // instantiating a copy of a Client class so variables can be "non-static"
        Client nonStaticClient = new Client();
        nonStaticClient.run(args);
        
    };

    public void run(String[] args){
        System.out.println("Client is up and running!"); 

        try{
            int port;

            // if a command variable is provided for port use that else use 6666
            if(args.length == 1){
                port = Integer.parseInt(args[0]);
                System.out.println("running on port " + port);
            } else { port = 6666; };
            Socket socket = new Socket("localhost", port);


            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writeThenRead("HELO");
            writeThenRead("AUTH James");
            writeThenRead("REDY");
            writeThenRead("GETS Capable 3 700 3800");
            writeThenRead("OK");
            writeThenRead("OK");
            writeThenRead("QUIT");
            
            dataOutputStream.close();
            reader.close();
            socket.close();

        }
        
        catch(Exception e) {
            System.out.println(e);
        }
    }

    //this method writes a messages to an output stream then reads from the server back
    public void writeThenRead(String message){
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
