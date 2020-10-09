$(function() {

});

function checkData() {
    var name = $("input[name='name']").val();
    if($.trim(name)=='') {
        showDialog("请输入套餐名称");
    } else {
        return true;
    }
    return false;
}
