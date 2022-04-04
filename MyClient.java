import java.net.*;
import java.io.*;
import java.util.ArrayList;


class MyClient {

    // state variables


    public ArrayList<String> serverMessageList = new ArrayList<String>();
    public ArrayList<Server> allServersList;
    public ArrayList<Server> largestServersList;
    public int currentServerIndex = 0;
    public Socket socket;
    public BufferedReader reader;
    public DataOutputStream dataOutputStream;
    public int numberOfMessages;
    public Job currentJob;
    public Server selectedServer;

    public static void main(String[] args) {
        
        // instantiating a copy of a Client class so variables can be "non-static"
        MyClient nonStaticClient = new MyClient();
        nonStaticClient.run(args);
        
    };

    public void run(String[] args){
        System.out.println("Client is up and running!"); 

        try{

            initialise(args);
            connect();
            while(true){
                writeThenRead("REDY");
                if(getLatestMessage().equals("NONE") ) {
                    break;
                } else if (getLatestMessage().contains("JCPL")){
                    continue;
                } else if (getLatestMessage().contains("JOBN")){
                    getAndScheduleJobToNextLargestServer();
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
            
            socket = new Socket("localhost", 50000);
            
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e){
            System.out.println(e);
        }
    };

    public void connect(){
        writeThenRead("HELO");
        writeThenRead("AUTH " + System.getProperty("user.name"));
    };

    public void getAndScheduleJobToNextLargestServer(){
        updateCurrentJob();
        writeThenRead("GETS All");
        writeThenRead("OK");
        if(largestServersList == null){
            updateAllServersList();
            findLargestTypeThenUpdateLargestServersList();
        };
        selectedServer = getCurrentLargestServer();
        System.out.println("Current largest server is " + selectedServer.serverType + " " + selectedServer.serverID);
        writeThenRead("OK");
        writeThenRead("SCHD " + currentJob.jobID + " " + selectedServer.serverType + " " + selectedServer.serverID);
    };

    public void findLargestTypeThenUpdateLargestServersList(){
        String largestType = "";
        int largestCoreSize = 0;
        for(Server server: allServersList){
            if(server.core > largestCoreSize){
                largestType = server.serverType;
                largestCoreSize = server.core;
            }
        }
        largestServersList = new ArrayList<Server>();
        for(Server server: allServersList){
            if(server.serverType.equals(largestType)){
                largestServersList.add(server);
            }
        }
        System.out.println("Largest server type is " + largestType + ", there are " + largestServersList.size() + " " + largestType + " servers.");
    };

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

    public void updateAllServersList(){
        allServersList = new ArrayList<Server>();
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
            allServersList.add(0, newServer);
        }
    }
    
    public Server getCurrentLargestServer(){
        Server result = largestServersList.get(currentServerIndex);
        if(currentServerIndex == largestServersList.size()-1){
            currentServerIndex = 0;
        } else {
            currentServerIndex++;
        }
        return result;
    };

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

            while(! reader.ready()){
                continue;
            }

            while(reader.ready()){
                numberOfMessages++;
                String serverResponse = reader.readLine();
                serverMessageList.add(serverResponse);
                System.out.println("Server says: " + serverResponse);
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
