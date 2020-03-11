var orderNo = "";
$(function() {
    orderNo = $("input[name='orderNo']").val();
    $("input[name='keyword']").focus(); //默认获取光标

    $(".food-nav-ul").find("li").click(function() {
        $(".food-nav-ul").find("li.active").removeClass("active");
        $(this).addClass("active");

        $(".nav-con-div").each(function() {$(this).css("display", "none")});
        $(("."+$(this).attr("targetCls"))).css("display", "block");
    });
});

function changePeopleAmount(obj) {
    var oldVal = $(obj).attr("oldVal");
    var val = parseInt($(obj).val());
    //console.log(oldVal+"----------"+val)
    if(oldVal!=val && val>0) {
       // console.log("order:::", orderNo)
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

//点击操作按钮
function onClickOperator(obj, flag) {
    const pobj = $(obj).parents("li");
    const price = $(pobj).attr("price");
    const foodId = $(pobj).attr("foodId");
    const curAmount = parseInt($(pobj).find(".food_amount").html());
    if("-"==flag) {
        if(curAmount>1) {
            $(pobj).find(".food_amount").html(curAmount-1);
            $(pobj).find(".total").html("￥"+((curAmount-1)*price));
            if(curAmount-1>1) {
                $(pobj).find(".food_amount").addClass("amount");
            } else {
                $(pobj).find(".food_amount").removeClass("amount");
            }
        } else {
            showDialog("数量为1，不能再减了", "系统提示");
        }
    } else if("+"==flag) {
        $(pobj).find(".food_amount").addClass("amount");
        $(pobj).find(".food_amount").html(curAmount+1);
        $(pobj).find(".total").html("￥"+((curAmount+1)*price));
    } else if("d"==flag) {
        const foodName = $(pobj).find(".name").html();
        var delDialog = confirmDialog("确定移除【"+foodName+"】吗？", "系统提示", function() {
            $(pobj).remove();
            $(delDialog).remove();
            calAppendTotalMoney(); //重新计算金额，这里是异步调用，所以需要单独再计算一次
        })
    } else if("cur"==flag) { //打印当前菜品

    } else if("batch"==flag) { //打印本批次
    } else if("dd" == flag) {
    }
    calAppendTotalMoney(); //重新计算金额
//    console.log(price, foodId)
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
    let totalCount = 0;
    $(conObj).find("li").each(function() {
        const count = parseInt($(this).find(".food_amount").html());
        total += (parseFloat($(this).attr("price"))*count); //总金额
        totalCount += count;
//        total+=parseFloat($(this).find(".food_amount").html());
    });
    total = Number(total).toFixed(2);
    $(conObj).parents(".new-append-div").find("li.result").find(".total").html("￥"+total +" 元");
    if(totalCount>0) { //设置徽章数字
        $(".append-badge").html(totalCount);
    } else {
        $(".append-badge").html('');
    }
}

function setActive(cls) {
    $(".food-nav-ul").find("li.active").removeClass("active");
    $(".food-nav-ul").find("li."+cls).addClass("active");

    $(".nav-con-div").each(function() {$(this).css("display", "none")});
    $(("."+$("."+cls).attr("targetCls"))).css("display", "block");
}

function onSubmitFood(obj) {
    const conObj = $('.food-list-container');
    let totalMoney = 0;
    let foodData = '';
    $(conObj).find("li").each(function() {
        const foodId = $(this).attr("foodId");
        const count = parseInt($(this).find(".food_amount").html()); //数量
        totalMoney += (parseFloat($(this).attr("price"))*count); //总金额
        foodData += foodId+"-"+count+"_";
    });
    if(totalMoney<=0) {
        showDialog("未添加任何菜品，不可提交。请先点击左边菜品进行添加", "系统提示");
    } else {
        //console.log("-----可以提交------"+totalMoney+"======orderNO:" +orderNo, foodData);
        const loadDialog = showLoading("数据正在提交...", "static");
        $(obj).attr("disabled", "true")
        $.post("/web/foodOrder/appendFood", {orderNo:orderNo, foodData: foodData}, function(res) {
            //console.log(res);
            if(res=='1') {
                showDialog("操作成功", "系统提示");
                setTimeout(function() {window.location.reload();}, 2500);
            } else {
                showDialog("提交异常："+res, "系统提示");
            }
            $(loadDialog).remove();
            $(obj).removeAttr("disabled"); //让提交按钮能够使用
        });
    }
    //console.log('-----------------')
}

/** 结算订单 */
function onSettleOrder(obj) {
    const appendAmount = parseInt($(".append-badge").html());
    if(appendAmount>=1) {
        showDialog("存在新增菜品，请先处理后再结算订单", "系统提示");
    } else {
        const totalAmount = parseInt($(".already-amount").html());
        console.log(totalAmount);
        if(totalAmount>=1) {
//            showDialog("可以结算订单", "系统提示");
            window.location.href = "/web/foodOrder/onSettle?orderNo="+orderNo;
        } else {
            showDialog("无菜品不可结算", "系统提示");
        }
    }
    //console.log(appendAmount)
}

function onCloseOrder() {
    const totalAmount = parseInt($(".already-amount").html());
    if(totalAmount>=1) {
        showDialog("<p>存在菜品不能关闭订单。</p><p>若此订单是异常订单需要关闭的，需要正常结算并<b style='color:#f00'>留存【消费单】</b>，以便财务对账</p>", "系统提示");
    } else {
        const conDialog = confirmDialog("确定关闭此订单吗？", "操作提示", function() {
            $.post("/web/foodOrder/closeOrder", {orderNo: orderNo}, function(res) {
                if("1"==res) {
                    showDialog("订单成功关闭，此桌可重新接单 ", "操作提示");
                    setTimeout(function() {window.location.reload()}, 2000);
                } else if("-1"==res) {showDialog("关闭失败【订单不存在】或【不在用餐状态】", "操作提示");}
            });
        });
    }
}


function onChangeTable() {
    const loadDialog = showLoading("数据正在加载中...", "static");
    $.post("/web/foodOrder/queryEmptyTables", {}, function(res) {
//        console.log(res)
        showChoiceTable(res);
        $(loadDialog).remove();
    });
}

function showChoiceTable(tableList) {
    let html = '';
    tableList.map(function(item) {
        //console.log(item);
        html += '<button class="btn btn-default table-btn" tableId="'+item.id+'" tableName="'+item.name+'" onClick="changeTable(this)"><b>'+item.name+'</b><span style="font-size:12px; color:#999;">【'+item.remark+'】</span></button>'
    })
    const conDialog = confirmDialog(html, "请选择相应餐桌进行切换", function() {
        $(conDialog).remove();
    });
}

function changeTable(obj) {
    const tableId = $(obj).attr("tableId");
    const tableName = $(obj).attr("tableName");
    console.log(tableId)
    const conDialog = confirmDialog("确定更换到【"+tableName+"】吗？", "操作提示", function() {
        $.post("/web/foodOrder/changeTable", {orderNo: orderNo, tableId: tableId, tableName: tableName}, function(res) {
            if("1"==res) {
                showDialog("换桌成功", "操作提示");
                setTimeout(function() {window.location.reload()}, 2000);
            } else if("-1"==res) {showDialog("换桌失败【订单不存在】或【不在用餐状态】", "操作提示");}
        })
        $(conDialog).remove();
    });
}

function checkRefresh() {
    console.log("------------")
    return true;
}