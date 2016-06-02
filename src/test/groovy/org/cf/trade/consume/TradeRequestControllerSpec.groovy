package org.cf.trade.consume

import java.text.SimpleDateFormat
import java.util.HashSet;
import java.util.List;

import org.cf.trade.ConfigurationSettings;
import org.cf.trade.consume.messages.TradeRequest
import org.cf.trade.process.messages.TradeEvent
import org.cf.trade.process.queue.TradeEventQueue;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

class TradeRequestControllerSpec extends spock.lang.Specification {

	TradeRequestController controller = new TradeRequestController();
	
	def configurationSettings
	def tradeEventQueue = Mock (TradeEventQueue)	
	TradeRequest tradeRequest
	
	def servletReq = Mock(HttpServletRequest)
	
	def setup(){
				
		configurationSettings = new ConfigurationSettings(
			['USD', 'EUR'], //supportedCurrencies
			['IE', 'US'], //supportedCountries
			2, //amountRounding
			1, //amountMin
			1000000, //amountMax
			4, //rateRounding
			1, //rateMin
			100000, //rateMax
			10, //timePlacedTTLMin
			"dd-MMM-yy HH:mm:ss", //timePlacedFormat
			"trades", //tradeQueueName 
			"feed" //feedQueueName
			)
		configurationSettings.init()
				
		controller = new TradeRequestController()
		controller.setConfigurationSettings(configurationSettings)
		controller.setTradeEventQueue(tradeEventQueue)
		
		Set<String> supportedCurrencies = new HashSet<>()
		supportedCurrencies.add("EUR")
		supportedCurrencies.add("USD")
		
		Set<String> supportedCountries = new HashSet<>()
		supportedCountries.add("IE")
		
		def userId = "1234"
		def currencyFrom = "EUR"
		def currencyTo = "USD"
		def amountSell = 1000.00
		def amountBuy = 2000.00
		def rate = 2.000
		def timePlaced = new SimpleDateFormat(configurationSettings.getTimePlacedFormat()).format(new java.util.Date())
		def originatingCountry = "IE"
		tradeRequest = new TradeRequest(userId, currencyFrom, currencyTo, amountSell, amountBuy, rate, timePlaced, originatingCountry)
	}
	
	def "Offer Trade"(){
		
		when:
		ResponseEntity<String> response = controller.trade(servletReq, tradeRequest)
		
		then:		
		1 * tradeEventQueue.publish({
			it.getUserId() == 1234  && it.getCurrencyFrom().equals(tradeRequest.getCurrencyFrom()) &&
			it.getCurrencyTo().equals(tradeRequest.getCurrencyTo()) &&
			it.getAmountSell() == 100000 && it.getAmountBuy() == 200000 && it.getRate() == 20000 &&
			it.getTimePlaced() !=null && it.getOriginatingCountry().equals(tradeRequest.getOriginatingCountry())
			
		} as TradeEvent) 
		response != null
		response.getStatusCode() == HttpStatus.OK		
	}
	
	def "Offer Trade: bad inputs"(){
		
		expect:
		code == this.getResponse(trade)
		
		where:
		trade	|	code	
		null	|	HttpStatus.NOT_ACCEPTABLE
		new TradeRequest("1234", "EUR", "USD", 10, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")	|	HttpStatus.OK		
		new TradeRequest("", "EUR", "USD", 10, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")		|	HttpStatus.NOT_ACCEPTABLE
		new TradeRequest("1234", "", "USD", 10, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")		|	HttpStatus.NOT_ACCEPTABLE
		new TradeRequest("1234", "EUR", "", 10, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")		|	HttpStatus.NOT_ACCEPTABLE
		new TradeRequest("1234", "EUR", "USD", 101, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")	|	HttpStatus.NOT_ACCEPTABLE
		new TradeRequest("1234", "EUR", "USD", 10, 0, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")		|	HttpStatus.NOT_ACCEPTABLE		
		new TradeRequest("1234", "EUR", "USD", 0.001, 0002, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")		|	HttpStatus.NOT_ACCEPTABLE		
		new TradeRequest("1234", "EUR", "USD", 10, 0.002, 0.0002, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")	|	HttpStatus.NOT_ACCEPTABLE		
		new TradeRequest("1234", "EUR", "USD", 10, 0.01, 0.00001, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")	|	HttpStatus.NOT_ACCEPTABLE						
		new TradeRequest("1234", "EUR", "USD", 10, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date(System.currentTimeMillis()-(1000*60*60*24))), "IE")	|	HttpStatus.NOT_ACCEPTABLE
		new TradeRequest("1234", "EUR", "USD", 10, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "FR")	|	HttpStatus.NOT_ACCEPTABLE
		
		new TradeRequest("1234", "EUR", "GBP", 10, 20, 2, new SimpleDateFormat("dd-MMM-yy HH:mm:ss").format(new java.util.Date()), "IE")	|	HttpStatus.NOT_ACCEPTABLE
		
	}
	HttpStatus getResponse(TradeRequest trade){
		ResponseEntity<String> response = controller.trade(servletReq, trade)
		return response.getStatusCode()
	}
	
	
	def "Offer Trade: queue exception"(){
		given:
		def errorTradeEventQueue = new TradeEventQueue(){
			@Override
			public void publish (TradeEvent event){
				throw new Exception();
			}
		}
		controller.setTradeEventQueue(errorTradeEventQueue)
		
		when:		
		ResponseEntity<String> response = controller.trade(servletReq, tradeRequest)		
		then:		
		response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR
	}
	
	
}
