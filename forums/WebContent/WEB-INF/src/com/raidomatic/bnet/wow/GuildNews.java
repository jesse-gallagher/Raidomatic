// TODO: Integrate this with the proper API using http://us.battle.net/api/wow/guild/thorium-brotherhood/The%20Risen?fields=news

package com.raidomatic.bnet.wow;

import java.io.*;

import java.net.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class GuildNews {
	public List<Map<String, String>> getNews() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		List<Map<String, String>> result = new Vector<Map<String, String>>();
		
		org.w3c.dom.Document armoryPage = fetchDOM("http://us.battle.net/wow/en/guild/thorium-brotherhood/The%20Risen/");
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList cells = (NodeList)xpath.compile("//ul[@class='activity-feed']/li").evaluate(armoryPage, XPathConstants.NODESET);
		for(int i = 0; i < cells.getLength(); i++) {
			Map<String, String> item = new HashMap<String, String>();
			
			Node feat = (Node)xpath.compile("dl/dd").evaluate(cells.item(i), XPathConstants.NODE);
			Node time = (Node)xpath.compile("dl/dt").evaluate(cells.item(i), XPathConstants.NODE);
			
			item.put("title", feat.getTextContent().replaceAll("^\\s+", "").replaceAll("\\s+$", ""));
			item.put("time", time.getTextContent());
			
			result.add(item);
		}
		
		return result;
	}
	
	public String fetchURL(String urlString) throws IOException  {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		//conn.setRequestProperty("User-Agent", "Firefox/2.0");
		
		BufferedReader in = new BufferedReader(new InputStreamReader((InputStream)conn.getContent()));
		StringWriter resultWriter = new StringWriter();
		String inputLine;
		while((inputLine = in.readLine()) != null) {
			resultWriter.write(inputLine);
		}
		in.close();
		
		return resultWriter.toString().replace("<HTTP-EQUIV", "<meta http-equiv");

	}
	
	public org.w3c.dom.Document fetchDOM(String url) throws ParserConfigurationException, SAXException, IOException  {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(new org.xml.sax.InputSource(url));
	}
}
