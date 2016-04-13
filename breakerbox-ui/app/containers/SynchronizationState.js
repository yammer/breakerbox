var React = require('react');
var $ = require('jquery');
var _ = require('lodash');

var SynchronizationState = React.createClass({
  _REFRESH_INTERVAL: 30000,

  getInitialState: function() {
    return {
      syncStates: []
    };
  },

  componentDidMount: function() {
    this.throttledRefreshSyncState = _.throttle(this._refreshSyncState, this._REFRESH_INTERVAL, {'leading': false});
    this._refreshSyncState(this.props);
  },

  componentWillUnmount: function() {
    this.throttledRefreshSyncState.cancel();
  },

  componentWillReceiveProps: function(nextProps) {
    if ((this.props.service !== nextProps.service) || (this.props.propertyKey !== nextProps.propertyKey)) {
      this._refreshSyncState(nextProps);
    }
  },

  _refreshSyncState: function(props) {
    var url = ['/sync', props.service, props.propertyKey].join('/');
    $.ajax({
      url: url,
      dataType: 'json',
      cache: false,
      context: this
    })
    .done(function(data) {
      this.setState({syncStates: data});
    })
    .fail(function(xhr, status, err) {
      console.error(url, status, err.toString());
    })
    .always(function() {
      this.throttledRefreshSyncState(props);
    });
  },

  _instanceStates: function() {
    return this.state.syncStates.map(function(item) {
      var dt = this._syncStatusElement(item.syncStatus);
      return (
        <span key={item.uri}>
          {dt}
          <dd>{item.uri}</dd>
        </span>
        );
    }, this);
  },

  _syncStatusElement: function(syncStatus) {
    switch (syncStatus) {
      case 'SYNCHRONIZED':   return <dt>Synchronized <span className="glyphicon glyphicon-ok-sign"></span></dt>;
      case 'UNSYNCHRONIZED': return <dt>Unsynchronized <span className="glyphicon glyphicon-exclamation-sign"></span></dt>;
      default:               return <dt>Unknown <span className="glyphicon glyphicon-question-sign"></span></dt>; 
    }
  },

  render: function() {
    var dlStyle = {marginTop: '14px'};
    return (
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">{this.props.service} Synchronization State</h3>
        </div>
        <div className="panel-body">
          <dl className="dl-horizontal" style={dlStyle}>
            {this._instanceStates()}
          </dl>
        </div>
      </div>
      );
  }
});

module.exports = SynchronizationState;