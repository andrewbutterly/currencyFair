package org.cf.trade.feed

import org.cf.trade.feed.messages.MarketTrade
import org.cf.trade.feed.messages.MarketUpdate
import org.springframework.messaging.simp.SimpMessagingTemplate

import reactor.bus.Event;

class TradeProcessedEventProcessorSpec extends spock.lang.Specification {
	
	TradeProcessedEventProcessor processor	
	SimpMessagingTemplate socket = Mock(SimpMessagingTemplate)
	
	def setup(){		
		processor = new TradeProcessedEventProcessor()
		processor.setWebSocket(socket)
	}
	
	def "TradeProcessedEventProcessor: send event"(){
		given:			
		MarketUpdate update = new MarketUpdate("EUR-GBP", new ArrayList<>())
		
		when:
		processor.accept(Event.wrap(update))
		
		then:
		socket.convertAndSend("/feed/info", update);					
	}	
}
