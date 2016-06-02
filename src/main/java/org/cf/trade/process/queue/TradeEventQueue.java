package org.cf.trade.process.queue;

import org.cf.trade.process.messages.TradeEvent;

@FunctionalInterface
public interface TradeEventQueue {

	public void publish (TradeEvent event);

}
