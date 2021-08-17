// system = require('system')
// address = system.args[1];
//
// var page = require('webpage').create();
//
// var url = address;
//
// page.open(url, function (status) {
//     if(status != "success"){
//         let result =JSON.stringify({
//             "success": 0,
//             "url": url
//         }, undefined, 4);
//         console.log(result)
//     }else{
//         console.log(page.evaluate(function () {
//             return JSON.stringify({
//                 "success": 200,
//                 "url": url,
//                 "document.body.scrollHeight": document.body.scrollHeight,
//
//                 "document.body.offsetHeight": document.body.offsetHeight,
//
//                 "document.documentElement.clientHeight": document.documentElement.clientHeight,
//
//                 "document.documentElement.scrollHeight": document.documentElement.scrollHeight
//
//             }, undefined, 4);
//
//         }));
//     }
//
//     phantom.exit();
//
// });
system = require('system')
address = system.args[1];

var page = require('webpage').create();
page.viewportSize = {width: 440, height: 900};

var url = address;

page.open(url, function () {
    console.log(page.evaluate(function () {
        return JSON.stringify({
            "window.screen.width": window.screen.width,
            "window.screen.height": window.screen.height,
            "document.body.scrollHeight": document.body.scrollHeight,
            "document.body.offsetHeight": document.body.offsetHeight,
            "document.documentElement.clientHeight": document.documentElement.clientHeight,
            "document.documentElement.scrollHeight": document.documentElement.scrollHeight,
            "document.documentElement.clientWidth": document.documentElement.clientWidth,
            "document.documentElement.scrollWidth": document.documentElement.scrollWidth
        }, undefined, 4);

    }));

    phantom.exit();

});