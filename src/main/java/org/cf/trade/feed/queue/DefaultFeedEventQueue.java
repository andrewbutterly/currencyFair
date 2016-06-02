package org.cf.trade.feed.queue;

import org.apache.log4j.Logger;
import org.cf.trade.ConfigurationSettings;
import org.cf.trade.feed.messages.MarketUpdate;
import org.cf.trade.feed.messages.TradeProcessed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.bus.Event;
import reactor.bus.EventBus;

/**
 * Feed Event - push feed events to a non blocking feed queue  
 * */
@Service
public class DefaultFeedEventQueue implements FeedEventQueue {

	private static final Logger LOGGER = Logger.getLogger(DefaultFeedEventQueue.class);
	
	@Autowired
	private ConfigurationSettings configurationSettings;
	
	@Autowired
	private EventBus queue;
	
	@Override
	public void publish(MarketUpdate event) {
		queue.notify(configurationSettings.getFeedQueueName(), Event.wrap(event));					
		LOGGER.debug(String.format("published feed event [%s]", event));
	}
	
	@Override
	public void publish(TradeProcessed event) {
		queue.notify(configurationSettings.getFeedQueueName(), Event.wrap(event));					
		LOGGER.debug(String.format("published feed event [%s]", event));
	}

	void setConfigurationSettings(ConfigurationSettings configurationSettings) {
		this.configurationSettings = configurationSettings;
	}

	void setQueue(EventBus queue) {
		this.queue = queue;
	}
	
}
