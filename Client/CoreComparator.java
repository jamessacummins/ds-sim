package Client;

import java.util.Comparator;

/* 
This is a Comparator. It basically sorts a Server list by smallest core to largest core.
*/
public class CoreComparator implements Comparator<Server>{
    @Override
    public int compare(Server serverA, Server serverB){
        return Integer.compare(serverA.core, serverB.core);
    };
}