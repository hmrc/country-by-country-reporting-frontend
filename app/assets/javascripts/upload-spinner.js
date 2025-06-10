// =====================================================
// UpScan upload
// =====================================================
$("#uploadForm").submit(function (e) {
    e.preventDefault();
    const fileLength = $("#file-upload")[0].files.length;
    if (fileLength === 0) {
        var errorRequestId = $("#x-amz-meta-request-id").val();
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=InvalidArgument&errorMessage=FileNotSelected&errorRequestId=" + errorRequestId;
        window.location = errorUrl;
    } else if (isFileNameInvalid()) {
        var errorRequestId = $("[name='x-amz-meta-request-id']").val();
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=InvalidArgument&errorMessage=InvalidFileNameLength&errorRequestId=" + errorRequestId;
        window.location = errorUrl;
    } else {
        function disableFileUpload() {
            $("#file-upload").attr('disabled', 'disabled')
        }

        function addUploadSpinner() {
            $("#processing").empty();
            $("#processing").append('<div><p class="govuk-visually-hidden">' + $("#processingMessage").val() + '</p><div><svg class="ccms-loader" height="100" width="100"><circle cx="50" cy="50" r="40"  fill="none"/></svg></div></div>');
            $(".govuk-form-group--error").removeClass("govuk-form-group--error");
            $("#file-upload-error").remove();
            $("#error-summary").remove();
            $("#submit").remove();
        }

        addUploadSpinner();
        setTimeout(function () {
            this.submit();
            disableFileUpload();
        }.bind(this), 0);
    }

});

function isFileNameInvalid() {
    var fileName = $("#file-upload")[0].files[0].name;
    var trimmedFileName = fileName.replace(".xml", "");
    if (trimmedFileName.length > 100) {
        return true;
    }
    return false;
}

$(document).ready(function () {
    var hasError = (window.location.href.indexOf("errorCode") > -1);
    var currentTitle = $("title").html();

    if (hasError && !currentTitle.startsWith("Error:")) {
        $("title").html("Error: " + currentTitle);
    }
});
