var Breakerbox = {
     SyncState: function(opts) {
        opts = opts || {};

        this.serviceId = opts.serviceId;
        this.dependencyId = opts.dependencyId;
        this.syncSpinnerId = opts.syncSpinnerId;
        this.domId = opts.domId;

        var self = this;

        this.inSync();

        this._ticker = setInterval(function() {
            self.inSync();
        }, 30000);
    },

    ConfigureForm: function(opts) {
        opts = opts || {};

        this.serviceId = opts.serviceId;
        this.dependencyId = opts.dependencyId;
        this.dependencyConfigVersion = opts.dependencyConfigVersion;
        this.formObj = opts.formObj;

        this.registerConfigVersionChange();
        this.registerSubmit();
    },

    ActivePropertyKey: function(opts) {
        opts = opts || {};

        var propertyKey = this.parseKey(opts.uri);
        if (propertyKey) {
            this.propertyKey = propertyKey;
        } else {
            this.propertyKey = this.firstPropertyKey();
        }
    }
};

Breakerbox.ActivePropertyKey.prototype.parseKey = function(uri) {
    return uri.split('/')[3];
};

Breakerbox.ActivePropertyKey.prototype.firstPropertyKey = function() {
    return $('.list-group-item > p').first().parent()[0].id;
};

Breakerbox.ActivePropertyKey.prototype.highlightActive = function() {
    $('#' + this.propertyKey).addClass('active');
};

Breakerbox.ConfigureForm.prototype.registerConfigVersionChange = function() {
    var serviceId = this.serviceId;
    var dependency = this.dependencyId;
    this.dependencyConfigVersion.change(function() {
        window.location.href = '/configure/' + serviceId + '/' + dependency + '?version=' + $(this).val();
    });
};

Breakerbox.ConfigureForm.prototype.registerSubmit = function() {
    var self = this;
    this.formObj.submit(function() {
        event.preventDefault();

        var buttonSelector = $('.btn');
        buttonSelector.button('loading');

        $.ajax({
            type: "POST",
            timeout: 30000,
            data: $(this).serialize(),
            url: '/configure/' + self.serviceId + '/' + self.dependencyId,
            success: function() {
                buttonSelector.button('complete');
                setTimeout(function() {
                    window.location.href = '/configure/' + self.serviceId + '/' + self.dependencyId;
                }, 1000);
            },
            error: function() {
                buttonSelector.button('reset');
            }
        });
    });
};

Breakerbox.SyncState.prototype.showSpinner = function() {
    $('.' + this.syncSpinnerId).show();
};

Breakerbox.SyncState.prototype.hideSpinner = function() {
    $('.' + this.syncSpinnerId).hide();
};

Breakerbox.SyncState.prototype.showDom = function() {
    $('#' + this.domId).show();
};

Breakerbox.SyncState.prototype.hideDom = function() {
    $('#' + this.domId).hide();
};

Breakerbox.SyncState.prototype.inSync = function() {
    this.showSpinner();
    this.hideDom();
    var self = this;

    $.ajax({
        type: 'GET',
        dataType: 'json',
        url: "/sync/" + this.serviceId + '/' + this.dependencyId,
        timeout: 30000,
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
};

Breakerbox.SyncState.prototype.createErrorDom = function() {
    return '<dt><span class="glyphicon glyphicon-question-sign"></dt>' +
           '<dd>Unable to determine synchronized status</dd>';

};
