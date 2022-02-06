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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import com.maxprograms.fluenta.Constants;
import com.maxprograms.xml.Attribute;
import com.maxprograms.xml.Document;
import com.maxprograms.xml.Element;
import com.maxprograms.xml.PI;
import com.maxprograms.xml.SAXBuilder;
import com.maxprograms.xml.XMLNode;
import com.maxprograms.xml.XMLUtils;

public class TMXExporter {

	public static final String DOUBLEPRIME = "\u2033"; //$NON-NLS-1$
	public static final String MATHLT = "\u2039"; //$NON-NLS-1$
	public static final String MATHGT = "\u200B\u203A"; //$NON-NLS-1$
	public static final String GAMP = "\u200B\u203A"; //$NON-NLS-1$

    static Document doc;
	static Hashtable<String, String> docProperties;	
    private static FileOutputStream output;
    private static EntityResolver resolver = null;
    private static String sourceLang;
    private static String targetLang;    
    private static String today;
	private static int match;
	private static String original;
	private static int filenumbr;

	public static void export(String xliff, String tmx,  boolean acceptUnaproved) throws URISyntaxException, MalformedURLException, SAXException, IOException, ParserConfigurationException {
		
        today = TMUtils.TMXDate();
        filenumbr = 0;
        
        SAXBuilder builder = new SAXBuilder();
        if (resolver != null) {
            builder.setEntityResolver(resolver);
        }
        if (xliff.indexOf("file:") != -1) { //$NON-NLS-1$
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
        Element firstFile = root.getChild("file"); //$NON-NLS-1$

		docProperties = new Hashtable<String,String>();
		List<PI> slist = root.getPI("subject"); //$NON-NLS-1$
		if (slist.size()>0) {
			docProperties.put("subject", slist.get(0).getData()); //$NON-NLS-1$
		} else {
			docProperties.put("subject", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		List<PI> plist = root.getPI("project"); //$NON-NLS-1$
		if (plist.size()>0) {
			docProperties.put("project", plist.get(0).getData()); //$NON-NLS-1$
		} else {
			docProperties.put("project",""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		List<PI> clist = root.getPI("customer"); //$NON-NLS-1$
		if (clist.size()>0) {
			docProperties.put("customer",clist.get(0).getData()); //$NON-NLS-1$
		} else {
			docProperties.put("customer", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

        
        sourceLang = firstFile.getAttributeValue("source-language", ""); //$NON-NLS-1$ //$NON-NLS-2$

        writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
        writeString("<!DOCTYPE tmx PUBLIC \"-//LISA OSCAR:1998//DTD for Translation Memory eXchange//EN\" \"tmx14.dtd\" >\n"); //$NON-NLS-1$
        writeString("<tmx version=\"1.4\">\n"); //$NON-NLS-1$
        writeString("<header \n" + //$NON-NLS-1$
        		"      creationtool=\"" +  //$NON-NLS-1$
        		Constants.NAME + 
        		"\" \n" + //$NON-NLS-1$
        		"      creationtoolversion=\"" + //$NON-NLS-1$
                Constants.VERSION +
                "\"  \n" + //$NON-NLS-1$
                "      srclang=\"" + //$NON-NLS-1$
                sourceLang +
                "\" \n" + //$NON-NLS-1$
                "      adminlang=\"en\"  \n" + //$NON-NLS-1$
                "      datatype=\"xml\" \n" + //$NON-NLS-1$
                "      o-tmf=\"XLIFF\" \n" + //$NON-NLS-1$
                "      segtype=\"block\"\n" + //$NON-NLS-1$
                ">\n" + //$NON-NLS-1$
                "</header>\n"); //$NON-NLS-1$
        writeString("<body>\n"); //$NON-NLS-1$

        List<Element> files = root.getChildren();
        Iterator<Element> fileiterator = files.iterator();
        while (fileiterator.hasNext()) {
            Element file = fileiterator.next();
            sourceLang = file.getAttributeValue("source-language", ""); //$NON-NLS-1$ //$NON-NLS-2$
            targetLang = file.getAttributeValue("target-language", ""); //$NON-NLS-1$ //$NON-NLS-2$
            original = "" + file.getAttributeValue("original").hashCode(); //$NON-NLS-1$ //$NON-NLS-2$
			recurse(file,acceptUnaproved);
            file = null;
        	filenumbr++;
        }
        writeString("</body>\n"); //$NON-NLS-1$
        writeString("</tmx>"); //$NON-NLS-1$

        output.close();

        output = null;
        root = null;
        doc = null;
    }
	
	private static void recurse(Element e, boolean acceptUnaproved) throws IOException {
		List<Element> list = e.getChildren();
		Iterator<Element> i = list.iterator();
		while (i.hasNext()) {
			Element element = i.next();
			if (element.getName().equals("trans-unit")) { //$NON-NLS-1$
                writeSegment(element, acceptUnaproved);
			} else {
				recurse(element, acceptUnaproved);
			}
		}
	}
    /**
     * Method writeSegment.
     * 
     * @param segment
     */
    private static void writeSegment(Element segment, boolean acceptUnaproved) throws IOException {

    
       String id = original + "-" + filenumbr + "-" + segment.getAttributeValue("id").hashCode(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        if (segment.getAttributeValue("approved").equals("yes") || acceptUnaproved) { //$NON-NLS-1$ //$NON-NLS-2$
            Element source = segment.getChild("source"); //$NON-NLS-1$
            if (source.getContent().size() == 0) {
            	// merged segment, nothing to export
            	return;
            }
            Element target = segment.getChild("target"); //$NON-NLS-1$
            if (target == null) {
            	return;
            }
            String srcText = extractText(source,true);
            String tgtText = extractText(target,true);
            if (acceptUnaproved && tgtText.trim().isEmpty()) {
            	return;
            }
            String srcLang = source.getAttributeValue("xml:lang", ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (srcLang.equals("")) { //$NON-NLS-1$
                srcLang = sourceLang;
            }
            String tgtLang = target.getAttributeValue("xml:lang", ""); //$NON-NLS-1$ //$NON-NLS-2$
            if (tgtLang.equals("")) { //$NON-NLS-1$
                tgtLang = targetLang;
            }
            writeString("<tu creationtool=\"" //$NON-NLS-1$
					+ Constants.NAME
					+"\" creationtoolversion=\"" //$NON-NLS-1$
                    + Constants.VERSION 
                    + "\" tuid=\"" + id + "\" creationdate=\""+ today +"\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         
            String customer = docProperties.get("customer"); //$NON-NLS-1$
            String project = docProperties.get("project"); //$NON-NLS-1$
            String subject = docProperties.get("subject"); //$NON-NLS-1$

            if (!subject.equals("")){ //$NON-NLS-1$
            	writeString("<prop type=\"subject\">" + XMLUtils.cleanText(subject) + "</prop>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!project.equals("")){             //$NON-NLS-1$
            	writeString("<prop type=\"project\">" + XMLUtils.cleanText(project) + "</prop>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!customer.equals("")){             //$NON-NLS-1$
            	writeString("<prop type=\"customer\">" + XMLUtils.cleanText(customer) + "</prop>\n"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            List<Element> notes = segment.getChildren("note"); //$NON-NLS-1$
            Iterator<Element> it = notes.iterator();
            while ( it.hasNext()) {
                Element note = it.next();
                String lang = note.getAttributeValue("xml:lang",""); //$NON-NLS-1$ //$NON-NLS-2$
                if ( !lang.equals("")) { //$NON-NLS-1$
                    lang = " xml:lang=\"" + lang + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                }
                writeString("<note" + lang + ">" + XMLUtils.cleanText(note.getText()) + "</note>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            if (!segment.getAttributeValue("xml:space","default").equals("preserve")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	srcText = srcText.trim();
            	tgtText = tgtText.trim();
            }
            writeString("<tuv xml:lang=\"" + srcLang + "\" creationdate=\""+ today +"\">\n<seg>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            		+ srcText + "</seg>\n</tuv>\n"); //$NON-NLS-1$
            writeString("<tuv xml:lang=\"" + tgtLang + "\" creationdate=\""+ today +"\">\n<seg>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            		+ tgtText + "</seg>\n</tuv>\n"); //$NON-NLS-1$

            writeString("</tu>\n"); //$NON-NLS-1$
            source = null;
            target = null;
            srcLang = null;
            tgtLang = null;
        }
    }

    public static String extractText(Element src, boolean _level1) {
		
		String type = src.getName();

		if (type.equals("source") || type.equals("target")) { //$NON-NLS-1$ //$NON-NLS-2$
			match = 0;
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String text = ""; //$NON-NLS-1$
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
				case XMLNode.TEXT_NODE:
					text = text + o.toString();
					break;
				case XMLNode.ELEMENT_NODE:
					if (!_level1) {
						Element e = (Element)o;
						text = text + extractText(e, false);
						e = null;
					}
					if ( _level1 && ((Element)o).getName().equals("mrk")) { //$NON-NLS-1$
						Element e = (Element)o;
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
			String text = ""; //$NON-NLS-1$
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
				case XMLNode.TEXT_NODE:
					text = text + o.toString();
					break;
				case XMLNode.ELEMENT_NODE:
					if (!_level1) {
						Element e = (Element)o;
						text = text + extractText(e, true);
						e = null;
					}
					break;
				}
				o = null;
			}
			i = null;
			l = null;
			return text;
		}

		if (type.equals("bx") //$NON-NLS-1$
				|| type.equals("ex") //$NON-NLS-1$
				|| type.equals("ph")) { //$NON-NLS-1$
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String ctype = src.getAttributeValue("ctype", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!ctype.equals("")) { //$NON-NLS-1$
				ctype = " type=\"" + XMLUtils.cleanText(ctype) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String assoc = src.getAttributeValue("assoc", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!assoc.equals("")) { //$NON-NLS-1$
				assoc = " assoc=\"" + XMLUtils.cleanText(assoc) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String id = ""; //$NON-NLS-1$
			if (type.equals("ph")) { //$NON-NLS-1$
				id = src.getAttributeValue("id", ""); //$NON-NLS-1$ //$NON-NLS-2$
				if (!id.equals("")) { //$NON-NLS-1$
					id = " x=\"" + XMLUtils.cleanText(id) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			String text = "<ph" + ctype + assoc + id + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
				case XMLNode.TEXT_NODE:
					text = text + o.toString();
					break;
				case XMLNode.ELEMENT_NODE:
					Element e = (Element)o;
					if (e.getName().equals("sub")) { //$NON-NLS-1$
						text = text + extractText(e, false);
					} else {
						if (!e.getName().equals("mrk")) { //$NON-NLS-1$
							text = text + extractText(e, true);
						}
					}
					break;
				}
				o = null;
			}
			i = null;
			l = null;
			return text + "</ph>"; //$NON-NLS-1$
		}

		if (type.equals("g")||type.equals("x")) { //$NON-NLS-1$ //$NON-NLS-2$
			String open = "<" + src.getName(); //$NON-NLS-1$
			List<Attribute> atts = src.getAttributes();
			Iterator<Attribute> h = atts.iterator();
			while (h.hasNext()) {
				Attribute a = h.next();
				open = open + " " + a.getName() + "=\"" + a.getValue() + "\"";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			List<XMLNode> l = src.getContent();
			if (l.size() > 0) {
				open = open + ">"; //$NON-NLS-1$
				int i = match;
				match++;
				String text =  "<bpt type=\"xliff-"+src.getName()+"\" i=\""+ i +"\">" + XMLUtils.cleanText(open) + "</bpt>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				Iterator<XMLNode> k = l.iterator();
				while(k.hasNext()) {
					XMLNode n = k.next();
					if (n.getNodeType() == XMLNode.TEXT_NODE) {
						text = text + n.toString();
					}
					if (n.getNodeType() == XMLNode.ELEMENT_NODE) {
						text = text + extractText((Element)n, _level1);
					}
				}
				String close = "</" + src.getName() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
				return text + "<ept i=\""+ i +"\">" + XMLUtils.cleanText(close) + "</ept>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			} 
			return "<ph type=\"xliff-"+src.getName()+"\">" + XMLUtils.cleanText(open+ "/>") + "</ph>";			 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		
		if (type.equals("it")) { //$NON-NLS-1$
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String ctype = src.getAttributeValue("ctype", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!ctype.equals("")) { //$NON-NLS-1$
				ctype = " type=\"" + XMLUtils.cleanText(ctype) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String pos = src.getAttributeValue("pos", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (pos.equals("open")) { //$NON-NLS-1$
				pos = " pos=\"begin\""; //$NON-NLS-1$
			} else if (pos.equals("close")) { //$NON-NLS-1$
				pos = " pos=\"end\""; //$NON-NLS-1$
			}
			String text = "<it" + ctype + pos + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
				case XMLNode.TEXT_NODE:
					text = text + o.toString();
					break;
				case XMLNode.ELEMENT_NODE:
					Element e = (Element)o;
					text = text + extractText(e, false);
					break;
				}
			}
			return text + "</it>"; //$NON-NLS-1$
		}

		if (type.equals("bpt") || type.equals("ept")) { //$NON-NLS-1$ //$NON-NLS-2$
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String ctype = src.getAttributeValue("ctype", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!ctype.equals("")) { //$NON-NLS-1$
				ctype = " type=\"" + XMLUtils.cleanText(ctype) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			String rid = src.getAttributeValue("rid", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!rid.equals("")) { //$NON-NLS-1$
				rid = " i=\"" + XMLUtils.cleanText(rid) + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				rid = " i=\"" + XMLUtils.cleanText(src.getAttributeValue("id", "")) + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			String text = "<" + type + ctype + rid + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
				case XMLNode.TEXT_NODE:
					text = text + o.toString();
					break;
				case XMLNode.ELEMENT_NODE:
					Element e = (Element)o;
					text = text + extractText(e, false);
					break;
				}
				o = null;
			}
			i = null;
			l = null;
			return text + "</" + type + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (type.equals("sub")) { //$NON-NLS-1$
			List<XMLNode> l = src.getContent();
			Iterator<XMLNode> i = l.iterator();
			String text = "<sub>"; //$NON-NLS-1$
			while (i.hasNext()) {
				XMLNode o = i.next();
				switch (o.getNodeType()) {
				case XMLNode.TEXT_NODE:
					text = text + o.toString();
					break;
				case XMLNode.ELEMENT_NODE:
					Element e = (Element)o;
					if (!e.getName().equals("mrk")) { //$NON-NLS-1$
						text = text + extractText(e, true);
					}
					break;
				}
				o = null;
			}
			i = null;
			l = null;
			return text + "</sub>"; //$NON-NLS-1$
		}
		if( type.equals("mrk")) { //$NON-NLS-1$
			if (src.getAttributeValue("mtype", "").equals("term")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				// ignore terminology entries
				return XMLUtils.cleanText(src.getText());
			}
			if (src.getAttributeValue("mtype", "").equals("protected")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				String ts = src.getAttributeValue("ts", ""); //$NON-NLS-1$ //$NON-NLS-2$
				ts = restoreChars(ts).trim();
				String name = ""; //$NON-NLS-1$
				for (int i=1 ; i<ts.length() ; i++) {
					if (Character.isSpaceChar(ts.charAt(i))) {
						break;
					}
					name = name + ts.charAt(i);
				}
				return "<ph type=\"mrk-protected\" " + " x=\"" + XMLUtils.cleanText(src.getAttributeValue("mid","-")) + "\"" +">" + XMLUtils.cleanText(ts) + "</ph>" + XMLUtils.cleanText(src.getText()) + "<ph type=\"mrk-close\">" + XMLUtils.cleanText("</" + name + ">")+ "</ph>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
			}
			return "<hi type=\"" + src.getAttributeValue("mtype", "xliff-mrk") + "\">" + XMLUtils.cleanText(src.getText()) + "</hi>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return null;
    }

    private static String restoreChars(String value) {
    	value = value.replaceAll(MATHLT, "<"); //$NON-NLS-1$
    	value = value.replaceAll(MATHGT, ">"); //$NON-NLS-1$
    	value = value.replaceAll(DOUBLEPRIME, "\""); //$NON-NLS-1$
    	value = value.replaceAll(GAMP, "&"); //$NON-NLS-1$
    	return value;
    }

    private static void writeString(String input) throws IOException {
        output.write(input.getBytes("utf-8")); //$NON-NLS-1$
    } // end writeString
	
}