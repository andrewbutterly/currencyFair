package org.cf.trade;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ParametersAreNonnullByDefault
@Configuration
@PropertySource("classpath:application.properties")
public final class ConfigurationSettings {
			
	@Value("#{'${trade.supported_currencies}'.split(',')}") 
    private List<String> supportedCurrencies;
				
	@Value("#{'${trade.supported_countries}'.split(',')}")
    private List<String> supportedCountries;	

    @Value("${trade.amount.rounding_places}")
    private int amountRounding;
    
    @Value("${trade.amount.min}")
    private long amountMin;
    
    @Value("${trade.amount.max}")
    private long amountMax;    
    
    @Value("${trade.rate.rounding_places}")
    private int rateRounding;     
    
    @Value("${trade.rate.min}")
    private long rateMin;      
    
    @Value("${trade.rate.max}")
    private long rateMax;
    
    @Value("${trade.time_placed.ttl_minutes}")
    private int timePlacedTTLMin;
    
    @Value("${trade.time_placed.format}")
    private String timePlacedFormat;    
    
    @Value("${trade.processor.queue_name}")
    private String tradeQueueName;     
    
    @Value("${feed.processor.queue_name}")
    private String feedQueueName; 
                	 
    private static Set<String> supportedCurrenciesSet;
    private static Set<String> supportedCountriesSet;
    
    public ConfigurationSettings(){
        //used for testing only
    }
	    
    //used for testing only
	ConfigurationSettings(List<String> supportedCurrencies,
			List<String> supportedCountries, int amountRounding,
			long amountMin, long amountMax, int rateRounding, long rateMin,
			long rateMax, int timePlacedTTLMin, String timePlacedFormat,
			String tradeQueueName, String feedQueueName) {
		super();
		this.supportedCurrencies = supportedCurrencies;
		this.supportedCountries = supportedCountries;
		this.amountRounding = amountRounding;
		this.amountMin = amountMin;
		this.amountMax = amountMax;
		this.rateRounding = rateRounding;
		this.rateMin = rateMin;
		this.rateMax = rateMax;
		this.timePlacedTTLMin = timePlacedTTLMin;
		this.timePlacedFormat = timePlacedFormat;
		this.tradeQueueName = tradeQueueName;
		this.feedQueueName = feedQueueName;
	}
	
	@PostConstruct
	private void init(){
		
		supportedCurrenciesSet = new HashSet<>();
		supportedCurrencies.forEach(supportedCurrenciesSet::add);
		
		supportedCountriesSet = new HashSet<>();
		supportedCountries.forEach(supportedCountriesSet::add);
		
	}
	
	
	public boolean isSupportedCurrency(String currency) {		
		return supportedCurrenciesSet.contains(currency);
	}

	public boolean isSupportedCountry(String country) {				
		return supportedCountriesSet.contains(country);
	}

	public int getAmountRounding() {			
		return amountRounding;
	}

	public double getAmountMin() {
		return amountMin;
	}

	public double getAmountMax() {
		return amountMax;
	}

	public int getRateRounding() {
		return rateRounding;
	}

	public double getRateMin() {
		return rateMin;
	}

	public double getRateMax() {
		return rateMax;
	}

	public int getTimePlacedTTLMin() {
		return timePlacedTTLMin;
	}

	public String getTimePlacedFormat(){
		return timePlacedFormat;
	}

	public String getTradeQueueName() {
		return tradeQueueName;
	}

	public String getFeedQueueName() {
		return feedQueueName;
	} 
	
	public long convertAmount(double amount){
		return convert(amount, getAmountRounding());
	}
	
	public double convertAmount(long amount){
		return convert(amount, getAmountRounding());		
	}
	
	public long convertRate(double rate){
		return convert(rate, getRateRounding());
	}
	
	public double convertRate(long rate){
		return convert(rate, getRateRounding());
	}
	
	private static long convert(double value, int rounding){
		BigDecimal amt = new BigDecimal(String.valueOf(value));		
		return amt.movePointRight(rounding).setScale(rounding, BigDecimal.ROUND_HALF_UP).longValue();			
	}
	
	private static double convert(long value, int rounding){
		BigDecimal amt = new BigDecimal(String.valueOf(value));		
		return amt.movePointLeft(rounding).setScale(rounding, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
}
