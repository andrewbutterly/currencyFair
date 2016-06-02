package org.cf.trade.load;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cf.trade.feed.messages.TradeProcessed;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * simple load test, to run against a running server
 *  
 * creates the same trade THEAD_POOL*ITERATIONS_PER_POOL times, then loads the trades from to make sure they were all created
 * 
 * NOTE: has to be run against a *newly started* server, to prevent old data from interfering with the test assertions
 * */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({})
public class LoadTestPerformer {
	
	private static Integer THEAD_POOL = 20;
	private static Integer THREADS = 1_000;
	private static String  BASE_URL = "http://localhost:8080";	
	
	@Test
	public void run(){
		
		double amountSell = 1.00;		
		double rate = 2.00;
		String currencyFrom = "EUR";
		String currencyTo = "USD";
		String originatingCountry = "US";
		
		RequestSender sender = new RequestSender();
		
		System.out.println(String.format("starting %d threads", THEAD_POOL));
		
		ExecutorService executor = Executors.newFixedThreadPool(THEAD_POOL);
        for (int i = 0; i < THREADS; i++) {
            Runnable worker = RequestSender.makeTradeRequest(BASE_URL+"/trade", amountSell, rate, currencyFrom, currencyTo, originatingCountry);
            executor.execute(worker);
          }
        executor.shutdown();
        
        //give the code time to chug through lagging events	
        while (!executor.isTerminated()) {
        }                              					
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {			
			//do nothing
		}
        System.out.println("Finished submitting...verifying results");
        
        List<TradeProcessed> info = sender.getTradesProcessed(BASE_URL+"/feed/info/init"); 			
		assertTrue( info != null );
		assertTrue( !info.isEmpty() );
		assertTrue( info.get(0).getOriginatingCountry().equals(originatingCountry) );			
		assertTrue( info.get(0).getCulmativeAmount() == (amountSell*THREADS) );
			      
	}

}
