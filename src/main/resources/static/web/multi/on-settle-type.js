var foodBagDetailList = [];
$(function() {
    $(".order-type-div").find("button").click(function() {
        cleanCon();
        cleanInfo();
        var orderType = $(this).attr("orderType");
        $(this).addClass("btn-info");
//        alert(orderType);
        switchCon(orderType);
    })
})

function switchCon(orderType) {
    //console.log(buildMtHtml());
    if("2"==orderType) {
        setCon(buildMtHtml());
    } else if("3"==orderType) {
//        setCon("选择相应套餐");
        $.post("/web/foodBag/findAll", {}, function(res) {
            //console.log(res, orderNo)
            setCon(buildBagHtml(res));
        }, "json");
    }
    updateType(orderType);
}

/** 修改订单类型 */
function updateType(type) {
    $.post("/web/foodOrder/updateType", {orderNo: orderNo, type: type}, function(res) {

    }, "json");
}

function buildBagHtml(bagList) {
    var html = '<div style="padding: 8px;" class="bag-discount-div"><p>请选择相应套餐（<b style="color:#F00">请一定要验券哦</b>）</p>';
    for(var i=0;i<bagList.length;i++) {
        var bag = bagList[i];
        html += '<button class="btn " style="margin: 5px;" onClick="choiceBag(this)" bagId="'+bag.id+'"><b>'+bag.name+'</b><p>'+bag.remark+'</p></button>';
    }
    html += '<div class="show-discount-result"></div>';
    html += '</div>';
    return html;
}

function choiceBag(obj) {
    var cls = $(obj).attr("class");
    var clsName = "btn-danger";
    if(cls.indexOf(clsName)<0) {
        var bagId = $(obj).attr("bagId");
    //    console.log(bagId, orderNo)
        $.post("/web/foodOrder/onBagDiscount", {orderNo: orderNo, bagId: bagId}, function(res) {
            //console.log(res)
            foodBagDetailList = res.detailList;
            $(".show-discount-result").html('套餐抵扣：<b style="color:#F00">'+res.discountMoney+' </b> 元，<span onClick="showDetail()" style="color:#00F;cursor:pointer;">点这里查看详情</span>');

            var oriMoney = $(".total-money2").attr("oriMoney");
            $(".total-money2").html(oriMoney-res.discountMoney);
            setDiscount("套餐抵价-"+bagId, res.discountMoney, "13");
        }, "json");
    }
    $(".bag-discount-div").find("button."+clsName).removeClass(clsName);
    $(obj).addClass(clsName);

}

function showDetail() {
    var detailList = foodBagDetailList
    //console.log(detailList)
    var html = '';
    for(var i=0;i<detailList.length;i++) {
        var detail = detailList[i];
        html += '<p>'+detail.foodName+'：'+detail.price+'* <b style="color:#F00">'+detail.amount+'</b>=<b style="color:#00F;">'+detail.money+'</b> 元</p>';
    }
    //console.log(html)
    showDialog(html, "套餐抵扣详情");
}

function cleanInfo() {
    $(".order-type-div").find("button.btn-info").each(function() {$(this).removeClass("btn-info")});
}

function cleanCon() {
    $(".special-type").html("");
}

function setCon(html) {
    var qwzqObj = $(".special-type");
    $(qwzqObj).css({"display":"block"});
    $(qwzqObj).html(html);
}

function buildMtHtml() {
    var html = '';
    html += '<div class="form-group mt-coupon-div">'+
        '<div class="input-group input-group">'+
            '<span class="input-group-addon">券码1：</span>'+
            '<input type="number" class="form-control" placeholder="认真输入券码" onKeyup="changeCoupon()"/>'+
            '<span class="input-group-addon">券码2：</span>'+
            '<input type="number" class="form-control" placeholder="认真输入券码" onKeyup="changeCoupon()"/>'+

            '<span class="input-group-addon">券码3：</span>'+
            '<input type="number" class="form-control" placeholder="认真输入券码" onKeyup="changeCoupon()"/>'+
        '</div>'+
        '<div style="margin-top: 10px;">美团抵价：<b class="mt-worth" style="color:#F00;">0</b> 元 </div>'+
    '</div>'
    return html;
}

function changeCoupon() {
    var count = 0;
    mtValues = "美团抵价-";
    $(".mt-coupon-div").find("input").each(function() {
        var val = $(this).val();
        //console.log("---"+val)
        if(val && val.length>=5) {count ++; mtValues += (val+",");}
    })
    var money = count * 50;
    $(".mt-worth").html(money);
    setDiscount(mtValues, money, "6");

    var oriMoney = $(".total-money2").attr("oriMoney");
    $(".total-money2").html((oriMoney-money).toFixed(2));
    //console.log(money)
}

/** 设置抵价 */
function setDiscount(reason, money, discountType) {
    var target = $("input[name='discount']");
    $(target).attr("reason", reason);
    $(target).attr("money", money);
    $(target).attr("discountType", discountType);
}