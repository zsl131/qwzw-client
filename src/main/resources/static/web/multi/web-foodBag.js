var allFoodList = [];
function showFoodBag(obj) {
    var objId = $(obj).attr("bagId");
    //console.log(objId);
    $.post("/web/foodBag/loadOne", {id: objId}, function(res) {
//        console.log(res);
        showBagDialog(res);
    });
}

function showBagDialog(obj) {
    var foodList = obj.foodList;
//    console.log(foodList);
    allFoodList = foodList;
    var dialog = confirmDialog(buildHtml(obj), "套餐下单", function() {
        $(dialog).find(".has-choice-food").each(function() {
            //var foodId = $(this).attr("foodId");
            //var foodName = $(this).attr("foodName");
            //var price = $(this).attr("price");
            var amount = parseInt($(this).find(".single-food-amount").html());
            for(var i=0;i<amount;i++) {
                addBasket($(this));
            }
            /*console.log("---------------------------")
            console.log(foodId, foodName)
            console.log(price, amount)*/
        });
        $(dialog).remove();
    }, "static")
}
function buildHtml(obj) {
    var dialogHeight = $(window).height();
    //console.log(dialogHeight)
    var html = '<div style="height: '+dialogHeight*0.7+'px; overflow:auto; ">';
    var detailList = obj.detailList;
    for(var i=0;i<detailList.length;i++) {
        //console.log(detailList[i])
        var detail = detailList[i];
        html += "<div class='single-food-div' cateId='"+detail.categoryId+"' detailId='"+detail.id+"' cateName='"+detail.categoryName+"'>";
        html += '<div><b>'+detail.categoryName+'('+detail.categoryId+')</b>（<b>'+detail.totalCount+'</b>选<b style="color:#F00">'+detail.amount+'</b>）</div>';
        var names = buildFoodList(detail.foodNames);
        var ids = buildFoodList(detail.foodIds);
        for(var j=0;j<names.length;j++) {
            html += '<button class="btn" onclick="onChoiceFood(this)" foodId="'+ids[j]+'" foodName="'+names[j]+'">'+names[j]+'</button>&nbsp;';
        }


        html += "<div class='single-cate-choice' detailId='"+detail.id+"' cateName='"+detail.categoryName+"' cateId='"+detail.categoryId+"' total='"+detail.totalCount+"' amount='"+detail.amount+"'></div></div>";
    }
    ///html += '<div class="choice-div"></div>';
    html += '</div>';
    return html;
}

function buildFoodList(values) {
    var array = values.split("-");
    var res = [];
    for(var i=0;i<array.length;i++) {
        if(array[i] && array[i]!='') {res.push(array[i]);}
    }
    return res;
}

function onChoiceFood(obj) {
    var bagObj = $(obj).parents(".single-food-div");
    var detailId = $(bagObj).attr("detailId");
    var cateId = $(bagObj).attr("cateId"); var cateName = $(bagObj).attr("cateName");
    var foodId = $(obj).attr("foodId"); var foodName = $(obj).attr("foodName");
    //console.log(cateId, cateName)
    //console.log(detailId)
    //console.log(foodId, foodName)
    checkAndAdd(detailId, cateId, cateName, foodId, foodName)
}

function checkAndAdd(detailId, cateId, cateName, foodId, foodName) {
    var choiceObj = $(".single-cate-choice[detailId='"+detailId+"']"); //存放已选择的DIV

    //var checkRes = checkAmount(choiceObj);
    if(checkAmount(choiceObj)) {
        //console.log($(choiceObj).html())
        var oldObj = $(choiceObj).find("button[foodId='"+foodId+"']");
        if(oldObj.html()) { //表示存在
            $(oldObj).find("b.single-food-amount").html(parseInt($(oldObj).find("b.single-food-amount").html())+1);
            $(oldObj).removeClass("btn-primary"); $(oldObj).addClass("btn-danger");
        } else {
            var singleObj = '<button class="btn btn-primary has-choice-food" foodId="'+foodId+'" price="'+queryPrice(foodId)+'" foodName="'+foodName+'" onClick="minusFood(this)">'+foodName+'<b class="single-food-amount">1</b> 份</button>';
            $(choiceObj).html(singleObj + $(choiceObj).html());
        }
    }
}

function queryPrice(foodId) {
    var price = 0;
    for(var i=0;i<allFoodList.length;i++) {if(allFoodList[i].id==parseInt(foodId)) {price = allFoodList[i].price;}}
    return price;
}

/** 数量减少 */
function minusFood(obj) {
    var amount = parseInt($(obj).find("b.single-food-amount").html());
    if(amount<=1) {$(obj).remove();}
    else {$(obj).find("b.single-food-amount").html(amount-1); if(amount-1<=1) {$(obj).removeClass("btn-danger"); $(obj).addClass("btn-primary")}}
}

/** 检查是否还可添加 */
function checkAmount(obj) {
    var cateId = $(obj).attr("cateId");
    var cateName = $(obj).attr("cateName");
    var amount = parseInt($(obj).attr("amount"));
    var total = $(obj).attr("total");

    var curAmount = 0;
    $(obj).find("button").each(function() {
        curAmount += parseInt($(this).find("b.single-food-amount").html());
    })
    //console.log("---->"+curAmount, total)
    if(curAmount>=amount) {
        showDialog("【"+cateName+"("+cateId+")】部份总数量不可超过【"+amount+"】", "操作提示");
        return false;
    } else {
//        console.log("--0-0--")
        return true;
    }
}

/** 暂时不用 */
function onFoodToggle(obj) {
    var clsName = "btn-primary";
    var cls = $(obj).attr("class");
    var flag = 0;
    if(cls.indexOf(clsName)>=0) {flag = 1;}
    console.log(cls, flag)
    if(flag==1) {$(obj).removeClass(clsName);}
    else {$(obj).addClass(clsName);}
}