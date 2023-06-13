package cl.clillo.ilumination.model;

import cl.clillo.ilumination.config.QLCReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QLCModel {

    private final List<QLCFixture> fixtureList = new ArrayList<>();
    private final List<QLCFunction> functionList = new ArrayList<>();
    private final List<QLCFixtureModel> fixtureModelList = new ArrayList<>();
    private final Map<String, QLCFixtureModel> fixtureModelMap = new HashMap<>();
    private final Map<Integer, QLCFixture> fixtureMap = new HashMap<>();
    private final Map<Integer, QLCFunction> functionMap = new HashMap<>();

    public QLCModel() {
        try {
            readFixtureModelsList();

            File inputFile = new File(QLCReader.repoBase +"config.qxw");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputFile);

            Node engine = doc.getElementsByTagName("Engine").item(0);

            readFixtureList(engine);
            readFunctionList(engine, "Scene");
            readFunctionList(engine, "Sequence");
            readFunctionList(engine, "Chaser");
            readFunctionList(engine, "Efx");
            readFunctionList(engine, "RGBMatrix");
            readFunctionList(engine, "Collection");

            NodeList list = engine.getChildNodes();

            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;

                    if ("Function".equals(eElement.getNodeName()) || "Fixture".equals(eElement.getNodeName())) {
                        continue;
                    }
                    if ("InputOutputMap".equals(eElement.getNodeName())) {
                        continue;
                    }
                    if ("FixtureGroup".equals(eElement.getNodeName())) {
                        continue;
                    }

                    if ("Monitor".equals(eElement.getNodeName())) {
                        continue;
                    }
                    System.out.println(eElement.getNodeName());
                    //  System.out.println(eElement.getNodeName());

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFixtureList(final Node engine){
        NodeList list = engine.getChildNodes();

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element eElement = (Element) node;

            if (!"Fixture".equals(eElement.getNodeName()))
                continue;

            QLCFixture fixture = getFixture(node);
            fixtureMap.put(fixture.getId(), fixture);
            fixtureList.add(fixture);
        }
    }

    private void readFunctionList(final Node engine, final String type){
        final int nAnterior = functionList.size();
        final NodeList list = engine.getChildNodes();

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            final Element eElement = (Element) node;

            if (!"Function".equals(eElement.getNodeName()))
                continue;

            final QLCFunction function = getFunction(node, type);
            if (function!=null) {
                functionList.add(function);
                functionMap.put(function.getId(), function);
            }
        }

        System.out.println(type+"\t"+(functionList.size()-nAnterior));

    }

    private void readFixtureModelsList() throws ParserConfigurationException, IOException, SAXException {
        File dir = new File(QLCReader.repoBase + "Fixtures");
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            QLCFixtureModel qlcFixtureModel = getFixtureModel(file);
            fixtureModelList.add(qlcFixtureModel);
            fixtureModelMap.put(qlcFixtureModel.getManufacturer()+"."+qlcFixtureModel.getModel(), qlcFixtureModel);
        }
    }

    private QLCFixtureModel getFixtureModel(final File inputFile) throws ParserConfigurationException, IOException, SAXException {
        final QLCFixtureModel.QLCFixtureModelBuilder builder = QLCFixtureModel.builder();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputFile);

        Node engine = doc.getElementsByTagName("FixtureDefinition").item(0);

        NodeList list = engine.getChildNodes();

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element eElement = (Element) node;
            if ("Manufacturer".equals(eElement.getNodeName()))
                builder.manufacturer(eElement.getTextContent());
            if ("Model".equals(eElement.getNodeName()))
                builder.model(eElement.getTextContent());
            if ("Type".equals(eElement.getNodeName()))
                builder.type(eElement.getTextContent());

            if ("Mode".equals(eElement.getNodeName())) {
                builder.channels(getFixtureMode(node));
            }
        }

        //   System.out.println( builder.build());
        return builder.build();
    }

    private String [] getFixtureMode(final Node fixtureNode) {
        final NodeList list = fixtureNode.getChildNodes();
        final Map<Integer, String> channels = new HashMap<>();

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                if ("Channel".equals(eElement.getNodeName()))
                    channels.put(getAttributeInt(node, "Number"), node.getTextContent());
            }
        }

        String [] output = new String[channels.size()];
        for (int i=0; i< channels.size(); i++)
            output[i] = channels.get(i);

        return output;
    }

    private QLCFixture getFixture(final Node fixtureNode) {
        final NodeList list = fixtureNode.getChildNodes();
        final QLCFixture.QLCFixtureBuilder builder = QLCFixture.builder();

        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;
                if ("Manufacturer".equals(eElement.getNodeName()))
                    builder.manufacturer(eElement.getTextContent());
                if ("Name".equals(eElement.getNodeName()))
                    builder.name(eElement.getTextContent());
                if ("Model".equals(eElement.getNodeName()))
                    builder.model(eElement.getTextContent());
                if ("Mode".equals(eElement.getNodeName()))
                    builder.mode(eElement.getTextContent());
                if ("ID".equals(eElement.getNodeName()))
                    builder.id(Integer.parseInt(eElement.getTextContent()));
                if ("Universe".equals(eElement.getNodeName()))
                    builder.universe(Integer.parseInt(eElement.getTextContent()));
                if ("Address".equals(eElement.getNodeName()))
                    builder.address(Integer.parseInt(eElement.getTextContent()));
                if ("Channels".equals(eElement.getNodeName()))
                    builder.channels(Integer.parseInt(eElement.getTextContent()));
            }
        }

        final QLCFixture fixture = builder.build();
        fixture.setFixtureModel(fixtureModelMap.get(fixture.getManufacturer()+"."+fixture.getModel()));

        //  System.out.println(fixture);
        return fixture;
    }

    private String getAttribute(final Node node, final String name) {
        NamedNodeMap attr = node.getAttributes();
        Node nodeAttr = attr.getNamedItem(name);
        if (nodeAttr == null)
            return "";
        return nodeAttr.getTextContent();
    }

    private int getAttributeInt(final Node node, final String name) {
        return Integer.parseInt(getAttribute(node, name));
    }

    private QLCFunction getFunction(final Node functionNode, final String type) {
        final QLCFunction.QLCFunctionBuilder builder = QLCFunction.builder();
        final int id = getAttributeInt(functionNode, "ID");
        final String nodeType = getAttribute(functionNode, "Type");

        if (!type.equalsIgnoreCase(nodeType))
            return null;

        builder.id(id);
        builder.type(nodeType);
        builder.name(getAttribute(functionNode, "Name"));
        builder.path(getAttribute(functionNode, "Path"));
        boolean hidden = getAttribute(functionNode, "Hidden").equalsIgnoreCase("True");

        if (hidden)
            return null;

        final NodeList list = functionNode.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element eElement = (Element) node;
            if ("FixtureVal".equals(eElement.getNodeName())) {
                QLCFixture fixture = fixtureMap.get(getAttributeInt(node, "ID"));
                String [] values = eElement.getTextContent().split(",");
                for (int i=0; i<values.length; i+=2){
                    int channel = Integer.parseInt(values[i]);
                    QLCPoint qlcPoint = QLCPoint.builder()
                            .fixture(fixture)
                            .channel(channel)
                            .value(Integer.parseInt(values[i+1]))
                            .dmxChannel(channel + fixture.getAddress())
                            .build();
                    builder.addPointList(qlcPoint);
                }
            }

            if ("Collection".equalsIgnoreCase(type) && "Step".equals(eElement.getNodeName())) {
                final int functionInt = Integer.parseInt(eElement.getTextContent());
                QLCFunction function = functionMap.get(functionInt);
                if (function==null){
                    System.out.println("Collection ["+id+"] function not found: " +functionInt);
                }
                builder.addFunctionList(function);
            }

            if ("Sequence".equalsIgnoreCase(type) && "Step".equals(eElement.getNodeName())) {
                builder.addStepList(getSequenceStep(node));

            }

            if ("Sequence".equalsIgnoreCase(type) && "Direction".equals(eElement.getNodeName())) {
                builder.direction(QLCDirection.valueOf(eElement.getTextContent().toUpperCase()));

            }

            if ("Sequence".equalsIgnoreCase(type) && "RunOrder".equals(eElement.getNodeName())) {
                builder.runOrder(QLCRunOrder.valueOf(eElement.getTextContent().toUpperCase()));

            }
        }

        QLCFunction qlcFunction = builder.build();

        if ("Sequence".equalsIgnoreCase(type))
            System.out.println(qlcFunction.toSmallString());

        return qlcFunction;
    }

    private QLCStep getSequenceStep(final Node node){
        QLCStep.QLCStepBuilder builder = QLCStep.builder();
        builder.fadeIn(getAttributeInt(node, "FadeIn"));
        builder.hold(getAttributeInt(node, "Hold"));
        builder.fadeOut(getAttributeInt(node, "FadeOut"));

        Element eElement = (Element) node;
        System.out.println(eElement.getTextContent());

        return builder.build();
    }
}
