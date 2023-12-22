package cl.clillo.lighting.model;

import cl.clillo.lighting.config.FixtureListBuilder;
import cl.clillo.lighting.repository.XMLParser;
import lombok.Getter;
import lombok.ToString;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
@Getter
public class QLCScene extends QLCFunction{

    private final List<QLCPoint> qlcPointList;
    private final QLCEfxScene qlcEfxScene;

    public QLCScene(final int id, final String type, final String name, final String path, final List<QLCPoint> qlcPointList) {
        super(id, type, name, path);
        this.qlcPointList = qlcPointList;
        if (isEfx()) {
            final List<QLCEfxFixtureData> fixtureList = new ArrayList<>();
            for (QLCPoint qlcPoint: qlcPointList ){
                QLCEfxFixtureData fixtureData = QLCEfxFixtureData.builder().fixture(qlcPoint.getFixture())
                        .startOffset(0)
                        .reverse(false)
                        .build();

                boolean exist = false;
                for (QLCEfxFixtureData qlcEfxFixtureData: fixtureList)
                    if (qlcEfxFixtureData.getRoboticFixture().equals(fixtureData.getRoboticFixture())) {
                        exist = true;
                        break;
                    }

                if (!exist)
                    fixtureList.add(fixtureData);
            }
            qlcEfxScene = new QLCEfxScene(id, type, "scene: " + id, path, qlcPointList, fixtureList);

        }else
            qlcEfxScene = null;


    }

    public String toSmallString(){
        StringBuilder sb = new StringBuilder(super.toSmallString());

        for (QLCPoint point: qlcPointList)
            sb.append('{').append(point.getFixture().getId()).append(',')
                    .append(point.getChannel()).append(',')
                    .append(point.getData()).append('}');
        return sb.toString();
    }

    private boolean isEfx(){
        for(QLCPoint qlcPoint: qlcPointList)
            if (!qlcPoint.isMovement())
                return false;

        return true;
    }

    public static QLCScene read(final FixtureListBuilder fixtureListBuilder, final File file) throws ParserConfigurationException, IOException, SAXException {
        final Document doc = XMLParser.getDocument(file);
        final QLCElement function = QLCElement.read(doc);

        final List<QLCPoint> qlcPointList = new ArrayList<>();

        Node common = doc.getElementsByTagName("points").item(0);
        NodeList list = common.getChildNodes();
        for (int temp = 0; temp < list.getLength(); temp++) {
            Node node = list.item(temp);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final QLCPoint point = QLCPoint.build(function.getId(), fixtureListBuilder, node);
                if (point!=null)
                    qlcPointList.add(point);
            }
        }

        Collections.sort(qlcPointList);
        final QLCScene scene = new QLCScene(function.getId(), function.getType(), function.getName(),function.getPath(), qlcPointList);
        scene.setBlackout(function.isBlackout());
        scene.setTotalBlackout(function.isTotalBlackout());

        return scene;
    }

    protected void writeElements(final XMLStreamWriter out) throws XMLStreamException {
        super.writeElements(out);
        QLCPoint.write(out, qlcPointList);
    }

    public QLCEfxScene getQlcEfxScene() {
        return qlcEfxScene;
    }

    @Override
    public void setShow(Show show) {
        super.setShow(show);
        if (qlcEfxScene!=null)
            qlcEfxScene.setShow(getShow());
    }

    @Override
    public void setTotalBlackout(boolean totalBlackout) {
        super.setTotalBlackout(totalBlackout);
        if (!totalBlackout)
            return;

        this.qlcPointList.clear();
        for (int i=0; i<512; i++)
            this.qlcPointList.add(QLCPoint.builder()
                    .channel(i)
                    .data(0)
                    .dmxChannel(i)
                    .build());

    }
}
