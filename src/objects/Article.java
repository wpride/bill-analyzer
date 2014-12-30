package objects;

import java.io.Serializable;
import java.util.ArrayList;

import utils.PlosUtil;

public class Article implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String title;
	String citeURL;
	String articleURL;
	int numCitations;
	ArrayList<Article> citers = new ArrayList<Article>();
	Boolean citersSet = false;
	
	public Article(String t, String c, String aURL){
		title = t.replaceAll("\\<.*?\\>", "");;
		citeURL = c;
		articleURL = aURL;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getCiteURL(){
		return citeURL;
	}
	
	public String toString(){

		return title;
	}
	
	public void setNumberCitations(int c){
		numCitations = c;
	}
	
	public String citeURL(){
		return citeURL;
	}
	
	public boolean isThisArticle(Article art){
		return isThisArticle(art.getTitle());
	}
	
	private boolean citedByArticle(Article mArticle){
		for(int i=0; i<citers.size();i++){
			if(mArticle.isThisArticle(citers.get(i))){
				return true;
			}
		}
		return false;
	}
	
	private void addCiter(Article mArticle){
		citersSet = true;
		if(citedByArticle(mArticle)){
			return;
		}
		citers.add(mArticle);
	}
	
	private void addAll(ArrayList<Article> mArticles){
		for(int i=0;i<mArticles.size();i++){
			addCiter(mArticles.get(i));
		}
	}
	
	public void addCiters(ArrayList<Article> mArticles){
		citersSet = true;
		this.addAll(mArticles);
	}
	
	public ArrayList<Article> getCiters(){
		return citers;
	}
	
	public boolean citersSet(){
		return citersSet;
	}
	
	public void setCitedSet(boolean s){
		citersSet = s;
		if(!s){
			citers = new ArrayList<Article>();
		}
	}
	
	public boolean isThisArticle(String testTitle){
		
		String mTitle = this.title.replace("+", "").replace(" ", "");
		String tTitle = testTitle.replace("+", "").replace(" ", "");
		
		return computeLevenshteinDistance(mTitle, tTitle) < PlosUtil.lev_min;
	}
	
	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}
 
	public static int computeLevenshteinDistance(String str1,String str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];
 
		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;
 
		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
 
		return distance[str1.length()][str2.length()];    
	}
	
	public boolean equals (Article a){
		return isThisArticle(a.getTitle());
	}
}