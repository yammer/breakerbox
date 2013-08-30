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
    $('#' + this.syncSpinnerId).show();
}

Breakerbox.SyncState.prototype.hideSpinner = function() {
    $('#' + this.syncSpinnerId).hide();
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
            self.hideSpinner();
            $('#' + self.domId)[0].innerHTML = "<span>poop</span>";
            self.showDom();
        }
    });
};