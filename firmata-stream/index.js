var SerialPort = require('serialport').SerialPort,
    util = require('util'),
    events = require('events'), 
	Stream = require('stream').Stream;
	
/**
 * constants
 */

var PIN_MODE = 0xF4,
    REPORT_DIGITAL = 0xD0,
    REPORT_ANALOG = 0xC0,
    DIGITAL_MESSAGE = 0x90,
    START_SYSEX = 0xF0,
    END_SYSEX = 0xF7,
    QUERY_FIRMWARE = 0x79,
    REPORT_VERSION = 0xF9,
    ANALOG_MESSAGE = 0xE0,
    CAPABILITY_QUERY = 0x6B,
    CAPABILITY_RESPONSE = 0x6C,
    PIN_STATE_QUERY = 0x6D,
    PIN_STATE_RESPONSE = 0x6E,
    ANALOG_MAPPING_QUERY = 0x69,
    ANALOG_MAPPING_RESPONSE = 0x6A,
    I2C_REQUEST = 0x76,
    I2C_REPLY = 0x77,
    I2C_CONFIG = 0x78,
    STRING_DATA = 0x71,
    SYSTEM_RESET = 0xFF;
	
	exports.board = function (port) {
			var stream = new Stream();
			stream.readable = true;
			
			this.sp = new SerialPort(port, {
                baudrate: 57600,
                buffersize: 1
            });
			
			this.sp.write(REPORT_VERSION);
			this.sp.on('data', function(data) {
				stream.emit('data', JSON.stringify(data));
			});
			
			return stream;
			
	}
	
	