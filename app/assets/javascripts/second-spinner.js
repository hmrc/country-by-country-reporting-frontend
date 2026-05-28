// =====================================================
// Second spinner wheel to wait for response from EIS
// =====================================================
var checkProgress = false
$("#sendYourFileForm").submit(function(e){
    e.preventDefault();
    {
        var sendYourFileForm = this;

        function addSpinner(){
            $("#processing").empty();
            $("#processing").append('<p>' + $("#processingMessage").val() + '</p><div><svg class="ccms-loader" height="100" width="100"><circle cx="50" cy="50" r="40"  fill="none"/></svg></div>')
            $("#processing").removeClass("govuk-visually-hidden");
            $("#submit").remove()
        };

        function sendYourFile(form){
            var formData = new FormData(form);
            formData.append("", ""); //IE 11 fix to avoid empty form
            if (checkProgress === false) {
                addSpinner();
                $.ajax({
                    url: form.action,
                    type: "POST",
                    data: formData,
                    processData: false,
                    contentType: false,
                    crossDomain: true
                }).fail(function(jqXHR, textStatus, errorThrown ){
                    window.location =  $("#technicalDifficultiesRedirectUrl").val()
                }).done(function(){
                    checkProgress = true
                    refreshToCheckStatusPage();
                });
            }
        };
        sendYourFile(sendYourFileForm)
    }

});

// =====================================================
//  Refresh status page with min/max duration
// =====================================================
function refreshToCheckStatusPage(){
    var refreshUrl = $("#fileStatusRefreshUrl").val();
    var minDurationSeconds = parseInt($("#minSpinnerDuration").val() || 10); // Default 10 seconds
    var maxDurationSeconds = parseInt($("#maxSpinnerDuration").val() || 120); // Default 2 minutes
    var pollIntervalMs = 3000; // Poll every 3 seconds

    var startTime = Date.now();
    var minDurationMs = minDurationSeconds * 1000;
    var maxDurationMs = maxDurationSeconds * 1000;

    if (refreshUrl) {
        window.refreshIntervalId = setInterval(function () {
            var elapsedTime = Date.now() - startTime;

            if (elapsedTime >= maxDurationMs) {
                clearInterval(window.refreshIntervalId);
                window.location = $("#slowJourneyUrl").val();
                return;
            }

            if (elapsedTime >= minDurationMs) {
                $.getJSON(refreshUrl)
                    .done(function (data, textStatus, jqXhr) {
                        if (jqXhr.status === 200) {
                            clearInterval(window.refreshIntervalId);
                            window.location = data.url;
                        }
                    }).fail(function(jqxhr, textStatus, error) {
                    window.location = $("#technicalDifficultiesRedirectUrl").val()
                });
            }
        }, pollIntervalMs);
    }
}
