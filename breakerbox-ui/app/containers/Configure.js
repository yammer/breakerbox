var React = require('react');
var $ = require('jquery');
var _ = require('lodash');

var TenacityConfiguration = require('./TenacityConfiguration')
var PropertyKeys = require('../components/PropertyKeys');
var SynchronizationState = require('./SynchronizationState');

var Configure = React.createClass({
  contextTypes: {
    router: React.PropTypes.object.isRequired
  },

  _REFRESH_INTERVAL: 30000,

  getInitialState: function() {
    return {
      propertyKeys: [],
      selectedPropertyKey: this.props.params.propertyKey
    };
  },

  componentDidMount: function() {
    this.throttledRefreshSyncState = _.throttle(this._refreshSyncState, this._REFRESH_INTERVAL, {'leading': false});
    this._fetchPropertyKeys(this.props);
  },

  _fetchPropertyKeys: function(props) {
    var propertyKeysUri = ['/clusters', props.params.id, 'propertykeys'].join('/');
   $.ajax({
      url: propertyKeysUri,
      dataType: 'json',
      cache: false,
      context: this
    })
    .done(function(data) {
      var items = data.map(function(item) {
        return {
          propertyKey: item,
          syncStatus: 'UNKNOWN'
        }
      });
      this.setState({propertyKeys: items});
    })
    .fail(function(xhr, status, err) {
      console.error(propertyKeysUri, status, err.toString());
    })
    .always(function() {
      this._refreshSyncState(props);
    });
  },

  componentWillUnmount: function() {
    this.throttledRefreshSyncState.cancel();
  },

  _refreshSyncState: function(props) {
    var syncStateUri = ['/sync', props.params.id].join('/');
    $.ajax({
      url: syncStateUri,
      dataType: 'json',
      cache: false,
      context: this
    })
    .done(function(data) {
      if (_.isUndefined(this.state.selectedPropertyKey)) {
        this.context.router.push(['/configure', props.params.id, this._firstPropertyKey()].join('/'));
      }
      this.setState({propertyKeys: data});
    })
    .fail(function(xhr, status, err) {
      console.error(syncStateUri, status, err.toString());
    })
    .always(function() {
      this.throttledRefreshSyncState(props);
    });
  },

  componentWillReceiveProps: function(nextProps) {
    if (this.props.params.id !== nextProps.params.id) {
      this._fetchPropertyKeys(nextProps);
    }
    this.setState({selectedPropertyKey: nextProps.params.propertyKey});
  },

  _firstPropertyKey: function() {
    if (!_.isEmpty(this.state.propertyKeys)) {
      return _.head(this.state.propertyKeys).propertyKey;
    }
  },

  _selectedOrFirstPropertyKey: function() {
    return this.state.selectedPropertyKey || this._firstPropertyKey();
  },

  _tenacityConfigurationComponent: function(propertyKey) {
    if (!_.isUndefined(propertyKey)) {
      return <TenacityConfiguration service={this.props.params.id} configuration={propertyKey} configurationVersion={this.props.location.query.version} />
    }
  },

  _synchronizationStateComponent: function(propertyKey) {
    if (!_.isUndefined(propertyKey)) {
      return <SynchronizationState service={this.props.params.id} propertyKey={propertyKey}/>
    }
  },

  render: function() {
    var propertyKey = this._selectedOrFirstPropertyKey();
    return (
    <div>
      <div className="col-lg-3">
        <div className="well well-sm">
          <div className="list-group">
            <span className="list-group-item">
              <h5 className="list-group-item-heading">PROPERTY KEYS</h5>
            </span>
            <PropertyKeys data={this.state.propertyKeys} service={this.props.params.id} selected={propertyKey}/>
          </div>
        </div>
      </div>
      <div className="col-lg-9">
        {this._tenacityConfigurationComponent(propertyKey)}
        {this._synchronizationStateComponent(propertyKey)}
      </div>
    </div>);
  }
});

module.exports = Configure;