package xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CongressXmlTools {
	public static void main(String[] args){
		try {
			String[] members = getMembers("C:\\Users\\wspride\\Desktop\\912014\\references\\Referencer\\congresses\\111thsenators.html");

			for(String member: members){
				System.out.println("Member: " + member);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
