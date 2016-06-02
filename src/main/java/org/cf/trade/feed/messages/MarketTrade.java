package org.cf.trade.feed.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketTrade {

	private double rate;
	private double amount;
	
	public MarketTrade(){
		super();
		//used by JSON converter
	}
	
	public MarketTrade(double rate, double amount) {
		super();
		this.rate = rate;
		this.amount = amount;
	}
	public double getRate() {
		return rate;
	}
	public double getAmount() {
		return amount;
	}	
}
