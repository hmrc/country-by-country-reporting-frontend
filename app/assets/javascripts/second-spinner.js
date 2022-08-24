// =====================================================
// Second spinner wheel to wait for response from EIS
// =====================================================
var checkProgress = false
$("#sendYourFileForm").submit(function(e){
e.preventDefault();
{
     var sendYourFileForm = this;

       function addSpinner(){
           $("#processing").append('<h2 class="govuk-heading-m">'+$("#processingMessage").val()+'</h2><div><div class="ccms-loader"></div></div>')
       };

    function sendYourFile(form){
        var formData = new FormData(form);
        formData.append("", ""); //IE 11 fix to avoid empty form
        if (checkProgress === false) {
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
                 addSpinner();
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
