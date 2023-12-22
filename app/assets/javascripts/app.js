// prevent resubmit warning
if (window.history && window.history.replaceState && typeof window.history.replaceState === 'function') {
  window.history.replaceState(null, null, window.location.href);
}

document.addEventListener('DOMContentLoaded', function(event) {

  // handle back click
  var backLink = document.querySelector('.govuk-back-link');
  if (backLink !== null) {
    backLink.addEventListener('click', function(e){
      e.preventDefault();
      e.stopPropagation();
      window.history.back();
    });
  }

  // handle print click
  var printLink = document.querySelector('.cbc-print-link');
  if (printLink !== null) {
    var html = printLink.innerHTML;
    printLink.innerHTML = '<a class="govuk-link" href="#">' + html + '</a>';

    printLink.addEventListener('click', function(e){
      e.preventDefault();
      e.stopPropagation();
      window.print();
    });
  }

});
