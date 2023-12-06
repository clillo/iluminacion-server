package cl.clillo.lighting.repository;

import cl.clillo.lighting.config.QLCReader;
import cl.clillo.lighting.model.Point;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StateRepository {

    private static final String FILENAME = QLCReader.repoBase + "/general.state.xml";

    private List<Point> limitMasterDimmer = new ArrayList<>();
    private int rgbwMasterDimmer;
    private int movingHeadSpotBeamMasterDimmer;
    private int movingHeadSpotMasterDimmer;
    private int movingHeadBeamMasterDimmer;

    private HashMap<Integer, Integer> maxDimmerValues;

    private static final class InstanceHolder {
        private static StateRepository instance;

        public static StateRepository getInstance() {
            if (instance==null){
                instance = new StateRepository();
                instance.read();
            }
            return instance;
        }
    }

    public static StateRepository getInstance() {
        return InstanceHolder.getInstance();
    }

    public void setLimitMasterDimmer(List<Point> limitMasterDimmer) {
        this.limitMasterDimmer = limitMasterDimmer;
    }

    public List<Point> getLimitMasterDimmer() {
        return limitMasterDimmer;
    }

    public void setRgbwMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.rgbwMasterDimmer = value;
    }

    public int getRgbwMasterDimmer() {
        return rgbwMasterDimmer;
    }

    public int getMovingHeadSpotBeamMasterDimmer() {
        return movingHeadSpotBeamMasterDimmer;
    }

    public int getMovingHeadSpotMasterDimmer() {
        return movingHeadSpotMasterDimmer;
    }

    public int getMovingHeadBeamMasterDimmer() {
        return movingHeadBeamMasterDimmer;
    }

    public void setMovingHeadBeamMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.movingHeadBeamMasterDimmer = value;
    }

    public void setMovingHeadSpotBeamMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.movingHeadSpotBeamMasterDimmer = value;
    }

    public void setMovingHeadSpotMasterDimmer(int value, int[] masterDimmerChannels) {
        for (int channel: masterDimmerChannels)
            maxDimmerValues.put(channel, value);
        this.movingHeadSpotMasterDimmer = value;
    }

    public int getMaxValue(int channel){
        if (maxDimmerValues.containsKey(channel))
            return maxDimmerValues.get(channel);

        return -1;
    }

    public void write(){
        ByteArrayOutputStream outputStream;
        try {
            String name = this.getClass().getSimpleName() ;

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

            XMLParser.writeXMLFile(FILENAME, outputStream);

        } catch ( XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        out.writeStartElement("masterDimmer");
        out.writeStartElement("rgbw");
        out.writeCharacters(String.valueOf(rgbwMasterDimmer));
        out.writeEndElement();

        out.writeStartElement("movingHeadSpotBeam");
        out.writeCharacters(String.valueOf(movingHeadSpotBeamMasterDimmer));
        out.writeEndElement();

        out.writeStartElement("movingHeadSpot");
        out.writeCharacters(String.valueOf(movingHeadSpotMasterDimmer));
        out.writeEndElement();

        out.writeStartElement("movingHeadBeam");
        out.writeCharacters(String.valueOf(movingHeadBeamMasterDimmer));
        out.writeEndElement();
        out.writeEndElement();
    }

    private void read(){
        try {
            maxDimmerValues = new HashMap<>();
            final Document doc = XMLParser.getDocument(new File(FILENAME));
            rgbwMasterDimmer = XMLParser.getPathInt(doc, "doc/masterDimmer/rgbw");
            movingHeadSpotBeamMasterDimmer = XMLParser.getPathInt(doc, "doc/masterDimmer/movingHeadSpotBeam");
            movingHeadSpotMasterDimmer = XMLParser.getPathInt(doc, "doc/masterDimmer/movingHeadSpot");
            movingHeadBeamMasterDimmer = XMLParser.getPathInt(doc, "doc/masterDimmer/movingHeadBeam");
        }  catch (ParserConfigurationException | IOException| SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
