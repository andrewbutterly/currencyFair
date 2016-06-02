package org.cf.trade.process

import org.cf.trade.process.messages.TradeEvent;

import reactor.bus.Event;

import org.cf.trade.dao.MarketStore;
import org.cf.trade.feed.messages.MarketUpdate
import org.cf.trade.feed.messages.TradeProcessed
import org.cf.trade.feed.queue.FeedEventQueue;
import org.cf.trade.process.messages.TradeEvent;

import reactor.bus.Event;

class TradeEventProcessorSpec extends spock.lang.Specification {

	MarketStore marketStore = Mock(MarketStore)
	FeedEventQueue feedEventQueue = Mock(FeedEventQueue)
	
	TradeEventProcessor processor
	
	def setup(){		
		processor = new TradeEventProcessor();
		processor.setFeedEventQueue(feedEventQueue)
		processor.setMarketStore(marketStore)
	}
	
	
	def "TradeEventProcessor: process event"(){
		
		given:
		TradeEvent event = new TradeEvent(1234, "EUR", "USD", 100000, 200000, 2000, 123457890L, "FR");
		MarketUpdate update = Mock(MarketUpdate)
		TradeProcessed tradeInfo = Mock(TradeProcessed)
					
		when:
		processor.accept(Event.wrap(event))
				
		then:
		1 * marketStore.updateandGetMarketUpdates(event) >> update
		1 * marketStore.updateandGetTradeProcessed(event) >> tradeInfo
		
		1 * feedEventQueue.publish(update)
		1 * feedEventQueue.publish(tradeInfo)		
	}
}
