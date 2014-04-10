package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import objects.Article;

public class PlosUtil {

	public static int lev_min = 10;
	
	public static String filename = "C:\\Users\\wspride\\Desktop\\references\\Referencer\\w.tmp";
	
	public static ArrayList<Article> articles = new ArrayList<Article>();

	public static void main(String [] args){	
		load();
		try{
			//http://scholar.google.com/scholar?hl=en&q=Fission+Yeast+Cells+Undergo+Nuclear+Division+in+the+Absence+of+Spindle+Microtubules+&btnG=&as_sdt=1%2C22&as_sdtp=","");
			Article mArticle = getArticle("Images of technology pieces of butt");
			System.out.println("mArticle: " + mArticle.citeURL());
			ArrayList<Article> citers = getCitingArticles(mArticle);
			mArticle.addCiters(citers);
			for(int i =0; i< 1; i++){
				Article currentArticle = articles.get(i);
				System.out.println("article: " + currentArticle);
				ArrayList<Article> citers2 = getCitingArticles(currentArticle);
				mArticle.addCiters(citers2);
			}
			save();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static void save(){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(articles);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Article> load(){
		FileInputStream fis;
		try {
			fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			articles = (ArrayList<Article>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return articles;
	}
	
	public static Article lookupArticle(String mTitle){
		for(int i=0;i<articles.size();i++){
			Article currentArticle = articles.get(i);
			if(currentArticle.isThisArticle(mTitle)){
				System.out.println("match! : " + mTitle);
				return currentArticle;
			}
		}
		return null;
	}
	
	public static Article addOrMakeArticle(String mTitle, String citeURL, String aURL){
		for(int i=0;i<articles.size();i++){
			Article currentArticle = articles.get(i);
			if(currentArticle.isThisArticle(mTitle)){
				System.out.println("match! : " + mTitle);
				return currentArticle;
			}
		}
		System.out.println("make! : " + mTitle);
		Article mArticle = new Article(mTitle, citeURL, aURL);
		articles.add(mArticle);
		return mArticle;
	}

	public static Article getArticle(String articleTitle) throws Exception {
		
		if(lookupArticle(articleTitle) != null){
			return lookupArticle(articleTitle);
		}
		
		articleTitle = prepTitle(articleTitle);

		BufferedReader in = null;

		try{
			System.setProperty("http.agent", "");

			String mURL = "http://scholar.google.com/scholar?hl=en&q=" + articleTitle + "+&btnG=&as_sdt=0,22&as_sdtp=";

			URL oracle = new URL(mURL);
			URLConnection yc = oracle.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				Article mArticle = processTitle(inputLine);
				if(mArticle != null){
					return mArticle;
				}
			}
			return null;
		}
		finally{
			if(in != null){
				in.close();
			}
		}
	}

	public static Article processTitle(String inputLine){
		if(inputLine.contains("class=\"gs_rt\"")){

			int indexTrim1 = inputLine.indexOf("<h3 class=\"gs_r");

			inputLine = inputLine.substring(indexTrim1);

			int indexStart = inputLine.indexOf("href=") + 5;
			int indexEnd = inputLine.substring(indexStart).indexOf("class=") + indexStart;

			String mLink = inputLine.substring(indexStart, indexEnd);

			int indexTrim = inputLine.indexOf("class=yC");

			inputLine = inputLine.substring(indexTrim);

			int indexStart1 = inputLine.indexOf(">") + 1;
			int indexEnd1 = inputLine.substring(indexStart1).indexOf("</a>") + indexStart1;


			String mTitle = inputLine.substring(indexStart1, indexEnd1);

			int indexStart2 = inputLine.indexOf("Cited by") + 9;
			int indexTest = inputLine.indexOf("class=\"gs_rt\"");

			String citeURL = "null";

			if(indexStart2 < indexTest){
				;
				int indexStartHref = inputLine.indexOf("a href=\"/scholar?cites=") + 9;
				int indexEndHref = inputLine.indexOf("Cited by") - 1;

				citeURL = scholarPrepend(inputLine.substring(indexStartHref, indexEndHref));

			}

			Article mArticle = addOrMakeArticle(mTitle, citeURL, mLink);

			return mArticle;
		}

		return null;
	}

	public static String scholarPrepend(String relativePath){
		return "http://scholar.google.com/" + relativePath;
	}

	public static void processLine(ArrayList<Article> iter, String inputLine){

		try{

			if(inputLine.contains("class=\"gs_rt\"") && inputLine.contains("class=yC")){

				int indexTrim1 = inputLine.indexOf("<h3 class=\"gs_r");

				inputLine = inputLine.substring(indexTrim1);

				int indexStart = inputLine.indexOf("href=") + 5;
				int indexEnd = inputLine.substring(indexStart).indexOf("class=") + indexStart;

				String mLink = inputLine.substring(indexStart, indexEnd);

				int indexTrim = inputLine.indexOf("class=yC");

				inputLine = inputLine.substring(indexTrim);

				int indexStart1 = inputLine.indexOf(">") + 1;
				int indexEnd1 = inputLine.substring(indexStart1).indexOf("</a>") + indexStart1;


				String mTitle = inputLine.substring(indexStart1, indexEnd1);

				int indexStart2 = inputLine.indexOf("Cited by") + 9;
				int indexTest = inputLine.indexOf("class=\"gs_rt\"");

				String citeURL = "null";

				if(indexStart2 < indexTest && inputLine.indexOf("Cited by") > 5){

					int indexStartHref = inputLine.indexOf("a href=\"/scholar?cites=") + 9;
					int indexEndHref = inputLine.indexOf("Cited by") - 1;

					citeURL = "http://scholar.google.com/" + inputLine.substring(indexStartHref, indexEndHref);

				}

				Article mArticle = addOrMakeArticle(mTitle, citeURL, mLink);

				iter.add(mArticle);
				processLine(iter, inputLine.substring(indexEnd1));
			} 
		}catch(StringIndexOutOfBoundsException sie){
			System.out.println("couldn't parse");
		}

		return;

	}
	
	public static String prepTitle(String title){
		return title.replace(" ", "+");
	}

	public static ArrayList<Article> getCitingArticles(Article ref) throws Exception {
		
		String citeURL = ref.getCiteURL();
		if(ref.citersSet()){
			System.out.println("This shits casshedd");
			return ref.getCiters();
		}
		
		ArrayList<Article> mArticles = getCitingArticles(citeURL, new ArrayList<Article>());
		ref.addCiters(mArticles);

		return mArticles;
	}

	public static ArrayList<Article> getCitingArticles(String citeURL, ArrayList<Article> mArticles) throws Exception {

		BufferedReader in = null;
		
		if(citeURL == null || citeURL.equals("null")){
			System.out.println("no citations for this guy!");
			return mArticles;
		}

		try{
			
			Thread.sleep(500);
			
			System.setProperty("http.agent", "");

			URL oracle = new URL(citeURL);
			URLConnection yc = oracle.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;
			String nextRef = "null";
			while ((inputLine = in.readLine()) != null) {
				processLine(mArticles, inputLine);
				if(inputLine.contains("px\">Next<")){
					String mLine = inputLine.substring(0, inputLine.indexOf(">Next<"));
					int startIndex = mLine.lastIndexOf("href=") + 6;
					int endIndex = mLine.substring(startIndex).indexOf(">") + startIndex;
					nextRef = scholarPrepend(mLine.substring(startIndex, endIndex)).replace("amp;", "");
				}
			}

			if(!nextRef.equals("null")){
				getCitingArticles(nextRef, mArticles);
			}

			return mArticles;
		}
		finally{
			if(in != null){
				in.close();
			}
		}
	}

}
