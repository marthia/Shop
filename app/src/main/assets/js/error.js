jQuery(document).ready(function(){
	if(typeof(novalids) !== 'undefined' && novalids.length > 0)
	{
		var res = novalids.split(",");
		for(i=0; i < res.length; i++)
		{
			if(jQuery('#' + res[i]).length)
			{
				jQuery('#' + res[i]).addClass('rsform-error');
			}
		}
	}
	if(typeof(novalidcomids) !== 'undefined' && novalidcomids.length > 0)
	{
		var res = novalidcomids.split(",");
		for(i=0; i < res.length; i++)
		{
			if(jQuery('#component' + res[i]).length)
			{
				jQuery('#component' + res[i]).attr('class', 'formError');
			}
		}
	}
	
	if(typeof(formvalues) !== 'undefined' && formvalues.length > 0)
	{
		var res = formvalues.split("&");
		var vals = {};
		
		for(i=0; i < res.length; i++)
		{
			var tmp = res[i].split("=");
		    if(tmp[0] in vals == false){
		    	vals[tmp[0]] = []; // must initialize the sub-object, otherwise will get 'undefined' errors
		    }
		    
		    vals[tmp[0]].push(tmp[1]);
		}
				
	    for (key in vals){	    	
	    	var elem = jQuery("*[name='" + key + "']");
	    	
			if(elem.length)
			{
				var type = elem.attr('type');
				var tagn = elem.prop("tagName");
				
				if(tagn == "INPUT" && type == "text")
				{
					elem.val(vals[key][0]);
				}
				
				if(tagn == "INPUT" && type == "hidden")
				{
					elem.val(vals[key][0]);
				}				
				
				if(tagn == "INPUT" && type == "textarea")
				{
					elem.html(vals[key][0]);
				}				
				
				if(tagn == "INPUT" && (type == "checkbox" || type == "radio"))
				{
					elem.each(function(i){
						if(vals[key].indexOf(jQuery(this).attr('value')) != -1)
							jQuery(this).attr('checked', 'checked');
						else
							jQuery(this).attr('checked', false);
					});
				}
				
				if(tagn == "SELECT")
				{
					elem.find("option").each(function(i){
						if(vals[key].indexOf(jQuery(this).attr('value')) != -1)
							jQuery(this).attr('selected', 'selected');
						else
							jQuery(this).attr('selected', false);
					});
				}
			}
	    }
	}
	
	

});