package org.cf.trade.feed;

import java.util.Collections;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;

import org.cf.trade.dao.MarketStore;
import org.cf.trade.feed.messages.MarketUpdate;
import org.cf.trade.feed.messages.TradeProcessed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * FeedRequestController 
 * 
 * called on client startup to request full copy of the current server state. 
 * After this, client will receive updates via the websocket  
 * 
 * */
@ParametersAreNonnullByDefault
@RestController
public class FeedRequestController {

	@Autowired
	private MarketStore marketStore;
	
	@RequestMapping(value = "/feed/info/init", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<TradeProcessed> initMarketInfo(HttpServletRequest request) {
		//blocking call ! - too heavyweight for a production version of the app !				 
		return marketStore.getAllActiveProcessedTradeEvents();
	}
	
	@RequestMapping(value = "/feed/update/init", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<MarketUpdate> initMarketFeed(HttpServletRequest request) {
		//blocking call - too heavyweight for a production app. Also the in place sort is not exactly production ready!		
		List<MarketUpdate> all = marketStore.getAllActiveMarkets();
		all.forEach(update->Collections.sort(update.getMarketTrades(), (a,b) -> Double.compare(a.getRate(), b.getRate())));		
		return all;	
	}	

	void setMarketStore(MarketStore marketStore) {
		this.marketStore = marketStore;
	}
	
}
