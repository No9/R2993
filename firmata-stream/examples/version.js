/*  var serialport = require("serialport");
serialport.list(function (err, ports) {
    ports.forEach(function(port) {
      console.log(port.comName);
      console.log(port.pnpId);
      console.log(port.manufacturer);
    });
  });
*/
var streamboard = require('../');
var stm = streamboard.board('USB\VID_2341&PID_0043\64131383331351E03130');
stm.pipe(process.stdout);
