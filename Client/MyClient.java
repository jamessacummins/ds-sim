package Client;

import Client.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;

class MyClient {

    // state variables

    public ArrayList<String> serverMessageList = new ArrayList<String>();
    public ArrayList<Server> allServersList;
    public ArrayList<Server> capableServersList = new ArrayList<Server>();
    public ArrayList<Server> largestServersList;
    public ArrayList<Server> activeServersList = new ArrayList<Server>();
    public int currentServerIndex = 0;
    public Socket socket;
    public BufferedReader reader;
    public DataOutputStream dataOutputStream;
    public int numberOfMessages;
    public Job currentJob;
    public Server selectedServer;
    public Boolean printAllMessages = false;

    public static void main(String[] args) {

        // instantiating a copy of a Client class so variables can be "non-static"
        MyClient nonStaticClient = new MyClient();
        nonStaticClient.run(args);

    };

    public void run(String[] args) {
        print("Client is up and running!");

        try {

            initialise(args);
            connect();
            optimise();
            writeThenRead("QUIT");
            dataOutputStream.close();
            reader.close();
            socket.close();

        }

        catch (Exception e) {
            System.out.println(e);
        }
    }

    public void initialise(String[] args) {
        try {
            int port = 50000;

            // if first command variable is provided for port use that else use 50000
            // if either variable 1 or 2 is "v" use verbose mode (print everything)
            if (args.length >= 1) {
                if (args[0].equals("v")) {
                    printAllMessages = true;
                } else {
                    port = Integer.parseInt(args[0]);
                    print("Running on port " + port);
                }
                ;
                if (args.length >= 2) {
                    if (args[1].equals("v"))
                        printAllMessages = true;
                }
            };

            socket = new Socket("localhost", port);

            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println(e);
        }
    };

    public void connect() {
        writeThenRead("HELO");
        writeThenRead("AUTH " + System.getProperty("user.name"));
    };

    public void firstCapable(){
        while (true) {
                writeThenRead("REDY");
                if (getLatestMessage().equals("NONE")) {
                    break;
                } else if (getLatestMessage().contains("JCPL")) {
                    continue;
                } else if (getLatestMessage().contains("JOBN")) {
                    getAndScheduleJobToFirstCapable();
                    continue;
                } else {
                    break;
                }
            }
            ;
    }
    public void optimise(){
        while (true) {
            writeThenRead("REDY");
            if (getLatestMessage().equals("NONE")) {
                break;
            } else if (getLatestMessage().contains("JCPL")) {
                continue;
            } else if (getLatestMessage().contains("JOBN")) {
                getAndScheduleJobViaOptimisation();
                continue;
            } else {
                break;
            }
        }
        ;
    }
    public void getAndScheduleJobViaOptimisation(){
            updateCurrentJob();
            if(allServersList == null) {
                writeThenRead("GETS All");
                writeThenRead("OK");
                initialiseAllServersList();
                writeThenRead("OK");
            }
            else {
                writeThenRead("GETS Capable " + currentJob.core + " " + currentJob.memory + " " + currentJob.disk);
                writeThenRead("OK");
                updateAllServersList();
                selectedServer = getOptimisedServer3();
                print("First capable server is " + selectedServer.type + " " + selectedServer.serverID);
                writeThenRead("OK");
                writeThenRead("SCHD " + currentJob.jobID + " " + selectedServer.type + " " + selectedServer.serverID);
            }
    };
    public int waitingJobsThreshold = 1;
    public Server getOptimisedServer4(){
        // sort servers by turnaround time
        capableServersList.sort(reverseCoreComparator);
        capableServersList.sort(turnaroundComaprator);
        Server server = capableServersList.get(0);
        return server;
    };
    public Server getOptimisedServer3(){
        capableServersList.sort(coreComparator);
        capableServersList.sort(reverseWJobsComparator);
        Server server = capableServersList.get(capableServersList.size()-1);
        print("Server core is " + server.core);
        return capableServersList.get(capableServersList.size()-1);
    };
    public Server getOptimisedServer2(){
        capableServersList.sort(coreComparator);
        Server current = null;
        // first check if their is an active, booting or idle server below the threshold.
        for(int i = capableServersList.size()-1; i >= 0; i--){
            current = capableServersList.get(i);
            if(current.state.equals("idle")) return current;
            if(current.state.equals("active") && current.wJobs < waitingJobsThreshold) return current;
            if(current.state.equals("booting") && current.wJobs < waitingJobsThreshold) return current;
        }
        //then assign it to the first inactive
        for(int i = capableServersList.size()-1; i >= 0; i--){
            current = capableServersList.get(i);
            if(current.state.equals("inactive")) return current;
        }
        //if all are active, idle or booting and above waitingJobsThreshold sort by wJobs then push
        capableServersList.sort(reverseWJobsComparator);
        return capableServersList.get(capableServersList.size()-1);
    };
    public Server getOptimisedServer(){
        Server currentServer;
        Server optimisedServer;
        // first sort capable by cheapest.
        capableServersList.sort(costComparator);
        /*
        // find first active or idle capable servers that has jobs waiting less than threshold
        for(int i = 0; i < capableServersList.size(); i++){
            optimisedServer = capableServersList.get(i);
            if(optimisedServer.state.equals("idle")) return optimisedServer;
            if(optimisedServer.state.equals("active") && optimisedServer.wJobs < waitingJobsThreshold) return optimisedServer;
            if(optimisedServer.state.equals("booting") && optimisedServer.wJobs < waitingJobsThreshold) return optimisedServer;
        }
        // turn on cheapest inactive server
        for(int i = 0; i < capableServersList.size(); i++){
            currentServer = capableServersList.get(i);
            if(currentServer.state.equals("inactive")){
                return currentServer;
            }
        }        
        // run on cheapest server
        for(int i = 0; i < capableServersList.size(); i++){
            return capableServersList.get(i);
        }
        */
        // if all else fails use first capable
        return capableServersList.get(0);
    };
    public class TurnaroundComparator implements Comparator<Server>{
        @Override
        public int compare(Server serverA, Server serverB){

            int serverATurnaround = 0;
            if(serverA.state.equals("booting") || serverA.state.equals("inactive")){
                serverATurnaround += serverA.bootupTime;
            };
            serverA.removeOldJobsFromJobTimes();
            serverATurnaround += serverA.delayTime();

            int serverBTurnaround = 0;
            if(serverB.state.equals("booting") || serverB.state.equals("inactive")){
                serverBTurnaround += serverB.bootupTime;
            };
            serverB.removeOldJobsFromJobTimes();
            serverBTurnaround += serverB.delayTime();

            return Integer.compare(serverATurnaround, serverBTurnaround);
        };
    }
    public class ReverseCoreComparator implements Comparator<Server>{
        @Override
        public int compare(Server serverA, Server serverB){
            return Integer.compare(serverB.core, serverA.core);
        };
    }
    public class CoreComparator implements Comparator<Server>{
        @Override
        public int compare(Server serverA, Server serverB){
            return Integer.compare(serverA.core, serverB.core);
        };
    }
    public class CostComparator implements Comparator<Server>{
        @Override
        public int compare(Server serverA, Server serverB){
            return (int) Double.compare(serverA.hourlyRate, serverB.hourlyRate);
        };
    }
    public class WJobsComparator implements Comparator<Server>{
        @Override
        public int compare(Server serverA, Server serverB){
            return Integer.compare(serverA.wJobs, serverB.wJobs);
        };
    }
    public class ReverseWJobsComparator implements Comparator<Server>{
        @Override
        public int compare(Server serverA, Server serverB){
            return Integer.compare(serverB.wJobs, serverA.wJobs);
        };
    }
    public ReverseCoreComparator reverseCoreComparator = new ReverseCoreComparator();
    public TurnaroundComparator turnaroundComaprator = new TurnaroundComparator();
    public CostComparator costComparator = new CostComparator();
    public WJobsComparator wJobsComparator = new WJobsComparator();
    public ReverseWJobsComparator reverseWJobsComparator = new ReverseWJobsComparator();
    public CoreComparator coreComparator = new CoreComparator();
    public void getAndScheduleJobToFirstCapable(){
        updateCurrentJob();
        writeThenRead("GETS All " + currentJob.core + " " + currentJob.memory + " " + currentJob.disk);
        writeThenRead("OK");
        updateCapableServersList();
        selectedServer = capableServersList.get(0);
        print("First capable server is " + selectedServer.type + " " + selectedServer.serverID);
        writeThenRead("OK");
        writeThenRead("SCHD " + currentJob.jobID + " " + selectedServer.type + " " + selectedServer.serverID);
    };

    public void updateCapableServersList(){
        if(capableServersList == null) {
            capableServersList = new ArrayList<Server>();
        };
        capableServersList.clear();
        for (int i = 0; i < numberOfMessages; i++) {
            String[] serverStringArray = getMessageFromEndSplit(i);
            Server newServer = new Server();
            newServer.type = serverStringArray[0];
            newServer.serverID = Integer.parseInt(serverStringArray[1]);
            newServer.state = serverStringArray[2];
            newServer.curStartTime = Integer.parseInt(serverStringArray[3]);
            newServer.core = Integer.parseInt(serverStringArray[4]);
            newServer.memory = Integer.parseInt(serverStringArray[5]);
            newServer.disk = Integer.parseInt(serverStringArray[6]);
            newServer.wJobs = Integer.parseInt(serverStringArray[7]);
            newServer.rJobs = Integer.parseInt(serverStringArray[8]);
            capableServersList.add(0, newServer);
        }
    }

    public void roundRobin(){
        while (true) {
                writeThenRead("REDY");
                if (getLatestMessage().equals("NONE")) {
                    break;
                } else if (getLatestMessage().contains("JCPL")) {
                    continue;
                } else if (getLatestMessage().contains("JOBN")) {
                    getAndScheduleJobToNextLargestServer();
                    continue;
                } else {
                    break;
                }
            }
            ;
    }

    public void getAndScheduleJobToNextLargestServer() {
        updateCurrentJob();
        writeThenRead("GETS All");
        writeThenRead("OK");
        if (largestServersList == null) {
            updateAllServersList();
            findLargestTypeThenUpdateLargestServersList();
        }
        ;
        selectedServer = getCurrentLargestServer();
        print("Current largest server is " + selectedServer.type + " " + selectedServer.serverID);
        writeThenRead("OK");
        writeThenRead("SCHD " + currentJob.jobID + " " + selectedServer.type + " " + selectedServer.serverID);
    };

    public void findLargestTypeThenUpdateLargestServersList() {
        String largestType = "";
        int largestCoreSize = 0;
        for (Server server : allServersList) {
            if (server.core > largestCoreSize) {
                largestType = server.type;
                largestCoreSize = server.core;
            }
        }
        largestServersList = new ArrayList<Server>();
        for (Server server : allServersList) {
            if (server.type.equals(largestType)) {
                largestServersList.add(server);
            }
        }
        print("Largest server type is " + largestType + ", there are " + largestServersList.size() + " " + largestType
                + " servers.");
    };

    public void updateCurrentJob() {
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
        capableServersList.clear();
        Server server;
        String[] serverStringArray;
        for (int i = 0; i < numberOfMessages; i++) {
            serverStringArray = getMessageFromEndSplit(i);
            server = allServersList.get(0);
            String targetType = serverStringArray[0];
            int targetID = Integer.parseInt(serverStringArray[1]);
            for(int j = 0; j < allServersList.size(); j++){
                server = allServersList.get(j);
                if(targetType.equals(server.type) && server.serverID == targetID) break;
            }
            server.state = serverStringArray[2];
            server.curStartTime = Integer.parseInt(serverStringArray[3]);
            server.core = Integer.parseInt(serverStringArray[4]);
            server.memory = Integer.parseInt(serverStringArray[5]);
            server.disk = Integer.parseInt(serverStringArray[6]);
            server.wJobs = Integer.parseInt(serverStringArray[7]);
            server.rJobs = Integer.parseInt(serverStringArray[8]);
            capableServersList.add(server);
        };
    }
    public void initialiseAllServersList() {
        allServersList = new ArrayList<Server>();
        HashMap<String, Server> serverTypeMap = Parser.getTypeMap();
        Server newServer;
        Server typeModel;
        String[] serverStringArray;
        for (int i = 0; i < numberOfMessages; i++) {
            serverStringArray = getMessageFromEndSplit(i);
            newServer = new Server();
            newServer.type = serverStringArray[0];
            typeModel = serverTypeMap.get(newServer.type);
            newServer.bootupTime = typeModel.bootupTime;
            newServer.limit = typeModel.limit;
            newServer.hourlyRate = typeModel.hourlyRate;
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

    public Server getCurrentLargestServer() {
        Server result = largestServersList.get(currentServerIndex);
        if (currentServerIndex == largestServersList.size() - 1) {
            currentServerIndex = 0;
        } else {
            currentServerIndex++;
        }
        return result;
    };

    public String getLatestMessage() {
        return serverMessageList.get(serverMessageList.size() - 1);
    }

    public String[] getLatestMessageSplit() {
        return getLatestMessage().split(" ");
    }

    public String getMessageFromEnd(int offset) {
        return serverMessageList.get(serverMessageList.size() - (1 + offset));
    }

    public String[] getMessageFromEndSplit(int offset) {
        return getMessageFromEnd(offset).split(" ");
    }

    // this method writes a messages to an output stream then reads from the server
    // back
    public void writeThenRead(String message) {
        try {
            numberOfMessages = 0;
            print("Client says: " + message);
            dataOutputStream.write(message.concat("\n").getBytes());
            dataOutputStream.flush();

            while (!reader.ready()) {
                continue;
            }

            while (reader.ready()) {
                numberOfMessages++;
                String serverResponse = reader.readLine();
                serverMessageList.add(serverResponse);
                print("Server says: " + serverResponse);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        ;
    };

    public void print(Object message) {
        if (printAllMessages) {
            System.out.println(message);
        }
    }

};


