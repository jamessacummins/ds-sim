package Client;
public class Server {
    String type;
    int  limit;
    int bootupTime;
    double hourlyRate;
    int serverID;
    String state;
    int curStartTime;
    int core;
    int memory;
    int disk;
    int wJobs;
    int rJobs;
    public String toString(){
        return "type: " + type + " limit: " + limit + " bootupTime: " + bootupTime + " hourlyRate: " + hourlyRate + " serverID: " + serverID + " state: " + state;
    }
}
