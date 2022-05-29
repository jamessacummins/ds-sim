import Client.Server;
import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class Parser{
    static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder  builder;
    public static void main(String[] args) {
        try {
            builder = factory.newDocumentBuilder();
            Document xml = builder.parse(new File("ds-system.xml"));
            xml.getDocumentElement().normalize();
            NodeList serverNodeList = xml.getElementsByTagName("server");
            for(int i = 0; i < serverNodeList.getLength(); i++){
                Element e = (Element) serverNodeList.item(i);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
    };
    public static ArrayList<Server> getServerList(){
        ArrayList<Server> serverList = new ArrayList<Server>();
    }
}