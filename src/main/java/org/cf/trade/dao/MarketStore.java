package org.cf.trade.dao;

import java.util.List;

import org.cf.trade.feed.messages.MarketUpdate;
import org.cf.trade.feed.messages.TradeProcessed;
import org.cf.trade.process.messages.TradeEvent;

public interface MarketStore {
	
	TradeProcessed updateandGetTradeProcessed(TradeEvent event);
	
	List<TradeProcessed> getAllActiveProcessedTradeEvents();
		
	MarketUpdate updateandGetMarketUpdates(TradeEvent event);
	
	List<MarketUpdate> getAllActiveMarkets();
}
