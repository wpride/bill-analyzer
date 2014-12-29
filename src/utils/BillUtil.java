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
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import utils.ProxyUtil.Pair;
import concurrency.ListToUrlWorker;
import concurrency.MoreToXmlWorker;
import concurrency.UrlToFileWorker;

public class BillUtil {

	public static int lev_min = 5;

	public static int RATE_LIMIT = 20000;

	public static String PROXY_SITE = "http://www.us-proxy.org/";

	public static String MODE = "xml";

	public static String html_filepath = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\bills\\html\\";

	public static String xml_filepath = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\bills\\xml\\";

	public static ArrayList<Pair> proxyPairs;

	public static final int MAX_PROXY_RECURSION = 5;

	public static void main(String[] args){
		
		System.setProperty("http.agent", "");
		proxyPairs = ProxyUtil.getProxies(PROXY_SITE);
		
		if(BillUtil.MODE.equals("xml")){
			BillUtil.generateXMLFiles();
		} else{
			BillUtil.generateHtmlFiles();
		}
	}
	
	public static void generateHtmlFiles(){

		ArrayList<Pair> billNumberRanges = pullAllBillXML(111,"hr");

		ArrayList<String> targetURLs = new ArrayList<String>();

		Pair first = billNumberRanges.get(0);

		try {
			String mUrl = BillUtil.getBillPage(111, "hr", first.left, first.right);
			//targetURLs.addAll(pullBillXML(mUrl, BillUtil.getProxyInputStream(mUrl, proxyPairs.remove(0))));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayBlockingQueue<String> billUrlQueue = new ArrayBlockingQueue<String>(8000, false, targetURLs);
		ArrayBlockingQueue<Pair> proxyQueue = new ArrayBlockingQueue<Pair>(8000, false, proxyPairs);
		ArrayBlockingQueue<Pair> rangeQueue = new ArrayBlockingQueue<Pair>(8000, false, billNumberRanges);

		ListToUrlWorker writer0 = new ListToUrlWorker(proxyQueue, rangeQueue, billUrlQueue, 111,"hr", "TEXT");

		UrlToFileWorker writer1 = new UrlToFileWorker(billUrlQueue, proxyQueue, html_filepath);

		new Thread(writer0).start();
		new Thread(writer1).start();
	}
	
	/**
	 *  Generate the XML files
	 */
	
	public static void generateXMLFiles(){

		ArrayList<Pair> billNumberRanges = pullAllBillXML(111,"hr");

		ArrayList<String> targetURLs = new ArrayList<String>();

		Pair first = billNumberRanges.get(0);

		try {
			String mUrl = BillUtil.getBillPage(111, "hr", first.left, first.right);
			//targetURLs.addAll(pullBillXML(mUrl, BillUtil.getProxyInputStream(mUrl, proxyPairs.remove(0))));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayBlockingQueue<Pair> proxyQueue = new ArrayBlockingQueue<Pair>(8000, false, proxyPairs);
		ArrayBlockingQueue<Pair> rangeQueue = new ArrayBlockingQueue<Pair>(8000, false, billNumberRanges);
		ArrayBlockingQueue<String> billXmlUrlQueue = new ArrayBlockingQueue<String>(8000, false, targetURLs);
		ArrayBlockingQueue<String> urlMoreQueue = new ArrayBlockingQueue<String> (8000, false);

		ListToUrlWorker writer0 = new ListToUrlWorker(proxyQueue, rangeQueue, urlMoreQueue, 111,"hr", "XML");
		
		MoreToXmlWorker writer1 = new MoreToXmlWorker(urlMoreQueue, billXmlUrlQueue, proxyQueue);

		UrlToFileWorker writer2 = new UrlToFileWorker(billXmlUrlQueue, proxyQueue, xml_filepath);

		new Thread(writer0).start();
		new Thread(writer1).start();
		new Thread(writer2).start();

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
	public static ArrayList<Pair> pullAllBillXML(int congress, String code){
	
		ArrayList<Pair> ret = new ArrayList<Pair>();

		String url = getBillListUrl(congress, code);

		BufferedReader in = getProxyInputStream(url, proxyPairs.remove(0));
		String inputLine = "";

		try {

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;

	}

	public static BufferedReader getProxyInputStream(String url, Pair pair){

		return getProxyInputStream(url, 0, pair);

	}

	public static BufferedReader getProxyInputStream(String url, int count, Pair pair){

		Proxy proxy = new Proxy(Proxy.Type.HTTP, 
				new InetSocketAddress(pair.getLeft(),Integer.valueOf(pair.getRight())));

		return getProxyInputStream(url, count, proxy);
	}
	
	public static BufferedReader getProxyInputStream(String url, int count, Proxy proxy){
		
		if(count >= BillUtil.MAX_PROXY_RECURSION){
			return null;
		}

		try{

			URL oracle = new URL(url);

			URLConnection yc = oracle.openConnection(proxy);

			yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			return new BufferedReader(new InputStreamReader(
					yc.getInputStream()));

		} catch(IOException ioe) {
			System.out.println("Caught IOE: " + ioe);
			ioe.printStackTrace();

			Pair mProxy = proxyPairs.remove(0);

			return getProxyInputStream(url, count +1, mProxy);
		}
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
	
	public static ArrayList<String> getKeyRef(String url, BufferedReader in, String key) throws Exception{

		ArrayList<String> urlList = new ArrayList<String>();

		try{
			System.setProperty("http.agent", "");

			String mUrl = url;

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				if(inputLine.contains(key)){

					int startIndex = inputLine.indexOf("href=") + 6;
					int endIndex = inputLine.indexOf("target=")-2;

					String ref = inputLine.substring(startIndex, endIndex);

					ref = BillUtil.processReference(ref);

					urlList.add(ref);
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
		return ret;
	}

	public static boolean doesBillExist(String code){
		if(BillUtil.MODE.equals("xml")){
			File f = new File(xml_filepath + code);
			return f.exists();
		} else if(BillUtil.MODE.equals("html")){
			File f = new File(html_filepath + code);
			return f.exists();
		}
		return false;
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

	public static String getFilePath(){
		if(BillUtil.MODE.equals("xml")){
			return xml_filepath;
		} else if(BillUtil.MODE.equals("html")){
			return html_filepath;
		}
		return "null";
	}

	public static boolean writeFile(String url, BufferedReader in, String writePath) throws Exception {
		
		File pathFile = new File(writePath);

		if(!pathFile.exists()){

			String path = writePath + getBillNameFromRef(url);

			System.out.println("writing file for url: " + url + ".");

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
			if(inputLine.contains(">xml<")){
				int startIndex = inputLine.indexOf("href=") + 6;
				int endIndex = inputLine.indexOf("target=")-2;
				String ret = inputLine.substring(startIndex, endIndex);
				return ret;
			}
		}
		in.close();
		return "null";

	}

}