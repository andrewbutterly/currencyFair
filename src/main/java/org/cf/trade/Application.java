package org.cf.trade;

import org.cf.trade.feed.TradeProcessedEventProcessor;
import org.cf.trade.feed.MarketUpdateEventProcessor;
import org.cf.trade.process.TradeEventProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import reactor.Environment;
import reactor.bus.EventBus;

import static reactor.bus.selector.Selectors.$;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = { "org.cf.trade.*" })
public class Application implements CommandLineRunner {

	@Autowired
	private ConfigurationSettings configurationSettings;

	@Autowired
	private EventBus eventBus;

	@Autowired
	private TradeEventProcessor tradeEventProcessor;

	@Autowired
	private MarketUpdateEventProcessor marketUpdateEventProcessor;

	@Autowired
	private TradeProcessedEventProcessor tradeProcessedEventProcessor;
	
	public static void main(String... args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	ConfigurationSettings ConfigurationSettings() {
		return new ConfigurationSettings();
	}

	@Bean
	Environment env() {
		return Environment.initializeIfEmpty().assignErrorJournal();
	}

	@Bean
	EventBus createEventBus(Environment env) {
		return EventBus.create(env, env.getDefaultDispatcher());
	}

	@Override
	public void run(String... args) throws Exception {
		eventBus.on($(configurationSettings.getTradeQueueName()), tradeEventProcessor);
		eventBus.on($(configurationSettings.getFeedQueueName()), marketUpdateEventProcessor);
		eventBus.on($(configurationSettings.getFeedQueueName()), tradeProcessedEventProcessor);
	}

}
