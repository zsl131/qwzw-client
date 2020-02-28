$(function() {
    $("input[name='keyword']").focus(); //默认获取光标

    $(".food-nav-ul").find("li").click(function() {
        $(".food-nav-ul").find("li.active").removeClass("active");
        $(this).addClass("active");
    });
});

function changePeopleAmount(obj) {
    var oldVal = $(obj).attr("oldVal");
    var val = parseInt($(obj).val());
    console.log(oldVal+"----------"+val)
    if(oldVal!=val && val>0) {
        var orderNo = $("input[name='orderNo']").val();
        console.log("order:::", orderNo)
        $.post("/web/foodOrder/updatePeopleAmount", {orderNo:orderNo, amount: val}, function(count) {
            if(count) {
                $(obj).attr("oldVal", val);
                showDialog("设置成功", "系统提示");
            } else {
                showDialog("设置失败，建议先刷新页面（按·F5·）再试", "系统提示");
            }
        });
    }
    else {
        $(obj).val(oldVal);
    }
}

/** 添加到购物车 */
function addBasket(obj) {
//    console.log(obj);
    //step 1.
    setActive("new-append-nav"); //有新添加菜品时都重新定位nav的active
    //step 2.
    rebuildBasket(obj);
}

function rebuildBasket(obj) {
    const foodId = $(obj).attr("foodId"); //菜品ID
    const foodName = $(obj).attr("foodName");
    const price = $(obj).attr("price");
    const conObj = $('.food-list-container');
    const oldLi = $(conObj).find("li[foodId='"+foodId+"']"); //判断是否已经存在
    //console.log($(oldLi).html());

    removeAppendClass(); //添加之前先取消新行
    var html = "";
    if($(oldLi).html()) { //如果已经存在
        const oldAmount = parseInt($(oldLi).find(".food_amount").html());
        $(oldLi).find(".food_amount").html(oldAmount+1);
        $(oldLi).find(".food_amount").addClass("amount");
        $(oldLi).find(".total").html("￥"+(parseInt(oldAmount+1)*parseFloat(price)));
        html = '<li class="new-append" foodId="'+foodId+'" price="'+price+'" onmouseenter="onOperator(this)" onmouseleave="finishOperator(this)">'+$(oldLi).html()+'</li>';
        $(oldLi).remove();
    } else { //如果不存在
        html += '<li class="new-append" foodId="'+foodId+'" price="'+price+'"  onmouseenter="onOperator(this)"  onmouseleave="finishOperator(this)">'+
            '<div class="name">'+foodName+'</div>'+
            '<div class="price">￥'+price+'x<b class="food_amount">1</b></div>'+
            '<div class="total">￥'+price+'</div>'+
            '<div class="operator-div" style="display: none">' +
                '<div title="数量减少"><button class="btn btn-default" onClick="onClickOperator(this, \'-\')">-</button></div>'+
                '<div title="数量增加"><button class="btn btn-default" onClick="onClickOperator(this, \'+\')">+</button></div>'+
                '<div title="取消此菜"><button class="btn btn-danger" onClick="onClickOperator(this, \'d\')">删</button></div>'+
            '</div>'+
            '</li>';
    }
    $(conObj).html(html+$(conObj).html());
    newAppendBorder();

    calAppendTotalMoney(); //重新计算金额
}

function onClickOperator(obj, flag) {
    const pobj = $(obj).parents("li");
    console.log(pobj, flag)
}

function onOperator(obj) {
    //console.log($(obj).html());
    $(obj).addClass("hover");
    $(obj).find(".operator-div").slideDown(100);
}

function finishOperator(obj) {
    $(obj).removeClass("hover");
    $(obj).find(".operator-div").slideUp(100);
}

function removeAppendClass() {
    $("li.new-append").each(function() {
        $(this).removeClass("new-append"); //添加之前先取消新行
        $(this).removeClass("new-append-border");
    });
}

function newAppendBorder() {
    //$("li.new-append").removeClass("new-append");
    $("li.new-append").each(function() {
        const obj = $(this);
        let count = 0;
        let interval = setInterval(function() {count ++; setBorder(obj); if(count>=10) {clearInterval(interval);}},80);
    });
}

function setBorder(obj) {
    $(obj).toggleClass("new-append-border");
}

//计算新增菜品总金额
function calAppendTotalMoney() {
    const conObj = $('.food-list-container');
    let total = 0;
    $(conObj).find("li").each(function() {
        total += (parseFloat($(this).attr("price"))*parseInt($(this).find(".food_amount").html())); //总金额
//        total+=parseFloat($(this).find(".food_amount").html());
    });
    total = Number(total).toFixed(2);
    $(conObj).parents(".new-append-div").find("li.result").find(".total").html("￥"+total +" 元");
}

function setActive(cls) {
    $(".food-nav-ul").find("li.active").removeClass("active");
    $(".food-nav-ul").find("li."+cls).addClass("active");
}

function checkRefresh() {
    console.log("------------")
    return true;
}