package cl.clillo.lighting.model;

import cl.clillo.lighting.config.QLCReader;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.fixture.qlc.QLCFixtureModel;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.utils.StringUtils;
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
import java.util.Collections;
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
                  //  System.out.println(eElement.getNodeName());
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

     //   System.out.println(type+"\t"+(functionList.size()-nAnterior));

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
        return QLCFixtureModel.builder().build(inputFile);

    }

    private QLCFixture getFixture(final Node fixtureNode) {
        return QLCFixture.builder().build(fixtureNode,fixtureModelMap);

    }

    private static String getAttribute(final Node node, final String name) {
        NamedNodeMap attr = node.getAttributes();
        Node nodeAttr = attr.getNamedItem(name);
        if (nodeAttr == null)
            return "";
        return nodeAttr.getTextContent();
    }

    public static int getAttributeInt(final Node node, final String name) {
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

    //    if (hidden)
   //         return null;

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
                            .data(Integer.parseInt(values[i+1]))
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

            if ("Efx".equalsIgnoreCase(type) && "Algorithm".equals(eElement.getNodeName())) {
                builder.algorithm(QLCAlgorithm.valueOf(eElement.getTextContent().toUpperCase()));

            }

            if ("Sequence".equalsIgnoreCase(type) && "Speed".equals(eElement.getNodeName())) {
                builder.speed(getSequenceSpeed(node));

            }
        }

        if ("Sequence".equalsIgnoreCase(type)) {
            builder.boundScene((QLCScene) functionMap.get(getAttributeInt(functionNode, "BoundScene")));

        }


        QLCFunction qlcFunction = builder.build();

    //    if ("Efx".equalsIgnoreCase(type))
      //      System.out.println(qlcFunction.toSmallString());

        return qlcFunction;
    }

    private QLCSpeed getSequenceSpeed(final Node node) {
        return QLCSpeed.builder()
                .duration(getAttributeInt(node, "Duration"))
                .fadeIn(getAttributeInt(node, "FadeIn"))
                .fadeOut(getAttributeInt(node, "FadeOut"))
                .build();
    }

    private QLCStep getSequenceStep(final Node node){
        final QLCStep.QLCStepBuilder builder = QLCStep.builder();
        builder.id(getAttributeInt(node, "Number"));
        builder.fadeIn(getAttributeInt(node, "FadeIn"));
        builder.hold(getAttributeInt(node, "Hold"));
        builder.fadeOut(getAttributeInt(node, "FadeOut"));

        final Element eElement = (Element) node;
        //System.out.println(eElement.getTextContent());
        final List<QLCPoint> pointList = new ArrayList<>();
        builder.pointList(pointList);

        if (eElement.getTextContent().length()>0){
            String []part1 = eElement.getTextContent().split(":");

            for (int i=0; i<part1.length;i+=2){
                final int fixtureId = Integer.parseInt(part1[i]);
                final QLCFixture fixture = fixtureMap.get(fixtureId);
                String []part2 = part1[i+1].split(",");

                for (int j=0; j<part2.length; j+=2) {
                    final int dxmChannel = Integer.parseInt(part2[j]);
                    final int dmxValue = Integer.parseInt(part2[j+1]);
                    QLCPoint qlcPoint = QLCPoint.builder()
                            .data(dmxValue)
                            .channel(dxmChannel)
                            .dmxChannel(dxmChannel + fixture.getAddress())
                            .fixture(fixture)
                            .build();

                    pointList.add(qlcPoint);
                    //System.out.println(fixtureId + "\t" + dxmChannel+"\t"+dmxValue);
                }
            }
        }

        return builder.build();
    }

    public <T extends QLCFunction> T getFunction(final int id){
        return (T)functionMap.get(id);
    }

    public <T extends QLCFixture> T getFixture(final int id){
        return (T)fixtureMap.get(id);
    }

    public List<QLCFixtureModel> getFixtureModelList() {
        return fixtureModelList;
    }

    public List<QLCFunction> getFunctionList() {
        return functionList;
    }

    private static void printFixtures(){
        //ShowCollection showCollection = ShowCollection.getInstance();
        QLCModel qlcModel = new QLCModel();
        final List<String> fixtures = new ArrayList<>();
        for (int i=0; i<1000; i++) {
            QLCFixture fixture = qlcModel.getFixture(i);
            if (fixture!=null) {
            //    if (fixture instanceof QLCRoboticFixture)
                    //System.out.println(i + "\t" + fixture.getAddress()+"\t"+fixture.getFixtureModel().getType());
                    fixtures.add(StringUtils.buildString3Digits(fixture.getAddress()+1) + " - "
                        //  +  StringUtils.buildString3Digits(fixture.getAddress() + fixture.getFixtureModel().getChannels().length)
                            + "\t[" + i + "]\t"
                        //            +fixture.getFixtureModel().getType()
                       //     +"\t"+fixture.getFixtureModel().getModel());
                    );
            }
        }
        Collections.sort(fixtures);
        for (String str: fixtures)
            System.out.println(str);
    }



    public static void main(String[] args) {
        printFixtures();
    }
}
