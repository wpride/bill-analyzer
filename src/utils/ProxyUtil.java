package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ProxyUtil {	
	
	public static class Pair{
		String left;
		String right;
		
		public Pair(String l, String r){
			left = l;
			right = r;
		}
		
		public String getLeft(){
			return left;
		}
		
		public String getRight(){
			return right;
		}
		
		public String toString(){
			return "Left: " + left + ", right: " + right;
		}
	}
	
	public static class IntPair{
		int left;
		int right;
		
		public IntPair(int l, int r){
			left = l;
			right = r;
		}
		
		public int getLeft(){
			return left;
		}
		
		public int getRight(){
			return right;
		}
	}
	
	public static void main(String [] args){
		getProxies("http://www.us-proxy.org/");
	}
	
	public static ArrayList<Pair> getProxiesFromFile(){
		String csvFile = "C:\\Users\\wspride\\Desktop\\references\\Referencer\\lib\\proxies.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		ArrayList<Pair> mPairs = new ArrayList<Pair>();
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] country = line.split(cvsSplitBy);
	 
				System.out.println("Proxy: " + country[0] 
	                                 + " , port " + country[1] + "]");
				
				Pair mPair = new Pair(country[0], country[1]);
				mPairs.add(mPair);
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	 
		System.out.println("Done");
		return mPairs;
	}
	
	public static ArrayList<Pair> getProxies(String proxysite){
		
		String ValidIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		
		try{
		ArrayList<Pair> mPairs = new ArrayList<Pair>();
		
		BufferedReader in = null;
		
		URL oracle = new URL(proxysite);
		URLConnection yc = oracle.openConnection();
		yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
		in = new BufferedReader(new InputStreamReader(
				yc.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			if(inputLine.contains("<tr><td>")){
				int index1 = inputLine.indexOf("<tr><td")+8;
				String sub1 = inputLine.substring(index1);
				int index2 = sub1.indexOf("</td>");
				String IPADDR = sub1.substring(0, index2);
				System.out.println(IPADDR);
				
				int index3 = sub1.indexOf("<td>")+4;
				String sub2 = sub1.substring(index3);
				int index4 = sub2.indexOf("</td>");
				String PORT = sub2.substring(0,index4);
				
				Pair mPair = new Pair(IPADDR, PORT);
				mPairs.add(mPair);
				
				System.out.println(PORT);
				
			}
		}
		
		return mPairs;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
}
