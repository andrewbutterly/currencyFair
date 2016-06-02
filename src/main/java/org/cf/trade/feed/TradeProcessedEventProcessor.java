package org.cf.trade.feed;

import org.apache.log4j.Logger;
import org.cf.trade.feed.messages.TradeProcessed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import reactor.bus.Event;
import reactor.fn.Consumer;
/**
 * Receive trade processed update event and push it to subscribed web socket users
 * */
@Service
public class TradeProcessedEventProcessor implements Consumer<Event<TradeProcessed>> {

	private static final Logger LOGGER = Logger.getLogger(TradeProcessedEventProcessor.class);
	
	@Autowired	
	private SimpMessagingTemplate webSocket;
	
	@Override
	public void accept(Event<TradeProcessed> updateEvent) {
		webSocket.convertAndSend("/feed/info", updateEvent.getData());		
		LOGGER.debug(String.format("recieved trade processed info event [%s], sent to listening sockets", updateEvent.getData()));
	}

	void setWebSocket(SimpMessagingTemplate webSocket) {
		this.webSocket = webSocket;
	}
}
