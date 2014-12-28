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
import concurrency.BillListWriter;
import concurrency.BillUrlWriter;

public class BillUtil {

	public static int lev_min = 5;

	public static int RATE_LIMIT = 20000;

	public static String PROXY_SITE = "http://www.us-proxy.org/";
	
	public static String filepath = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\bills\\";

	public static ArrayList<Pair> proxyPairs;

	public static final int MAX_PROXY_RECURSION = 5;

	public static void main(String [] args){

		System.setProperty("http.agent", "");

		proxyPairs = ProxyUtil.getProxies(PROXY_SITE);

		ArrayList<Pair> billNumberRanges = pullAllBillXML(111,"hr");

		ArrayList<String> targetURLs = new ArrayList<String>();
		
		Pair first = billNumberRanges.get(0);

		try {
			targetURLs.addAll(pullBillXML(111, "hr", first.left, first.right));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayBlockingQueue<String> billUrlQueue = new ArrayBlockingQueue<String>(8000, false, targetURLs);
		ArrayBlockingQueue<Pair> proxyQueue = new ArrayBlockingQueue<Pair>(8000, false, proxyPairs);
		ArrayBlockingQueue<Pair> rangeQueue = new ArrayBlockingQueue<Pair>(8000, false, billNumberRanges);
		
		BillListWriter writer0 = new BillListWriter(proxyQueue, rangeQueue, billUrlQueue, 111,"hr");
		
		BillUrlWriter writer1 = new BillUrlWriter(billUrlQueue, proxyQueue);
		BillUrlWriter writer2 = new BillUrlWriter(billUrlQueue, proxyQueue);
		
		new Thread(writer0).start();
		new Thread(writer1).start();
		new Thread(writer2).start();
		
		
	}

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

		System.out.println("result is: " + result);

		return result;
	}

	public static String getBillList(int congress, String billCode){

		String base = "http://www.gpo.gov/fdsys/browse/collection.action?collectionCode=BILLS" +
				"&browsePath=" + congress + 
				"%2F" + billCode + 
				"&isCollapsed=false" +
				"&leafLevelBrowse=false";

		return base;
	}

	public static ArrayList<Pair> pullAllBillXML(int congress, String code){

		ArrayList<Pair> ret = new ArrayList<Pair>();

		String url = getBillList(congress, code);

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
		
		System.out.println("creating proxy with addr: " + pair.getLeft() + " port: " + pair.getRight());
		
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

			Pair mPair = proxyPairs.get(0);
			Pair mProxy = proxyPairs.remove(0);

			return getProxyInputStream(url, count +1, mPair);
		}
	}

	public static ArrayList<String> pullBillXML(int congress, String code, String startRange, String endRange) throws Exception{

		String mUrl = getBillPage(congress, code, startRange, endRange);
		BufferedReader in = getProxyInputStream(mUrl, proxyPairs.remove(0));
		
		return pullBillXML(congress, code, startRange, endRange, in);
		
	}
	
	public static ArrayList<String> pullBillXML(int congress, String code, String startRange, String endRange, BufferedReader in) throws Exception{

		ArrayList<String> urlList = new ArrayList<String>();

		try{
			System.setProperty("http.agent", "");

			String mUrl = getBillPage(congress, code, startRange, endRange);

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				if(inputLine.contains(">Text<")){

					int startIndex = inputLine.indexOf("href=") + 6;
					int endIndex = inputLine.indexOf("target=")-2;

					String ref = inputLine.substring(startIndex, endIndex);

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

	public static boolean doesBillExist(String code){
		File f = new File(filepath + code);
		return f.exists();
	}

	public static String getBillNameFromRef(String ref){
		int indexStart = ref.indexOf("/html/") + 6;
		return ref.substring(indexStart);
	}
	
	public static boolean writeBillFile(String url, BufferedReader in) throws Exception {

		if(!doesBillExist(getBillNameFromRef(url))){

			String path = filepath + getBillNameFromRef(url);

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
}