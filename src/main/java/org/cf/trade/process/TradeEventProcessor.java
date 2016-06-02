package org.cf.trade.process;

import org.apache.log4j.Logger;
import org.cf.trade.dao.MarketStore;
import org.cf.trade.feed.queue.FeedEventQueue;
import org.cf.trade.process.messages.TradeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.bus.Event;
import reactor.fn.Consumer;
/**
 * Receive trade offers for processing. 
 * Aggregate events, and push changes to asynch update queues 
 * 
 * */
@Service
public class TradeEventProcessor implements Consumer<Event<TradeEvent>> {

	private static final Logger LOGGER = Logger.getLogger(TradeEventProcessor.class);

	@Autowired
	private FeedEventQueue feedEventQueue;
	
	@Autowired
	private MarketStore marketStore;
	
	@Override
	public void accept(Event<TradeEvent> event) {
		
		/**
		 *  NOT IMPLEMENTED:
		 *  - validate trade request against user wallet 
		 *   - (blocking call against transaction-safe in memory cache of logged in users current wallet total)
		 *  - 'reserve' offered amount in users wallet until trade is completed or abandoned (also a blocking call) 
		 *   
		 *  NOT IMPLEMENTED:
		 *  - attempt to match the trade request
		 *  - persist the trade request & results to the DB/data store
		 *  - other business logic...
		 *  
		 *  IMPLEMENTED:
		 *  - aggregate the trade to an in-memory 'cache' and send update event
		 *  - aggregate the request event to an in-memory 'cache' and send update event
		 *  
		 * */
		
		feedEventQueue.publish(marketStore.updateandGetMarketUpdates(event.getData()));					
		feedEventQueue.publish(marketStore.updateandGetTradeProcessed(event.getData()));			
		
		LOGGER.debug(String.format("recieved event [%s], sent aggregated event to feed queues", event.getData()));
	}

	void setFeedEventQueue(FeedEventQueue feedEventQueue) {
		this.feedEventQueue = feedEventQueue;
	}

	void setMarketStore(MarketStore marketStore) {
		this.marketStore = marketStore;
	}	
	
	
}
