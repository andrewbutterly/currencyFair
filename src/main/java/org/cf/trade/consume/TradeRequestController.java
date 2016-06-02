package org.cf.trade.consume;

import static org.springframework.util.StringUtils.isEmpty;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cf.trade.ConfigurationSettings;
import org.cf.trade.consume.messages.TradeRequest;
import org.cf.trade.process.messages.TradeEvent;
import org.cf.trade.process.queue.TradeEventQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * TradeRequestController
 * 
 * Receive trade requests, perform basic input checking, forward request to business logic. 
 * 
 * */
@ParametersAreNonnullByDefault
@RestController
public class TradeRequestController {

	private static final Logger LOGGER = Logger.getLogger(TradeRequestController.class);
		
	@Autowired
	private ConfigurationSettings configurationSettings;	
	private static ThreadLocal<SimpleDateFormat> requestPlacedFormat;
	
	@Autowired
	private TradeEventQueue tradeEventQueue;
	
	private static final ResponseEntity<String> SUCCESS = new ResponseEntity<>(HttpStatus.OK);
	private static final ResponseEntity<String> NOT_ACCEPTABLE = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
	private static final ResponseEntity<String> FAILURE = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	
	/**
	 * Offer trade: POST option
	 * Trade passed in via HTTP body as JSON object
	 * 
	 * HTTP status codes used to indicate result
	 * 
	 * @param HttpServletRequest request
	 * @param String JSON bodyContent (but will return HTTP NOT ACCEPTABLE response if not correct)
	 * @return ResponseEntity - empty response with HTTP status code
	 * */
	@RequestMapping(value = "/trade", method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE, consumes={MediaType.APPLICATION_JSON_VALUE})	
	public ResponseEntity<String> trade(HttpServletRequest request, @RequestBody(required=true) TradeRequest tradeRequest) {
						
		/**
		 * validate input (range values, etc). Split requests with opposing sell and buy positions into multiple requests.
		 * note: if inputs are invalid, response is not specifying which inputs failed to the API user, or returning custom error codes in a JSON object, etc. 
		 * If the inputs are invalid at this level, this is likely to be a malicious request.
		 * */
		Optional<TradeEvent> event = validateEvent(tradeRequest);			
		if(!event.isPresent()){
			return NOT_ACCEPTABLE;
		}
		
		try {
			//push to asynchronous queue for processing
			tradeEventQueue.publish(event.get());			
			
			/** note: not returning if the trade was *accepted* for matching on the exchange, or if it was *matched*, just that the request was successfully made.
			 * User can find out the result of their trade attempt via other another call.*/
			return SUCCESS;
		} catch (Exception e) {
			LOGGER.error(String.format("error thrown in adding trade to queue [%s, %s]", tradeRequest, e.getMessage()));
			return FAILURE;
		}	
	}
	          
	private Optional<TradeEvent> validateEvent(@Nullable TradeRequest request){
			
		if(request == null){
			LOGGER.info("null trade request");
			return Optional.empty();
		}
		
		try{
			if(isEmpty(request.getUserId())){
				LOGGER.info(String.format("bad trade request: user id [%s]", request));
				return Optional.empty();
			}
			long userId = Long.parseLong(request.getUserId());
			if(userId <1){
				LOGGER.info(String.format("bad trade request: user id [%s]", request));
				return Optional.empty();
			}
		
			if(isEmpty(request.getCurrencyFrom()) || !configurationSettings.isSupportedCurrency(request.getCurrencyFrom())){
				LOGGER.info(String.format("bad trade request: currency from [%s]", request));
											
				return Optional.empty();
			}
			
			if(isEmpty(request.getCurrencyTo()) || !configurationSettings.isSupportedCurrency(request.getCurrencyTo())){
				LOGGER.info(String.format("bad trade request: currency to [%s]", request));
				return Optional.empty();
			}
			
			if(request.getCurrencyFrom().equalsIgnoreCase(request.getCurrencyTo())){
				LOGGER.info(String.format("bad trade request: currency to [%s]", request));
				return Optional.empty();
			}
			
			if(!checkValueInputs(request.getAmountSell(), request.getAmountBuy(), request.getRate(), 
					configurationSettings.getAmountRounding(), configurationSettings.getRateRounding())){
				LOGGER.info(String.format("bad trade request: amount sell*rate did not match buy value [%s]", request));
				return Optional.empty();
			}
			
			//assumption - if too many decimal places, round 
			long amountSell = configurationSettings.convertAmount(request.getAmountSell());				
			if(amountSell < configurationSettings.getAmountMin() || amountSell > configurationSettings.getAmountMax()){
				LOGGER.info(String.format("bad trade request: amount sell [%s]", request));
				return Optional.empty();
			}

			//assumption - if too many decimal places, round		
			long amountBuy = configurationSettings.convertAmount(request.getAmountBuy());
			if(amountBuy < configurationSettings.getAmountMin() || amountBuy > configurationSettings.getAmountMax()){
				LOGGER.info(String.format("bad trade request: amount buy [%s]", request));
				return Optional.empty();
			}
												
			//assumption - round to Y decimal places rather than reject the trade
			long rate = configurationSettings.convertRate(request.getRate());			
			if(rate < configurationSettings.getRateMin() || rate > configurationSettings.getRateMax()){				
				LOGGER.info(String.format("bad trade request: rate [%s]", request));
				return Optional.empty();
			}													
						
			if(isEmpty(request.getTimePlaced())){
				LOGGER.info(String.format("bad trade request: time placed [%s]", request));
				return Optional.empty();
			}
						
			Calendar timePlaced = Calendar.getInstance();
			timePlaced.setTime(getFormatter(configurationSettings.getTimePlacedFormat()).parse(request.getTimePlaced()));					
			Calendar requestTTL = Calendar.getInstance();
			requestTTL.add(Calendar.MINUTE, -configurationSettings.getTimePlacedTTLMin());
			
			if(timePlaced.before(requestTTL)){
				LOGGER.info(String.format("bad trade request: time placed is too long ago [%s]", request));
				return Optional.empty();
			}
			
			Calendar now = Calendar.getInstance();
			if(timePlaced.after(now)){
				LOGGER.info(String.format("trade request contained 'time placed' field in future: [%s]", request));
				//allow it, but change the time. Note: this is not a futures/options exchange :)
				timePlaced = now;				
			}
								
			if(isEmpty(request.getOriginatingCountry()) || !configurationSettings.isSupportedCountry(request.getOriginatingCountry())){
				LOGGER.info(String.format("bad trade request: originating country [%s]", request));
				return Optional.empty();
			}
	
			return Optional.of(new TradeEvent(userId, request.getCurrencyFrom(), request.getCurrencyTo(), 
					amountSell, amountBuy, rate, timePlaced.getTimeInMillis(), request.getOriginatingCountry()));
			
		}catch(Exception e){
			LOGGER.info(String.format("bad trade request. error in parsing request: [%s][%s]", request, e.getMessage()));	
		}
		
		return Optional.empty();
	}
	
	private boolean checkValueInputs(double amountSell, double amountBuy, double rate, int amountRounding, int rateRouding){
		//assumption - if too many decimal places, round	
		return new BigDecimal(String.valueOf(amountSell)).setScale(amountRounding, BigDecimal.ROUND_HALF_UP)
					.multiply(new BigDecimal(String.valueOf(rate)).setScale(amountRounding, BigDecimal.ROUND_HALF_UP))
					.setScale(amountRounding, BigDecimal.ROUND_HALF_UP)
					.equals(new BigDecimal(String.valueOf(amountBuy)).setScale(amountRounding, BigDecimal.ROUND_HALF_UP));										
	}
	
	private static SimpleDateFormat getFormatter(String format){
		if(requestPlacedFormat == null){
			requestPlacedFormat = new ThreadLocal<SimpleDateFormat>(){
			    @Override
			    protected SimpleDateFormat initialValue() {
			        return new SimpleDateFormat(format);	
			    }
			};
		}
		return requestPlacedFormat.get();
	}

	void setConfigurationSettings(ConfigurationSettings configurationSettings) {
		this.configurationSettings = configurationSettings;
	}

	void setTradeEventQueue(TradeEventQueue tradeEventQueue) {
		this.tradeEventQueue = tradeEventQueue;
	}
	
}
