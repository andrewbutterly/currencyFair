
var stompClient = null;

var initPage = function(){
	$('#vmap').vectorMap({
		map : 'world_en',
		onRegionOver: function (event, code, region) {
			event.preventDefault();
		},
		onRegionClick: function (event, code, region) {
			event.preventDefault();
		}
	});
	
	initDataFeeds();
	connectEventSocket();
};

function initDataFeeds(){
	
	$.ajax({url: "/feed/info/init", success: function(data){
			$.each(data, function(i, item) {
				recieveTradeAlertEvent(item);
			});
    	},
    	error: function(jqXHR, error, errorThrown) { 
    		//console.log('/feed/info/init error: ' + error);
    	}
	});
	
	$.ajax({url: "/feed/update/init", success: function(data){    			
			$.each(data, function(i, item) {
				recieveFeedUpdate(item);
			});
    	},
    	error: function(jqXHR, error, errorThrown) { 
    		//console.log('/feed/update/init error: ' + error);
    	}
	});
		
}

function connectEventSocket() {
	var socket = new SockJS('/register');
	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {				
		//console.log('Connected: ' + frame);
		stompClient.subscribe('/feed/update', function(event) {					
			recieveFeedUpdate(JSON.parse(event.body));
		});
		stompClient.subscribe('/feed/info', function(event) {					
			recieveTradeAlertEvent(JSON.parse(event.body));
		});
	}, errorCallback);
}

function errorCallback(error) {
	//console.log('stomp error: ' + error);	
    setTimeout(connectEventSocket, 3000);
    //console.log('stomp: Reconecting in 3 seconds');	      
};

function recieveFeedUpdate(tradesUpdate){
		
	var newOrUpdated = new Array();
	
	if(!$("#trade_"+tradesUpdate.market).length){
		
		var tradesOffered = "";		
		for (var i = 0; i < tradesUpdate.marketTrades.length; i++) {
			var id = cleanTextForId("trade_trades_"+tradesUpdate.market+"_"+tradesUpdate.marketTrades[i].rate);
			tradesOffered += $.templates("#tradeAtRate").render(
					[{
						id: id,
						amount: Number(tradesUpdate.marketTrades[i].amount).toLocaleString(),
						rate: tradesUpdate.marketTrades[i].rate
					}]
			);	
			newOrUpdated.push(id);
		}
		
		$("#tradeFeed").append($.templates("#market").render(
				[{
					market: tradesUpdate.market,
					trades: tradesOffered
				}]
		));						
	}else{
		
		var tradesOffered = "";		
		for (var i = 0; i < tradesUpdate.marketTrades.length; i++) {
									
			var id = cleanTextForId("trade_trades_"+tradesUpdate.market+"_"+tradesUpdate.marketTrades[i].rate);
			tradesOffered += $.templates("#tradeAtRate").render(
					[{
						id: id,
						amount: Number(tradesUpdate.marketTrades[i].amount).toLocaleString(),
						rate: tradesUpdate.marketTrades[i].rate
					}]
			);
			
			if($("#"+id).length){
				var newContent = Number(tradesUpdate.marketTrades[i].amount).toLocaleString();				
				if($("#"+id).html() !== newContent){
					newOrUpdated.push(id);
				}
			}else{
				newOrUpdated.push(id);
			}
										
		}
		
		$("#trade_trades_"+tradesUpdate.market).html(tradesOffered);
		
	}	
	
	for(var j = 0;j < newOrUpdated.length; j++) {
		var elem = $("#"+newOrUpdated[j]);
		elem.css('background-color', '#c9df79');		
	}
	setTimeout(function() {
		$('[id^="trade_trades_"]').each(function(){
				$(this).css('background-color', 'transparent');
			});
	}, 1000);	
	
			
};
function recieveTradeAlertEvent(tradeProcessedEvent){
			
	if($("#tp_id_"+tradeProcessedEvent.originatingCountry).length){		
		$("#tp_id_"+tradeProcessedEvent.originatingCountry).html(
				Number(tradeProcessedEvent.culmativeAmount).toLocaleString() 
				);
		
	}else{

		$("#tradeUpdate").append($.templates("#tradeProcessed").render(
				[{
					originatingCountry: tradeProcessedEvent.originatingCountry,
					culmativeAmount: Number(tradeProcessedEvent.culmativeAmount).toLocaleString() 
				}]		
		));	
	}
	
	flashCountryOnMap(tradeProcessedEvent.originatingCountry.toLowerCase());
	
	$("#tp_id_"+tradeProcessedEvent.originatingCountry).css('background-color', '#c9df79');
	setTimeout(function() {
		$("#tp_id_"+tradeProcessedEvent.originatingCountry).css('background-color', 'transparent');
	}, 1000);	
	
};
function cleanTextForId(id){
	return id.replace('.', '-')
}

function flashCountryOnMap(country){
	setColourOnMap(country, '#c9df79');
	setTimeout(function() {setColourOnMap(country, '#f4f3f0');}, 1000);	
};

function setColourOnMap(country, colour){
	$('#vmap').vectorMap('set', 'colors', JSON.parse("{\"" + country +"\" : \""+colour+"\"}"));	
};
