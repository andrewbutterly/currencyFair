package org.cf.trade.process.messages;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * TradeEvent
 * 
 * Processed trade event, ready for processing 
 * 
 * Note: the fields {amountSell, amountBuy, rate} were originally of type double, with two decimal place precision for 'amountSell' and 'amountBuy', and four decimal place precision for 'rate'.
 * To simplify message processing and prevent potential future rounding errors in performing operations using Java doubles, the values are represented internally as type Long using a known precision.
 * 
 * for example: a 'sell' value of 99.99 will be represented internally as 9999L. Or a 'rate' value of 1.2345 will be represented as 12345L.
 * */
@ParametersAreNonnullByDefault
public class TradeEvent {

	private final long userId;	
	private final String currencyFrom;	
	private final String currencyTo;    	
	private long amountSell;
	private long amountBuy;
	private final long rate;	
	private final long timePlaced;
	private final String originatingCountry;

	public TradeEvent(long userId, String currencyFrom, String currencyTo, long amountSell, long amountBuy,
			long rate, long timePlaced, String originatingCountry) {
		super();
		this.userId = userId;
		this.currencyFrom = currencyFrom;
		this.currencyTo = currencyTo;
		this.amountSell = amountSell;
		this.amountBuy = amountBuy;
		this.rate = rate;
		this.timePlaced = timePlaced;
		this.originatingCountry = originatingCountry;
	}

	@Override
	public String toString() {
		return "TradeEvent [userId=" + userId + ", currencyFrom=" + currencyFrom + ", currencyTo=" + currencyTo
				+ ", amountSell=" + amountSell + ", amountBuy=" + amountBuy + ", rate=" + rate + ", timePlaced="
				+ timePlaced + ", originatingCountry=" + originatingCountry + "]";
	}

	public long getUserId() {
		return userId;
	}

	public String getCurrencyFrom() {
		return currencyFrom;
	}

	public String getCurrencyTo() {
		return currencyTo;
	}

	public long getAmountSell() {
		return amountSell;
	}

	public long getAmountBuy() {
		return amountBuy;
	}

	public long getRate() {
		return rate;
	}

	public long getTimePlaced() {
		return timePlaced;
	}

	public String getOriginatingCountry() {
		return originatingCountry;
	}
	
}
