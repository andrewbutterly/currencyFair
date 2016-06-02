package org.cf.trade.feed.queue


import javax.annotation.Nullable;
import reactor.core.Dispatcher;
import org.cf.trade.ConfigurationSettings
import org.cf.trade.feed.messages.MarketUpdate;

import reactor.bus.Event;
import reactor.bus.EventBus;

class DefaultFeedEventQueueSpec extends spock.lang.Specification {

	DefaultFeedEventQueue component
	
	EventBus queue = Mock(EventBus)	
	def configurationSettings
		
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
		
		component = new DefaultFeedEventQueue()
		component.setConfigurationSettings(configurationSettings)
		component.setQueue(queue)
	}
	
	def "FeedEventQueue publish update"(){		
		when:
		component.publish(Mock(MarketUpdate))
		
		then:
		1 * queue.notify("feed", _ );				
	}
	
}
