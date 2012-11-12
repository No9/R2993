var sys = require("sys");
var net = require("net");
var firmata = require("firmata");	
var times = 0;
var client = net.connect( 4444, '192.168.1.37', function() {
			console.log("Starting Board");
			var ledPin = 13;
			var board = new firmata.Board(client, function(err){

				if (err) {
					console.log(err);
					return;
				}
				
				console.log('connected');
				//console.log('Firmware: ' + board.firmware.name + '-' + board.firmware.version.major + '.' + board.firmware.version.minor);

				var ledOn = true;
				board.pinMode(ledPin, board.MODES.OUTPUT);

				setInterval(function(){

				if (ledOn) {
					console.log('+');
					board.digitalWrite(ledPin, board.HIGH);
				}
				else {
					console.log('-');
					board.digitalWrite(ledPin, board.LOW);
				}

				ledOn = !ledOn;

				},500)
			  //arduino is ready to communicate
			}); 
});

client.on('data', function(data) {
  console.log(data.toString());
  client.end();
});

client.on('end', function() {
  console.log('client disconnected');
});

client.baudrate = 57600;
client.buffersize = 1;