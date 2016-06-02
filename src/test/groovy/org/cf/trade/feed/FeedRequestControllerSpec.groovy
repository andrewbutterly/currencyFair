package org.cf.trade.feed

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cf.trade.dao.MarketStore;
import org.cf.trade.feed.messages.MarketTrade;
import org.cf.trade.feed.messages.MarketUpdate;
import org.cf.trade.feed.messages.TradeProcessed

class FeedRequestControllerSpec extends spock.lang.Specification{
	
	def marketStore = Mock (MarketStore)
	
	FeedRequestController controller
	
	def setup(){
		controller = new FeedRequestController()
		controller.setMarketStore(marketStore)
	}
	
	def "call init trade update, test response and ordering"(){
		
		given:
		List<MarketTrade> marketTrades = new ArrayList<>()
		marketTrades.add(new MarketTrade(0.003, 100))
		marketTrades.add(new MarketTrade(0.002, 100))
		marketTrades.add(new MarketTrade(0.001, 100))
		
		MarketUpdate oneMarket = new MarketUpdate("EUR-GBP", marketTrades) 
		List<MarketUpdate> all = new ArrayList<>()
		all.add(oneMarket);
		
		when:
		List<MarketUpdate> update = controller.initMarketFeed(Mock(HttpServletRequest))
				
		then:
		1 * marketStore.getAllActiveMarkets() >> all
		update.get(0).getMarketTrades().get(0).getRate() == 0.001
		update.get(0).getMarketTrades().get(1).getRate() == 0.002
		update.get(0).getMarketTrades().get(2).getRate() == 0.003
		
	}
	
	def "call init info update, test response"(){
		
		given:				
		List<TradeProcessed> all = new ArrayList<>()
		all.add(new TradeProcessed("FR", 1000))
		all.add(new TradeProcessed("IE", 1000))
		
		when:
		List<TradeProcessed> info = controller.initMarketInfo(Mock(HttpServletRequest))
				
		then:
		1 * marketStore.getAllActiveProcessedTradeEvents() >> all
		info.size() == 2
		info.get(0).getCulmativeAmount() == 1000
		info.get(1).getCulmativeAmount() == 1000
		
		info.get(0).getOriginatingCountry().equals("FR") || info.get(0).getOriginatingCountry().equals("IE")
		info.get(1).getOriginatingCountry().equals("FR") || info.get(1).getOriginatingCountry().equals("IE")			
	}
	
}
