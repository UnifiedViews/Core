cz_cuni_mff_xrg_odcs_frontend_gui_components_pipelinecanvas_SessionRefresh = function() {
$(document).ready(function () {
    var userIsActive = false;
    var idleTimer = null;

    // refresh session interval every 15 minutes
    setInterval(function () {
        if(userIsActive) {
            var iframe = $('#renewFrame').find('iframe').get(0);
            iframe.src = iframe.src; // refresh iframe
            console.log("renew completed!");
        }
    }, 1000 * 60 * 15);

    // track user actions for last 15 minutes
    $('*').bind('mousemove click mouseup mousedown keydown keypress keyup submit change mouseenter scroll resize dblclick', function () {
        clearTimeout(idleTimer);
        userIsActive = true;
        idleTimer = setTimeout(function () {
            userIsActive = false;
        }, 1000 * 60 * 15);
    });
})
};
