package Client;

import Client.*;
import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Parser{
    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder  builder;
    public static void main(String[] args) {  
        Server x = new Server();
        System.out.println(x.getClass());
        ArrayList<Server> list = getServerList();
        System.out.println("finished");
    };
    public static ArrayList<Server> getServerList(){
        ArrayList<Server> serverList = new ArrayList<Server>();
        try {
            builder = factory.newDocumentBuilder();
            Document xml = builder.parse(new File("ds-system.xml"));
            xml.getDocumentElement().normalize();
            NodeList serverNodeList = xml.getElementsByTagName("server");
            Server server;
            for(int i = 0; i < serverNodeList.getLength(); i++){
                Element e = (Element) serverNodeList.item(i);
                server = parseServer(e);
                serverList.add(server);
            }
            System.out.println(serverList.get(0).type);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverList;
    }
    public static Server parseServer(Element e){
        Server server = new Server();
        server.type = e.getAttribute("type");
        server.limit = Integer.parseInt(e.getAttribute("limit"));
        server.bootupTime = Integer.parseInt(e.getAttribute("bootupTime"));
        server.hourlyRate = Double.parseDouble(e.getAttribute("hourlyRate"));
        server.core = Integer.parseInt(e.getAttribute("cores"));
        server.memory = Integer.parseInt(e.getAttribute("memory"));
        server.disk = Integer.parseInt(e.getAttribute("disk"));
        return server;
    }
}