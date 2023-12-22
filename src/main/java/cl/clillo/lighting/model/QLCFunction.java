package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.fixture.qlc.QLCRoboticFixture;
import cl.clillo.lighting.repository.XMLParser;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Node;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class QLCFunction extends QLCElement{

    private Show show;

    QLCFunction(int id, String type, String name, String path) {
        super(id, type, name, path);
    }

    public String toSmallString(){
        return String.valueOf(this.getId()) + '\t' +
                this.getPath() + '.' + this.getName() + '\t';
    }

    public static QLCFunctionBuilder builder() {
        return new QLCFunctionBuilder();
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        out.writeStartElement("common");
        out.writeStartElement("id");
        out.writeCharacters(String.valueOf(id));
        out.writeEndElement();
        out.writeStartElement("type");
        out.writeCharacters(String.valueOf(type));
        out.writeEndElement();
        out.writeStartElement("path");
        out.writeCharacters(String.valueOf(path));
        out.writeEndElement();
        out.writeStartElement("name");
        out.writeCharacters(name);
        out.writeEndElement();

        if (isBlackout()){
            out.writeStartElement("blackout");
            out.writeCharacters("true");
            out.writeEndElement();
        }

        if (isTotalBlackout()){
            out.writeStartElement("system");
            out.writeCharacters("TotalBlackout");
            out.writeEndElement();
        }

        out.writeEndElement();
    }

    protected void writeElementsFixture(final XMLStreamWriter out, final List<QLCEfxFixtureData> fixtureDataList) throws XMLStreamException {
        out.writeStartElement("fixtures");
        for (QLCEfxFixtureData data: fixtureDataList) {
            out.writeStartElement("fixture");

            out.writeStartElement("id");
            if (data.getRoboticFixture()!=null)
                out.writeCharacters(String.valueOf(data.getRoboticFixture().getId()));
            else
                out.writeCharacters(String.valueOf(data.getFixture().getId()));
            out.writeEndElement();
            out.writeStartElement("offset");
            out.writeCharacters(String.valueOf(data.getStartOffset()));
            out.writeEndElement();
            out.writeStartElement("reverse");
            out.writeCharacters(String.valueOf(data.isReverse()));
            out.writeEndElement();
            out.writeEndElement();
        }
        out.writeEndElement();

    }

    public void writeToConfigFile(final String dir){
        ByteArrayOutputStream outputStream;
        try {
            String name = this.getClass().getSimpleName() + "." ;

            if (path==null || path.isEmpty())
                name = name + this.name;
            else {
                String fakePath = path.replace('/','.');
                name = name + fakePath + "." + id;
            }
            outputStream = new ByteArrayOutputStream();
            XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(
                    new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            out.writeStartDocument();
            out.writeStartElement("doc");

            out.writeStartElement("title");
            out.writeCharacters(name);
            out.writeEndElement();

            writeElements(out);

            out.writeEndElement();
            out.writeEndDocument();

            out.close();

            XMLParser.writeXMLFile(dir  + "/" + name + ".xml", outputStream);

        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected static QLCEfxFixtureData buildFixtureData(final FixtureListBuilder fixtureListBuilder, final Node node){
        return QLCEfxFixtureData.builder().fixture(fixtureListBuilder.getFixture(XMLParser.getNodeInt(node, "id")))
                .startOffset(XMLParser.getNodeDouble(node, "offset"))
                .reverse(XMLParser.getNodeBoolean(node, "reverse"))
                .build();
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Show getShow() {
        return show;
    }

    public static class QLCFunctionBuilder {
        private int id;
        private String type;
        private String name;
        private String path;
        private QLCDirection direction;
        private QLCRunOrder runOrder;
        private QLCScene boundScene;
        private QLCAlgorithm algorithm;
        private QLCSpeed qlcSpeed;
        private final List<QLCPoint> qlcPointList = new ArrayList<>();
        private final List<QLCFunction> qlcFunctionList = new ArrayList<>();
        private final List<QLCStep> qlcStepList = new ArrayList<>();
        private final List<QLCEfxFixtureData> roboticFixtureList = new ArrayList<>();
        private final List<ChaserStep> chaserSteps = new ArrayList<>();

        QLCFunctionBuilder() {
        }

        public QLCFunctionBuilder id(final int id) {
            this.id = id;
            return this;
        }

        public QLCFunctionBuilder type(final String type) {
            this.type = type;
            return this;
        }

        public QLCFunctionBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public QLCFunctionBuilder path(final String path) {
            this.path = path;
            return this;
        }

        public QLCFunctionBuilder boundScene(final QLCScene boundScene) {
            this.boundScene = boundScene;
            return this;
        }

        public QLCFunctionBuilder speed(final QLCSpeed qlcSpeed) {
            this.qlcSpeed = qlcSpeed;
            return this;
        }
        public void direction(final QLCDirection direction) {
            this.direction = direction;
        }

        public void algorithm(final QLCAlgorithm algorithm) {
            this.algorithm = algorithm;
        }

        public void runOrder(final QLCRunOrder runOrder){
            this.runOrder = runOrder;
        }

        public void addFunctionList(final QLCFunction function) {
            this.qlcFunctionList.add(function);
        }

        public void addPointList(final QLCPoint fixture) {
            this.qlcPointList.add(fixture);
        }

        public QLCFunctionBuilder addStepList(final QLCStep step) {
            this.qlcStepList.add(step);
            return this;
        }

        public QLCFunctionBuilder addFixture(final QLCRoboticFixture fixture) {
            this.roboticFixtureList.add(QLCEfxFixtureData.builder().roboticFixture(fixture).build());
            return this;
        }

        public QLCFunctionBuilder addStepChaser(final ChaserStep step) {
            this.chaserSteps.add(step);
            return this;
        }

        public QLCFunction build() {
            if ("Scene".equalsIgnoreCase(type))
                return new QLCScene(this.id, this.type, this.name, this.path, this.qlcPointList);

            if ("Collection".equalsIgnoreCase(type))
                return new QLCCollection(this.id, this.type, this.name, this.path, List.of());

            if ("Sequence".equalsIgnoreCase(type))
                return new QLCSequence(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                        this.qlcStepList, boundScene, qlcSpeed);

            if ("EFX".equalsIgnoreCase(type)){

                if ( QLCAlgorithm.CIRCLE==algorithm)
                    return new QLCEfxCircle(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                            this.qlcStepList, boundScene, roboticFixtureList);

                if ( QLCAlgorithm.LINE==algorithm)
                    return new QLCEfxLine(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                            this.qlcStepList, boundScene, roboticFixtureList);
            }


            if ("Chaser".equalsIgnoreCase(type))
                return this.chaserSteps.size()>0? new Chaser(this.id, this.type, this.name, this.path, this.direction, this.runOrder,
                        1, this.chaserSteps): null;



            return new QLCFunction(this.id, this.type, this.name, this.path);
        }

    }

    public void save(){
        final String dir = ShowCollection.getInstance().getDirectory(this);
        this.writeToConfigFile(dir);
    }

}
