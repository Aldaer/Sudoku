const NUMPAD = $("#numpad");

function cellClick(el) {
    $('td').removeClass("selected");
    cellid = el.getAttribute("id");
    $('#coords').text(cellid);
    $('input[name="cell"]').val(cellid);
    $(el).addClass("selected");
    $(el).append(NUMPAD);
    NUMPAD.addClass("popup");
}

function hidePad(event) {
    NUMPAD.removeClass("popup");
    event.stopPropagation();
}

function numpush(event) {
    alert("Number pushed: " + alert(event.target.innerText));
    event.stopPropagation();
}

if ($('.hint').length) $('input[name="hint"]').prop("checked", true);
