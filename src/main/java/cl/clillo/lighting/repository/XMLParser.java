package cl.clillo.lighting.repository;

import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    public static Document getDocument(final File inputFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(inputFile);
    }

    public static String getPathString(final Document doc, final String path) {
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

    public static Node getNode(final Node node, final String name) {
        NodeList list = node.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node data = list.item(temp);
            if (name.equals(data.getNodeName())) {
                return data;
            }
        }
        return null;
    }

    public static List<Node> getNodeList(final Node node, final String name) {
        final List<Node> nodeList = new ArrayList<>();

        final NodeList list = node.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node data = list.item(temp);
            if (name.equals(data.getNodeName())) {
                NodeList listData = data.getChildNodes();
                for (int i = 0; i < listData.getLength(); i++) {
                    nodeList.add(listData.item(i));
                }
            }
        }
        return nodeList;
    }

    public static String getNodeString(final Node node, final String name) {
        NodeList list = node.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node data = list.item(temp);
            if (data.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) data;
                if (eElement.getNodeName().equals(name))
                    return eElement.getTextContent();

            }
        }
        return null;
    }

    public static int getNodeInt(final Node node, final String name) {
        String value = getNodeString(node, name);
        return (value == null ? -1 : Integer.parseInt(value));
    }

    public static int getPathInt(final Document doc, final String path) {
        String value = getPathString(doc, path);
        return (value == null ? -1 : Integer.parseInt(value));
    }

    public static double getNodeDouble(final Node node, final String name) {
        String value = getNodeString(node, name);
        return (value == null ? -1 : Double.parseDouble(value));
    }

    public static double getPathDouble(final Document doc, final String path) {
        String value = getPathString(doc, path);
        return (value == null ? -1 : Double.parseDouble(value));
    }

    public static boolean getPathBoolean(final Document doc, final String path) {
        String value = getPathString(doc, path);
        return ("true".equalsIgnoreCase(value));
    }

    public static boolean getNodeBoolean(final Node node, final String name) {
        String value = getNodeString(node, name);
        return ("true".equalsIgnoreCase(value));
    }

    public static void writeXMLFile(final String fileName, final ByteArrayOutputStream outputStream) {
        try {
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setIndentSize(3);
            format.setSuppressDeclaration(true);
            format.setEncoding("UTF-8");

            String result = outputStream.toString();
            org.dom4j.Document document = DocumentHelper.parseText(result);
            StringWriter sw = new StringWriter();
            XMLWriter writer = new XMLWriter(sw, format);
            writer.write(document);

            Files.writeString(Path.of(fileName), sw.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getStringAttributeValue(final Node node, final String name) {
        if (node.getAttributes() != null && node.getAttributes().getNamedItem(name)!=null)
            return node.getAttributes().getNamedItem(name).getTextContent();

        return null;
    }

    public static int getIntAttributeValue(final Node node, final String name) {
        String attrValue = getStringAttributeValue(node, name);
        if (attrValue == null)
            return -1;

        return Integer.parseInt(attrValue);
    }

    public static int getInt(final Node node, final String name) {
        int value = XMLParser.getNodeInt(node, name);
        if (value==-1)
            value =  XMLParser.getIntAttributeValue(node, name);

        return value;
    }

    public static boolean getBoolean(final Node node, final String name) {
        String value = XMLParser.getNodeString(node, name);
        if (value==null)
            value =  XMLParser.getStringAttributeValue(node, name);

        return ("true".equalsIgnoreCase(value));
    }
}
