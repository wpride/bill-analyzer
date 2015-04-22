package xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.yaml.snakeyaml.Yaml;

public class LegislatorCsvTools {
	
	private ArrayList<Map> main;
	
	public LegislatorCsvTools(){
		try {
			main = LegislatorCsvTools.getYaml();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getParty(String key){
		try {
			return LegislatorCsvTools.getParty(key, main);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		}
		return "null";
	}
	
	public static ArrayList<Map> getYaml() throws FileNotFoundException{
		Yaml yaml = new Yaml();
		String filepath = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\congresses\\legislators-current.yaml";
		
		InputStream input = new FileInputStream(new File(filepath));
		
		ArrayList<Map> aList = (ArrayList<Map>)yaml.load(input);

		return aList;
	}
	
	public static String getParty(String key, ArrayList<Map> mMap) throws FileNotFoundException{
		
		for(Map m: mMap){
			Map idMap = (Map) m.get("id");
			String n = (String) idMap.get("bioguide");
			if(n.equals(key)){
				ArrayList<Map> termsMap = (ArrayList<Map>) m.get("terms");
				Map termMap = termsMap.get(0);;
				return (String)termMap.get("party");
			}
		}
		return "ret";
	}

	public static HashMap<String, String> getLegislators() {

		String csvFile = "C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\congresses\\legislators.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		HashMap<String, String> ret = new HashMap<String, String>();

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] columns = line.split(cvsSplitBy);

				String lastName = columns[0];
				String firstName = columns[1];
				String party="";
				try{
					party = columns[7];
				} catch(ArrayIndexOutOfBoundsException e){
					//
				}
				
				ret.put(firstName + " " + lastName, party);
				
				System.out.println(firstName + " " + lastName + ": " + party);

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

		return ret;
	}

	public static String[] getMembers(String path) throws IOException{

		ArrayList<String> liList = new ArrayList<String>();
		String[] liArray;

		File input = new File(path);
		Document doc = Jsoup.parse(input, "UTF-8");

		Elements tables = doc.getElementsByTag("table");

		for(int h = 0; h< tables.size(); h++){

			Element table = tables.get(h);

			Elements trs = table.getElementsByTag("tr");

			for(int i = 0 ; i< trs.size(); i++){
				Element tr = trs.get(i);
				Elements tds = tr.getElementsByTag("td");
				for(int j = 0 ; j< tds.size(); j++){
					Element td = tds.get(j);
					Elements as = td.getElementsByTag("a");
					for(int k = 0 ; k< as.size(); k++){
						Element a = as.get(k);
						System.out.println("a: " + a.text());
					}
				}
			}
		}

		liArray = new String[liList.size()];

		for(int i=0; i< liList.size(); i++){
			liArray[i] = liList.get(i);
		}

		return liArray;
	}
}
