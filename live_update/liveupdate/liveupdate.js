/*
 * Marconi SC Live Wind Data
 * $Id: liveupdate.js,v 1.5 2010/11/16 00:26:22 david Exp $
 *
 * Copyright 2010, David Ball
 *
 * Licensed under the GPLv3 licence. See <http://www.gnu.org/licenses/>.
 */
 
 
 
/*******************************************************************************
 * Work out the path to this script so we can load associated files such as
 * images that should be in the same folder.
 ******************************************************************************/
var scr=document.getElementsByTagName('script');
var src=scr[scr.length-1].getAttribute("src");
var mvScriptDir = src.substring(0, src.lastIndexOf("/"));
if (mvScriptDir.length > 0) { mvScriptDir = mvScriptDir + "/" }


/*******************************************************************************
 * For IE, include excanvas because this browser does not support Canvas natively.
 ******************************************************************************/
document.write('<!--[if IE]><script type="text/javascript" src="' + mvScriptDir +'excanvas.compiled.js"></script><![endif]-->');



/*******************************************************************************
 * CONSTANTS
 ******************************************************************************/
var mvSpeedScaleMax = 67.0
var mvSpeedZeroDeg = 40.0
var mvSpeedZeroRad = Math.PI * mvSpeedZeroDeg / 180.0;
var mvSpeedRatioDeg = (360.0 - (2 * mvSpeedZeroDeg)) / mvSpeedScaleMax

var mvFrameIntervalMs = 100;  /* 10 fps */

var mvFileRetryIntervalMs = 2000; /* 2 seconds. */

var mvDataFile = "http://www.marconi-sc.org.uk/WeatherStation/live_update.php";

/*******************************************************************************
 * VARIABLES
 ******************************************************************************/
var mvXmlHttp = null;
var mvMSHttpInitFailed = false;

var mvArrReadings = null; // Array();

var mvCtxDir = null;
var mvCtxSpeed = null;

var mvWidth = 0;
var mvHeight = 0;
var mvInterval = null;

var mvDivDateText  = null;
var mvDivTimeText  = null;
var mvDivSpeedText = null;
var mvDivDirText   = null;
var mvDivLog       = null;

var mvFileHeaderTime = null;
var mvFileReadingInterval = null;
var mvFileReadingCount = null;
var mvFileAnimateStartTime = null;
var mvFileAnimateEndTime = null;

var mvStartAngle = 0.0;
var mvStartSpeed = 0.0;
var mvLastFileHeaderTime = 0;



/*******************************************************************************
 * IE specific code to create XMLHTTP object. Other browsers won't execute this code.
 ******************************************************************************/
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

/*******************************************************************************
 * Non-IE XMLHTTP creation.
 ******************************************************************************/
if (mvXmlHttp == null && mvMSHttpInitFailed == false ) {
    try {
        mvXmlHttp = new XMLHttpRequest();
    } catch (e) {
        alert("Your browser does not support the JavaScript features required by this page (mvXmlHttpRequest). Upgrade to newer browser to use this page")
    }
}

/*******************************************************************************
 * FUNCTIONS
 ******************************************************************************/
function log(s)
{
	var txt=document.createTextNode((new Date()).toLocaleString() + " : " + s + "\n");
    if (mvDivLog) {
        mvDivLog.appendChild(txt)
    }
}


function toRadians(pDeg) {
    return (Math.PI * pDeg / 180.0)
}

function roundNumber(num, dec) {
    var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
    return result;
}

function stateChange() {
    if (mvXmlHttp.readyState==4) {
    
        if (mvInterval == null && mvArrReadings == null) {
            mvArrReadings = removeEmptyLines(mvXmlHttp.responseText.split('\n'));

            if (mvArrReadings.length > 1) {
                /* Have at least header and one reading. */
                var arrHdr = mvArrReadings[0].split(',');
                if (arrHdr.length == 3 && arrHdr[0] == "WEATHER") {
                    mvFileHeaderTime = parseInt(arrHdr[1]);
                    if (mvFileHeaderTime > mvLastFileHeaderTime) {
                        mvLastFileHeaderTime = mvFileHeaderTime;
                        mvFileReadingInterval = parseInt(arrHdr[2]);
                        mvFileReadingCount = mvArrReadings.length - 1; /* First entry is header. */
                        mvFileAnimateStartTime = new Date().getTime();
                        mvFileAnimateEndTime = mvFileAnimateStartTime + (mvFileReadingInterval * mvFileReadingCount);
                        animate();
                    } else {
                        log("No new data available.");
                        retryFile();
                    }
                } else {
                    log("Invalid data header: '" + mvArrReadings[0] + "'");
                    retryFile();
                }
            } else {
                log("Invalid or empty data file.");
                retryFile();
            }
        } else {
            log("State change whilst still processing last file.");
        }
    }
}


function retryFile(mode) {
    if (mvInterval != null) {
        clearInterval(mvInterval);
        mvInterval = null;
    }
    
    if (mvArrReadings != null) {
        mvArrReadings = null;
    }

    if (mode == 1) {
        fetchData();
    } else {
        mvInterval = setInterval( function(){ retryFile(1); }, mvFileRetryIntervalMs );
    }
}


function removeEmptyLines(arrStr) {
    var arrNew = Array();
    var j = 0;
    for (var i=0; i < arrStr.length; i++) {
        if (arrStr[i] != "") {
            arrNew[j] = arrStr[i];
            j++;
        }
    }
    
    return arrNew;
}
    

function fetchData() {
    if (mvXmlHttp)
    {
        /* Use POST not GET to avoid caching problems in IE. */
        log("Fetching data file '" + mvDataFile + "'.");
        mvXmlHttp.open("POST", mvDataFile, true);
        mvXmlHttp.onreadystatechange=stateChange;
        mvXmlHttp.send(null);
    }
}

function draw(tm, speed, angle) {

    /*
     * Date and Time
     */
    var dt = new Date(parseInt(tm));
    var dtStrs = dt.toDateString().split(' ');
    mvDivDateText.innerHTML = dtStrs[0] + " " + dtStrs[1] + " " + dtStrs[2];
    mvDivTimeText.innerHTML = dt.toLocaleTimeString()

    /*
     * Draw Direction Part
     */
    mvCtxDir.save();
    mvCtxDir.clearRect(0, 0, mvWidth, mvHeight);

    mvCtxDir.translate(mvWidth/2, mvHeight/2);
    mvCtxDir.rotate(toRadians(angle));

    mvCtxDir.fillStyle = "red";
    mvCtxDir.beginPath();
    mvCtxDir.moveTo(-mvWidth/20,0);
    mvCtxDir.lineTo(mvWidth/20,0);
    mvCtxDir.lineTo(mvWidth/20,-mvWidth/2.9);
    mvCtxDir.lineTo(mvWidth/10,-mvWidth/2.9);
    mvCtxDir.lineTo(0,-mvWidth/2.3);
    mvCtxDir.lineTo(-mvWidth/10,-mvWidth/2.9);
    mvCtxDir.lineTo(-mvWidth/20,-mvWidth/2.9);
    mvCtxDir.closePath();
    mvCtxDir.fill();
    mvCtxDir.restore();
    
    mvDivDirText.innerHTML = roundNumber(angle,0) + "&deg;";


    /*
     * Draw Speed Part
     */

    mvCtxSpeed.save();
    mvCtxSpeed.clearRect(0, 0, mvWidth, mvHeight);
    mvCtxSpeed.fillStyle = "red";
    mvCtxSpeed.beginPath();
    mvCtxSpeed.arc(mvWidth/2, mvHeight/2, mvWidth/40,0,2*Math.PI,false);
    mvCtxSpeed.closePath();
    mvCtxSpeed.fill();

    mvCtxSpeed.translate(mvWidth/2, mvHeight/2);
    mvCtxSpeed.rotate(Math.PI + toRadians(mvSpeedZeroDeg + speed * mvSpeedRatioDeg));

    mvCtxSpeed.beginPath();
    mvCtxSpeed.moveTo(-mvWidth/40,0);
    mvCtxSpeed.lineTo(mvWidth/40,0);
    mvCtxSpeed.lineTo(0,-mvWidth/4);
    mvCtxSpeed.closePath();
    mvCtxSpeed.fill();
    mvCtxSpeed.restore();

    mvDivSpeedText.innerHTML = roundNumber(speed,0) + "kts";
}


function animate() {
    var now = new Date().getTime();
    var readingOffset = (now - mvFileAnimateStartTime)/mvFileReadingInterval;
    var readingIndex  = Math.floor(readingOffset);
    var readingRatio  = readingOffset - readingIndex; 
    
    if (now <= mvFileAnimateEndTime && readingIndex < mvArrReadings.length) {
    

        var tm    = (now - mvFileAnimateStartTime) + mvFileHeaderTime;
        var speed = 0;
        var angle = 0;

        if (readingIndex == 0) {
            /*
             * We're starting from last reading in the previous file.
             * Remeber also that first entry in mvArrReadings is the header.
             */
            speed = mvStartSpeed;
            angle = mvStartAngle;
        } else {
            var vals1 = mvArrReadings[readingIndex].split(',');
            speed = parseFloat(vals1[0]);
            angle = parseFloat(vals1[1]);
        }
        
        if (readingIndex + 1 < mvArrReadings.length) {
            /* Apply linear interpolation to calculate speed/angle between two readings */
            var vals2 = mvArrReadings[readingIndex + 1].split(',');
            var speed2 = parseFloat(vals2[0]);
            var angle2 = parseFloat(vals2[1]);
            speed = speed + (readingRatio * (speed2 - speed));
            
            var angleDelta = angle2 - angle;
            if (angleDelta > 180) {
                angleDelta = angleDelta - 360;
            } else if (angleDelta < -180) {
                angleDelta = angleDelta + 360;
            }
            angle = ((angle + (readingRatio * angleDelta)) + 3600) % 360; /* Make sure final angle is >=0 <=359.9999 */
        }
        draw(tm, speed, angle);

        /* animate(); */
        if (mvInterval == null) {
            mvInterval = setInterval( function(){ animate(); }, mvFrameIntervalMs );
        }

    } else {
        /* Save last reading from this file. Use as initial readings for next file. */
        var vals = mvArrReadings[mvArrReadings.length - 1].split(',');
        mvStartSpeed = parseFloat(vals[0]);
        mvStartAngle = parseFloat(vals[1]);

        /* Clear the interval timer. */
        clearInterval(mvInterval);
        mvInterval = null;
        
        /* null the array. */
        mvArrReadings = null;
        
        /* Load the next file. */
        fetchData();
    }
}


function initDiv(pd)
{
    pd.style.position = "absolute";
    pd.style.top = 0;
    pd.style.left = 0;
    pd.style.width = mvWidth;
    pd.style.height = mvWidth;
}

function initDirTextDiv(pd) {
    pd.style.position = "absolute";
    pd.style.bottom = mvWidth/100;
    pd.style.right = mvWidth/50;
    pd.style.fontFamily = '"Arial", sans-serif';
    pd.style.fontSize = (mvWidth / 17) + 'px';
}


function initSpeedTextDiv(pd) {
    pd.style.position = "absolute";
    pd.style.bottom = mvWidth/100;
    pd.style.left = mvWidth/50;
    pd.style.fontFamily = '"Arial", sans-serif';
    pd.style.fontSize = (mvWidth / 17) + 'px';
}

function initDateTextDiv(pd) {
    pd.style.position = "absolute";
    pd.style.top = mvWidth/100;
    pd.style.left = mvWidth/50;
    pd.style.fontFamily = '"Arial", sans-serif';
    pd.style.fontSize = (mvWidth / 17) + 'px';
}

function initTimeTextDiv(pd) {
    pd.style.position = "absolute";
    pd.style.top = mvWidth/100;
    pd.style.right = mvWidth/50;
    pd.style.fontFamily = '"Arial", sans-serif';
    pd.style.fontSize = (mvWidth / 17) + 'px';
}



function initWeather()
{
    mvDivLog = document.getElementById('log_messages');
    var divWeather = document.getElementById('weather');

    if ( divWeather != null )
    {
        log ('Have weather div');

        divWeather.innerHTML='<img src="' + mvScriptDir + 'bezel_image.png" style="vertical-align:middle; height:100%">';

        mvWidth = parseInt(divWeather.style.width);
        mvHeight = parseInt(divWeather.style.height);

        var divDirImage = document.createElement('div');
        var divCanvasDir = document.createElement('div');
        var divSpeedImage = document.createElement('div');
        var divCanvasSpeed = document.createElement('div');
        
        initDiv(divDirImage);
        initDiv(divCanvasDir);
        initDiv(divSpeedImage);
        initDiv(divCanvasSpeed);

        divWeather.appendChild(divDirImage);
        divWeather.appendChild(divCanvasDir);
        divWeather.appendChild(divSpeedImage);
        divWeather.appendChild(divCanvasSpeed);

        mvDivDirText = document.createElement('div');
        initDirTextDiv(mvDivDirText);
        divWeather.appendChild(mvDivDirText);

        mvDivSpeedText = document.createElement('div');
        initSpeedTextDiv(mvDivSpeedText);
        divWeather.appendChild(mvDivSpeedText);

        mvDivDateText = document.createElement('div');
        initDateTextDiv(mvDivDateText);
        divWeather.appendChild(mvDivDateText);

        mvDivTimeText = document.createElement('div');
        initTimeTextDiv(mvDivTimeText);
        divWeather.appendChild(mvDivTimeText);


        divDirImage.innerHTML='<img src="' + mvScriptDir + 'dir_image.png" style="vertical-align:middle; height:100%">';
        divSpeedImage.innerHTML='<img src="' + mvScriptDir + 'speed_image.png" style="vertical-align:middle; height:100%">';
        

        var canvasDir = document.createElement('canvas');
        var canvasSpeed = document.createElement('canvas');

        divCanvasDir.appendChild(canvasDir);
        divCanvasSpeed.appendChild(canvasSpeed);

        canvasDir.setAttribute("width",mvWidth);
        canvasDir.setAttribute("height",mvHeight);
        canvasDir.style.width =  mvWidth + "px";
        canvasDir.style.height = mvHeight + "px";

        canvasSpeed.setAttribute("width",mvWidth);
        canvasSpeed.setAttribute("height",mvHeight);
        canvasSpeed.style.width =  mvWidth + "px";
        canvasSpeed.style.height = mvHeight + "px";

        if (!canvasDir.getContext) {
            /* IE - Need to initialise dynamically generated canvas elements */
            G_vmlCanvasManager.initElement(canvasDir);
            G_vmlCanvasManager.initElement(canvasSpeed);
        }
        
        mvCtxDir = canvasDir.getContext("2d");
        mvCtxSpeed = canvasSpeed.getContext("2d");
    } else {
        log ( 'No weather div found' );
    }
    fetchData();
}


window.onload = initWeather;
