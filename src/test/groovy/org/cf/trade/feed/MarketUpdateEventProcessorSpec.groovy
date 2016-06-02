package org.cf.trade.feed

import org.cf.trade.feed.messages.MarketTrade
import org.cf.trade.feed.messages.MarketUpdate
import org.springframework.messaging.simp.SimpMessagingTemplate

import reactor.bus.Event;

class MarketUpdateEventProcessorSpec extends spock.lang.Specification {
	
	MarketUpdateEventProcessor processor	
	SimpMessagingTemplate socket = Mock(SimpMessagingTemplate)
	
	def setup(){		
		processor = new MarketUpdateEventProcessor()
		processor.setWebSocket(socket)
	}
	
	def "MarketUpdateEventProcessor: send event"(){
		given:			
		MarketUpdate update = new MarketUpdate("EUR-GBP", new ArrayList<>())
		
		when:
		processor.accept(Event.wrap(update))
		
		then:
		socket.convertAndSend("/feed/update", update);					
	}	
}
