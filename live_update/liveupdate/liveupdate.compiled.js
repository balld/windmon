var scr=document.getElementsByTagName("script");var src=scr[scr.length-1].getAttribute("src");var mvScriptDir=src.substring(0,src.lastIndexOf("/"));if(mvScriptDir.length>0){mvScriptDir=mvScriptDir+"/"}document.write('<!--[if IE]><script type="text/javascript" src="'+mvScriptDir+'excanvas.compiled.js"><\/script><![endif]-->');var mvSpeedScaleMax=67;var mvSpeedZeroDeg=40;var mvSpeedZeroRad=Math.PI*mvSpeedZeroDeg/180;var mvSpeedRatioDeg=(360-(2*mvSpeedZeroDeg))/mvSpeedScaleMax;var mvFrameIntervalMs=100;var mvFileRetryIntervalMs=2000;var mvDataFile="testfile3.php";var mvXmlHttp=null;var mvMSHttpInitFailed=false;var mvArrReadings=null;var mvCtxDir=null;var mvCtxSpeed=null;var mvWidth=0;var mvHeight=0;var mvInterval=null;var mvDivDateText=null;var mvDivTimeText=null;var mvDivSpeedText=null;var mvDivDirText=null;var mvFileHeaderTime=null;var mvFileReadingInterval=null;var mvFileReadingCount=null;var mvFileAnimateStartTime=null;var mvFileAnimateEndTime=null;var mvStartAngle=0;var mvStartSpeed=0;var mvLastFileHeaderTime=0;
/*@cc_on @*/
/*@if (@_jscript_version >= 5)
    // Try Msxml2.XMLHTTP first.
    try {
        mvXmlHttp=new ActiveXObject("Msxml2.XMLHTTP")
    } catch (e) {
        // That didn't work, try Microsoft.XMLHTTP
        try {
            mvXmlHttp=new ActiveXObject("Microsoft.XMLHTTP")
        } catch (E) {
            alert("Can't create control required to download weather data (XMLHTTP). See help at http://support.microsoft.com/kb/324460.")
                mvMSHttpInitFailed = true;
        }
    }
@else
    alert("You must have JScript version 5 or above. Please upgrade you Microsoft Browser.")
    mvXmlHttp=false
    mvMSHttpInitFailed=true
@end @*/
if(mvXmlHttp==null&&mvMSHttpInitFailed==false){try{mvXmlHttp=new XMLHttpRequest()}catch(e){alert("Your browser does not support the JavaScript features required by this page (mvXmlHttpRequest). Upgrade to newer browser to use this page")}}function toRadians(a){return(Math.PI*a/180)}function roundNumber(b,c){var a=Math.round(b*Math.pow(10,c))/Math.pow(10,c);return a}function stateChange(){if(mvXmlHttp.readyState==4){if(mvInterval==null&&mvArrReadings==null){mvArrReadings=removeEmptyLines(mvXmlHttp.responseText.split("\n"));if(mvArrReadings.length>1){var a=mvArrReadings[0].split(",");if(a.length==3&&a[0]=="WEATHER"){mvFileHeaderTime=parseInt(a[1]);if(mvFileHeaderTime>mvLastFileHeaderTime){mvLastFileHeaderTime=mvFileHeaderTime;mvFileReadingInterval=parseInt(a[2]);mvFileReadingCount=mvArrReadings.length-1;mvFileAnimateStartTime=new Date().getTime();mvFileAnimateEndTime=mvFileAnimateStartTime+(mvFileReadingInterval*mvFileReadingCount);animate()}else{log("No new data available.");retryFile()}}else{log("Invalid data header.");retryFile()}}else{log("Invalid or empty data file.");retryFile()}}else{log("State change whilst still processing last file.")}}}function retryFile(a){if(mvInterval!=null){clearInterval(mvInterval);mvInterval=null}if(mvArrReadings!=null){mvArrReadings=null}if(a==1){fetchData()}else{mvInterval=setInterval(function(){retryFile(1)},mvFileRetryIntervalMs)}}function removeEmptyLines(d){var c=Array();var a=0;for(var b=0;b<d.length;b++){if(d[b]!=""){c[a]=d[b];a++}}return c}function fetchData(){if(mvXmlHttp){log("Fetching data file '"+mvDataFile+"'.");mvXmlHttp.open("POST",mvDataFile,true);mvXmlHttp.onreadystatechange=stateChange;mvXmlHttp.send(null)}}function draw(a,c,f){var b=new Date(parseInt(a));var d=b.toDateString().split(" ");mvDivDateText.innerHTML=d[0]+" "+d[1]+" "+d[2];mvDivTimeText.innerHTML=b.toLocaleTimeString();mvCtxDir.save();mvCtxDir.clearRect(0,0,mvWidth,mvHeight);mvCtxDir.translate(mvWidth/2,mvHeight/2);mvCtxDir.rotate(toRadians(f));mvCtxDir.fillStyle="red";mvCtxDir.beginPath();mvCtxDir.moveTo(-mvWidth/20,0);mvCtxDir.lineTo(mvWidth/20,0);mvCtxDir.lineTo(mvWidth/20,-mvWidth/2.9);mvCtxDir.lineTo(mvWidth/10,-mvWidth/2.9);mvCtxDir.lineTo(0,-mvWidth/2.3);mvCtxDir.lineTo(-mvWidth/10,-mvWidth/2.9);mvCtxDir.lineTo(-mvWidth/20,-mvWidth/2.9);mvCtxDir.closePath();mvCtxDir.fill();mvCtxDir.restore();mvDivDirText.innerHTML=roundNumber(f,0)+"&deg;";mvCtxSpeed.save();mvCtxSpeed.clearRect(0,0,mvWidth,mvHeight);mvCtxSpeed.fillStyle="red";mvCtxSpeed.beginPath();mvCtxSpeed.arc(mvWidth/2,mvHeight/2,mvWidth/40,0,2*Math.PI,false);mvCtxSpeed.closePath();mvCtxSpeed.fill();mvCtxSpeed.translate(mvWidth/2,mvHeight/2);mvCtxSpeed.rotate(Math.PI+toRadians(mvSpeedZeroDeg+c*mvSpeedRatioDeg));mvCtxSpeed.beginPath();mvCtxSpeed.moveTo(-mvWidth/40,0);mvCtxSpeed.lineTo(mvWidth/40,0);mvCtxSpeed.lineTo(0,-mvWidth/4);mvCtxSpeed.closePath();mvCtxSpeed.fill();mvCtxSpeed.restore();mvDivSpeedText.innerHTML=roundNumber(c,0)+"kts"}function animate(){var a=new Date().getTime();var j=(a-mvFileAnimateStartTime)/mvFileReadingInterval;var g=Math.floor(j);var h=j-g;if(a<=mvFileAnimateEndTime&&g<mvArrReadings.length){var n=(a-mvFileAnimateStartTime)+mvFileHeaderTime;var d=0;var f=0;if(g==0){d=mvStartSpeed;f=mvStartAngle}else{var m=mvArrReadings[g].split(",");d=parseFloat(m[0]);f=parseFloat(m[1])}if(g+1<mvArrReadings.length){var l=mvArrReadings[g+1].split(",");var b=parseFloat(l[0]);var i=parseFloat(l[1]);d=d+(h*(b-d));var c=i-f;if(c>180){c=c-360}else{if(c<-180){c=c+360}}f=((f+(h*c))+3600)%360}draw(n,d,f);if(mvInterval==null){mvInterval=setInterval(function(){animate()},mvFrameIntervalMs)}}else{var k=mvArrReadings[mvArrReadings.length-1].split(",");mvStartSpeed=parseFloat(k[0]);mvStartAngle=parseFloat(k[1]);clearInterval(mvInterval);mvInterval=null;mvArrReadings=null;fetchData()}}function log(a){document.getElementById("log_messages").innerHTML=a}function initDiv(a){a.style.position="absolute";a.style.top=0;a.style.left=0;a.style.width=mvWidth;a.style.height=mvWidth}function initDirTextDiv(a){a.style.position="absolute";a.style.bottom=mvWidth/100;a.style.right=mvWidth/50;a.style.fontFamily='"Arial", sans-serif';a.style.fontSize=(mvWidth/17)+"px"}function initSpeedTextDiv(a){a.style.position="absolute";a.style.bottom=mvWidth/100;a.style.left=mvWidth/50;a.style.fontFamily='"Arial", sans-serif';a.style.fontSize=(mvWidth/17)+"px"}function initDateTextDiv(a){a.style.position="absolute";a.style.top=mvWidth/100;a.style.left=mvWidth/50;a.style.fontFamily='"Arial", sans-serif';a.style.fontSize=(mvWidth/17)+"px"}function initTimeTextDiv(a){a.style.position="absolute";a.style.top=mvWidth/100;a.style.right=mvWidth/50;a.style.fontFamily='"Arial", sans-serif';a.style.fontSize=(mvWidth/17)+"px"}function initWeather(){var d=document.getElementById("weather");if(d!=null){log("Have weather div");d.innerHTML='<img src="'+mvScriptDir+'bezel_image.png" style="vertical-align:middle; height:100%">';mvWidth=parseInt(d.style.width);mvHeight=parseInt(d.style.height);var c=document.createElement("div");var g=document.createElement("div");var b=document.createElement("div");var f=document.createElement("div");initDiv(c);initDiv(g);initDiv(b);initDiv(f);d.appendChild(c);d.appendChild(g);d.appendChild(b);d.appendChild(f);mvDivDirText=document.createElement("div");initDirTextDiv(mvDivDirText);d.appendChild(mvDivDirText);mvDivSpeedText=document.createElement("div");initSpeedTextDiv(mvDivSpeedText);d.appendChild(mvDivSpeedText);mvDivDateText=document.createElement("div");initDateTextDiv(mvDivDateText);d.appendChild(mvDivDateText);mvDivTimeText=document.createElement("div");initTimeTextDiv(mvDivTimeText);d.appendChild(mvDivTimeText);c.innerHTML='<img src="'+mvScriptDir+'dir_image.png" style="vertical-align:middle; height:100%">';b.innerHTML='<img src="'+mvScriptDir+'speed_image.png" style="vertical-align:middle; height:100%">';var h=document.createElement("canvas");var a=document.createElement("canvas");g.appendChild(h);f.appendChild(a);h.setAttribute("width",mvWidth);h.setAttribute("height",mvHeight);h.style.width=mvWidth+"px";h.style.height=mvHeight+"px";a.setAttribute("width",mvWidth);a.setAttribute("height",mvHeight);a.style.width=mvWidth+"px";a.style.height=mvHeight+"px";if(!h.getContext){G_vmlCanvasManager.initElement(h);G_vmlCanvasManager.initElement(a)}mvCtxDir=h.getContext("2d");mvCtxSpeed=a.getContext("2d")}else{log("No weather div found")}fetchData()}window.onload=initWeather;