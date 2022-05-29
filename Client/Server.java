package Client;

import java.util.ArrayList;

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
    ArrayList<Integer> outstandingJobTimes = new ArrayList<Integer>();
    public void removeOldJobsFromJobTimes(){
        int currentJobs = rJobs + wJobs;
        int removalSize = outstandingJobTimes.size() - currentJobs;
        for(int i = 0; i < removalSize; i++){
            outstandingJobTimes.remove(0);
        }
    }
    public int delayTime(){
        int result = 0;
        int jobsLeft = outstandingJobTimes.size();
        for(int i = jobsLeft - wJobs - 1; i < jobsLeft; i++){
            result += outstandingJobTimes.get(i);
        }
        return result;
    }
    public String toString(){
        return "type: " + type + " limit: " + limit + " bootupTime: " + bootupTime + " hourlyRate: " + hourlyRate + " serverID: " + serverID + " state: " + state + " wJobs:" + wJobs;
    }
}
