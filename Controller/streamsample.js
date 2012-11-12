var sys = require("sys");
var net = require("net");
var Stream = require('stream');

var times = 0;

var client = net.connect( 4444, '192.168.1.38', function() {
	var iv = setInterval(function () {
        client.write(times + ' ');
        if (++times === 5) {
		    client.write('\n');
            clearInterval(iv);
        }
    }, 1000);
});

client.on('data', function(data) {
  console.log(data.toString());
  //client.end();
});

client.on('end', function() {
  console.log('client disconnected');
});