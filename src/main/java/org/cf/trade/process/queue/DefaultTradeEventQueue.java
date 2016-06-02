package org.cf.trade.process.queue;

import org.apache.log4j.Logger;
import org.cf.trade.ConfigurationSettings;
import org.cf.trade.process.messages.TradeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;

@Service
public class DefaultTradeEventQueue implements TradeEventQueue {

	private static final Logger LOGGER = Logger.getLogger(DefaultTradeEventQueue.class);
	
	@Autowired
	private ConfigurationSettings configurationSettings;
	
	@Autowired
	private EventBus queue;
		
	@Override
	public void publish (TradeEvent event) {
		queue.notify(configurationSettings.getTradeQueueName(), Event.wrap(event));					
		LOGGER.debug(String.format("published trade event [%s]", event));
	}
	
}
