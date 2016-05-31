var React = require('react');
var HystrixThreadPoolUtils = require('../lib/HystrixThreadPoolUtils');
var HystrixThreadPoolContainer = require('./HystrixThreadPoolContainer');
var _ = require('lodash');
var $ = require('jquery');

var HystrixThreadPools = React.createClass({
   getInitialState: function() {
    this.props.hystrixThreadPoolMonitor.sortByVolume();
    this.props.source.addEventListener('message', this.eventSourceMessageListener.bind(this), false);
    this.props.source.addEventListener('error', function(e) {
        $("#" + this.props.hystrixThreadPoolMonitor.containerId + " .loading").html("Unable to connect to Command Metric Stream.");
        $("#" + this.props.hystrixThreadPoolMonitor.containerId + " .loading").addClass("failed");
        if (e.eventPhase === EventSource.CLOSED) {
          console.log("Connection was closed on error: " + e);
        } else {
          console.log("Error occurred while streaming: " + e);
        }
      }, false);

    return {
      hystrixThreadPoolMonitors: {}
    };
  },

  eventSourceMessageListener: function(e) {
    var data = JSON.parse(e.data);
    if (data) {
      data.index = this.props.hystrixThreadPoolMonitor.index;
      // check for reportingHosts (if not there, set it to 1 for singleHost vs cluster)
      if (!data.reportingHosts) {
        data.reportingHosts = 1;
      }
      
      if (data && data.type == 'HystrixThreadPool') {
        if (data.deleteData == 'true') {
          HystrixThreadPoolUtils.deleteThreadPool(data.escapedName);
        } else {
          this.displayThreadPool(data);
        }
      }
    }
  },

  displayThreadPool: function(data) { 
    try {
      HystrixThreadPoolUtils.preProcessData(data);
    } catch (err) {
      console.log("Failed preProcessData: " + err.message);
      return;
    }

    var newHystrixMonitor = {};
    newHystrixMonitor[data.escapedName] = <HystrixThreadPoolContainer {...data} hystrixThreadPoolMonitor={this.props.hystrixThreadPoolMonitor}/>;

    this.setState({
      hystrixThreadPoolMonitors: _.extend(this.state.hystrixThreadPoolMonitors, newHystrixMonitor)
    });
  },

  renderHystrixThreadPools: function() {
    if (_.isEmpty(this.state.hystrixThreadPoolMonitors)) {
      return <span className="loading">Loading ...</span>;
    } else {
      return _.map(this.state.hystrixThreadPoolMonitors, function(value, key) {
        return value;
      });
    }
  },

  render: function() {
    return (
      <div id={this.props.id} className="row dependencyThreadPools">
        {this.renderHystrixThreadPools()}
      </div>
      );
  }
});

module.exports = HystrixThreadPools;