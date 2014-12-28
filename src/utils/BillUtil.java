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

import utils.ProxyUtil.IntPair;
import utils.ProxyUtil.Pair;

public class BillUtil {

	public static int lev_min = 5;

	public static int RATE_LIMIT = 20000;

	public static String PROXY = "192.161.166.99";

	public static String PORT = "8800";
	
	public static String PROXY_SITE = "http://www.us-proxy.org/";
	
	public static boolean newProxy = false;
	
	public static boolean first = true;
	
	public static String filepath = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\bills\\";
	
	public static ArrayList<Pair> mPairs;
	
	public static final int MAX_PROXY_RECURSION = 5;

	public static void main(String [] args){
		
		System.setProperty("http.agent", "");

		mPairs = ProxyUtil.getProxies(PROXY_SITE);

		ArrayList<Pair> dateRanges = pullAllBillXML(111,"hr");
		
		for(Pair dateRange: dateRanges){
			try {
				System.out.println("Pair: " + dateRange.left +":"+ dateRange.right);
				pullBillXML(111, "hr", dateRange.left, dateRange.right);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
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
		
		BufferedReader in = getProxyInputStream(url);
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
	
	public static BufferedReader getProxyInputStream(String url){
		return getProxyInputStream(url, 0);
	}
	
	public static BufferedReader getProxyInputStream(String url, int count){
		
		if(count >= BillUtil.MAX_PROXY_RECURSION){
			return null;
		}
		
		try{
			
			URL oracle = new URL(url);
			
			URLConnection yc = oracle.openConnection(
					new Proxy(Proxy.Type.HTTP, 
							new InetSocketAddress(PROXY,Integer.valueOf(PORT))));
			
			yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			return new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			
		} catch(IOException ioe) {
			System.out.println("Caught IOE: " + ioe);
			ioe.printStackTrace();

			Pair mPair = mPairs.get(0);
			mPairs.remove(0);

			PROXY = mPair.getLeft();
			PORT = mPair.getRight();
			
			newProxy = true;
			
			System.out.println("Using proxy and port: " + PROXY + ", " + PORT);
			
			return getProxyInputStream(url, count +1);
		}
		
	}
	
	public static void pullBillXML(int congress, String code, String startRange, String endRange) throws Exception{
		
		BufferedReader in = null;
		
		try{
			System.setProperty("http.agent", "");
			System.setProperty("http.proxyHost", PROXY);
			System.setProperty("http.proxyPort", PORT);
			
			String mUrl = getBillPage(congress, code, startRange, endRange);

			in = getProxyInputStream(mUrl);
			
			String inputLine;
			
			ArrayList<String> urlList = new ArrayList<String>();
			
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.contains(">Text<")){
					
					int startIndex = inputLine.indexOf("href=") + 6;
					int endIndex = inputLine.indexOf("target=")-2;
					
					String ref = inputLine.substring(startIndex, endIndex);
					
					System.out.println("ref: " + ref);
					
					urlList.add(ref);
				}
			}
			
			for(String url : urlList){
				System.out.println("writing url: " + url);
				if(!doesBillExist(getBillNameFromRef(url))){
					System.out.println("writing file for url: " + url + ".");
					writeBillFile(filepath + getBillNameFromRef(url), url);
					System.out.println("sleeping : " + RATE_LIMIT);
					Thread.sleep(RATE_LIMIT);
				} else{
					System.out.println("file for url: " + url + " exists.");
				}
			}
			
		}
		finally{
			if(in != null){
				in.close();
			}
		}
	}
	
	public static boolean doesBillExist(String code){
		File f = new File(filepath + code);
		return f.exists();
	}
	
	public static String getBillNameFromRef(String ref){
		int indexStart = ref.indexOf("/html/") + 6;
		return ref.substring(indexStart);
	}
	
    public static void writeBillFile(String path, String url) throws Exception {
    	
        BufferedReader in = getProxyInputStream(url);
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        String inputLine;
        while ((inputLine = in.readLine()) != null){
            try{
                writer.write(inputLine);
            }
            catch(IOException e){
                e.printStackTrace();
                return;
            }
        }
        in.close();
        writer.close();
    }
}