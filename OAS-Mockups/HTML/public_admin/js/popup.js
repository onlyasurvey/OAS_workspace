var newWindow = null;

function closeWin(){
	if (newWindow != null){
		if(!newWindow.closed)
			newWindow.close();
	}
}

function popUpWin(url, type, strWidth, strHeight){
	
	closeWin();
		
	type = type.toLowerCase();
	
	// get the width of the screen
	screenWidth = screen.availWidth;
	screenHeight = screen.availHeight;
	
	// Set the pop-ups location
	windowTop = ((screenHeight - strHeight) / 2) + 'px';
	windowLeft = ((screenWidth - strWidth) / 2) + 'px';
	
	if (type == "fullscreen"){
		strWidth = screen.availWidth;
		strHeight = screen.availHeight;
	}
	
	var tools="";
	//if (type == "standard") tools = "resizable,toolbar=no,location=no,scrollbars=yes,menubar=no,width="+strWidth+",height="+strHeight+",top=50,left=50";
	if (type == "standard") tools = "resizable,toolbar=no,location=no,scrollbars=yes,menubar=no,width="+strWidth+",height="+strHeight+",top="+windowTop+",left="+windowLeft;
	if (type == "console" || type == "fullscreen") tools = "resizable,toolbar=no,location=no,scrollbars=no,width="+strWidth+",height="+strHeight+",left=0,top=0";
	newWindow = window.open(url, 'newWin', tools);
	newWindow.focus();
}

function doPopUp(e)
{
//set defaults - if nothing in rel attrib, these will be used
var t = "standard";
var w = "400";
var h = "300";

// Set window location based on link location on the page
//coors = findPos( obj );
///topOffset = 40 ;
//coors = findPos( this );
//windowTop = topOffset + coors[1] + 'px';
//windowLeft = coors[0] + 'px';
//alert(windowTop+" - "+windowLeft) ;

//look for parameters
attribs = this.rel.split(" ");
if (attribs[1]!=null) {t = attribs[1];}
if (attribs[2]!=null) {w = attribs[2];}
if (attribs[3]!=null) {h = attribs[3];}
//call the popup script
popUpWin(this.href,t,w,h);
//cancel the default link action if pop-up activated
if (window.event) 
	{
	window.event.returnValue = false;
	window.event.cancelBubble = true;
	} 
else if (e) 
	{
	e.stopPropagation();
	e.preventDefault();
	}
}

function findPopUps()
{
var popups = document.getElementsByTagName("a");
e = "" ;
for (i=0;i<popups.length;i++)
	{
	if (popups[i].rel.indexOf("popup")!=-1)
		{
		// attach popup behaviour
		//popups[i].onclick = popups[i].onkeypress = doPopUp(e, popups[i] );
		popups[i].onclick = popups[i].onclick = doPopUp ;
		
		/*
		// add popup indicator
		if (popups[i].rel.indexOf("noicon")==-1)
			{
			 popups[i].style.backgroundImage = "url(images/pop-up.gif)";
			popups[i].style.backgroundPosition = "0 center";
			popups[i].style.backgroundRepeat = "no-repeat";
			popups[i].style.paddingLeft = "15px";
			}
			
		// add info to title attribute to alert fact that it's a pop-up window
		popups[i].title = popups[i].title + " [Opens in pop-up window]";
		*/
		
		
		}
	}
}
addEvent(window, 'load', findPopUps, false);


/*  Find an opjects position*/

var hide  = true;

function showhide(obj)
{
	var x = document.getElementById('testP');
	hide = !hide;
	x.style.visibility = (hide) ? 'hidden' : 'visible';
	setLyr(obj,'testP');
}

function setLyr(obj,lyr)
{
	var coors = findPos(obj);
	if (lyr == 'testP') coors[1] -= 50;
	var x = document.getElementById(lyr);
	x.style.top = coors[1] + 'px';
	x.style.left = coors[0] + 'px';
}

function findPos(obj) {
	var curleft = curtop = 0;
	if (obj.offsetParent) {
		curleft = obj.offsetLeft
		curtop = obj.offsetTop
		while (obj = obj.offsetParent) {
			curleft += obj.offsetLeft
			curtop += obj.offsetTop
		}
	}
	return [curleft,curtop];
}

