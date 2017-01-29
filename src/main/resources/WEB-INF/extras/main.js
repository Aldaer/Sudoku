function cellClick(el) {
    $('td').removeClass("selected");

    $('#coords').text(el.getAttribute("id"));
    $(el).addClass("selected");
}