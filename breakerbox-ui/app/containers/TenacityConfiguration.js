var React = require('react');
var $ = require('jquery');
var _ = require('lodash');
var moment = require('moment');

var TenacityConfigurationVersionsFormItem = require('../components/configureForm/TenacityConfigurationVersionsFormItem');
var ExecutionTimeFormItem = require('../components/configureForm/ExecutionTimeFormItem');
var RequestVolumeThresholdFormItem = require('../components/configureForm/RequestVolumeThresholdFormItem');
var ErrorThresholdFormItem = require('../components/configureForm/ErrorThresholdFormItem');
var SleepWindowFormItem = require('../components/configureForm/SleepWindowFormItem');
var StatisticalWindowFormItem = require('../components/configureForm/StatisticalWindowFormItem');
var StatisticalWindowBucketsFormItem = require('../components/configureForm/StatisticalWindowBucketsFormItem');
var ExecutionIsolationStrategy = require('../components/configureForm/ExecutionIsolationStrategy');
var CorePoolSizeFormItem = require('../components/configureForm/CorePoolSizeFormItem');
var KeepAliveTimeFormItem = require('../components/configureForm/KeepAliveTimeFormItem');
var MaxQueueSizeFormItem = require('../components/configureForm/MaxQueueSizeFormItem');
var QueueSizeRejectionThresholdFormItem = require('../components/configureForm/QueueSizeRejectionThresholdFormItem');
var MaxRequestsFormItem = require('../components/configureForm/MaxRequestsFormItem');
var MaxFallbackRequestsFormItem = require('../components/configureForm/MaxFallbackRequestsFormItem');
var SaveButton = require('../components/SaveButton');

var TenacityConfigurationModel = function(options) {
  options = options || {};
  this.dependencyId = options.dependencyId;
  this.dateTime = options.dateTime;
  this.tenacityConfiguration = options.tenacityConfiguration || this.defaultTenacityConfiguration();
  this.user = options.user;
  this.serviceId = options.serviceId;
};

TenacityConfigurationModel.prototype.isDefault = function() {
  return _.isUndefined(this.user);
};

TenacityConfigurationModel.prototype.defaultTenacityConfiguration = _.once(function() {
  return {
    threadpool: {
      threadPoolCoreSize: 10,
      keepAliveTimeMinutes: 1,
      maxQueueSize: -1,
      queueSizeRejectionThreshold: 5,
      metricsRollingStatisticalWindowInMilliseconds: 10000,
      metricsRollingStatisticalWindowBuckets: 10
    },
    circuitBreaker: {
      requestVolumeThreshold: 20,
      sleepWindowInMillis: 5000,
      errorThresholdPercentage: 50,
      metricsRollingStatisticalWindowInMilliseconds: 10000,
      metricsRollingStatisticalWindowBuckets: 10
    },
    semaphore: {
      maxConcurrentRequests: 10,
      fallbackMaxConcurrentRequests: 10
    },
    executionIsolationThreadTimeoutInMillis: 1000,
    executionIsolationStrategy: undefined
  };
});

TenacityConfigurationModel.prototype.toString = function() {
  return [moment(new Date(this.dateTime)).format('llll ZZ'), 'by', this.user].join(' ');
};

var TenacityConfiguration = React.createClass({
  contextTypes: {
    router: React.PropTypes.object.isRequired
  },

  getInitialState: function() {
    return {
      configurations: [],
      saveButtonText: 'Save',
      selectedTenacityConfiguration: new TenacityConfigurationModel()
    }
  },

  _configurationsUrl: function(props) {
    return ['/clusters', props.service, 'configurations', props.configuration].join('/');
  },

  _url: function() {
    return ['/configure', this.props.service, this.props.configuration].join('/');
  },

  componentDidMount: function() {
    this._fetchTenacityConfigurations(this.props);
  },

  componentWillReceiveProps: function(nextProps) {
    if (this.props.configuration !== nextProps.configuration) {
      this.setState({selectedTenacityConfiguration: new TenacityConfigurationModel()});
      this._fetchTenacityConfigurations(nextProps);
    }
  },

  _fetchTenacityConfigurations: function(props) {
    var url = this._configurationsUrl(props);
    $.ajax({
      url: url,
      dataType: 'json',
      cache: false,
      context: this
    })
    .done(function(data) {
      var newConfigurations = this._initializeConfigurations(data);
      this.setState({
        configurations: newConfigurations,
        selectedTenacityConfiguration: this._configurationVersion(newConfigurations) || _.head(newConfigurations)
      });
    })
    .fail(function(xhr, status, err) {
      console.error(url, status, err.toString());
    });
  },

  _initializeConfigurations: function(configurations) {
    if (_.isEmpty(configurations)) {
      return [new TenacityConfigurationModel()];
    } else {
      return configurations.map(function(item) {
        return _.extend(item, TenacityConfigurationModel.prototype);
      });
    }
  },

  _configurationVersion: function(configurations) {
    if (!_.isUndefined(this.props.configurationVersion)) {
      return _.head(configurations.filter(function(item) {
        return Number(this.props.configurationVersion) === item.dateTime;
      }, this));
    }
  },

  _handleTenacityConfigurationVersionChange: function(event) {
    var tenacityConfiguration = _.head(this.state.configurations.filter(function(item) {
      return item.dateTime === Number(event.target.options[event.target.selectedIndex].value);
    }));
    this.setState({selectedTenacityConfiguration: tenacityConfiguration});
    this._routerPush({version: tenacityConfiguration.dateTime});
  },

  _routerPush: function(query) {
    this.context.router.push({
      pathname: this._url(),
      query: query
    });
  },

  onSubmit: function(event){
    event.preventDefault();

    var resetSaveButtonText = function() {
      this.setState({saveButtonText: 'Save'});
    }.bind(this);
    var url = this._url();

    this.setState({saveButtonText: 'Saving...'});

    $.ajax({
        url: url,
        type: 'POST',
        timeout: 30000,
        data: $(event.target).serialize(),
        dataType: 'json',
        context: this
    })
    .done(function() {
      this.setState({saveButtonText: 'Saved!'});
      _.delay(resetSaveButtonText, 1000);
      this._routerPush();
      this._fetchTenacityConfigurations(this.props);
    })
    .fail(function() {
      this.setState({saveButtonText: 'Failed!'});
      _.delay(resetSaveButtonText, 1000);
    });
  },

  render: function() {
    var tenacityModel = this.state.selectedTenacityConfiguration;
    var tenacityConfiguration = tenacityModel.tenacityConfiguration;
    var executionIsolationStrategy = {name: 'executionIsolationStrategy'};
    var threadRadio = _.extend({id: 'radioThreadStrategy', value: 'THREAD'}, executionIsolationStrategy);
    var semaphoreRadio = _.extend({id: 'radioSemaphoreStrategy', value: 'SEMAPHORE'}, executionIsolationStrategy);

    return (
      <form id="configure-form" className="form-horizontal" onSubmit={this.onSubmit}>
        <TenacityConfigurationVersionsFormItem configurations={this.state.configurations} onChange={this._handleTenacityConfigurationVersionChange} selectedValue={tenacityModel.dateTime} />
        <ExecutionTimeFormItem value={tenacityConfiguration.executionIsolationThreadTimeoutInMillis} />
        <RequestVolumeThresholdFormItem value={tenacityConfiguration.circuitBreaker.requestVolumeThreshold} />
        <ErrorThresholdFormItem value={tenacityConfiguration.circuitBreaker.errorThresholdPercentage} />
        <SleepWindowFormItem value={tenacityConfiguration.circuitBreaker.sleepWindowInMillis} />
        <StatisticalWindowFormItem id="inputCircuitBreakerRollingStatisticalWindow" name="circuitBreakerstatisticalWindow" value={tenacityConfiguration.circuitBreaker.metricsRollingStatisticalWindowInMilliseconds} />
        <StatisticalWindowBucketsFormItem id="inputCircuitBreakerStatisticalWindowBuckets" name="circuitBreakerStatisticalWindowBuckets" value={tenacityConfiguration.circuitBreaker.metricsRollingStatisticalWindowBuckets} />
        <ExecutionIsolationStrategy strategy={tenacityConfiguration.executionIsolationStrategy} thread={threadRadio} semaphore={semaphoreRadio} />
        <CorePoolSizeFormItem value={tenacityConfiguration.threadpool.threadPoolCoreSize} />
        <KeepAliveTimeFormItem value={tenacityConfiguration.threadpool.keepAliveTimeMinutes} />
        <MaxQueueSizeFormItem value={tenacityConfiguration.threadpool.maxQueueSize} />
        <QueueSizeRejectionThresholdFormItem value={tenacityConfiguration.threadpool.queueSizeRejectionThreshold} />
        <StatisticalWindowFormItem id="inputThreadpoolRollingStatisticalWindow" name="threadpoolStatisticalWindow" value={tenacityConfiguration.threadpool.metricsRollingStatisticalWindowInMilliseconds} />
        <StatisticalWindowBucketsFormItem id="inputThreadpoolStatisticalWindowBuckets" name="threadpoolStatisticalWindowBuckets" value={tenacityConfiguration.threadpool.metricsRollingStatisticalWindowBuckets} />
        <MaxRequestsFormItem value={tenacityConfiguration.semaphore.maxConcurrentRequests} />
        <MaxFallbackRequestsFormItem value={tenacityConfiguration.semaphore.fallbackMaxConcurrentRequests} />
        <SaveButton text={this.state.saveButtonText}/>
      </form>
    );
  }
});

module.exports = TenacityConfiguration;