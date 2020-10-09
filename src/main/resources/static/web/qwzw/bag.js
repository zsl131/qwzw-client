
function onSetDetail(obj) {
    //var foodId = $(obj).attr("foodId");
    //var foodName = $(obj).attr("foodName");
    var cateId = $(obj).attr("cateId");
    var cateName = $(obj).attr("cateName");

    console.log(cateId, cateName)

    var conDialog = confirmDialog(buildHtml(cateId, cateName), "套餐内容配置", function() {

        $(conDialog).remove();
    });
}

function buildHtml(cateId, cateName) {
    var foods = selectFood(cateId);
   // console.log(foods, cateId);
    var html = '<p>已选择：<b class="canClick" title="可点击" onClick="selectFood('+cateId+')">'+cateName+'</b></p>'; //-<b class="canClick" title="可点击">'+foodName+'</b>

    for(var i=0;i<foods.length;i++) {
        html += '<input type="checkbox" foodId="'+$(foods).attr("foodId")+'" id="food_'+i+'"/><label for="food_'+i+'">&nbsp;&nbsp;'+$(foods[i]).attr("foodName")+'</label>'+
                '&nbsp;&nbsp;&nbsp;&nbsp;';
    }

    html += '<p>勾选后输入数量&nbsp;&nbsp;<input type="number" style="width: 240px" name="any_count" placeholder="输入数量"/>&nbsp;&nbsp;项</p>'+
            '<p></p>';
    return html;
}

/** 查询菜品 */
function selectFood(cateId) {
    var res = [];
    $(".single-food").each(function() {
        var cId = $(this).attr("cateId");
        if(parseInt(cId) == parseInt(cateId)) {res.push(this);}
    })
    return res;
}