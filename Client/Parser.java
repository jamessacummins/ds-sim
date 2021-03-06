package Client;

import java.io.*;
import java.util.HashMap;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/* 
This class parses an xml file of server information and returns 
information on each Server back as a HashMap so that Servers can 
have additional attributes such as hourlyRate 
*/
public class Parser {
    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder builder;

    public static HashMap<String, Server> getTypeMap() {
        HashMap<String, Server> serverMap = new HashMap<String, Server>();
        try {
            builder = factory.newDocumentBuilder();
            Document xml = builder.parse(new File("ds-system.xml"));
            xml.getDocumentElement().normalize();
            NodeList serverNodeList = xml.getElementsByTagName("server");
            Server server;
            for (int i = 0; i < serverNodeList.getLength(); i++) {
                Element e = (Element) serverNodeList.item(i);
                server = parseServer(e);
                serverMap.put(server.type, server);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverMap;
    }

    public static Server parseServer(Element e) {
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