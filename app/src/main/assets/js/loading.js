function parseForm(event) {
    console.log("parseForm is called");
    var res = prepareFormData(this);
    Mediator.processFormData(res[0], res[1]);
    event.preventDefault();
    return false;
}

function prepareFormData(form)
{
    var hasFile = jQuery(form).find('input[type="file"]').length > 0;
    if(!hasFile)
        return [jQuery(form).serializeJSON(), 0];

    var formData = jQuery(form).serializeArray();
    var jsonObj = {};

    console.log(JSON.stringify(formData));

    for ( var i=0; i<formData.length; i++ ){
        var val = formData[i];
        jsonObj[val.name] = val.value;
    }

    return [JSON.stringify(jsonObj), 1];
}

function showForm(event) {
    Mediator.showForm();
    event.preventDefault();
    return false;
}

jQuery(document).ready(function() {
    console.log("loading.js is loaded successfully");
    var forms = document.getElementsByTagName("FORM");

    for (var i = 0; i < forms.length; i++) {
        var element = forms[i];
        if (element.addEventListener) {
            element.addEventListener('submit', parseForm, false);
        } else if (element.attachEvent) {
            element.attachEvent('onsubmit', parseForm);
        }
    }

    if (document.getElementById('continue')) {
        var element = document.getElementById('continue');
        if (element.addEventListener) {
            element.addEventListener('click', showForm, false);
        } else if (element.attachEvent) {
            element.attachEvent('onclick', showForm);
        }
    }

    jQuery("button[name=\"continue\"]").click(function(e){
        showForm(e);
    });

    jQuery("div.vote_item_holder").each(function(i) {
        var item = this;
        var parent = jQuery(item).parent('label');
        var id = parent.attr("for");
        jQuery("#" + id).click(function() {
            pageclicking(item);
        });
    });

    // handle external urls
    jQuery('a').click(function(e){

        var href = jQuery(this).attr('href');

        if (href.startsWith("#"))
            return;

        e.preventDefault();
        var ex = jQuery(this).data('ext-url');
        if(ex == 1)
            href = 'ext.url://' + href;

        Mediator.handleClickOnUrl(href);
    });


    jQuery('form input[type="file"]').on('click', function(e){
        var name = jQuery(this).attr("name");
        var exts = JSON.stringify(jQuery(this).attr("data-rsfp-exts"));
        var size = jQuery(this).attr("data-rsfp-size");
        var multiple = jQuery(this).attr('multiple');
        if (typeof multiple !== "undefined") {
            multiple = 1;
        }
        else
        {
            multiple = 0;
        }
        console.log('go to select file for: ' + name + ', ' + exts + ', ' + size + ', ' + multiple);
        console.log('go to select file for: ' + jQuery(this).html());
        Mediator.onBeforeOpenFileChooser(name, exts, size, multiple);
        var input = this;
        jQuery(document).on('focus', function(e){
            console.log('back from select file for: ' + jQuery(input).attr("name"));
//            Mediator.onAfterCloseFileChooser(jQuery(input).attr("name"));
            jQuery(document).off('focus');
        });
    });
});

function pageclicking(item) {
    var parent = jQuery(item).parents('fieldset');
    parent.find('.rsform-button').each(function(i) {
        var id = jQuery(this).attr('id');
        if (id.indexOf("Next") > -1) {
            var fn = jQuery(this).attr('onclick');
            fn = fn.replace("rsfp_changePage(", "");
            fn = fn.replace(")", "");
            var items = fn.split(",");
            for (var i = 0; i < items.length; i++) {
                items[i] = items[i].trim();
            }

            if (items[3] == "true")
                items[3] = true;
            else
                items[3] = false;

            rsfp_changePage(items[0], items[1], items[2], items[3]);
            return;
        }
    });
}

function searchInit(id) {
    jQuery(document).on("change", "#findAreaFor" + id, function() {
        jQuery("input[name='form[" + id + "]']").val(jQuery(this).val());
        jQuery("input[name='searchBoxFor" + id + "']").val("");
        //jQuery("*[name='searchBoxFor" + id + "']").val(jQuery(this).find("option:selected").text());
        //jQuery("#findAreaFor" + id).hide();
    });
}

function searchInItems(id) {
    var searchArea = jQuery("#findAreaFor" + id);
    searchArea.html("");
    var searchfor = jQuery("input[name='searchBoxFor" + id + "']").val();
    var show = false;
    var find = false;

    if (typeof(emarr) === 'undefined') {
        return;
    }

    var items = emarr[id];
    if (typeof(items) === 'undefined') {
        return;
    }

    if (searchfor.length > 2) {
        searchfor = persianUnicode(searchfor);
        for (var i = 0; i < emarr[id].length; i++) {
            var opttext = emarr[id][i].text;
            var optsearch = persianUnicode(emarr[id][i].search);
            if (optsearch.indexOf(searchfor) != -1) {
                find = true;
                var optvalue = emarr[id][i].value;
                searchArea.append(jQuery('<option value="' + optvalue + '">').text(opttext));
            }
        }
        show = true;
    } else {
        show = false;
    }

    if (!find) {
        searchArea.append(jQuery('<option value="">').text("گزینه ای یافت نشد!"));
    } else {
        searchArea.prepend(jQuery('<option value="">').text("- نتایج جستجو -"));
    }

    if (show) {
        searchArea.show();
    } else {
        searchArea.blur().hide();
    }
}

function persianUnicode(str) {
    str = replaceAll(str, "ى", "ى");
    str = replaceAll(str, "ي", "ى");
    str = replaceAll(str, "ی", "ى");
    str = replaceAll(str, "ئ", "ى");
    str = replaceAll(str, "آ", "ا");
    return str;
}

function replaceAll(str, find, replace) {
    return str.replace(new RegExp(find, 'g'), replace);
}