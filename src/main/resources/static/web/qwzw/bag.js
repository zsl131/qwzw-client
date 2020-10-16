
function onSetDetail(obj) {
    //var foodId = $(obj).attr("foodId");
    //var foodName = $(obj).attr("foodName");
    var cateId = $(obj).attr("cateId");
    var cateName = $(obj).attr("cateName");

    //console.log(cateId, cateName)

    var conDialog = confirmDialog(buildHtml(cateId, cateName), "套餐内容配置", function() {
        var amount = $(conDialog).find("input[name='count']").val();
        var names = "-", ids = "-"; var count = 0;
        $(conDialog).find("input[type='checkbox']:checked").each(function() {
            var foodName = $(this).attr("foodName");
            var foodId = $(this).attr("foodId");
            names += foodName+"-"; ids += foodId + "-";
            count ++;
            //console.log(foodId, checked);
        });
        console.log(names, ids)
        console.log(amount)
        if(!amount || isNaN(amount) || amount<=0) {
            showDialog("请输入数量", "系统提示");
        } else if(ids=='-') {
            showDialog("请选择相应菜品", "系统提示");
        } else {
        //Integer bagId, String bagName, String ids, String names, Integer amount
            //console.log("----------------------")
            var bagId = $("input[name='bagId']").val(); var bagName = $("input[name='bagName']").val();
            $.post("/web/foodBag/onAddDetail", {bagId: bagId, bagName: bagName, ids: ids, names: names, amount: amount,
                totalCount: count, cateId: cateId, cateName: cateName}, function(res) {
                if(res=='1') {alert("操作成功"); window.location.reload();}
            }, "json");
            $(conDialog).remove();
        }

    });
}

function buildHtml(cateId, cateName) {
    var foods = selectFood(cateId);
   // console.log(foods, cateId);
    var html = '<p>选择分类：<b class="canClick" title="可点击" onClick="selectFood('+cateId+')">'+cateName+'</b></p>'; //-<b class="canClick" title="可点击">'+foodName+'</b>

    for(var i=0;i<foods.length;i++) {
        html += '<input type="checkbox" foodName="'+$(foods[i]).attr("foodName")+'" foodId="'+$(foods[i]).attr("foodId")+'" id="food_'+i+'"/><label for="food_'+i+'">&nbsp;&nbsp;'+$(foods[i]).attr("foodName")+'</label>'+
                '&nbsp;&nbsp;&nbsp;&nbsp;';
    }

    html += '<p>勾选后输入数量&nbsp;&nbsp;<input type="number" style="width: 240px" name="count" placeholder="输入数量"/>&nbsp;&nbsp;项</p>'+
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

function deleteDetail(obj) {
    var objId = $(obj).attr("objId");
    //console.log(objId)
    var conDialog = confirmDialog("确定要删除这条信息吗？", "系统提示", function() {
        $.post("/web/foodBag/deleteDetail", {detailId: objId}, function(res) {
            if(res=='1') {alert("删除成功"); window.location.reload();}
        }, "json");
        $(conDialog).remove();
    });
}