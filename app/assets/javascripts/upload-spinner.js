// =====================================================
// UpScan upload
// =====================================================
$("#uploadForm").submit(function(e){
    e.preventDefault();
    const fileLength = $("#file-upload")[0].files.length;
    if(fileLength === 0){
        var errorRequestId = $("#x-amz-meta-request-id").val()
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=InvalidArgument&errorMessage=FileNotSelected&errorRequestId="+errorRequestId
        window.location = errorUrl
    } else {
        function disableFileUpload(){
            $("#file-upload").attr('disabled', 'disabled')
        };

        function addUploadSpinner(){
            $(".govuk-form-group--error").removeClass("govuk-form-group--error")
            $("#file-upload-error").remove()
            $("#error-summary").remove()
            $("#submit").remove()
            $("#processing").append('<h2 class="govuk-heading-m">'+$("#processingMessage").val()+'</h2><div><div class="ccms-loader"></div></div>')
        };

        addUploadSpinner();
        this.submit();
        disableFileUpload();
    }

});

$(document).ready(function ()
{
    var hasError = (window.location.href.indexOf("errorCode") > -1)
    var preFixError = hasError ? "Error: " : ""
    var appendError = preFixError + $("title").html()
    $("title").html(appendError)
});