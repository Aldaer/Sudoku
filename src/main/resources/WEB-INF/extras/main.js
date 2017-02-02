const NUMPAD = $("#numpad");
const SET_FORM = $("#set");

function cellClick(el) {
    $('td').removeClass("selected");
    $(el).addClass("selected");
    $(el).append(NUMPAD);
    NUMPAD.addClass("popup");
    NUMPAD.data("id",  el.getAttribute("id"));
}

function hidePad(event) {
    NUMPAD.removeClass("popup");
    event.stopPropagation();
    event.preventDefault();
}

function numpush(event) {
    pushed = event.target.innerText;
    if (isNaN(pushed)) pushed = 0;
    submitKey(pushed);
}

function submitKey(x) {
    $('input[name="cell"]').val(NUMPAD.data("id"));
    $('input[name="value"]').val(x);
    SET_FORM.submit();
}

if ($('.hint').length) $('input[name="hint"]').prop("checked", true);

$(document).keypress(function(e) {
    if (!NUMPAD.hasClass("popup")) return;
    switch(e.which) {
      case 0:
        hidePad(e);
        break;
      case 32:
        submitKey(0);
        break;
      default:
        key = e.which - 48;
        if (key >= 0 && key <= 9) submitKey(key);
    }
});
