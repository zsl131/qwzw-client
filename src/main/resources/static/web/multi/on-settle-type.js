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
    }

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
        '<div class="input-group input-group-lg">'+
            '<span class="input-group-addon">券码1：</span>'+
            '<input type="number" class="form-control" placeholder="认真输入券码" onKeyup="changeCoupon()"/>'+
            '<span class="input-group-addon">券码2：</span>'+
            '<input type="number" class="form-control" placeholder="认真输入券码" onKeyup="changeCoupon()"/>'+
        '</div>'+
        '<div style="margin-top: 10px;">美团抵价：<b class="mt-worth" style="color:#F00;">0</b> 元 </div>'+
    '</div>'
    return html;
}

function changeCoupon() {
    var count = 0;
    $(".mt-coupon-div").find("input").each(function() {
        var val = $(this).val();
        //console.log("---"+val)
        if(val && val.length>=5) {count ++;}
    })
    var money = count * 50;
    $(".mt-worth").html(money);

    var oriMoney = $(".total-money2").attr("oriMoney");
    $(".total-money2").html(oriMoney-money);
    //console.log(money)
}