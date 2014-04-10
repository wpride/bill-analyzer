package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import objects.Article;

public class ViewUtil {

	public static ArrayList<Article> articles = new ArrayList<Article>();
	
	public static void main(String [] args){
		load();
		Article mart = articles.get(0);
		System.out.println(mart.getCiters().size());
		
	}
	
	public static void printTree(){
		load();
		Article mart = articles.get(0);
		System.out.println(mart.toString());
		ArrayList<Article> marts = mart.getCiters();
		for(int i=0;i<marts.size(); i++){
			System.out.println("	"+marts.get(i).toString());
			ArrayList<Article> marts2 = mart.getCiters();
			for(int j=0;j<marts2.size(); j++){
				System.out.println("		" + marts2.get(j).toString());
			}
		}
	}
	
	public static void load(){
		FileInputStream fis;
		try {
			fis = new FileInputStream("t.tmp");
			ObjectInputStream ois = new ObjectInputStream(fis);
			articles = (ArrayList<Article>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
