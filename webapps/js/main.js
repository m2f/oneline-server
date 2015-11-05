window.document.onkeydown = function (e){
    if (!e){
		e = event;
    }
    if (e.keyCode == 27){
    	lightboxClose();
    }
}

function lightboxOpen(){
	window.scrollTo(0,0);
	document.getElementById('overlay-modal').style.display='block';
	document.getElementById('overlay-background').style.display='block';  
}

function lightboxClose(){
	document.getElementById('overlay-modal').style.display='none';
	document.getElementById('overlay-background').style.display='none';
}