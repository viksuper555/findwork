function sendRequest(level) {
    console.log(level)
    // console.log(category)
    var url = "/offers/";

    $.ajax( {
        type : "POST",
        url : url,
        data : body
    }).done(function(returnedData) {
        // Do something cool here with the returnedData.
    }).fail(function(returnedData) {
        // Do something not so cool here with the returnedData;
    }).always(function(returnedData) {
        // Always do something if you want to.
    });
}

$(function(){ /* DOM ready */
    $('#savedFilters').on('change', function(){
        var savedFilter = $(this).val();
        let values = savedFilter.split(' in ')
        window.location = '/offers/?level=' + values[0] + '&category=' + values[1];
    });
});
// $(window).on('load', function () {
//     var urlParams = new URLSearchParams(window.location.search);
//     if(urlParams.has('level')){
//         let level = urlParams.get('level')
//         $('#jobLevel option').removeAttr('selected')
//             .filter('[value=Mid]')
//             .attr('selected', true);
//     }
//     if(urlParams.has('category'))
//         $('#jobLevel').val(urlParams.get('category')).change()
// });
