import java.net.*;
import java.io.*;
import java.util.ArrayList;


class Client {

    // state variables


    public ArrayList<String> serverMessageList = new ArrayList<String>();

    public Socket socket;
    public BufferedReader reader;
    public DataOutputStream dataOutputStream;
    public int numberOfMessages;
    public Job currentJob;

    public static void main(String[] args) {
        
        // instantiating a copy of a Client class so variables can be "non-static"
        Client nonStaticClient = new Client();
        nonStaticClient.run(args);
        
    };

    public void run(String[] args){
        System.out.println("Client is up and running!"); 

        try{

            initialise(args);
            connect();
            

            getJobInformation();
            /*
            
            writeThenRead("REDY");
            writeThenRead("GETS Capable 3 700 3800");
            writeThenRead("OK");
            writeThenRead("OK");
            writeThenRead("QUIT");
            */

            dataOutputStream.close();
            reader.close();
            socket.close();

        }
        
        catch(Exception e) {
            System.out.println(e);
        }
    }

    public void initialise(String[] args){
        try{
            int port;

            // if a command variable is provided for port use that else use 6666
            if(args.length == 1){
                port = Integer.parseInt(args[0]);
                System.out.println("Running on port " + port);
            } else { port = 6666; };
            
            socket = new Socket("localhost", port);
            
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e){
            System.out.println(e);
        }
    };

    public void connect(){
        writeThenRead("HELO");
        writeThenRead("AUTH James");
    };

    public void getJobInformation(){
        writeThenRead("REDY");
        Job job = new Job();

        String[] jobStringArray = getLatestMessageSplit();
        job.submitTime = Integer.parseInt(jobStringArray[1]);
        job.jobID = Integer.parseInt(jobStringArray[2]);  
        job.estRuntime = Integer.parseInt(jobStringArray[3]);  
        job.core = Integer.parseInt(jobStringArray[4]);  
        job.memory = Integer.parseInt(jobStringArray[5]);  
        job.disk = Integer.parseInt(jobStringArray[6]);  

        currentJob = job;

        writeThenRead("GETS Capable " + job.core + " " + job.memory + " " + job.disk);
        writeThenRead("OK");
        System.out.println("There are " + numberOfMessages + " servers.");
        
    }

    public String getLatestMessage(){
        return serverMessageList.get(serverMessageList.size()-1);
    }

    public String[] getLatestMessageSplit(){
        return getLatestMessage().split(" ");
    }
    //this method writes a messages to an output stream then reads from the server back
    public void writeThenRead(String message){
        try{
            numberOfMessages = 0;
            System.out.println("Client says: " + message);
            dataOutputStream.write(message.concat("\n").getBytes());
            dataOutputStream.flush();
            while(reader.ready()){
                numberOfMessages++;
                serverMessageList.add(reader.readLine());
                System.out.println("The server says: " + getLatestMessage());
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    };

};
class Job {
    int submitTime;
    int jobID;
    int estRuntime;
    int core;
    int memory;
    int disk;
}

class Server {
    String type;
    int limit;
    double hourlyRate;
    int cores;
    int memory;
    int disk;
    int bootupTime;
}
