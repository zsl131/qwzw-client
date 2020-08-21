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
});

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
            //console.log(orderNo, payType+"---"+removeDot+"----"+totalMoney);
            $.post("/web/foodOrder/settlement", {orderNo:orderNo, totalMoney: totalMoney, removeDot: removeDot, payType: payType}, function(res) {
                if("-1"==res) {
                    showDialog("订单不存在或不在就餐中", "系统提示");
                    setTimeout(function() {window.location.reload();}, 2500);
                } else if("-2"==res) {
                    showDialog("订单金额和传入金额不一致", "系统提示");
                    setTimeout(function() {window.location.reload();}, 2500);
                } else if("1"==res) {
                    showDialog("对账成功", "系统提示");
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
    $(".total-money2").html(money);
    changeCheck(); //检测是否要抹零
}

///设置左右两边的高度
function resetHeight() {
    const rightHeight = $(".content-main").height();
    const leftHeight = $(".left-party").height();

    $(".content-main").height(rightHeight>leftHeight?rightHeight+"px":leftHeight+"px");
    $(".left-party").height(rightHeight>leftHeight?rightHeight+"px":leftHeight+"px");
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
    const totalMoney = parseFloat($(".total-money").html());
    if(checked) {
        $(".total-money2").html(parseInt(totalMoney));
    } else {
        $(".total-money2").html(totalMoney);
    }
}

