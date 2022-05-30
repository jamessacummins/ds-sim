package Client;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

class MyClient {

    // state variables used by more than one method

    public ArrayList<String> serverMessageList = new ArrayList<String>();
    public ArrayList<Server> allServersList;
    public ArrayList<Server> capableServersList = new ArrayList<Server>();
    public Socket socket;
    public BufferedReader reader;
    public DataOutputStream dataOutputStream;
    public int numberOfMessages;
    public Job currentJob;
    public Server selectedServer;
    public Boolean printAllMessages = false;

    /*
     * this method is the entry point for the application. It insantiates a
     * non-static version of the class and then calls the run method.
     */
    public static void main(String[] args) {

        // instantiating a copy of a Client class so variables can be "non-static"
        MyClient nonStaticClient = new MyClient();
        nonStaticClient.run(args);

    };

    /*
     * run handles the main flow of the application. It initialises the connection
     * with the server, connects with the first few messages, then runs the main
     * loop which
     * in this case is "optimise". Finally it closes the connection with the server
     * application.
     */
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

    /*
     * Initialise changes the port if it is fed in as a command line variable.
     * It then changes to verbose mode if fed in as a command line variable
     * (printing communication to stdout).
     * Finally it creates the basic boilerplate neccessary to connect to the server
     * application using a socket.
     */
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
            }
            ;

            socket = new Socket("localhost", port);

            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            System.out.println(e);
        }
    };

    /*
     * Connect is the second method called.
     * It simply handles the logic of exchanging initial messsages with the server.
     */
    public void connect() {
        writeThenRead("HELO");
        writeThenRead("AUTH " + System.getProperty("user.name"));
    };

    /*
     * Optimise handles the main logic with a loop.
     * It runs until the latest message is NONE.
     * Until then, it schedules jobs when the message from the server is JOBN, and
     * skips when the message is JCPL.
     */
    public void optimise() {
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

    /*
     * This method when run for the first time GETS all server information, and
     * parses the server.xml file, and creates a list of servers using the
     * initialiseAllServersList() method.
     * It then gets capable servers, updates all servers and runs an algorithm to
     * get the 'optimised' server. Finally it schedules a job to the optimised
     * server.
     */
    public void getAndScheduleJobViaOptimisation() {
        updateCurrentJob();
        if (allServersList == null) {
            writeThenRead("GETS All");
            writeThenRead("OK");
            initialiseAllServersList();
            writeThenRead("OK");
        } else {
            writeThenRead("GETS Capable " + currentJob.core + " " + currentJob.memory + " " + currentJob.disk);
            writeThenRead("OK");
            updateAllServersList();
            selectedServer = getOptimisedServer();
            print("First capable server is " + selectedServer.type + " " + selectedServer.serverID);
            writeThenRead("OK");
            writeThenRead("SCHD " + currentJob.jobID + " " + selectedServer.type + " " + selectedServer.serverID);
        }
    };

    /*
     * This is the crux of the application. All capable servers are first sorted by
     * size of available cores. Then the list is sorted by how many waiting jobs
     * there are. Finally the top of the list is popped off. The optimised server
     * always has the lowest jobs waiting, always is a capable server, and amongst
     * the aforementioned criteria is the largest server by cores.
     */
    public Server getOptimisedServer() {
        capableServersList.sort(coreComparator);
        capableServersList.sort(reverseWJobsComparator);
        Server optimisedServer = capableServersList.get(capableServersList.size() - 1);
        return optimisedServer;
    };

    public ReverseWJobsComparator reverseWJobsComparator = new ReverseWJobsComparator();
    public CoreComparator coreComparator = new CoreComparator();

    /*
     * This is a helper function to store information about jobs for scheduling
     * jobs. It creates a new job object for each job, and pops the latestmessage
     * off the message list to get information.
     */
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

    /*
     * This function updates all servers after a GETS Capable call. Before anything
     * it clears the capable list. It then starts by identifying which server object
     * needs to be updated by matching on type and ID. It then updates key fields
     * and adds to the capable ArrayList capableServersList.
     */
    public void updateAllServersList() {
        capableServersList.clear();
        Server server;
        String[] serverStringArray;
        for (int i = 0; i < numberOfMessages; i++) {
            serverStringArray = getMessageFromEndSplit(i);
            server = allServersList.get(0);
            String targetType = serverStringArray[0];
            int targetID = Integer.parseInt(serverStringArray[1]);
            for (int j = 0; j < allServersList.size(); j++) {
                server = allServersList.get(j);
                if (targetType.equals(server.type) && server.serverID == targetID)
                    break;
            }
            server.state = serverStringArray[2];
            server.curStartTime = Integer.parseInt(serverStringArray[3]);
            server.core = Integer.parseInt(serverStringArray[4]);
            server.memory = Integer.parseInt(serverStringArray[5]);
            server.disk = Integer.parseInt(serverStringArray[6]);
            server.wJobs = Integer.parseInt(serverStringArray[7]);
            server.rJobs = Integer.parseInt(serverStringArray[8]);
            capableServersList.add(server);
        }
        ;
    }

    /*
     * This function calls a parser to get information on the servers by the config
     * file in xml. It then reads in all servers from a "GETS All" call and creates
     * a custom Server object by combining the two using the newServer from GETS All
     * and the additional type attributes of "typeModel". Finally it adds it to the
     * list of all servers.
     */
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

    /* The following methods are helper methods to access the serverMessageList. */
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

    // this method prints everything when verbose method is activated and is called
    // mostly by writeThenRead.
    public void print(Object message) {
        if (printAllMessages) {
            System.out.println(message);
        }
    }

};
