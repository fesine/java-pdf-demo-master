function Base64() {
    // private property
    _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    // public method for encoding
    this.encode = function (input) {
        var output = "";
        var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
        var i = 0;
        input = _utf8_encode(input);
        while (i < input.length) {
            chr1 = input.charCodeAt(i++);
            chr2 = input.charCodeAt(i++);
            chr3 = input.charCodeAt(i++);
            enc1 = chr1 >> 2;
            enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            enc4 = chr3 & 63;
            if (isNaN(chr2)) {
                enc3 = enc4 = 64;
            } else if (isNaN(chr3)) {
                enc4 = 64;
            }
            output = output +
                _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
                _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
        }
        return output;
    }

    // public method for decoding
    this.decode = function (input) {
        var output = "";
        var chr1, chr2, chr3;
        var enc1, enc2, enc3, enc4;
        var i = 0;
        input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
        while (i < input.length) {
            enc1 = _keyStr.indexOf(input.charAt(i++));
            enc2 = _keyStr.indexOf(input.charAt(i++));
            enc3 = _keyStr.indexOf(input.charAt(i++));
            enc4 = _keyStr.indexOf(input.charAt(i++));
            chr1 = (enc1 << 2) | (enc2 >> 4);
            chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            chr3 = ((enc3 & 3) << 6) | enc4;
            output = output + String.fromCharCode(chr1);
            if (enc3 != 64) {
                output = output + String.fromCharCode(chr2);
            }
            if (enc4 != 64) {
                output = output + String.fromCharCode(chr3);
            }
        }
        output = _utf8_decode(output);
        return output;
    }

    // private method for UTF-8 encoding
    _utf8_encode = function (string) {
        string = string.replace(/\r\n/g, "\n");
        var utftext = "";
        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);
            if (c < 128) {
                utftext += String.fromCharCode(c);
            } else if ((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            } else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }
        }
        return utftext;
    }

    // private method for UTF-8 decoding
    _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;
        while (i < utftext.length) {
            c = utftext.charCodeAt(i);
            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            } else if ((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i + 1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            } else {
                c2 = utftext.charCodeAt(i + 1);
                c3 = utftext.charCodeAt(i + 2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }
        return string;
    }
}

// /////////////////////////////////
system = require('system'); // ????????????
//?????????????????????
var base64JsonStr = system.args[1];
//????????????????????????
var screenshotPath = system.args[2];
// ???base64??????
var paramsJsonStr = new Base64().decode(base64JsonStr);
// ?????????json??????
var jsonObj = JSON.parse(paramsJsonStr);
//Echarts???Option??????
var option = jsonObj.option;
//????????????????????????
var clipWidth = jsonObj.clipWidth;
var clipHeight = jsonObj.clipHeight;

function Convert(params) {
    this.params = params;
    this.external = {
        JQUERY3: 'jquery-3.2.1.min.js',
        ECHARTS3: 'echarts.min.js'
    };
    this.page = require('webpage').create(); // ?????????
    this.page.viewportSize = {width: clipWidth, height: clipHeight};
    // ????????????console,??????????????????????????????????????????
    this.page.onConsoleMessage = function (msg, lineNum, sourceId) {
        console.log('CONSOLE: ' + msg + ' (from line #' + lineNum + ' in "' + sourceId + '")');
    };
    var instance = this;
    this.page.onError = function (msg, trace) {
        var msgStack = ['PHANTOM ERROR: ' + msg];
        if (trace && trace.length) {
            msgStack.push('TRACE:');
            trace.forEach(function (t) {
                msgStack.push(' -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t.function ? ' (in function ' + t.function + ')' : ''));
            });
        }
        console.error(msgStack.join('\n'));
        //??????,??????????????????
        instance.output("", false, msg);
    };
    this.page.onLoadStarted = function () {
        // console.log('Start loading...');
    };
    this.page.onLoadFinished = function (status) {
        // console.log('Loading finished.');
    };
    // ??????????????????
    this.page.onResourceReceived = function (res) {
        // console.log('Response (#' + res.id + ', stage =' + res.stage + ', code = ' + res.status + ', redirect= ' + res.redirectURL + ') ');
    };
    // console.log("???????????????");
}

Convert.prototype.init = function () {
    var instance = this;
    instance.page.open("about:blank", function (status) {
        // ??????????????????JS???????????????????????????
        var hasJquery = instance.page.injectJs(instance.external.JQUERY3);
        var hasEchart = instance.page.injectJs(instance.external.ECHARTS3);
        // ??????js??????????????????
        if (!hasJquery || !hasEchart) {
            instance.output("Could not found " + instance.external.JQUERY3 + " or " + instance.external.ECHARTS3, false);
        }
        // ?????????????????????????????????; ????????????????????????????????????
        instance.page.evaluate(instance.createEchartsDom, instance.params);
        // ????????????????????????????????????????????????
        instance.page.clipRect = {
            top: 0,
            left: 0,
            width: clipWidth,
            height: clipHeight
        };
        // ??????
        var result = instance.render();
        // ??????????????????????????????????????????
        instance.output(result, true);
    });
}

Convert.prototype.render = function () {
    var instance = this;
    switch (instance.params.type) {
        case 'file':
            instance.page.render(screenshotPath);
            return screenshotPath;
        case 'base64':
        default:
            var base64 = instance.page.renderBase64('PNG');
            return base64;
    }
}

Convert.prototype.output = function (data, success, msg) {
    var instance = this;
    // console.log(success ? "[SUCCESS]:" : "[ERROR]:" + content);
    var result = {
        code: success ? 1 : 0,
        msg: undefined === msg ? success ? "success" : "failure" : msg,
        data: data
    };
    console.log(JSON.stringify(result))
    instance.page.close();
    instance.exit(instance.params);
};

Convert.prototype.exit = function (params) {
    // console.log("??????phantom");
    phantom.exit()
};


//
// ??????eCharts Dom???
// @param params ??????
// params.opt
// params.width
// params.height
// params.outfile
// params.type = 'PNG'
//
Convert.prototype.createEchartsDom = function (params) {
    var instance = this;
    var options = params.option;
    // ????????????js?????????options??????
    $('<script>')
        .attr('type', 'text/javascript')
        .appendTo(document.head);
    // ????????????,?????????????????????????????????????????????
    if (options !== undefined) {
        options.animation = false;
    }
    // body?????????????????????
    $(document.body).css('backgroundColor', 'white');
    // echarts??????
    var container = $("<div>")
        .attr('id', 'container')
        .css({
            width: params.clipWidth,
            height: params.clipHeight
        }).appendTo(document.body);

    var eChart = echarts.init(container[0]);
    eChart.setOption(options);
};

// ??????,??????
new Convert(jsonObj).init();