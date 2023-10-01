package cl.clillo.lighting.repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

public class XMLParser {

    public static Document getDocument(final File inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(inputFile);
    }

    public static String getPathString(final Document doc, final String path){
        XPath xPath = XPathFactory.newInstance().newXPath();
        Node node = null;
        try {
            node = (Node) xPath.compile(path).evaluate(doc, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

        if (node != null) {
            return node.getTextContent();
        }

        return null;
    }

    public static String getNodeString(final Node node, final String name){
        NodeList list = node.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node data = list.item(temp);
            if (data.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) data;
                if(eElement.getNodeName().equals(name))
                    return eElement.getTextContent();

            }
        }
        return null;
    }

    public static int getNodeInt(final Node node, final String name){
        String value = getNodeString(node, name);
        return (value==null?-1: Integer.parseInt(value));
    }

    public static int getPathInt(final Document doc, final String path){
        String value = getPathString(doc, path);
        return (value==null?-1: Integer.parseInt(value));
    }

    public static double getNodeDouble(final Node node, final String name){
        String value = getNodeString(node, name);
        return (value==null?-1: Double.parseDouble(value));
    }

    public static double getPathDouble(final Document doc, final String path){
        String value = getPathString(doc, path);
        return (value==null?-1: Double.parseDouble(value));
    }

    public static boolean getPathBoolean(final Document doc, final String path){
        String value = getPathString(doc, path);
        return ("true".equalsIgnoreCase(value));
    }

    public static boolean getNodeBoolean(final Node node, final String name){
        String value = getNodeString(node, name);
        return ("true".equalsIgnoreCase(value));
    }

}
