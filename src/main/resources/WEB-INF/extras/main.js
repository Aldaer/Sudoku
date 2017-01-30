function cellClick(el) {
    $('td').removeClass("selected");
    cellid = el.getAttribute("id");
    $('#coords').text(cellid);
    $('input[name="cell"]').val(cellid);
    $(el).addClass("selected");
}

if ($('.hint').length) $('input[name="hint"]').prop("checked", true);
