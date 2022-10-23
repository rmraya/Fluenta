/*******************************************************************************
 * Copyright (c) 2015-2022 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Constants;
import com.maxprograms.swordfish.tm.TMUtils;
import com.maxprograms.xml.Attribute;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.PI;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLNode;
import com.maxprograms.xml.XMLUtils;

public class TMXExporter {

	public static final String DOUBLEPRIME = "\u2033";
	public static final String MATHLT = "\u2039";
	public static final String MATHGT = "\u200B\u203A";
	public static final String GAMP = "\u200B\u203A";

	static Document doc;
	static Map<String, String> docProperties;
	private static FileOutputStream output;
	private static EntityResolver resolver = null;
	private static String today;
	private static int match;
	private static String original;
	private static int filenumbr;

	public static void export(String xliff, String tmx, boolean acceptUnaproved)
			throws URISyntaxException, SAXException, IOException, ParserConfigurationException {

		today = TMUtils.tmxDate();
		filenumbr = 0;

		SAXBuilder builder = new SAXBuilder();
		if (resolver != null) {
			builder.setEntityResolver(resolver);
		}
		if (xliff.indexOf("file:") != -1) {
			URI uri = new URI(xliff);
			doc = builder.build(uri.toURL());
			uri = null;
		} else {
			File file = new File(xliff);
			doc = builder.build(file.getAbsolutePath());
			file = null;
		}

		builder = null;
		Element root = doc.getRootElement();

		output = new FileOutputStream(tmx);
		Element firstFile = root.getChild("file");

		docProperties = new Hashtable<>();
		List<PI> slist = root.getPI("subject");
		if (!slist.isEmpty()) {
			docProperties.put("subject", slist.get(0).getData());
		} else {
			docProperties.put("subject", "");
		}
		List<PI> plist = root.getPI("project");
		if (!plist.isEmpty()) {
			docProperties.put("project", plist.get(0).getData());
		} else {
			docProperties.put("project", "");
		}
		List<PI> clist = root.getPI("customer");
		if (!clist.isEmpty()) {
			docProperties.put("customer", clist.get(0).getData());
		} else {
			docProperties.put("customer", "");
		}

		String sourceLang = firstFile.getAttributeValue("source-language");

		writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		writeString(
				"<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\" >\n");
		writeString("<tmx version=\"1.4\">\n");
		writeString("<header \n" +
				"      creationtool=\"" +
				Constants.NAME +
				"\" \n" +
				"      creationtoolversion=\"" +
				Constants.VERSION +
				"\"  \n" +
				"      srclang=\"" +
				sourceLang +
				"\" \n" +
				"      adminlang=\"en\"  \n" +
				"      datatype=\"xml\" \n" +
				"      o-tmf=\"XLIFF\" \n" +
				"      segtype=\"block\"\n" +
				">\n" +
				"</header>\n");
		writeString("<body>\n");

		List<Element> files = root.getChildren();
		Iterator<Element> fileiterator = files.iterator();
		String targetLang = "";
		while (fileiterator.hasNext()) {
			Element file = fileiterator.next();
			sourceLang = file.getAttributeValue("source-language");
			targetLang = file.getAttributeValue("target-language");
			original = "" + file.getAttributeValue("original").hashCode();
			recurse(file, acceptUnaproved, sourceLang, targetLang);
			file = null;
			filenumbr++;
		}
		writeString("</body>\n");
		writeString("</tmx>");

		output.close();
	}

	private static void recurse(Element e, boolean acceptUnaproved, String srcLang, String tgtLang) throws IOException {
		List<Element> list = e.getChildren();
		Iterator<Element> i = list.iterator();
		while (i.hasNext()) {
			Element element = i.next();
			if (element.getName().equals("trans-unit")) {
				writeSegment(element, acceptUnaproved, srcLang, tgtLang);
			} else {
				recurse(element, acceptUnaproved, srcLang, tgtLang);
			}
		}
	}

	private static void writeSegment(Element segment, boolean acceptUnaproved, String srcLang, String tgtLang)
			throws IOException {

		String id = original + "-" + filenumbr + "-" + segment.getAttributeValue("id").hashCode();

		if (segment.getAttributeValue("approved").equals("yes") || acceptUnaproved) {
			Element source = segment.getChild("source");
			if (source.getContent().isEmpty()) {
				// merged segment, nothing to export
				return;
			}
			Element target = segment.getChild("target");
			if (target == null) {
				return;
			}
			String srcText = extractText(source, true);
			String tgtText = extractText(target, true);
			if (acceptUnaproved && tgtText.trim().isEmpty()) {
				return;
			}
			writeString("<tu creationtool=\""
					+ Constants.NAME
					+ "\" creationtoolversion=\""
					+ Constants.VERSION
					+ "\" tuid=\"" + id + "\" creationdate=\"" + today + "\">\n");

			String customer = docProperties.get("customer");
			String project = docProperties.get("project");
			String subject = docProperties.get("subject");

			if (!subject.isEmpty()) {
				writeString("<prop type=\"subject\">" + XMLUtils.cleanText(subject) + "</prop>\n");
			}
			if (!project.isEmpty()) {
				writeString("<prop type=\"project\">" + XMLUtils.cleanText(project) + "</prop>\n");
			}
			if (!customer.isEmpty()) {
				writeString("<prop type=\"customer\">" + XMLUtils.cleanText(customer) + "</prop>\n");
			}

			List<Element> notes = segment.getChildren("note");
			Iterator<Element> it = notes.iterator();
			while (it.hasNext()) {
				Element note = it.next();
				String lang = note.getAttributeValue("xml:lang");
				if (!lang.isEmpty()) {
					lang = " xml:lang=\"" + lang + "\"";
				}
				writeString("<note" + lang + ">" + XMLUtils.cleanText(note.getText()) + "</note>\n");
			}

			if (!segment.getAttributeValue("xml:space", "default").equals("preserve")) {
				srcText = srcText.trim();
				tgtText = tgtText.trim();
			}
			writeString("<tuv xml:lang=\"" + srcLang + "\" creationdate=\"" + today + "\">\n<seg>"
					+ srcText + "</seg>\n</tuv>\n");
			writeString("<tuv xml:lang=\"" + tgtLang + "\" creationdate=\"" + today + "\">\n<seg>"
					+ tgtText + "</seg>\n</tuv>\n");

			writeString("</tu>\n");
		}
	}

	public static String extractText(Element src, boolean _level1) {

		String type = src.getName();

		if (type.equals("source") || type.equals("target")) {
			match = 0;
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String text = "";
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
					case XMLNode.TEXT_NODE:
						text = text + o.toString();
						break;
					case XMLNode.ELEMENT_NODE:
						if (!_level1) {
							Element e = (Element) o;
							text = text + extractText(e, false);
							e = null;
						}
						if (_level1 && ((Element) o).getName().equals("mrk")) {
							Element e = (Element) o;
							text = text + XMLUtils.cleanText(e.getText());
							e = null;
						}
						break;
				}
			}
			return text;
		}

		if (_level1) {
			// extract only the text from nested inline
			// elements
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String text = "";
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
					case XMLNode.TEXT_NODE:
						text = text + o.toString();
						break;
					case XMLNode.ELEMENT_NODE:
						if (!_level1) {
							Element e = (Element) o;
							text = text + extractText(e, true);
							e = null;
						}
						break;
				}
			}
			return text;
		}

		if (type.equals("bx")
				|| type.equals("ex")
				|| type.equals("ph")) {
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String ctype = src.getAttributeValue("ctype");
			if (!ctype.isEmpty()) {
				ctype = " type=\"" + XMLUtils.cleanText(ctype) + "\"";
			}
			String assoc = src.getAttributeValue("assoc");
			if (!assoc.isEmpty()) {
				assoc = " assoc=\"" + XMLUtils.cleanText(assoc) + "\"";
			}
			String id = "";
			if (type.equals("ph")) {
				id = src.getAttributeValue("id");
				if (!id.isEmpty()) {
					id = " x=\"" + XMLUtils.cleanText(id) + "\"";
				}
			}
			String text = "<ph" + ctype + assoc + id + ">";
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
					case XMLNode.TEXT_NODE:
						text = text + o.toString();
						break;
					case XMLNode.ELEMENT_NODE:
						Element e = (Element) o;
						if (e.getName().equals("sub")) {
							text = text + extractText(e, false);
						} else {
							if (!e.getName().equals("mrk")) {
								text = text + extractText(e, true);
							}
						}
						break;
				}
			}
			return text + "</ph>";
		}

		if (type.equals("g") || type.equals("x")) {
			String open = "<" + src.getName();
			List<Attribute> atts = src.getAttributes();
			Iterator<Attribute> h = atts.iterator();
			while (h.hasNext()) {
				Attribute a = h.next();
				open = open + " " + a.getName() + "=\"" + a.getValue() + "\"";
			}
			List<XMLNode> srcContent = src.getContent();
			if (!srcContent.isEmpty()) {
				open = open + ">";
				int i = match;
				match++;
				String text = "<bpt type=\"xliff-" + src.getName() + "\" i=\"" + i + "\">" + XMLUtils.cleanText(open)
						+ "</bpt>";
				Iterator<XMLNode> k = srcContent.iterator();
				while (k.hasNext()) {
					XMLNode n = k.next();
					if (n.getNodeType() == XMLNode.TEXT_NODE) {
						text = text + n.toString();
					}
					if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
						text = text + extractText((Element) n, _level1);
					}
				}
				String close = "</" + src.getName() + ">";
				return text + "<ept i=\"" + i + "\">" + XMLUtils.cleanText(close) + "</ept>";
			}
			return "<ph type=\"xliff-" + src.getName() + "\">" + XMLUtils.cleanText(open + "/>") + "</ph>";
		}

		if (type.equals("it")) {
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String ctype = src.getAttributeValue("ctype");
			if (!ctype.isEmpty()) {
				ctype = " type=\"" + XMLUtils.cleanText(ctype) + "\"";
			}
			String pos = src.getAttributeValue("pos");
			if (pos.equals("open")) {
				pos = " pos=\"begin\"";
			} else if (pos.equals("close")) {
				pos = " pos=\"end\"";
			}
			String text = "<it" + ctype + pos + ">";
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
					case XMLNode.TEXT_NODE:
						text = text + o.toString();
						break;
					case XMLNode.ELEMENT_NODE:
						Element e = (Element) o;
						text = text + extractText(e, false);
						break;
				}
			}
			return text + "</it>";
		}

		if (type.equals("bpt") || type.equals("ept")) {
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String ctype = src.getAttributeValue("ctype");
			if (!ctype.isEmpty()) {
				ctype = " type=\"" + XMLUtils.cleanText(ctype) + "\"";
			}
			String rid = src.getAttributeValue("rid");
			if (!rid.isEmpty()) {
				rid = " i=\"" + XMLUtils.cleanText(rid) + "\"";
			} else {
				rid = " i=\"" + XMLUtils.cleanText(src.getAttributeValue("id")) + "\"";
			}
			String text = "<" + type + ctype + rid + ">";
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
					case XMLNode.TEXT_NODE:
						text = text + o.toString();
						break;
					case XMLNode.ELEMENT_NODE:
						Element e = (Element) o;
						text = text + extractText(e, false);
						break;
				}
			}
			return text + "</" + type + ">";
		}

		if (type.equals("sub")) {
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String text = "<sub>";
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
					case XMLNode.TEXT_NODE:
						text = text + o.toString();
						break;
					case XMLNode.ELEMENT_NODE:
						Element e = (Element) o;
						if (!e.getName().equals("mrk")) {
							text = text + extractText(e, true);
						}
						break;
				}
			}
			return text + "</sub>";
		}
		if (type.equals("mrk")) {
			if (src.getAttributeValue("mtype").equals("term")) {
				//
				// ignore terminology entries
				return XMLUtils.cleanText(src.getText());
			}
			if (src.getAttributeValue("mtype").equals("protected")) {
				String ts = src.getAttributeValue("ts");
				ts = restoreChars(ts).trim();
				String name = "";
				for (int i = 1; i < ts.length(); i++) {
					if (Character.isSpaceChar(ts.charAt(i))) {
						break;
					}
					name = name + ts.charAt(i);
				}
				return "<ph type=\"mrk-protected\" " + " x=\"" + XMLUtils.cleanText(src.getAttributeValue("mid", "-"))
						+ "\"" + ">" + XMLUtils.cleanText(ts) + "</ph>" + XMLUtils.cleanText(src.getText())
						+ "<ph type=\"mrk-close\">" + XMLUtils.cleanText("</" + name + ">") + "</ph>";
			}
			return "<hi type=\"" + src.getAttributeValue("mtype", "xliff-mrk") + "\">"
					+ XMLUtils.cleanText(src.getText()) + "</hi>";
		}
		return null;
	}

	private static String restoreChars(String value) {
		value = value.replace(MATHLT, "<");
		value = value.replace(MATHGT, ">");
		value = value.replace(DOUBLEPRIME, "\"");
		value = value.replace(GAMP, "&");
		return value;
	}

	private static void writeString(String input) throws IOException {
		output.write(input.getBytes(StandardCharsets.UTF_8));
	}

}