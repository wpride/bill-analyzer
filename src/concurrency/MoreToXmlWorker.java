package concurrency;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import utils.BillUtil;
import utils.ProxyUtil.Pair;

public class MoreToXmlWorker implements Runnable {

	private final BlockingQueue<String> inputUrlQueue;
	private final BlockingQueue<String> outputUrlQueue;
	private final BlockingQueue<Pair> proxyQueue;
	private Pair mPair;

	
	
	public MoreToXmlWorker(BlockingQueue<String> inputUrlQ, BlockingQueue<String> outputUrlQ, BlockingQueue<Pair> proxyQ) { 
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
			System.out.println("BillUrlWriter produced exception: " + ex);
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
			String ret = BillUtil.getXmlUrl(url, BillUtil.getProxyInputStream(url, mPair));
			outputUrlQueue.add(ret);
			if(true){
				Thread.sleep(BillUtil.RATE_LIMIT);
			}
		} 
		catch(IOException ioe){
			System.out.println("BillUrlWriter consumption produced IOException: " + ioe);
			inputUrlQueue.add(url);
			mPair = proxyQueue.remove();
		} 
		catch (Exception e) {
			System.out.println("BillUrlWriter produced consumption produced exception: " + e);
			e.printStackTrace();
			inputUrlQueue.add(url);
		}
		
	}
}
