$(function() {
    $(".mealing_table_div").find("input.mealing_table_id").each(function() {
        const id = "table_li_"+$(this).val();
        const obj = "#"+id;
        $(obj).addClass("has_mealing");
        $(obj).attr("title", $(obj).attr("title")+"-就餐中...");
    });
});

/** 检查是否在就餐中 */
function checkMealing(tableId) {
    const orderNo = $("input[value='"+tableId+"']").attr("orderNo");
    if(orderNo) {
        window.location.href = "/web/foodOrder/onOrder?orderNo="+orderNo;
        return false;
    } else {return true;}
}

function onOrder(obj) {
    const remark = $(obj).attr("remark");
    const name = $(obj).attr("title");
    const tableId = $(obj).attr("tableId");

    checkMealing(tableId);

    let countHtml = '';
    for(let i=1;i<=10;i++) {
        countHtml += '<button class="btn" onClick="setTableCount('+i+')" style="margin: 5px 5px 0px 0px;">'+i+' 人</button>';
    }

    var html = '<div class="input-group input-group-lg">' +
                '<span class="input-group-addon">用餐人数</span>' +
                '<input type="number" class="form-control" name="count" style="color:#00F;" placeholder="输入用餐人数，或点击下面按钮"/>' +
                '<span class="input-group-addon"> 人</span>'+
            '</div>'+
            '<div>'+countHtml+'</div>' +
            '<div style="font-size:12px; color:#999; padding-top: 8px;">  餐桌备注：'+remark+'</div>';
    const dialog = confirmDialog(html, "【"+name+"】下单", function() {
        //console.log("----------")
        const count = parseInt($(dialog).find("input[name='count']").val());
        if(count>0) {
            //console.log(count)
//            window.location.href = "/web/food/index?tableId="+tableId+"&count="+count;
            $.post("/web/foodOrder/onOrder", {count: count, tableId: tableId}, function(orderNo) {
                if(orderNo) {
                    window.location.href = "/web/foodOrder/onOrder?orderNo="+orderNo;
                }
            });
        } else {
            showDialog("请输入用餐人数", "系统提示");
        }
    }, "static");
    //console.log(remark, name+"---"+tableId)
}

function setTableCount(count) {
    $("input[name='count']").val(count);
}