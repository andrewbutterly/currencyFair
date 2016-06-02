package org.cf.trade.feed.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeProcessed {

	private String originatingCountry;
	private double culmativeAmount;
	
	public TradeProcessed(){
		super();
		//used by JSON converter
	}
	
	public TradeProcessed(String originatingCountry, double culmativeAmount) {
		super();
		this.originatingCountry = originatingCountry;
		this.culmativeAmount = culmativeAmount;
	}
	public String getOriginatingCountry() {
		return originatingCountry;
	}
	public double getCulmativeAmount() {
		return culmativeAmount;
	}	
}
