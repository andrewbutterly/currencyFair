package org.cf.trade.acceptance;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.List;

import org.cf.trade.consume.messages.TradeRequest;
import org.cf.trade.feed.messages.MarketTrade;
import org.cf.trade.feed.messages.MarketUpdate;
import org.cf.trade.feed.messages.TradeProcessed;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

/**
 * Stepdefs for the acceptance test. inspection is a little clumsy
 * */

public class Stepdefs {
			
	private static String baseUrl = "http://localhost:8080"; 
				
	@Given("^Trade Request Sent - sell: \"([^\"]*)\" rate: \"([^\"]*)\", currency from: \"([^\"]*)\", currency to: \"([^\"]*)\", country: \"([^\"]*)\"$")
	public void trade_Request_Sent_sell_rate_currency_from_currency_to_country(String amountSellStr, String rateStr, 
			String currencyFrom, String currencyTo, String originatingCountry) throws Throwable {
		
		Double amountSell = Double.parseDouble(amountSellStr);
		Double rate = Double.parseDouble(rateStr);
		
		TradeRequest request = new TradeRequest("1", currencyFrom, currencyTo,
				amountSell, (amountSell*rate), rate,
				new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()),
				originatingCountry);
		
		RestTemplate restTemplate = new RestTemplate();		
		restTemplate.postForObject(baseUrl+"/trade", request, ResponseEntity.class);			
	}
	
	@Then("^The service holds at least that trade in value - sell: \"([^\"]*)\" rate: \"([^\"]*)\", currency from: \"([^\"]*)\", currency to: \"([^\"]*)\", country: \"([^\"]*)\"$")
	public void the_service_holds_at_least_that_trade_in_value_sell_rate_currency_from_currency_to_country(String amountSellStr, String rateStr, 
			String currencyFrom, String currencyTo, String originatingCountry) throws Throwable {
		
		Double amountSell = Double.parseDouble(amountSellStr);
		Double rate = Double.parseDouble(rateStr);
					
		RestTemplate restTemplate = new RestTemplate();	
									
		ResponseEntity<List<MarketUpdate>> updateResponse = restTemplate.exchange(baseUrl+"/feed/update/init", HttpMethod.GET, null, new ParameterizedTypeReference<List<MarketUpdate>>() {});		
		assertTrue( updateResponse != null );
		List<MarketUpdate> update = updateResponse.getBody(); 			
		assertTrue( update != null );
		assertTrue( !update.isEmpty() );
		
		boolean success = false;
		for(MarketUpdate market:update){
			if(market.getMarket().equals(currencyFrom+"-"+currencyTo) && !market.getMarketTrades().isEmpty()){
				for(MarketTrade trades:market.getMarketTrades()){
					if(trades.getRate() == rate && trades.getAmount() >= amountSell){
						success = true;
						break;
					}
				}
			}
		}	
		assertTrue( success );
		
		success = false;		
				
		ResponseEntity<List<TradeProcessed>> infoResponse = restTemplate.exchange(baseUrl+"/feed/info/init", HttpMethod.GET, null, new ParameterizedTypeReference<List<TradeProcessed>>() {});		
		assertTrue( infoResponse != null );		
		List<TradeProcessed> info = infoResponse.getBody(); 			
		assertTrue( info != null );
		assertTrue( !info.isEmpty() );
		
		for(TradeProcessed country:info){
			if(country.getOriginatingCountry().equals(originatingCountry) && country.getCulmativeAmount() >= amountSell){
				success = true;
				break;
			}
		}
		assertTrue( success );
	}
	
}
