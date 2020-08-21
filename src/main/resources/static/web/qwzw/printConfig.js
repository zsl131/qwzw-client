$(function() {
    $(".print-test-class").click(function() {
        var name = $(this).parents(".input-group").find("input").val();
        console.log(name)
        if(!name) {
            alert("请先设置打印名称再打印测试页");
        } else {
            alert("打印测试已发送，请等待打印机响应。")
            $.post("/web/printConfig/testPrint", {name: name}, function(res) {
                alert(res);
            })
        }
    });
});