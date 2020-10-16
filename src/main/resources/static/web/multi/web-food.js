var displayClass = "has_display";
var currentCateId = '';
function onCategoryClick(obj) {
    $(obj).parents(".category-list").find(".current-href").removeClass("current-href");
    $(obj).addClass("current-href");

    const cateId = $(obj).attr("cateId");
    //console.log(cateId)
    currentCateId = cateId;
    setFoodByKeyword("");
    $("input[name='keyword']").val("");
}

function setFoodByKeyword(keyword) {
    $("ul.food-list-ul").find("li").each(function() {
        $(this).css("display", "none");
        $(this).addClass(displayClass);
    });
    $("ul.food-list-ul").find("li").each(function() {
        try {
            const curCateId = $(this).attr("cateId");
            const name = $(this).attr("foodName");
            const sn = $(this).attr("sn");
            const nameLetter = $(this).attr("nameLetter");
            if((!currentCateId || curCateId==currentCateId) &&
                (name.indexOf(keyword)>=0 || sn.indexOf(keyword)>=0 || nameLetter.indexOf(keyword.toUpperCase())>=0)) {
                $(this).css("display", "block");
                $(this).removeClass(displayClass);
            }
        } catch(e) {}

    });
}

function onSearch(obj) {
    const val = $(obj).val();
    //console.log(val)
    setFoodByKeyword(val);
}