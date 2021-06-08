/*******************************************************************************
 * Copyright (c) 2015-2021 Maxprograms.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.utils.TextUtils;
import com.maxprograms.xml.Attribute;
import com.maxprograms.xml.Catalog;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.TextNode;
import com.maxprograms.xml.XMLNode;

public class TagErrorsReport {

	static final String DOUBLEPRIME = "\u2033";  //$NON-NLS-1$
	static final String MATHLT = "\u2039";  //$NON-NLS-1$
	static final String MATHGT = "\u200B\u203A";  //$NON-NLS-1$
	static final String GAMP = "\u200B\u203A";  //$NON-NLS-1$

	private static FileOutputStream out;
	
	public static String run(String file) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {

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
			outName = outName.substring(0, outName.lastIndexOf('.')) + "_error.html"; //$NON-NLS-1$
		}
		File output = new File(f.getParentFile(), outName);
		out = new FileOutputStream(output);
		writeStr("<html>\n"); //$NON-NLS-1$
		writeStr("  <head>\n"); //$NON-NLS-1$
		writeStr("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"); //$NON-NLS-1$
		writeStr("    <title>"+ "Tags Analysis" + "</title>\n"  ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writeStr("    <style type=\"text/css\">\n"); //$NON-NLS-1$
		writeStr("     table{\n" +  //$NON-NLS-1$
		        "         width:100%;\n" + //$NON-NLS-1$
				"         border-left:1px solid grey;\n" +  //$NON-NLS-1$
				"     }\n" +  //$NON-NLS-1$
				"     th{\n" +  //$NON-NLS-1$
				"         border-left:1px solid grey;\n" +  //$NON-NLS-1$
				"         border-right:1px solid grey;\n" +  //$NON-NLS-1$
				"         background:#003854;\n" +  //$NON-NLS-1$
				"         color:white;\n" +  //$NON-NLS-1$
				"         text-align:center;\n" +  //$NON-NLS-1$
				"         padding:3px\n" +  //$NON-NLS-1$
				"     }\n" +  //$NON-NLS-1$
				"     td.left{\n" +  //$NON-NLS-1$
				"         border-right:1px solid grey;\n" +  //$NON-NLS-1$
				"         border-bottom:1px solid grey;\n" +  //$NON-NLS-1$
				"         text-align:left;\n" +  //$NON-NLS-1$
				"         padding:2px;\n" +  //$NON-NLS-1$
				"         max-width:400px;\n" +  //$NON-NLS-1$
				"     }\n" +  //$NON-NLS-1$
				"     td.center{\n" +  //$NON-NLS-1$
				"         border-right:1px solid grey;\n" +  //$NON-NLS-1$
				"         border-bottom:1px solid grey;\n" +  //$NON-NLS-1$
				"         text-align:center;\n" +  //$NON-NLS-1$
				"         padding:2px;\n" +  //$NON-NLS-1$
				"     }\n" +  //$NON-NLS-1$
				"     td.right{\n" +  //$NON-NLS-1$
				"         border-right:1px solid grey;\n" +  //$NON-NLS-1$
				"         border-bottom:1px solid grey;\n" +  //$NON-NLS-1$
				"         text-align:right;\n" +  //$NON-NLS-1$
				"         padding:2px;\n" +  //$NON-NLS-1$
				"     }\n" + //$NON-NLS-1$
				"     span {\n" + //$NON-NLS-1$
				"         background:#3db6b9;\n" + //$NON-NLS-1$
				"         color:white;\n" + //$NON-NLS-1$
				"         padding-left:2px;\n" + //$NON-NLS-1$
				"         padding-right:2px;\n" + //$NON-NLS-1$
				"         margin-left:2px;\n" + //$NON-NLS-1$
				"         margin-right:2px;\n" + //$NON-NLS-1$
				"     }\n" + //$NON-NLS-1$
				"     span.mrk {\n" + //$NON-NLS-1$
				"         background:#1565c0;\n" + //$NON-NLS-1$
				"         padding-left:2px;\n" + //$NON-NLS-1$
				"         padding-right:2px;\n" + //$NON-NLS-1$
				"         color:white;\n" + //$NON-NLS-1$
				"         margin-left:0px;\n" + //$NON-NLS-1$
				"         margin-right:0px;\n" + //$NON-NLS-1$
				"     }\n" + //$NON-NLS-1$
				"     span.protected {\n" + //$NON-NLS-1$
				"         background:#e3f2fd;\n" + //$NON-NLS-1$
				"         padding-left:3px;\n" + //$NON-NLS-1$
				"         padding-right:3px;\n" + //$NON-NLS-1$
				"         color:black;\n" + //$NON-NLS-1$
				"         margin-left:0px;\n" + //$NON-NLS-1$
				"         margin-right:0px;\n" + //$NON-NLS-1$
				"     }\n"); //$NON-NLS-1$
		writeStr("    </style>\n"); //$NON-NLS-1$
		writeStr("  </head>\n"); //$NON-NLS-1$
		writeStr("  <body>\n"); //$NON-NLS-1$
		writeStr("    <h3>" + TextUtils.cleanString(f.getName()) + "</h3>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		writeStr("    <table class='analysis'>\n"); //$NON-NLS-1$
		writeStr("      <tr>\n"); //$NON-NLS-1$
		writeStr("        <th>#</th>\n"); //$NON-NLS-1$
		writeStr("        <th>" + Messages.getString("TagErrorsReport.77") + "</th>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writeStr("        <th>" + Messages.getString("TagErrorsReport.80") + "</th>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writeStr("        <th>" + Messages.getString("TagErrorsReport.83") + "</th>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writeStr("      </tr>\n"); //$NON-NLS-1$

		int size = segments.size();
		for (int i = 0; i < size; i++) {

			Element e = segments.get(i);
			source = e.getChild("source");  //$NON-NLS-1$
			target = e.getChild("target");  //$NON-NLS-1$
			if (target == null) {
			    continue;
			}
			srclist = buildTagList(source);
			trglist = buildTagList(target);

			/* check empty target */
			if (trglist.size() != 0) {
				int tLength = trglist.size();
				int j;
				if (tLength > srclist.size()) {
					writeSegment(i+1, source, target, Messages.getString("TagErrorsReport.88"));    //$NON-NLS-1$
				} else if (tLength < srclist.size()) {
					writeSegment(i+1, source, target, Messages.getString("TagErrorsReport.89"));    //$NON-NLS-1$
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
						if (paired == false) {
							writeSegment(i+1, source, target, Messages.getString("TagErrorsReport.90")); //$NON-NLS-1$
						}
					}
					trglist = buildTagList(target);
					for (j = 0; j < srclist.size(); j++) {
						String es = srclist.get(j);
						String et = trglist.get(j);
						if ( !es.equals(et)) {
							writeSegment(i+1, source, target, Messages.getString("TagErrorsReport.91")); //$NON-NLS-1$
						}
					}
				}
			} else {
				// all tags are missing
				if (srclist.size()>0) {
					writeSegment(i+1, source, target, Messages.getString("TagErrorsReport.89")); //$NON-NLS-1$
				}
			}
		}
		writeStr("    </table>\n"); //$NON-NLS-1$
		writeStr("  </body>\n"); //$NON-NLS-1$
		writeStr("</html>\n"); //$NON-NLS-1$
		out.close();
		return output.getAbsolutePath();
	}
	
	private static void writeSegment(int id, Element source, Element target, String description) throws UnsupportedEncodingException, IOException {
		writeStr("      <tr>\n"); //$NON-NLS-1$
		writeStr("        <td class='center'>" + id + "</td>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		writeStr("        <td class='left'>" + tag(source) + "</td>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		writeStr("        <td class='left'>" + tag(target) + "</td>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		writeStr("        <td class='left'>" + TextUtils.cleanString(description) + "</td>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		writeStr("      </tr>\n");		 //$NON-NLS-1$
	}

	private static String tag(Element e) {
		String result = "";  //$NON-NLS-1$
		List<XMLNode> content = e.getContent();
		
		if (e.getName().equals("mrk") && e.getAttributeValue("mtype", "").equals("protected")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			String ts = unclean(e.getAttributeValue("ts")); //$NON-NLS-1$
			String end = "</" + getName(ts) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			result = result + "<span class='mrk'>" + TextUtils.cleanString(removeClass(ts)) + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
			result = result + "<span class='protected'>" + TextUtils.cleanString(e.getText()) + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
			result = result + "<span class='mrk'>" + TextUtils.cleanString(end) + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
		}
			
		if ( e.getName().equals("g")) {    //$NON-NLS-1$
			// these tags contain translatable text and tags
			String t1 = "<" + e.getName();  //$NON-NLS-1$
			List<Attribute> attrs = e.getAttributes();
			Iterator<Attribute> it = attrs.iterator();
			while (it.hasNext()) {
				Attribute a = it.next();
				t1 = t1 + " " + a.toString();   //$NON-NLS-1$
			}
			t1 = t1 + ">";  //$NON-NLS-1$
			// add initial tag
			result = result + "<span>" + TextUtils.cleanString(t1) + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
			for (int i=0 ; i<content.size() ; i++) {
				XMLNode n = content.get(i);
				switch (n.getNodeType()) {
				case XMLNode.TEXT_NODE:
					result = result + ((TextNode) n).getText();
					break;
				case XMLNode.ELEMENT_NODE:
					result = result + tag((Element)n);
				}
			}
			// add closing tag			
			result = result + "<span>" +TextUtils.cleanString("</" + e.getName() + ">") + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			return result;
		}
		if (e.getName().equals("ph")) { //$NON-NLS-1$
			result = result + "<span>" + TextUtils.cleanString(removeClass(e.getText())) + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
			return result;	
		}
		
		// <source> elements
		for (int i=0 ; i<content.size() ; i++) {
			XMLNode n = content.get(i);
			switch (n.getNodeType()) {
			case XMLNode.TEXT_NODE:
				result = result + ((TextNode)n).getText();
				break;
			case XMLNode.ELEMENT_NODE:
				result = result + tag((Element)n);
			}
		}
		return result;
	}

	private static String getName(String element) {
		String result = ""; //$NON-NLS-1$
		for (int i=1 ; i<element.length() ; i++) {
			char c = element.charAt(i);
			if (c == '>' || Character.isWhitespace(c)) {
				break;
			}
			result = result + c;
		}
		return result;
	}

	private static String unclean(String string) {
			String result = string.replaceAll(MATHLT, "<");  //$NON-NLS-1$
			result = result.replaceAll(MATHGT, ">");  //$NON-NLS-1$
			result = result.replaceAll(DOUBLEPRIME,"\"");  //$NON-NLS-1$
			result = result.replaceAll(GAMP,"&");  //$NON-NLS-1$
			return result;
	}

	private static String removeClass(String string) {
		int index = string.indexOf(" class="); //$NON-NLS-1$
		if (index == -1) {
			return string;
		}
		String start = string.substring(0,  index);
		String end = string.substring(index+8);
		index = end.indexOf("\""); //$NON-NLS-1$
		end = end.substring(index + 1);
		string = start + end;
		index = string.indexOf(" status=\"removeContent\""); //$NON-NLS-1$
		if (index != -1) {
			start = string.substring(0, index);
			end = string.substring(index + " status=\"removeContent\"".length()); //$NON-NLS-1$
			string = start + end;			
		}
		index = string.indexOf(" removeTranslate=\""); //$NON-NLS-1$
		if (index != -1) {
			start = string.substring(0, index);
			end = string.substring(index + " removeTranslate=\"".length()); //$NON-NLS-1$
			index = end.indexOf("\""); //$NON-NLS-1$
			end = end.substring(index + 1);
			string = start + end;			
		}
		return string;
	}

	private static void writeStr(String string) throws UnsupportedEncodingException, IOException {
		out.write(string.getBytes("UTF-8")); //$NON-NLS-1$
	}

	private static Document loadXliff(String fileName) throws SAXException, IOException, ParserConfigurationException, URISyntaxException {
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(new Catalog(Fluenta.getCatalogFile()));
		Document doc = builder.build(fileName);
		Element root = doc.getRootElement();
		if (!root.getName().equals("xliff")) {   //$NON-NLS-1$
			throw new IOException(Messages.getString("TagErrorsReport.147"));   //$NON-NLS-1$
		}
		try {
    		Element tool = root.getChild("file").getChild("header").getChild("tool");       //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		if (tool == null) {
    			throw new IOException(Messages.getString("TagErrorsReport.151"));    //$NON-NLS-1$
    		}
    		String toolId = tool.getAttributeValue("tool-id",""); //$NON-NLS-1$ //$NON-NLS-2$
    		if (!toolId.equals("Fluenta")) {     //$NON-NLS-1$
    			throw new IOException(Messages.getString("TagErrorsReport.151"));    //$NON-NLS-1$
    		}  
    	} catch (Exception e) {
    		throw new IOException(Messages.getString("TagErrorsReport.151"));   //$NON-NLS-1$
    	}		
    	checkXliffMarkup(doc.getRootElement());    	
		return doc;
	}

	private static void checkXliffMarkup(Element e) {
		if (e.getName().equals("trans-unit")) {  //$NON-NLS-1$
			Element seg = e.getChild("seg-source");  //$NON-NLS-1$
			if (seg != null) {
				e.removeChild(seg);
				Element t = e.getChild("target");   //$NON-NLS-1$
				if (t != null) {
					removeSegMrk(e.getChild("target"));   //$NON-NLS-1$
					e.setAttribute("approved", "yes");     //$NON-NLS-1$ //$NON-NLS-2$
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
		Vector<XMLNode> vector = new Vector<>();
		List<XMLNode> content = target.getContent();
		for (int i=0 ; i<content.size() ; i++) {
			XMLNode node = content.get(i);
			if (node.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element e = (Element) node;
				if (e.getName().equals("mrk") && e.getAttributeValue("mtype", "").equals("seg")) {     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					List<XMLNode> children = e.getContent();
					for (int j=0 ; j<children.size() ; j++) {
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
		List<Element> matches = e.getChildren("alt-trans");  //$NON-NLS-1$
		if (matches.size() > 0) {
			for (int i=0 ; i<matches.size() ; i++) {
				e.removeChild(matches.get(i));
			}
			children = e.getChildren();
		}
		for (int i=0 ; i<children.size() ; i++) {
			removeAltTrans(children.get(i));
		}
	}
	
	private static void createList(Element root, List<Element> segments) {
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element el = it.next();
			if (el.getName().equals("trans-unit")) {			 //$NON-NLS-1$
				segments.add(el);
			} else {
				createList(el, segments);
			}
		}
	}
	
	public static Vector<String> buildTagList(Element e) {
    	Vector<String> result = new Vector<>();
		List<XMLNode> content = e.getContent();
		Iterator<XMLNode> i = content.iterator();
		while (i.hasNext()) {
			XMLNode o = i.next();
			if (o.getNodeType() == XMLNode.ELEMENT_NODE) {
				Element el = (Element)o;
				if (el.getName().equals("ph")   //$NON-NLS-1$
						|| el.getName().equals("bpt")   //$NON-NLS-1$
						|| el.getName().equals("ept")  //$NON-NLS-1$
						|| el.getName().equals("it"))   //$NON-NLS-1$
				{
					if (el.getChildren().size()>0) {
						String open = "<"+ el.getName()+ " ";   //$NON-NLS-1$ //$NON-NLS-2$
						List<Attribute> att = el.getAttributes();
						for (int j=0 ; j<att.size() ; j++) {
							Attribute a = att.get(j);
							open = open + a.getName() + "=\"" + a.getValue().replaceAll("\"", "&quot;") + "\" ";     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						}
						result.add(open.substring(0,open.length()-1) + ">");  //$NON-NLS-1$
						List<XMLNode> list = el.getContent();
						for (int j=0 ; j<list.size() ; j++) {
							XMLNode n = list.get(j);
							if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
								result.addAll(buildTagList((Element)n));
							}
						}
						result.add("</"+ el.getName()+ ">");   //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						result.add(el.toString());
					}					
				} else if (el.getName().equals("mrk")   //$NON-NLS-1$
						|| el.getName().equals("g")   //$NON-NLS-1$
						|| el.getName().equals("sub"))   //$NON-NLS-1$
				{
					String open = "<"+ el.getName()+ " ";   //$NON-NLS-1$ //$NON-NLS-2$
					List<Attribute> att = el.getAttributes();
					for (int j=0 ; j<att.size() ; j++) {
						Attribute a = att.get(j);
						open = open + a.getName() + "=\"" + a.getValue().replaceAll("\"", "&quot;") + "\" ";     //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
					result.add(open.substring(0,open.length()-1) + ">");  //$NON-NLS-1$
					List<XMLNode> list = el.getContent();
					for (int j=0 ; j<list.size() ; j++) {
						XMLNode n = list.get(j);
						if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
							result.addAll(buildTagList((Element)n));
						}
					}
					result.add("</"+ el.getName()+ ">");   //$NON-NLS-1$ //$NON-NLS-2$
				} else if (el.getName().equals("x") || el.getName().equals("bx") || el.getName().equals("ex")) {    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
