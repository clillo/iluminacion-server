package cl.clillo.utilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Lectura y escritura de CueList en XML.
 *
 * Formato:
 * <cuelist name="..." loop="true|false">
 *   <cue id="..." name="..." fadeMs="..." holdMs="...">
 *     <channel id="21" value="255"/>
 *     ...
 *   </cue>
 *   ...
 * </cuelist>
 */
public class CueListXmlIO {

    public void write(File file, CueList list) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("cuelist");
        root.setAttribute("name", list.name());
        root.setAttribute("loop", String.valueOf(list.loop()));
        doc.appendChild(root);

        for (Cue cue : list.cues()) {
            Element c = doc.createElement("cue");
            c.setAttribute("id", cue.id());
            c.setAttribute("name", cue.name());
            c.setAttribute("fadeMs", String.valueOf(cue.fadeMs()));
            c.setAttribute("holdMs", String.valueOf(cue.holdMs()));
            root.appendChild(c);

            for (Map.Entry<Integer,Integer> e : cue.channelValues().entrySet()) {
                Element ch = doc.createElement("channel");
                ch.setAttribute("id", String.valueOf(e.getKey()));
                ch.setAttribute("value", String.valueOf(e.getValue()));
                c.appendChild(ch);
            }
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.transform(new DOMSource(doc), new StreamResult(file));
    }

    public CueList read(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        Element root = (Element) doc.getElementsByTagName("cuelist").item(0);
        String name = root.getAttribute("name");
        boolean loop = Boolean.parseBoolean(root.getAttribute("loop"));
        CueList list = new CueList(name, loop);

        NodeList cues = root.getElementsByTagName("cue");
        for (int i = 0; i < cues.getLength(); i++) {
            Element c = (Element) cues.item(i);
            String id = c.getAttribute("id");
            String cname = c.getAttribute("name");
            int fade = Integer.parseInt(c.getAttribute("fadeMs"));
            int hold = Integer.parseInt(c.getAttribute("holdMs"));
            Map<Integer,Integer> vals = new HashMap<>();
            NodeList chs = c.getElementsByTagName("channel");
            for (int j = 0; j < chs.getLength(); j++) {
                Element ch = (Element) chs.item(j);
                int cid = Integer.parseInt(ch.getAttribute("id"));
                int cv = Integer.parseInt(ch.getAttribute("value"));
                vals.put(cid, cv);
            }
            list.addCue(new Cue(id, cname, vals, fade, hold));
        }
        return list;
    }
}