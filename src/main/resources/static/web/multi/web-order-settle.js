var orderNo = "";
$(function() {
    orderNo = $("input[name='orderNo']").val();
    resetHeight();
    setTotalMoney();
    $(".pay-type-btn").click(function() {
        const activeCls = "btn-danger";
        $(".pay-type-div").find("button."+activeCls).removeClass(activeCls);
        $(this).addClass(activeCls);
    });

    $(".food-nav-ul").find("li").click(function() {
        $(".food-nav-ul").find("li.active").removeClass("active");
        $(this).addClass("active");

        $(".nav-con-div").each(function() {$(this).css("display", "none")});
        $(("."+$(this).attr("targetCls"))).css("display", "block");
    });

    $('[data-toggle="tooltip"]').tooltip();

    setCount();
});

function setCount() {
    var usefulCount = 0 ; var refundCount = 0 ;
    $(".food-list-container").find("li").each(function() {
        usefulCount += parseInt($(this).find(".total").find("b").html());
    });
    $(".refund-list-container").find("li").each(function() {
        refundCount += parseInt($(this).find(".total").find("b").html())
    })

    $(".useful-badge").html(usefulCount); $(".refund-badge").html(refundCount);
}

function printOrder() {
    var orderNo = $("input[name='orderNo']").val();
    var conDialog = confirmDialog("确定打印预结单吗？", "操作提示", function() {
        $.post("/web/foodOrder/printSettle", {orderNo: orderNo}, function(res) {
            if(res=='1') {alert("打印成功，请注意打印机");}
        })
        $(conDialog).remove();
    });
}

function refundFood(obj) {
    //foodId=${food.foodId},foodName=${food.foodName},batchNo=${food.batchNo},detailId=${food.id}
    var foodId = $(obj).attr("foodId"); var foodName = $(obj).attr("foodName");
    var orderNo = $("input[name='orderNo']").val(); var detailId = $(obj).attr("detailId");
    var amount = parseInt($(obj).find(".total").find('b').html()); //最大数量
    //console.log(foodId, foodName)
    //console.log(orderNo, detailId)
    //console.log(amount)
    var html = '<div>'+
                '<p>菜品：<b style="color:#F00;">'+foodName+'</b></p>'+
                '<div class="input-group">'+
                '<span class="input-group-addon" >需退数量</span>'+
                '<input type="number" name="refund-amount" placeholder="请输入要退的数量，且不能大于 '+amount+'" class="form-control"/></div>'+
                '<p style="margin-top:10px;"><b>注意：</b>输入的数量必须是正整数，且不大于  <b style="color:#F00; font-size: 18px;">'+amount+'</b></p>'+
                '</div>';
    const conDialog = confirmDialog(html, "退菜操作【"+foodName+"】", function() {
        var refundAmount = parseInt($(conDialog).find("input[name='refund-amount']").val());
        if(!refundAmount || refundAmount<=0 || refundAmount>amount) {
            showDialog("请认真输入需退数量，且不能大于 "+amount, "操作警告");
        } else {
            $.post("/web/foodOrder/refundFood", {orderNo: orderNo, foodId: foodId, refundAmount: refundAmount}, function(res) {
                if(res=='1') {
                    alert("操作成功"); window.location.reload();
                }
            });
            $(conDialog).remove();
        }
        /*$.post("/web/foodOrder/settlement", {orderNo:orderNo, totalMoney: totalMoney, removeDot: removeDot, payType: payType}, function(res) {
        })*/
    });
}

function submitMoney() {
//    console.log($(".pay-type-div").html())
    const payType = $(".pay-type-div").find(".btn-danger").attr("payType");
    if(!payType) {
        showDialog("请先选择付款方式", "操作提示");
    } else {
        const conDialog = confirmDialog("此操作不可逆，确定要结账吗？", "操作提示", function() {
            const checked = $("input[name='money-check']").prop("checked");
            const removeDot = checked?"1":"0"; //是否移除小数点
            const totalMoney = parseFloat($(".total-money").html()); //总金额
            var discountMoney = 0, discountReason = "", discountType="";
           /* $(".mt-coupon-div").find("input").each(function() {
                discountReason += ($(this).val())+",";
            });
            discountMoney = parseFloat($(".mt-coupon-div").find(".mt-worth").html());*/
            var targetObj = $("input[name='discount']");
            discountMoney = parseFloat($(targetObj).attr("money"));
            discountReason = $(targetObj).attr("reason");
            discountType = $(targetObj).attr("discountType");
            discountReason = discountReason?discountReason:"";
            discountMoney = discountMoney?discountMoney:0;
            //console.log(discountReason, discountMoney);
            //console.log("-----------")
            //console.log(orderNo, payType+"---"+removeDot+"----"+totalMoney);
            $.post("/web/foodOrder/settlement", {orderNo:orderNo, discountType:discountType, discountReason: discountReason, discountMoney:discountMoney, totalMoney: totalMoney, removeDot: removeDot, payType: payType}, function(res) {
                if("-1"==res) {
                    showDialog("订单不存在或不在就餐中", "系统提示");
                    setTimeout(function() {window.location.reload();}, 2500);
                } else if("-2"==res) {
                    showDialog("订单金额和传入金额不一致", "系统提示");
                    setTimeout(function() {window.location.reload();}, 2500);
                } else if("1"==res) {
                    showDialog("操作成功", "系统提示");
                    setTimeout(function() {window.location.reload();}, 2500);
                }
            })
            $(conDialog).remove();
        });
    }
}

/** 设置总金额 */
function setTotalMoney() {
    let money = 0;
    $(".food-list-container").find("li").each(function() {
        money += parseFloat($(this).find(".money").find("b").html());
    })
    money = money.toFixed(2); //保留两位小数
    $(".total-money").html(money);
    $(".total-money2").attr("oriMoney", money);
    $(".total-money2").html(money);
    changeCheck(); //检测是否要抹零
}

///设置左右两边的高度
function resetHeight() {
    const rightHeight = $(".content-main").height();
    const leftHeight = $(".left-party").height();

    //console.log(leftHeight, rightHeight)

    //$(".content-main").height(rightHeight>leftHeight?rightHeight+"px":leftHeight+"px");
    //$(".left-party").height(rightHeight>leftHeight?rightHeight+"px":leftHeight+"px");
    $(".content-main").css({"minHeight":(rightHeight>leftHeight?rightHeight+"px":leftHeight+"px")});
    $(".left-party").css({"minHeight": rightHeight>leftHeight?rightHeight+"px":leftHeight+"px"});
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

function changeCheck() {
//    const checked = $(obj).prop("checked");
    const checked = $("input[name='money-check']").prop("checked");
//    const totalMoney = parseFloat($(".total-money").html());
    var totalMoney = parseFloat($(".total-money2").attr("oriMoney")); //原始份额
    var discountMoney = parseFloat($(".mt-worth").html()); //优惠金额
    discountMoney = discountMoney?discountMoney:0;
    //console.log(discountMoney)
    var money = totalMoney - discountMoney;
    if(checked) {
        $(".total-money2").html(parseInt(money));
    } else {
        $(".total-money2").html(money);
    }
}

