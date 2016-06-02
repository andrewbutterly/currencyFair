package org.cf.trade.dao

import java.util.List;

import org.cf.trade.ConfigurationSettings;
import org.cf.trade.feed.messages.MarketUpdate
import org.cf.trade.feed.messages.TradeProcessed
import org.cf.trade.process.messages.TradeEvent

class DefaultMarketStoreSpec extends spock.lang.Specification {
		
	ConfigurationSettings configurationSettings
	
	DefaultMarketStore marketStore = new DefaultMarketStore()
	
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
	}
	
	def "MarketStore: offered trades. add values, request full listing"(){
		given:
		marketStore = new DefaultMarketStore()
		marketStore.setConfigurationSettings(configurationSettings)
				
		TradeEvent event1 = new TradeEvent(0, "USD", "EUR", 100000, 200000, 2000, 0, "IE")
		TradeEvent event2 = new TradeEvent(0, "USD", "EUR", 100000, 200000, 2000, 0, "IE")
		
		when:
		MarketUpdate update1 = marketStore.updateandGetMarketUpdates(event1);
		MarketUpdate update2 = marketStore.updateandGetMarketUpdates(event2);
		List<MarketUpdate> allMarketUpdates = marketStore.getAllActiveMarkets()
		
		then:
		update1.getMarket().equals("USD-EUR")
		!update1.getMarketTrades().isEmpty()
		update1.getMarketTrades().get(0).getAmount() == 1000.00
		update1.getMarketTrades().get(0).getRate() == 0.2
		
		!update2.getMarketTrades().isEmpty()
		update2.getMarketTrades().get(0).getAmount() == 2000.00
		update2.getMarketTrades().get(0).getRate() == 0.2
		
		allMarketUpdates.size() == 1			
	}
	
	def "MarketStore: info update. add values, request full listing"(){
		given:
		marketStore = new DefaultMarketStore()
		marketStore.setConfigurationSettings(configurationSettings)
		
		
		TradeEvent event1 = new TradeEvent(0, "USD", "EUR", 100000, 200000, 2000, 0, "IE")
		TradeEvent event2 = new TradeEvent(0, "USD", "EUR", 100000, 200000, 2000, 0, "IE")
		
		when:		
		TradeProcessed trade1 = marketStore.updateandGetTradeProcessed(event1);
		TradeProcessed trade2 = marketStore.updateandGetTradeProcessed(event2);
		List<TradeProcessed> allInfoUpdates = marketStore.getAllActiveProcessedTradeEvents()
		
		then:
		trade1.getOriginatingCountry().equals("IE")
		trade1.getCulmativeAmount() == 1000	
		trade2.getOriginatingCountry().equals("IE")
		trade2.getCulmativeAmount() == 2000
		!allInfoUpdates.isEmpty()
		allInfoUpdates.get(0).getOriginatingCountry().equals("IE")
		allInfoUpdates.get(0).getCulmativeAmount() == 2000		
	}
	
}
