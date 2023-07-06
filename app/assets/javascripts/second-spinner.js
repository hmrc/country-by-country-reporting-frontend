// =====================================================
// Second spinner wheel to wait for response from EIS
// =====================================================
var checkProgress = false
$("#sendYourFileForm").submit(function(e){
e.preventDefault();
{
     var sendYourFileForm = this;

       function addSpinner(){
           $("#processing").append('<p class="govuk-visually-hidden">'+$("#processingMessage").val()+'</p><div><svg class="ccms-loader" height="100" width="100"><circle cx="50" cy="50" r="40"  fill="none"/></svg></div>')
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
//  Refresh status page
// =====================================================
function refreshToCheckStatusPage(){
    var refreshUrl = $("#fileStatusRefreshUrl").val();
    var count = 0;
    if (refreshUrl) {
        window.refreshIntervalId = setInterval(function () {
            if (count < $("#spinner-counter").val()) {
                $.getJSON(refreshUrl)
                .done(function (data, textStatus, jqXhr) {
                    if (jqXhr.status === 200) {
                          window.location = data.url;
                    } else {
                       count += 1
                       return false
                    }
                }).fail(function( jqxhr, textStatus, error ) {
                    window.location =  $("#technicalDifficultiesRedirectUrl").val()
                });
            } else {
                window.location =  $("#slowJourneyUrl").val()
            }
        }, 3000);
    }

}
