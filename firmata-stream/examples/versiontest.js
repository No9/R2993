/*var serialport = require("serialport");
serialport.list(function (err, ports) {
    ports.forEach(function(port) {
      console.log(port);
    });
  });
  */
  
  
var version = require('./version').REPORT_VERSION;
var streamboard = require('../');
var stm = streamboard.board('COM6');
console.log(version);

stm.pipe(process.stdout);
stm.write(version);