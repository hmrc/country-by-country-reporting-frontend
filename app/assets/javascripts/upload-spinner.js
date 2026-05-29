// =====================================================
// UpScan upload
// =====================================================
$("#uploadForm").submit(function (e) {
    e.preventDefault();
    const fileError = validateFile()
    if(fileError.isPresent){
        var errorRequestId = $("#x-amz-meta-request-id").val();
        var errorUrl = $("#upScanErrorRedirectUrl").val() + "?errorCode=InvalidArgument&errorMessage="+fileError.errorType+"&errorRequestId=" + errorRequestId;
        window.location = errorUrl;
    }else {
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

function validateFile() {
    if(isFileNotSelected()) {
        return {
            isPresent: true,
            errorType: 'FileNotSelected'
        };
    }
    var fileName = $("#file-upload")[0].files[0].name;
    var trimmedFileName = fileName.replace(".xml", "");
    if (isFileNameLengthInvalid(trimmedFileName)) {
        return {
            isPresent: true,
            errorType: 'InvalidFileNameLength'
        };
    }
    if (isFileNameContainsDisallowedCharacters(trimmedFileName)) {
        return {
            isPresent: true,
            errorType: 'DisallowedCharacters'
        };
    }
    return {
        isPresent: false,
        errorType: null
    };
}

function isFileNotSelected() {
    const fileLength = $("#file-upload")[0].files.length;
    return fileLength == 0
}

function isFileNameLengthInvalid(fileName) {
    return fileName.length > 100;
}

function isFileNameContainsDisallowedCharacters(fileName) {
    return /[<>:"/\\|?*]/.test(fileName)
}

$(document).ready(function () {
    var hasError = (window.location.href.indexOf("errorCode") > -1);
    var currentTitle = $("title").html();

    if (hasError && !currentTitle.startsWith("Error:")) {
        $("title").html("Error: " + currentTitle);
    }
});
