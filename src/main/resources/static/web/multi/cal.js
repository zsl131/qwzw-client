$(function() {
    $(".food-nav-ul").find("li").click(function() {
        $(".food-nav-ul").find("li.active").removeClass("active");
        $(this).addClass("active");

        $(".nav-con-div").each(function() {$(this).css("display", "none")});
        $(("."+$(this).attr("targetCls"))).css("display", "block");
    });
});