var Breakerbox = {
     SyncState: function(opts) {
        opts = opts || {};

        this.serviceId = opts.serviceId;
        this.dependencyId = opts.dependencyId;
        this.syncSpinnerId = opts.syncSpinnerId;
        this.domId = opts.domId;

        this._ticker = setInterval(this.inSync(), 5000);
    }
};

Breakerbox.SyncState.prototype.showSpinner = function() {
    $('.' + this.syncSpinnerId).show();
}

Breakerbox.SyncState.prototype.hideSpinner = function() {
    $('.' + this.syncSpinnerId).hide();
}

Breakerbox.SyncState.prototype.showDom = function() {
    $('#' + this.domId).show();
}

Breakerbox.SyncState.prototype.hideDom = function() {
    $('#' + this.domId).hide();
}

Breakerbox.SyncState.prototype.inSync = function() {
    this.showSpinner();
    this.hideDom();
    var self = this;

    $.ajax({
        dataType: "json",
        url: "/sync/" + this.serviceId + '/' + this.dependencyId,
        timeout: 2000,
        success: function(data) {
            $('#' + self.domId)[0].innerHTML = self.createDom(data);
            self.hideSpinner();
            self.showDom();
        },
        error: function() {
            $('#' + self.domId)[0].innerHTML = self.createErrorDom();
            self.hideSpinner();
            self.showDom();
        }
    });
};

Breakerbox.SyncState.prototype.createDom = function(jsonData) {
    var htmlAcc = '';

    $(jsonData).each(function(index, value) {
        if (value.syncStatus == 'UNSYNCHRONIZED') {
            htmlAcc += '<dt>Unsynchronized <span class="glyphicon glyphicon-exclamation-sign"></span></dt>';
        } else if (value.syncStatus == 'SYNCHRONIZED') {
            htmlAcc += '<dt>Synchronized <span class="glyphicon glyphicon-ok-sign"></span></dt>';
        } else {
            htmlAcc += '<dt>Unknown <span class="glyphicon glyphicon-question-sign"></span></dt>';
        }
        htmlAcc += '<dd>' + value.uri +'</dd>';
    });

    return htmlAcc;
}

Breakerbox.SyncState.prototype.createErrorDom = function() {
    return '<dt><span class="glyphicon glyphicon-question-sign"></dt>' +
           '<dd>Unable to determine synchronized status</dd>';

}