package concurrency;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;

import utils.BillUtil;
import utils.ProxyUtil.Pair;

public class MoreToXmlWorker implements Runnable {

	private final BlockingQueue<String> inputUrlQueue;
	private final BlockingQueue<String> outputUrlQueue;
	private final BlockingQueue<Pair> proxyQueue;
	private Pair mPair;

	
	
	public MoreToXmlWorker(BlockingQueue<Pair> proxyQ, BlockingQueue<String> inputUrlQ, BlockingQueue<String> outputUrlQ) { 
		inputUrlQueue = inputUrlQ;
		outputUrlQueue = outputUrlQ;
		proxyQueue = proxyQ;
		mPair = proxyQ.remove();
	}

	public void run() {
		try {
			while (true) { 
				consume(inputUrlQueue.take()); 			
			}
		} catch (InterruptedException ex) {
			System.out.println("MoreToXMLWorker produced exception: " + ex);
			ex.printStackTrace();
		}
	}

	/**
	 * Consume a link to a "More page", add to output queue the link to the XML pages
	 * 
	 * @param url the link to the "more" page
	 */
	
	void consume(String url) { 
		
		try {
			
			System.out.println("MoreToXML using URL: " + url);
			
			String ret = BillUtil.getXmlUrl(url, BillUtil.getProxyInputStream(url, mPair));
			
			System.out.println("MoreToXML added " + ret + " URLs with url: " + url);
			
			outputUrlQueue.add(ret);
			if(true){
				Thread.sleep(BillUtil.RATE_LIMIT);
			}
		} 
		catch(IOException ioe){
			System.out.println("MoreToXMLWorker consumption produced IOException: " + ioe);
			inputUrlQueue.add(url);
			try {
				mPair = proxyQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParseException pe){
			System.out.println("Couldn't find XML! in URL : " + url);
			try {
				mPair = proxyQueue.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			System.out.println("MoreToXMLWorker produced consumption produced exception: " + e);
			e.printStackTrace();
			inputUrlQueue.add(url);
		}
		
	}
}
