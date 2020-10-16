function updateStatus(obj) {
    var objId = $(obj).attr("objId");
    var status = $(obj).attr("status");
    var newStatus = status=="1"?"0":"1";
    var dialog = confirmDialog("确定修改状态为【"+(newStatus=='1'?"启用":'停用')+"】吗？", "系统提示", function() {
        $.post("/web/foodBag/updateStatus",{id: objId, status: newStatus}, function(res) {
            if(res=='1') {alert("修改成功！"); window.location.reload();}
        });
    });
}