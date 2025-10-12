package cl.clillo.utilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class SnapshotXmlWriter {

    static void write(File file, DmxModel model, SnapshotMeta meta,
                      int fixtureId, boolean fixtureRobotic, int startChannel, int dimmerChannelIndex) throws Exception {

        if (file == null) throw new IllegalArgumentException("file is null");
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("No se pudo crear el directorio: " + parent);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("doc");
        doc.appendChild(root);

        Element title = doc.createElement("title"); title.setTextContent(meta.title); root.appendChild(title);

        Element common = doc.createElement("common"); root.appendChild(common);
        Element id = doc.createElement("id"); id.setTextContent(meta.id); common.appendChild(id);
        Element type = doc.createElement("type"); type.setTextContent(meta.type); common.appendChild(type);
        Element path = doc.createElement("path"); path.setTextContent(meta.path); common.appendChild(path);
        Element name = doc.createElement("name"); name.setTextContent(meta.name); common.appendChild(name);

        Element points = doc.createElement("points"); root.appendChild(points);

        for (int i = 0; i < model.size(); i++) {
            int value = model.get(i);
            if (i == dimmerChannelIndex) {
                Element point = doc.createElement("point");
                point.setAttribute("fixture-robotic", String.valueOf(fixtureRobotic));
                point.setAttribute("fixture", String.valueOf(fixtureId));
                point.setAttribute("type", "DIMMER");
                point.setAttribute("value", String.valueOf(value));
                points.appendChild(point);
            } else {
                Element point = doc.createElement("point");
                point.setAttribute("fixture-robotic", String.valueOf(fixtureRobotic));
                point.setAttribute("fixture", String.valueOf(fixtureId));
                point.setAttribute("channel", String.valueOf(startChannel + i));
                point.setAttribute("value", String.valueOf(value));
                points.appendChild(point);
            }
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
        transformer.transform(new DOMSource(doc), new StreamResult(file));
    }
}
