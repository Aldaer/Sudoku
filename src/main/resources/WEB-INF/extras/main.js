const NUMPAD = $(".setnumber");

function cellClick(el) {
    $('td').removeClass("selected");
    cellid = el.getAttribute("id");
    $('#coords').text(cellid);
    $('input[name="cell"]').val(cellid);
    $(el).addClass("selected");
    $(el).append(NUMPAD);
    NUMPAD.removeClass("hidden");
    NUMPAD.css("display", "inherit");
}

function hidePad() {
    alert("Hiding");
    NUMPAD.addClass("hidden");
}

if ($('.hint').length) $('input[name="hint"]').prop("checked", true);
