var exec = require('cordova/exec');

module.exports = {
    printString: function (text, resolve, reject) {
        exec(resolve, reject, "smartTillPrinter", "printString", [text]);
    }  
}