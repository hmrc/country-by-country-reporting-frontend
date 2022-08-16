// =====================================================
// UpScan upload
// =====================================================
var inProgress = false
$("#uploadForm").submit(function(e){
e.preventDefault();
const fileLength = $("#file-upload")[0].files.length;
if(fileLength === 0){
        var errorRequestId = $("#x-amz-meta-request-id").val()
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=InvalidArgument&errorMessage=FileNotSelected&errorRequestId="+errorRequestId
        window.location = errorUrl
} else {

    var uploadForm = this;
    function submitError(error, jqXHR){
        var errorCode = jqXHR.responseJSON.errorCode
        var errorMessage  = jqXHR.responseJSON.errorMessage
        var errorRequestId = jqXHR.responseJSON.errorRequestId
         var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode="+errorCode+"&errorMessage="+errorMessage+"&errorRequestId="+errorRequestId
         window.location = errorUrl
    };

   function addSpinner(){
       $(".govuk-form-group--error").removeClass("govuk-form-group--error")
       $("#file-upload-error").remove()
       $("#error-summary").remove()
       $("#processing").append('<h2 class="govuk-heading-m">'+$("#processingMessage").val()+'</h2><div><div class="ccms-loader"></div></div>')
       $("#file-upload").attr('disabled', 'disabled')
   };

    function fileUpload(form){
        if (inProgress === false) {
                $.ajax({
                  url: form.action,
                  type: "POST",
                  data: new FormData(form),
                  processData: false,
                  contentType: false,
                  crossDomain   : true
            }).fail(function(jqXHR, textStatus, errorThrown ){
                submitError("4000", jqXHR)
            }).done(function(){
                // Disable UI
                 inProgress = true
                 addSpinner();
                 refreshPage();
            });
        }
    };

    fileUpload(uploadForm);
}

});
// =====================================================
//  Refresh status page
// =====================================================
function refreshPage(){
    var refreshUrl = $("#fileUploadRefreshUrl").val();
    if (refreshUrl) {
        window.refreshIntervalId = setInterval(function () {
            $.getJSON(refreshUrl, function (data, textStatus, jqXhr) {
                if (jqXhr.status === 200) {
                      window.location = data.url;
                } else if (jqXhr.status === 100) {
                   return false
                } else {
                    console.debug("Something went wrong", jqXhr);
                }
            });
        }, 3000);
    }

}

$(document).ready(function ()
{
  var hasError = (window.location.href.indexOf("errorCode") > -1)
  var preFixError = hasError ? "Error: " : ""
  var appendError = preFixError + $("title").html()
  $("title").html(appendError)
});