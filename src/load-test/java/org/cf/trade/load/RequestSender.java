package org.cf.trade.load;

import java.text.SimpleDateFormat;
import java.util.List;

import org.cf.trade.consume.messages.TradeRequest;
import org.cf.trade.feed.messages.TradeProcessed;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Generate and send a trade request. inspect for results
 * */
public class RequestSender {
		
	public static Runnable makeTradeRequest(String url, double amountSell, double rate, 
			String currencyFrom, String currencyTo, String originatingCountry){
		return () -> {
			try{
				new RestTemplate().postForObject(url, new TradeRequest("1", currencyFrom, currencyTo,
						amountSell, (amountSell*rate), rate,
						new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()),
						originatingCountry), ResponseEntity.class);
			}catch(Exception e){
				e.printStackTrace();
				
			}
		} ;
		
	}
	
	public List<TradeProcessed> getTradesProcessed(String url){
		ResponseEntity<List<TradeProcessed>> infoResponse = new RestTemplate().exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<TradeProcessed>>() {});
		return infoResponse.getBody();
	}
				
}
