package Client;

import java.util.Comparator;

/* 
This is a Comparator. It basically sorts a Server list by largest wJobs to smallest.
*/
public class ReverseWJobsComparator implements Comparator<Server> {
    @Override
    public int compare(Server serverA, Server serverB) {
        return Integer.compare(serverB.wJobs, serverA.wJobs);
    };
}