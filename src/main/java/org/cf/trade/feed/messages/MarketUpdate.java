package org.cf.trade.feed.messages;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketUpdate {

	private String market;
	private List<MarketTrade> marketTrades;
	
	public MarketUpdate(){
		super();
		//used by JSON converter
	}
	
	public MarketUpdate(String market, List<MarketTrade> marketTrades) {
		super();
		this.market = market;
		this.marketTrades = marketTrades;
	}
	public String getMarket() {
		return market;
	}
	public List<MarketTrade> getMarketTrades() {
		return marketTrades;
	}
	
}
