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
import utils.ProxyUtil.Pair;

public class PlosUtil {

	public static int lev_min = 5;

	public static int RATE_LIMIT = 20000;

	public static String filename = "C:\\Users\\wspride\\Desktop\\references\\education2.tmp";

	public static String PROXY = "192.161.166.99";

	public static String PORT = "8800";

	public static String TITLE = "The cult of efficiency in education: Comparative reflections on the reality and the rhetoric";
	
	public static int MAX_ITERATION = 100;

	public static int CITE_DEPTH_LIMIT = 50;
	
	public static String PROXY_SITE = "http://www.us-proxy.org/";
	
	public static boolean newProxy = false;
	
	public static boolean first = true;

	public static void main(String [] args){

		ArrayList<Pair> mPairs = ProxyUtil.getProxies(PROXY_SITE);
		
		for(int i =0; i< mPairs.size(); i++){
			System.out.println("pair : " + i + " is: " + mPairs.get(i).getLeft().toString());
		}

		while(mPairs.size() > 0){

			try{
				generateFromTitle(filename);
			}catch(IOException e){
				System.out.println("Caught IOE: ");
				e.printStackTrace();

				Pair mPair = mPairs.get(0);
				mPairs.remove(0);

				PROXY = mPair.getLeft();
				PORT = mPair.getRight();
				
				newProxy = true;
				
				System.out.println("Using proxy and port: " + PROXY + ", " + PORT);
			}
		}
	}

	public static void addAllArticlesSafe(ArrayList<Article> mArticles,ArrayList<Article> nArticles){
		for(int i=0; i<mArticles.size();i++){
			addArticleSafe(mArticles.get(i), nArticles);
		}
	}

	public static void addAllArticlesSafe(ArrayList<Article> mArticles,ArrayList<Article> nArticles, ArrayList<Article> pArticles){
		for(int i=0; i<mArticles.size();i++){
			addArticleSafe(mArticles.get(i), nArticles, pArticles);
		}
	}

	public static void addArticleSafe(Article mArticle, ArrayList<Article> mArticles, ArrayList<Article> nArticles){
		for(int i=0;i<mArticles.size();i++){
			if(mArticle.isThisArticle(mArticles.get(i))){
				return;
			}
		}
		for(int i=0;i<nArticles.size();i++){
			if(mArticle.isThisArticle(nArticles.get(i))){
				return;
			}
		}
		mArticles.add(mArticle);
		System.out.println("add article safe: " + mArticles.size());
	}

	public static void addArticleSafe(Article mArticle, ArrayList<Article> mArticles){
		for(int i=0;i<mArticles.size();i++){
			if(mArticle.isThisArticle(mArticles.get(i))){
				return;
			}
		}
		mArticles.add(mArticle);
	}

	public static void generateFromTitle(String filename) throws IOException{
		ArrayList<Article> mArticlesTemp =load(filename + ".t");
		ArrayList<Article> mainArticles = load(filename);
		if(mainArticles == null){
			mainArticles = new ArrayList<Article>();
		}
		if(mArticlesTemp == null || mArticlesTemp.size()<1){
			mArticlesTemp = new ArrayList<Article>();
			Article mArticle;
			try {
				mArticle = getArticle(mainArticles, TITLE);
				mArticlesTemp.add(mArticle);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("main articles size is: " + mainArticles.size());
		System.out.println("main articles tmep size is: " + mArticlesTemp.size());
		try{

			int counter=0;

			while(mArticlesTemp.get(0)!= null && counter < MAX_ITERATION){
				System.out.println("main articles size is: " + mainArticles.size());

				Article currentArticle = mArticlesTemp.get(0);

				System.out.println("current article is: " + currentArticle);

				System.out.println("articlesTemp is: " + mArticlesTemp.size());

				ArrayList<Article> citers = getCitingArticles(mainArticles, currentArticle);

				if(citers.size() < 1 && !currentArticle.citeURL().equals("null")){
					currentArticle.setCitedSet(false);
					throw new IOException("think we're returning blanks here...");
				}

				System.out.println("citers size is: " + citers.size());

				currentArticle.addCiters(citers);
				mArticlesTemp.addAll(citers);

				addAllArticlesSafe(citers,mainArticles,mArticlesTemp);
				addArticleSafe(currentArticle,mainArticles);

				mArticlesTemp.remove(0);

				System.out.println("articlesTemp is: " + mArticlesTemp.size());
				counter++;
				System.out.println("counter: " + counter);
				
				ViewUtil.printMatrix(mainArticles);
				
				save(filename, mainArticles);
				save(filename + ".t", mArticlesTemp);
				save(filename+"x", mainArticles);
				save(filename + ".tx", mArticlesTemp);
				//ViewUtil.printMatrix(mainArticles);
			}

		} catch(IOException ioe){
			System.out.println("caught IO");
			throw ioe;
		} catch(Exception e){
			System.out.println("catching e and proceeding");
			e.printStackTrace();
		}
		save(filename, mainArticles);
		save(filename + ".t", mArticlesTemp);
		ViewUtil.printMatrix(mainArticles);
		//ViewUtil.printTitles(mainArticles);
		//ViewUtil.printCitations(mainArticles);
	}

	public static void save(String fname, ArrayList<Article> mArticles){
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fname);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(mArticles);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
		}
	}

	public static ArrayList<Article> load(String fname){
		FileInputStream fis;
		try {
			fis = new FileInputStream(fname);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList<Article> articles = (ArrayList<Article>) ois.readObject();
			ois.close();
			return articles;
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Article lookupArticle(ArrayList<Article> articles, String mTitle){
		for(int i=0;i<articles.size();i++){
			Article currentArticle = articles.get(i);
			if(currentArticle.isThisArticle(mTitle)){
				System.out.println("match! : " + mTitle);
				return currentArticle;
			}
		}
		return null;
	}

	public static Article addOrMakeArticle(ArrayList<Article> articles, String mTitle, String citeURL, String aURL){
		for(int i=0;i<articles.size();i++){
			Article currentArticle = articles.get(i);
			if(currentArticle.isThisArticle(mTitle)){
				System.out.println("match! : " + mTitle);
				return currentArticle;
			}
		}
		System.out.println("make! : " + mTitle);
		Article mArticle = new Article(mTitle, citeURL, aURL);
		//articles.add(mArticle);
		return mArticle;
	}

	public static Article getArticle(ArrayList<Article> articles, String articleTitle) throws Exception {

		if(lookupArticle(articles, articleTitle) != null){
			return lookupArticle(articles, articleTitle);
		}

		articleTitle = prepTitle(articleTitle);

		BufferedReader in = null;

		try{
			System.setProperty("http.agent", "");
			System.setProperty("http.proxyHost", PROXY);
			System.setProperty("http.proxyPort", PORT);

			String mURL = "http://scholar.google.com/scholar?hl=en&q=" + articleTitle + "+&btnG=&as_sdt=0,22&as_sdtp=";

			URL oracle = new URL(mURL);
			URLConnection yc = oracle.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				Article mArticle = processTitle(articles, inputLine);
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

	public static Article processTitle(ArrayList<Article> articles, String inputLine){
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

			String citedby = inputLine.substring(indexStart2,indexStart2+20);
			System.out.println("cited by: "  +citedby);

			int indexTest = inputLine.indexOf("class=\"gs_rt\"");

			String citeURL = "null";

			if(indexStart2 < indexTest){
				;
				int indexStartHref = inputLine.indexOf("a href=\"/scholar?cites=") + 9;
				int indexEndHref = inputLine.indexOf("Cited by") - 1;

				citeURL = scholarPrepend(inputLine.substring(indexStartHref, indexEndHref));

			}

			Article mArticle = addOrMakeArticle(articles, mTitle, citeURL, mLink);

			return mArticle;
		}

		return null;
	}

	public static String scholarPrepend(String relativePath){
		return "http://scholar.google.com/" + relativePath;
	}

	public static void processLine(ArrayList<Article> articles, ArrayList<Article> iter, String inputLine){

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

				Article mArticle = addOrMakeArticle(articles, mTitle, citeURL, mLink);

				iter.add(mArticle);
				processLine(articles, iter, inputLine.substring(indexEnd1));
			} 
		}catch(StringIndexOutOfBoundsException sie){
			System.out.println("couldn't parse");
		}

		return;

	}

	public static String prepTitle(String title){
		return title.replace(" ", "+");
	}

	public static ArrayList<Article> getCitingArticles(ArrayList<Article> articles, Article ref) throws Exception {

		String citeURL = ref.getCiteURL();
		if(ref.citersSet()){
			System.out.println("This shits casshedd");
			return new ArrayList<Article>();
		}

		ArrayList<Article> mArticles = getCitingArticles(articles, citeURL, new ArrayList<Article>());
		ref.addCiters(mArticles);

		return mArticles;
	}

	public static ArrayList<Article> getCitingArticles(ArrayList<Article> articles, String citeURL, ArrayList<Article> mArticles) throws Exception {

		BufferedReader in = null;

		if(citeURL == null || citeURL.equals("null")){
			System.out.println("no citations for this guy!");
			return mArticles;
		}

		try{
			System.out.println("sleep start, mArticles size:" + mArticles.size());
			if(mArticles.size() >= CITE_DEPTH_LIMIT){
				return mArticles;
			}
			if(!newProxy){
				Thread.sleep(RATE_LIMIT);
			}
			else{
				newProxy = false;
			}
			System.out.println("sleep over");

			System.setProperty("http.agent", "");

			System.setProperty("http.proxyHost", PROXY);
			System.setProperty("http.proxyPort", PORT);

			URL oracle = new URL(citeURL);
			URLConnection yc = oracle.openConnection();
			yc.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;
			String nextRef = "null";
			while ((inputLine = in.readLine()) != null) {
				processLine(articles, mArticles, inputLine);
				if(inputLine.contains("px\">Next<")){
					String mLine = inputLine.substring(0, inputLine.indexOf(">Next<"));
					int startIndex = mLine.lastIndexOf("href=") + 6;
					int endIndex = mLine.substring(startIndex).indexOf(">") + startIndex;
					nextRef = scholarPrepend(mLine.substring(startIndex, endIndex)).replace("amp;", "");
				}
			}

			if(!nextRef.equals("null")){
				getCitingArticles(articles, nextRef, mArticles);
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
