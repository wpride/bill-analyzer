package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import utils.ProxyUtil.Pair;
import concurrency.ListToUrlWorker;
import concurrency.MoreToXmlWorker;
import concurrency.UrlToFileWorker;

public class BillUtil {

	public static int RATE_LIMIT = 20000;

	public static String PROXY_SITE = "http://www.us-proxy.org/";

	public static String MODE = "xml";

	public static String html_filepath = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\bills\\html\\";

	public static String xml_filepath = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\bills\\xml\\";

	public static final int MAX_PROXY_RECURSION = 5;
	
	public static final int NUMBER_MORE_TO_XML_WORKERS = 20;
	public static final int NUMBER_XML_TO_FILE_WORKERS = 20;

	public static void main(String[] args){

		System.setProperty("http.agent", "");
		ArrayList<Pair> proxyPairs = ProxyUtil.getProxies(PROXY_SITE);

		System.out.println("Proxy pairs size: " + proxyPairs.size());
		
		if(BillUtil.MODE.equals("xml")){
			BillUtil.generateXMLFiles(proxyPairs);
		} else{
			BillUtil.generateHtmlFiles(proxyPairs);
		}
	}

	public static void generateHtmlFiles(ArrayList<Pair> proxyPairs){

		ArrayList<Pair> validBillRanges = getValidRanges(111,"hr", proxyPairs);

		ArrayBlockingQueue<Pair> proxyQueue = new ArrayBlockingQueue<Pair>(8000, false, proxyPairs);
		ArrayBlockingQueue<Pair> rangeQueue = new ArrayBlockingQueue<Pair>(8000, false, validBillRanges);
		ArrayBlockingQueue<String> htmlUrlQueue = new ArrayBlockingQueue<String>(8000, false);

		ListToUrlWorker writer0 = new ListToUrlWorker(proxyQueue, rangeQueue, htmlUrlQueue, 111,"hr", ">TEXT<", false);
		UrlToFileWorker writer1 = new UrlToFileWorker(proxyQueue, htmlUrlQueue, html_filepath);

		new Thread(writer0).start();
		new Thread(writer1).start();
	}

	/**
	 *  Generate the XML files	
	 */

	public static void generateXMLFiles(ArrayList<Pair> proxyPairs){
		
		int congCode = 111;
		String billCode = "hr";

		ArrayList<Pair> billNumberRanges = getValidRanges(congCode,billCode, proxyPairs);
		
		ArrayBlockingQueue<Pair> rangeQueue = new ArrayBlockingQueue<Pair>(8000, false);
		
		for(Pair p: billNumberRanges){
			
			String start = p.getLeft();
			String end = p.getRight();
			
			String startString = congCode + billCode + start;
			String endString = congCode + billCode + end;

			if(pathContainsFile(xml_filepath, startString) && pathContainsFile(xml_filepath, endString)){
				System.out.println("Match! Removed range: " + p.toString());
			} else{
				rangeQueue.add(p);
			}
		}

		ArrayBlockingQueue<Pair> proxyQueue = new ArrayBlockingQueue<Pair>(8000, false, proxyPairs);
		//ArrayBlockingQueue<Pair> rangeQueue = new ArrayBlockingQueue<Pair>(8000, false, billNumberRanges);
		ArrayBlockingQueue<String> urlMoreQueue = new ArrayBlockingQueue<String> (8000, false);
		ArrayBlockingQueue<String> xmlUrlQueue = new ArrayBlockingQueue<String>(8000, false);

		ListToUrlWorker writer0 = new ListToUrlWorker(proxyQueue, rangeQueue, urlMoreQueue, 111,"hr", "More<", true);
		new Thread(writer0).start();
		
		MoreToXmlWorker[] moreToXmlWorkers = new MoreToXmlWorker[NUMBER_MORE_TO_XML_WORKERS];
		UrlToFileWorker[] urlToFileWorkers = new UrlToFileWorker[NUMBER_XML_TO_FILE_WORKERS];
		
		for(int i =0; i < NUMBER_MORE_TO_XML_WORKERS; i++){
			moreToXmlWorkers[i] = new MoreToXmlWorker(proxyQueue, urlMoreQueue, xmlUrlQueue);
			new Thread(moreToXmlWorkers[i]).start();
		}
		
		for(int i =0; i < NUMBER_XML_TO_FILE_WORKERS; i++){
			urlToFileWorkers[i] = new UrlToFileWorker(proxyQueue, xmlUrlQueue, xml_filepath);
			new Thread(urlToFileWorkers[i]).start();
		}

	}

	/**
	 * given a congress number, bill code, and range, return the URL to the expanded list of these bills
	 * 
	 * @param congress
	 * @param billCode
	 * @param rangeStart
	 * @param rangeEnd
	 * @return
	 */

	public static String getBillPage(int congress, String billCode, String rangeStart, String rangeEnd){
		String base = "http://www.gpo.gov/fdsys/browse/collection.action" +
				"?collectionCode=BILLS";

		String congressString = "&browsePath="+congress;
		String billCodeString = "%2F" + billCode;
		String bufferCodeString = "%2F";
		String rangeStartString =	"%5B" + rangeStart;
		String rangeEndString =	"%3B" + rangeEnd;

		String endString = "%5D" +
				"&isCollapsed=false" +
				"&leafLevelBrowse=false" +
				"&isDocumentResults=true";

		String result = base + congressString + billCodeString + bufferCodeString + rangeStartString + rangeEndString + endString;

		return result;
	}

	/**
	 * Given a congress number and bill code, give the URL to the home listing
	 * 
	 * @param congress - congress code
	 * @param billCode - bill code
	 * @return
	 */

	public static String getBillListUrl(int congress, String billCode){

		String base = "http://www.gpo.gov/fdsys/browse/collection.action?collectionCode=BILLS" +
				"&browsePath=" + congress + 
				"%2F" + billCode + 
				"&isCollapsed=false" +
				"&leafLevelBrowse=false";

		return base;
	}

	/**
	 * Given a congress number and code, return all valid bill ranges
	 * 
	 * @param congress the congress number (IE 111)
	 * @param code the bill code (IE hr, sr, etc) 
	 */
	public static ArrayList<Pair> getValidRanges(int congress, String code, ArrayList<Pair> proxyPairs){

		ArrayList<Pair> ret = new ArrayList<Pair>();

		String url = getBillListUrl(congress, code);

		try{
			BufferedReader in = getProxyInputStream(url, proxyPairs.remove(0));

			String inputLine = "";

			while ((inputLine = in.readLine()) != null){
				if(inputLine.contains("[") && inputLine.contains(";") && inputLine.contains("]")){

					String subInput = inputLine.substring(inputLine.indexOf("["));

					int indexOne = 0;
					int indexTwo = subInput.indexOf(";");
					int indexThree = subInput.indexOf("]");

					String rangeStart;
					if(indexOne + 1 == indexTwo){
						rangeStart = "";
					} else {
						rangeStart = subInput.substring(indexOne + 1, indexTwo);
					}
					String rangeEnd = subInput.substring(indexTwo + 1, indexThree);

					Pair mPair = new Pair(rangeStart, rangeEnd);
					ret.add(mPair);

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return getValidRanges(congress, code, proxyPairs);
		}

		return ret;

	}

	public static boolean pathContainsFile(String path, String code){
		
		String[] filenames = filenamesInFolder(path);
		for(String s:filenames){
			if(s.contains(code)){
				return true;
			}
		}
		
		return false;
	}
	
	public static String[] filenamesInFolder(String path){
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		String[] listOfFileNames = new String[listOfFiles.length];

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				listOfFileNames[i] = listOfFiles[i].getName();
			} else if (listOfFiles[i].isDirectory()) {
				System.out.println("BAD folder in path");
			}
		}
		
		return listOfFileNames;
	}

	public static BufferedReader getProxyInputStream(String url, Pair pair) throws IOException{

		return getProxyInputStream(url, 0, pair);

	}

	public static BufferedReader getProxyInputStream(String url, int count, Pair pair) throws IOException{

		Proxy proxy = new Proxy(Proxy.Type.HTTP, 
				new InetSocketAddress(pair.getLeft(),Integer.valueOf(pair.getRight())));

		return getProxyInputStream(url, count, proxy);
	}

	public static BufferedReader getProxyInputStream(String url, int count, Proxy proxy) throws IOException{

		if(count >= BillUtil.MAX_PROXY_RECURSION){
			return null;
		}

		URL oracle = new URL(url);

		URLConnection yc = oracle.openConnection(proxy);

		yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		return new BufferedReader(new InputStreamReader(
				yc.getInputStream()));
	}


	/**
	 * 
	 * Given a url buffered reader and key, find all hrefs associated with that text key
	 * 
	 * @param url
	 * @param in
	 * @param key
	 * @return
	 * @throws Exception
	 */

	public static ArrayList<String> getKeyRef(String url, BufferedReader in, String key, boolean relative) throws Exception{

		ArrayList<String> urlList = new ArrayList<String>();

		try{
			System.setProperty("http.agent", "");

			String mUrl = url;

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				if(inputLine.contains(key)){
					
					int startIndex = inputLine.indexOf("href=") + 6;
					
					String newString = inputLine.substring(startIndex);
					
					int endIndex = newString.indexOf('"');
					
					String ref = newString.substring(0, endIndex);
					
					if(relative){
						ref = "http://www.gpo.gov/fdsys/" + ref;
					}

					ref = BillUtil.processReference(ref);

					if(ref != null)	{
						urlList.add(ref);
					}
				}
			}
		}
		finally{
			if(in != null){
				in.close();
			}
		}
		return urlList;
	}

	private static String processReference(String ref) {

		String ret = ref;

		if(BillUtil.MODE.equals("xml")){

			ret = ret.replace("/html/", "/xml/");
			ret = ret.replace(".htm",".xml");

		}
		
		ret = ret.replace("amp;", "");
		
		return ret;
	}

	public static String getBillNameFromRef(String ref){	
		if(BillUtil.MODE.equals("xml")){
			int indexStart = ref.indexOf("/xml/") + 5;
			return ref.substring(indexStart);
		} else if(BillUtil.MODE.equals("html")){
			int indexStart = ref.indexOf("/html/") + 6;
			return ref.substring(indexStart);
		}

		return "null";
	}

	public static boolean writeFile(String url, BufferedReader in, String writePath) throws Exception {

		String path = writePath + getBillNameFromRef(url);
		
		File pathFile = new File(path);

		if(!pathFile.exists()){

			System.out.println("writing file for url: " + url + " and filepath: " + path);

			BufferedWriter writer = new BufferedWriter(new FileWriter(path));

			String inputLine;
			while ((inputLine = in.readLine()) != null){
				try{
					writer.write(inputLine);
				}
				catch(IOException e){
					e.printStackTrace();
					writer.close();
					return false;
				}
			}
			in.close();
			writer.close();
			return true;
		} else{
			System.out.println("file for url: " + url + " exists.");
			return false;
		}
	}

	public static String getXmlUrl(String url, BufferedReader in) throws Exception {

		String inputLine;
		while ((inputLine = in.readLine()) != null){
			if(inputLine.contains(">XML<")){
				
				int endIndex = inputLine.indexOf(".xml");
				
				String trimmed = inputLine.substring(0, endIndex + 4);
				
				int startIndex = trimmed.lastIndexOf("href=") + 6;
				
				trimmed = trimmed.substring(startIndex);
				
				return trimmed;
			}
		}
		in.close();
		throw new ParseException("Couldn't find XML!", 0);

	}

}