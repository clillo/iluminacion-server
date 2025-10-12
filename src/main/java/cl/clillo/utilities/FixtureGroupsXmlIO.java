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
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia XML para grupos de fixtures con submaster.
 *
 * Formato:
 * <groups>
 *   <group name="Front" submaster="0.85">
 *     <ch id="1"/>
 *     <ch id="2"/>
 *   </group>
 * </groups>
 */
public class FixtureGroupsXmlIO {

    public void write(File file, List<FixtureGroup> groups) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("groups");
        doc.appendChild(root);

        for (FixtureGroup g : groups) {
            Element ge = doc.createElement("group");
            ge.setAttribute("name", g.name());
            ge.setAttribute("submaster", String.valueOf(g.submaster()));
            root.appendChild(ge);

            for (Integer ch : g.channels()) {
                Element ce = doc.createElement("ch");
                ce.setAttribute("id", String.valueOf(ch));
                ge.appendChild(ce);
            }
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.transform(new DOMSource(doc), new StreamResult(file));
    }

    public List<FixtureGroup> read(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        Element root = (Element) doc.getElementsByTagName("groups").item(0);
        if (root == null) throw new IllegalArgumentException("Documento sin <groups>");

        NodeList nl = root.getElementsByTagName("group");
        List<FixtureGroup> out = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Element ge = (Element) nl.item(i);
            String name = ge.getAttribute("name");
            double sub = 1.0;
            try { sub = Double.parseDouble(ge.getAttribute("submaster")); } catch (Exception ignored) {}
            FixtureGroup g = new FixtureGroup(name);
            g.setSubmaster(sub);

            NodeList chs = ge.getElementsByTagName("ch");
            for (int j = 0; j < chs.getLength(); j++) {
                Element ce = (Element) chs.item(j);
                int cid = Integer.parseInt(ce.getAttribute("id"));
                g.addChannel(cid);
            }
            out.add(g);
        }
        return out;
    }
}