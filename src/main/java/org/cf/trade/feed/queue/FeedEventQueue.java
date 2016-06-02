package org.cf.trade.feed.queue;

import org.cf.trade.feed.messages.MarketUpdate;
import org.cf.trade.feed.messages.TradeProcessed;

public interface FeedEventQueue {

	public void publish (MarketUpdate event);
	
	public void publish (TradeProcessed event);
}
