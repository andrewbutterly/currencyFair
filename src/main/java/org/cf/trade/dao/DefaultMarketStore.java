package org.cf.trade.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import org.cf.trade.ConfigurationSettings;
import org.cf.trade.feed.messages.MarketTrade;
import org.cf.trade.feed.messages.MarketUpdate;
import org.cf.trade.feed.messages.TradeProcessed;
import org.cf.trade.process.messages.TradeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gs.collections.impl.map.mutable.ConcurrentHashMap;
/**
 * Data store. Simple in-memory tables. 
 * 
 *  note:  A 'full' version of this application would use a robust, process (& probably network) separate cache service for critical state information (also for wallet totals, market state, etc..)
 *  This would also be backed by a transactional persistence layer. The failure model of this version of the app is not robust, and there is no persistent store
 * 
 * */
@ParametersAreNonnullByDefault
@Service
public class DefaultMarketStore implements MarketStore {

	@Autowired
	private ConfigurationSettings configurationSettings;
	
	/*
	 * local only in-memory cache. Enough for this use case but will not scale! 
	 **/
	private static final Map<String, AtomicLong> tradeProcessedStore;
	private static final Map<String, Map<Long, AtomicLong>> tradesOfferedStore;
	static{
		tradeProcessedStore = new ConcurrentHashMap<>();
		tradesOfferedStore = new ConcurrentHashMap<>();
	}
		
	public void setConfigurationSettings(ConfigurationSettings configurationSettings) {
		this.configurationSettings = configurationSettings;
	}

	@Override
	public TradeProcessed updateandGetTradeProcessed(TradeEvent event){
		return new TradeProcessed(event.getOriginatingCountry(), getAndSetCulmativeForCountry(event));
	}
	
	@Override
	public List<TradeProcessed> getAllActiveProcessedTradeEvents(){
		if(tradeProcessedStore.isEmpty()){
			return Collections.<TradeProcessed>emptyList();
		}
		return tradeProcessedStore.entrySet().stream()
				.map(e -> new TradeProcessed(e.getKey(), configurationSettings.convertAmount(e.getValue().get())))
				.collect(Collectors.<TradeProcessed>toList());		
	}
	
	@Override
	public MarketUpdate updateandGetMarketUpdates(TradeEvent event){
		String marketDirection = event.getCurrencyFrom()+"-"+event.getCurrencyTo();
		Map<Long, AtomicLong> tradeBands = getAndSetCulmativesForMarketAndRates(marketDirection, event);
		List<MarketTrade> trades = new ArrayList<>();
		tradeBands.forEach((k,v) -> trades.add(new MarketTrade(configurationSettings.convertRate(k), configurationSettings.convertAmount(v.get()))));
		return new MarketUpdate(marketDirection, trades);
	}
	
	@Override
	public List<MarketUpdate> getAllActiveMarkets(){
		if(tradesOfferedStore.isEmpty()){
			return Collections.<MarketUpdate>emptyList();
		}
		return tradesOfferedStore.entrySet().stream()
				.map(e -> new MarketUpdate(e.getKey(), getTrades(e.getValue())))
				.collect(Collectors.<MarketUpdate>toList());						
	}
	private List<MarketTrade> getTrades(Map<Long, AtomicLong> map){
		return map.entrySet().stream()
				.map(e -> new MarketTrade(configurationSettings.convertRate(e.getKey()), configurationSettings.convertAmount(e.getValue().get())))
				.collect(Collectors.<MarketTrade>toList());	
	}
	
	private Map<Long, AtomicLong> getAndSetCulmativesForMarketAndRates(String market, TradeEvent event){
		
		Map<Long, AtomicLong> rates = tradesOfferedStore.get(market);
		if(rates == null){
			rates = new ConcurrentHashMap<>();			
			tradesOfferedStore.put(market, rates);
			rates.put(event.getRate(), new AtomicLong(event.getAmountSell()));
		}else{				
			if(rates.containsKey(event.getRate()) ){
				rates.get(event.getRate()).addAndGet(event.getAmountSell());			
			}else{		
				rates.put(event.getRate(), new AtomicLong(event.getAmountSell()));				
			}			
		}
		
		return rates;
	}
	
	private double getAndSetCulmativeForCountry(TradeEvent event){
		
		long total;		
		if(tradeProcessedStore.containsKey(event.getOriginatingCountry()) ){
			total = tradeProcessedStore.get(event.getOriginatingCountry()).addAndGet(event.getAmountSell());			
		}else{		
			tradeProcessedStore.put(event.getOriginatingCountry(), new AtomicLong(event.getAmountSell()));
			total = event.getAmountSell();
		}
		
		return configurationSettings.convertAmount(total);
	}

}
