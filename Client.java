import java.net.*;
import java.io.*;
import java.util.ArrayList;


class Client {

    // state variables


    public ArrayList<String> serverMessageList = new ArrayList<String>();
    public ArrayList<Server> currentServersList;
    public Socket socket;
    public BufferedReader reader;
    public DataOutputStream dataOutputStream;
    public int numberOfMessages;
    public Job currentJob;
    public Server selectedServer;

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
            while(true){
                writeThenRead("REDY");
                System.out.println(getLatestMessage());
                if(getLatestMessage().equals("NONE") ) {
                    break;
                } else if (getLatestMessage().contains("JCPL")){
                    continue;
                } else if (getLatestMessage().contains("JOBN")){
                    getJobInformation();
                    scheduleCurrentJobWithSelectedServer();
                    continue;
                } else {
                    break;
                }
            };

            
            writeThenRead("QUIT");
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
        updateCurrentJob();
        writeThenRead("GETS Capable " + currentJob.core + " " + currentJob.memory + " " + currentJob.disk);
        writeThenRead("OK");
        System.out.println("There are " + numberOfMessages + " servers.");
        updateCurrentServersList();
        selectedServer = useRoundRobinByCoreToGetServer();
        System.out.println("Largest server is " + selectedServer.serverType + " " + selectedServer.serverID);
        writeThenRead("OK");
    }
    
    public void scheduleCurrentJobWithSelectedServer(){
        writeThenRead("SCHD " + currentJob.jobID + " " + selectedServer.serverType + " " + selectedServer.serverID);
    }

    public void updateCurrentJob(){
        currentJob = new Job();
        String[] jobStringArray = getLatestMessageSplit();
        currentJob.submitTime = Integer.parseInt(jobStringArray[1]);
        currentJob.jobID = Integer.parseInt(jobStringArray[2]);  
        currentJob.estRuntime = Integer.parseInt(jobStringArray[3]);  
        currentJob.core = Integer.parseInt(jobStringArray[4]);  
        currentJob.memory = Integer.parseInt(jobStringArray[5]);  
        currentJob.disk = Integer.parseInt(jobStringArray[6]);  
    }

    public void updateCurrentServersList(){
        currentServersList = new ArrayList<Server>();
        for(int i = 0; i < numberOfMessages; i++){
            String[] serverStringArray = getMessageFromEndSplit(i);
            Server newServer = new Server();
            newServer.serverType = serverStringArray[0];
            newServer.serverID = Integer.parseInt(serverStringArray[1]);
            newServer.state = serverStringArray[2];
            newServer.curStartTime = Integer.parseInt(serverStringArray[3]);
            newServer.core = Integer.parseInt(serverStringArray[4]);
            newServer.memory = Integer.parseInt(serverStringArray[5]);
            newServer.disk = Integer.parseInt(serverStringArray[6]);
            newServer.wJobs = Integer.parseInt(serverStringArray[7]);
            newServer.rJobs = Integer.parseInt(serverStringArray[8]);
            currentServersList.add(0, newServer);
        }
    }
    
    public Server useRoundRobinByCoreToGetServer(){
        Server result = currentServersList.get(0);
        for(Server server : currentServersList){
            if(server.core > result.core){
                result = server;
            }
        };
        return result;
    }

    public String getLatestMessage(){
        return serverMessageList.get(serverMessageList.size()-1);
    }

    public String[] getLatestMessageSplit(){
        return getLatestMessage().split(" ");
    }
    public String getMessageFromEnd(int offset){
        return serverMessageList.get(serverMessageList.size()-(1+offset));
    }

    public String[] getMessageFromEndSplit(int offset){
        return getMessageFromEnd(offset).split(" ");
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
        };
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
    String serverType;
    int serverID;
    String state;
    int curStartTime;
    int core;
    int memory;
    int disk;
    int wJobs;
    int rJobs;
}
