/*******************************************************************************
 * Copyright (c) 2023 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.fluenta.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.utils.Preferences;
import com.maxprograms.xml.Attribute;
import com.maxprograms.xml.Catalog;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.TextNode;
import com.maxprograms.xml.XMLNode;
import com.maxprograms.xml.XMLUtils;

public class TagErrorsReport {

	static final String DOUBLEPRIME = "\u2033";
	static final String MATHLT = "\u2039";
	static final String MATHGT = "\u200B\u203A";
	static final String GAMP = "\u200B\u203A";

	private static FileOutputStream out;

	private TagErrorsReport() {
		// do not instantiate this class
	}

	public static String run(String file)
			throws IOException, SAXException, ParserConfigurationException, URISyntaxException {

		Element source;
		Element target;
		List<String> srclist;
		List<String> trglist;
		List<Element> segments = new Vector<>();

		Document doc = loadXliff(file);
		Element root = doc.getRootElement();
		removeAltTrans(root);

		createList(root, segments);

		File f = new File(file);
		String outName = f.getName();
		if (outName.indexOf('.') != -1) {
			outName = outName.substring(0, outName.lastIndexOf('.')) + "_error.html";
		}
		File output = new File(f.getParentFile(), outName);
		out = new FileOutputStream(output);
		writeStr("<html>\n");
		writeStr("  <head>\n");
		writeStr("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n");
		writeStr("    <title>" + com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.0") + "</title>\n");
		writeStr("    <style type=\"text/css\">\n");

		writeStr(readCss());

		writeStr("    </style>\n");
		writeStr("  </head>\n");
		writeStr("  <body>\n");
		writeStr("    <h3>" + XMLUtils.cleanText(f.getName()) + "</h3>\n");
		writeStr("    <table class='analysis'>\n");
		writeStr("      <tr>\n");
		writeStr("        <th>#</th>\n");
		writeStr("        <th>" + com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.1") + "</th>\n");
		writeStr("        <th>" + com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.2") + "</th>\n");
		writeStr("        <th>" + com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.3") + "</th>\n");
		writeStr("      </tr>\n");

		int size = segments.size();
		for (int i = 0; i < size; i++) {

			Element e = segments.get(i);
			source = e.getChild("source");
			target = e.getChild("target");
			if (target == null) {
				continue;
			}
			srclist = buildTagList(source);
			trglist = buildTagList(target);

			/* check empty target */
			if (!trglist.isEmpty()) {
				int tLength = trglist.size();
				int j;
				if (tLength > srclist.size()) {
					writeSegment(i + 1, source, target, com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.4"));
				} else if (tLength < srclist.size()) {
					writeSegment(i + 1, source, target, com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.5"));
				} else {
					for (j = 0; j < srclist.size(); j++) {
						String es = srclist.get(j);
						boolean paired = false;
						for (int k = 0; k < trglist.size(); k++) {
							String et = trglist.get(k);
							if (es.equals(et)) {
								paired = true;
								trglist.remove(k);
								break;
							}
						}
						if (!paired) {
							writeSegment(i + 1, source, target, com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.6"));
						}
					}
					trglist = buildTagList(target);
					for (j = 0; j < srclist.size(); j++) {
						String es = srclist.get(j);
						String et = trglist.get(j);
						if (!es.equals(et)) {
							writeSegment(i + 1, source, target, com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.7"));
						}
					}
				}
			} else {
				// all tags are missing
				if (!srclist.isEmpty()) {
					writeSegment(i + 1, source, target, com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.5"));
				}
			}
		}
		writeStr("    </table>\n");
		writeStr("  </body>\n");
		writeStr("</html>\n");
		out.close();
		return output.getAbsolutePath();
	}

	private static String readCss() throws IOException {
		StringBuilder sb = new StringBuilder();
		try (InputStream is = TagErrorsReport.class.getResourceAsStream("tagErrors.css")) {
			try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				try (BufferedReader buffered = new BufferedReader(reader)) {
					String line = "";
					while ((line = buffered.readLine()) != null) {
						if (!sb.isEmpty()) {
							sb.append('\n');
						}
						sb.append(line);
					}
				}
			}
		}
		return sb.toString();
	}

	private static void writeSegment(int id, Element source, Element target, String description) throws IOException {
		writeStr("      <tr>\n");
		writeStr("        <td class='center'>" + id + "</td>\n");
		writeStr("        <td class='left'>" + tag(source) + "</td>\n");
		writeStr("        <td class='left'>" + tag(target) + "</td>\n");
		writeStr("        <td class='left'>" + XMLUtils.cleanText(description) + "</td>\n");
		writeStr("      </tr>\n");
	}

	private static String tag(Element e) {
		StringBuilder result = new StringBuilder();
		List<XMLNode> content = e.getContent();

		if (e.getName().equals("mrk") && e.getAttributeValue("mtype").equals("protected")) {
			String ts = unclean(e.getAttributeValue("ts"));
			String end = "</" + getName(ts) + ">";
			result.append("<span class='mrk'>");
			result.append(XMLUtils.cleanText(removeClass(ts)));
			result.append("</span><span class='protected'>");
			result.append(XMLUtils.cleanText(e.getText()));
			result.append("</span><span class='mrk'>");
			result.append(XMLUtils.cleanText(end));
			result.append("</span>");
		}

		if (e.getName().equals("g")) {
			// these tags contain translatable text and tags
			StringBuilder t1 = new StringBuilder("<");
			t1.append(e.getName());
			List<Attribute> attrs = e.getAttributes();
			Iterator<Attribute> it = attrs.iterator();
			while (it.hasNext()) {
				Attribute a = it.next();
				t1.append(' ');
				t1.append(a.toString());
			}
			t1.append('>');
			// add initial tag
			result.append("<span>");
			result.append(XMLUtils.cleanText(t1.toString()));
			result.append("</span>");
			for (int i = 0; i < content.size(); i++) {
				XMLNode n = content.get(i);
				if (n.getNodeType() == XMLNode.TEXT_NODE) {
					result.append(((TextNode) n).getText());
				}
				if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
					result.append(tag((Element) n));
				}
			}
			// add closing tag
			result.append("<span>");
			result.append(XMLUtils.cleanText("</" + e.getName() + ">"));
			result.append("</span>");
			return result.toString();
		}
		if (e.getName().equals("ph")) {
			result.append("<span>");
			result.append(XMLUtils.cleanText(removeClass(e.getText())));
			result.append("</span>");
			return result.toString();
		}

		// <source> elements
		for (int i = 0; i < content.size(); i++) {
			XMLNode n = content.get(i);
			if (n.getNodeType() == XMLNode.TEXT_NODE) {
				result.append(((TextNode) n).getText());
			}
			if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
				result.append(tag((Element) n));
			}
		}
		return result.toString();
	}

	private static String getName(String element) {
		StringBuilder result = new StringBuilder();
		for (int i = 1; i < element.length(); i++) {
			char c = element.charAt(i);
			if (c == '>' || Character.isWhitespace(c)) {
				break;
			}
			result.append(c);
		}
		return result.toString();
	}

	private static String unclean(String string) {
		String result = string.replace(MATHLT, "<");
		result = result.replace(MATHGT, ">");
		result = result.replace(DOUBLEPRIME, "\"");
		result = result.replace(GAMP, "&");
		return result;
	}

	private static String removeClass(String string) {
		int index = string.indexOf(" class=");
		if (index == -1) {
			return string;
		}
		String start = string.substring(0, index);
		String end = string.substring(index + 8);
		index = end.indexOf("\"");
		end = end.substring(index + 1);
		string = start + end;
		index = string.indexOf(" status=\"removeContent\"");
		if (index != -1) {
			start = string.substring(0, index);
			end = string.substring(index + " status=\"removeContent\"".length());
			string = start + end;
		}
		index = string.indexOf(" removeTranslate=\"");
		if (index != -1) {
			start = string.substring(0, index);
			end = string.substring(index + " removeTranslate=\"".length());
			index = end.indexOf("\"");
			end = end.substring(index + 1);
			string = start + end;
		}
		return string;
	}

	private static void writeStr(String string) throws IOException {
		out.write(string.getBytes(StandardCharsets.UTF_8));
	}

	private static Document loadXliff(String fileName)
			throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalog(Preferences.getInstance().getCatalogFile()));
		Document doc = builder.build(fileName);
		Element root = doc.getRootElement();
		if (!root.getName().equals("xliff")) {
			throw new IOException(com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.8"));
		}
		try {
			Element tool = root.getChild("file").getChild("header").getChild("tool");
			if (tool == null) {
				throw new IOException(com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.9"));
			}
			String toolId = tool.getAttributeValue("tool-id");
			if (!toolId.equals("OpenXLIFF")) {
				throw new IOException(com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.9"));
			}
		} catch (IOException e) {
			throw new IOException(com.maxprograms.fluenta.controllers.Messages.getString("TagErrorsReport.9"));
		}
		checkXliffMarkup(doc.getRootElement());
		return doc;
	}

	private static void checkXliffMarkup(Element e) {
		if (e.getName().equals("trans-unit")) {
			Element seg = e.getChild("seg-source");
			if (seg != null) {
				e.removeChild(seg);
				Element t = e.getChild("target");
				if (t != null) {
					removeSegMrk(e.getChild("target"));
					e.setAttribute("approved", "yes");
				}
			}
		}
		List<Element> files = e.getChildren();
		Iterator<Element> it = files.iterator();
		while (it.hasNext()) {
			checkXliffMarkup(it.next());
		}
	}

	private static void removeSegMrk(Element target) {
		if (target == null) {
			return;
		}
		List<XMLNode> vector = new Vector<>();
		List<XMLNode> content = target.getContent();
		for (int i = 0; i < content.size(); i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) node;
				if (e.getName().equals("mrk") && e.getAttributeValue("mtype").equals("seg")) {
					List<XMLNode> children = e.getContent();
					for (int j = 0; j < children.size(); j++) {
						vector.add(children.get(j));
					}
				}
			} else {
				vector.add(node);
			}
		}
		target.setContent(vector);
	}

	private static void removeAltTrans(Element e) {
		List<Element> children = e.getChildren();
		List<Element> matches = e.getChildren("alt-trans");
		if (!matches.isEmpty()) {
			for (int i = 0; i < matches.size(); i++) {
				e.removeChild(matches.get(i));
			}
			children = e.getChildren();
		}
		for (int i = 0; i < children.size(); i++) {
			removeAltTrans(children.get(i));
		}
	}

	private static void createList(Element root, List<Element> segments) {
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			if (el.getName().equals("trans-unit")) {
				segments.add(el);
			} else {
				createList(el, segments);
			}
		}
	}

	public static List<String> buildTagList(Element e) {
		List<String> result = new Vector<>();
		List<XMLNode> content = e.getContent();
		Iterator<XMLNode> i = content.iterator();
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element el = (Element) o;
				if (el.getName().equals("ph") || el.getName().equals("bpt") || el.getName().equals("ept")
						|| el.getName().equals("it")) {
					if (!el.getChildren().isEmpty()) {
						String open = "<" + el.getName() + " ";
						List<Attribute> att = el.getAttributes();
						for (int j = 0; j < att.size(); j++) {
							Attribute a = att.get(j);
							open = open + a.getName() + "=\"" + a.getValue().replace("\"", "&quot;") + "\" ";
						}
						result.add(open.substring(0, open.length() - 1) + ">");
						List<XMLNode> list = el.getContent();
						for (int j = 0; j < list.size(); j++) {
							XMLNode n = list.get(j);
							if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
								result.addAll(buildTagList((Element) n));
							}
						}
						result.add("</" + el.getName() + ">");
					} else {
						result.add(el.toString());
					}
				} else if (el.getName().equals("mrk") || el.getName().equals("g") || el.getName().equals("sub")) {
					String open = "<" + el.getName() + " ";
					List<Attribute> att = el.getAttributes();
					for (int j = 0; j < att.size(); j++) {
						Attribute a = att.get(j);
						open = open + a.getName() + "=\"" + a.getValue().replace("\"", "&quot;") + "\" ";
					}
					result.add(open.substring(0, open.length() - 1) + ">");
					List<XMLNode> list = el.getContent();
					for (int j = 0; j < list.size(); j++) {
						XMLNode n = list.get(j);
						if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
							result.addAll(buildTagList((Element) n));
						}
					}
					result.add("</" + el.getName() + ">");
				} else if (el.getName().equals("x") || el.getName().equals("bx") || el.getName().equals("ex")) {
					result.add(el.toString());
				} else {
					// foreign element?
					result.add(el.toString());
				}
			}
		}
		return result;
	}
}
