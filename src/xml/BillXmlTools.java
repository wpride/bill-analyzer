	package xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BillXmlTools {
	
	public static void main(String[] args){
		try {
			
			LegislatorCsvTools tools = new LegislatorCsvTools();
			File dir = new File("C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\bills\\xml2");
			File[] bills = dir.listFiles();
			
			for(File bill: bills){
				String text = getText(bill.getAbsolutePath());
				int polarity = getPolarity(bill.getAbsolutePath(), tools);
				System.out.println("Polarity of: " + bill.toString() + " polarity: " + polarity);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getPolarity(String path, LegislatorCsvTools tools) throws IOException{
		
		String[] bioGuide = getBioguides(path);
		
		int demCount = 0;
		int repCount = 0;
		
		for(int i=0; i<bioGuide.length; i++){
			String party = tools.getParty(bioGuide[i]);
			if(party.equals("Democrat")){
				demCount++;
			} else if(party.equals("Republican")){
				repCount++;				
			}
		}
		
		System.out.println("Dem/Rep:" + demCount + "/" + repCount);
		
		if(demCount*10000/(repCount * 100 + 1) > 75){
			System.out.println("Democratic");
			return 1;
		} else if( repCount*10000/(demCount * 100 + 1) > 75){
			System.out.println("Republican");
			return -1;
		}
		System.out.println("Neutral");
		return 0;
	}
	
	public static String getText(String path) throws IOException{
		
		String ret = "";
		
		File input = new File(path);
		Document doc = Jsoup.parse(input, "UTF-8");
		
		Elements texts = doc.getElementsByTag("text");
		
		for(Element text: texts){
			ret = ret + text.text();
		}
		
		System.out.println(ret);
		
		return ret;
	}
	
	public static String[] getSponsors(String path) throws IOException{
		
		ArrayList<String> allSponsors = new ArrayList<String>();
		
		File input = new File(path);
		Document doc = Jsoup.parse(input, "UTF-8");
		
		Elements sponsor = doc.getElementsByTag("sponsor");
		Elements cosponsors = doc.getElementsByTag("cosponsor");
		
		for(Element spon: sponsor){
			allSponsors.add(spon.text());
		}
		for(Element cospon: cosponsors){
			allSponsors.add(cospon.text());
		}
		
		String[] ret = new String[allSponsors.size()];
		
		for(int i=0; i< allSponsors.size(); i++){
			ret[i] = allSponsors.get(i);
		}
		
		return ret;
	}
	
	public static String[] getBioguides(String path) throws IOException{
		
		ArrayList<String> allBioguides = new ArrayList<String>();
		
		File input = new File(path);
		Document doc = Jsoup.parse(input, "UTF-8");
		
		Elements sponsor = doc.getElementsByTag("sponsor");
		Elements cosponsors = doc.getElementsByTag("cosponsor");
		
		for(Element spon: sponsor){
			allBioguides.add(spon.attr("name-id"));
		}
		for(Element cospon: cosponsors){
			allBioguides.add(cospon.attr("name-id"));
		}
		
		String[] ret = new String[allBioguides.size()];
		
		for(int i=0; i< allBioguides.size(); i++){
			ret[i] = allBioguides.get(i);
		}
		
		return ret;
	}
}
