package utils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import objects.Article;

public class ViewUtil {

	public static ArrayList<Article> articles = new ArrayList<Article>();

	public static void main(String [] args){
		
		
		
		generateCsvFile("C:\\Users\\wspride\\Desktop\\references\\education2");
		//save();
	}
	
	private static void generateCsvFile(String filename){
		String outputFilename = filename+"output.csv";
		String inputFilename = filename+".tmp";
		String titleFilename = filename+"titles.txt";
		generateCsvFile(outputFilename, inputFilename, titleFilename);
	}

	private static void generateCsvFile(String oFileName, String iFilename, String tFilename)
	{
		try
		{
			FileWriter writer = new FileWriter(oFileName);
			FileWriter tWriter = new FileWriter(tFilename);
			
			load(iFilename);
			
			for(int i =0; i< articles.size();i++){
				Article mart = articles.get(i);
				String title = mart.getTitle();
				//System.out.println(mart.getTitle());
				String builder = "";
				ArrayList<Article> citers = mart.getCiters();
				for(int j=0; j<articles.size(); j++){
					if(myContains(articles.get(j),citers)){
						builder=builder+"1";
					} else{
						builder=builder+"0";
					}
					builder=builder+",";
				}
				builder=builder.substring(0, builder.length()-1);
				
				writer.append(builder);
				writer.append("\n");
				
				tWriter.append(title);
				tWriter.append("\n");
				
				System.out.println(builder);
			}

			//generate whatever data you want

			writer.flush();
			writer.close();
			
			tWriter.flush();
			tWriter.close();
			
			System.out.println("done");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		} 
	}

	public static void printCitations(ArrayList<Article> arts){
		for(int i =0; i< arts.size();i++){
			Article mart = arts.get(i);
			System.out.println(mart.getTitle());
			ArrayList<Article> citers = mart.getCiters();
			for(int j=0; j<citers.size(); j++){
				System.out.println("	-	" + citers.get(j));
			}

		}
	}

	public static void printTitles(ArrayList<Article> arts){
		for(int i =0; i< arts.size();i++){
			System.out.println(arts.get(i));
		}
	}

	public static void printMatrix(ArrayList<Article> arts){
		System.out.println(arts.size());
		for(int i =0; i< arts.size();i++){
			Article mart = arts.get(i);
			String builder = "";
			ArrayList<Article> citers = mart.getCiters();
			for(int j=0; j<arts.size(); j++){
				if(myContains(arts.get(j),citers)){
					builder=builder+"1";
				} else{
					builder=builder+"0";
				}
			}
			System.out.println(builder);
		}
	}

	public static int articleIndex(Article ma){
		return articles.indexOf(ma);
	}

	public static boolean myContains(Article ma, ArrayList<Article> mas){
		for(int i=0; i< mas.size(); i++){
			if(mas.get(i).isThisArticle(ma)){
				return true;
			}
		}
		return false;
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
			fis = new FileInputStream("geology.tmp");
			ObjectInputStream ois = new ObjectInputStream(fis);
			articles = (ArrayList<Article>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void load(String fn){
		FileInputStream fis;
		try {
			fis = new FileInputStream(fn);
			ObjectInputStream ois = new ObjectInputStream(fis);
			articles = (ArrayList<Article>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
