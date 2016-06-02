package org.cf.trade.feed;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.cf.trade.feed.messages.MarketUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import reactor.bus.Event;
import reactor.fn.Consumer;
/**
 * Receive market update event and push it to subscribed web socket users
 * */
@Service
public class MarketUpdateEventProcessor implements Consumer<Event<MarketUpdate>> {

	private static final Logger LOGGER = Logger.getLogger(MarketUpdateEventProcessor.class);
	
	@Autowired	
	private SimpMessagingTemplate webSocket;
	
	@Override
	public void accept(Event<MarketUpdate> updateEvent) {
		
		MarketUpdate update = updateEvent.getData();
		Collections.sort(update.getMarketTrades(), (a,b) -> Double.compare(a.getRate(), b.getRate()));
		
		webSocket.convertAndSend("/feed/update", update);		
		LOGGER.debug(String.format("recieved market update event [%s], sent to listening sockets", update));
	}

	void setWebSocket(SimpMessagingTemplate webSocket) {
		this.webSocket = webSocket;
	}
	
}
