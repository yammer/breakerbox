var React = require('react');
var $ = require('jquery');
var _ = require('lodash');
var HystrixCircuitContainer = require('./HystrixCircuitContainer');
var HystrixUtils = require('../lib/HystrixUtils');

var HystrixCircuits = React.createClass({
  getInitialState: function() {
    this.props.hystrixMonitor.sortByErrorThenVolume();
    this.props.dependencyThreadPoolMonitor.sortByVolume();

    var proxyStream = '/tenacity/proxy.stream?origin=localhost:8080/turbine.stream?cluster=production&delay=1000'
    var source = new EventSource(proxyStream);

    source.addEventListener('message', this.hystrixCircuitEventSourceMessageListener.bind(this), false);
    source.addEventListener('message', this.props.dependencyThreadPoolMonitor.eventSourceMessageListener, false);
    source.addEventListener('error', function(e) {
        $("#" + this.props.hystrixMonitor.containerId + " .loading").html("Unable to connect to Command Metric Stream.");
        $("#" + this.props.hystrixMonitor.containerId + " .loading").addClass("failed");
        if (e.eventPhase === EventSource.CLOSED) {
          // Connection was closed.
          console.log("Connection was closed on error: " + e);
        } else {
          console.log("Error occurred while streaming: " + e);
        }
      }, false);

    return {
      source: source,
      hystrixMonitors: {}
    };
  },

   hystrixCircuitEventSourceMessageListener: function(e) {
    var data = JSON.parse(e.data);
    if (data) {
      data.index = this.props.hystrixMonitor.index;
      // check for reportingHosts (if not there, set it to 1 for singleHost vs cluster)
      if (!data.reportingHosts) {
        data.reportingHosts = 1;
      }
      
      if(data && data.type == 'HystrixCommand') {
        if (data.deleteData == 'true') {
          HystrixUtils.deleteCircuit(data.escapedName);
        } else {
          this.displayCircuit(data);
        }
      }
    }
  },

  displayCircuit: function(data) { 
    try {
      HystrixUtils.preProcessData(data);
    } catch (err) {
      console.log("Failed preProcessData: " + err.message);
      return;
    }
    
    var addNew = false;
    // check if we need to create the container
    if (!_.has(this.state.hystrixMonitors, data.escapedName)) {
      // args for display
      if (this.props.hystrixMonitor.args.includeDetailIcon != undefined && this.props.hystrixMonitor.includeDetailIcon) {
        data.includeDetailIcon = true;
      }else {
        data.includeDetailIcon = false;
      }

      addNew = true;
    }

    var newHystrixMonitor = {};
    newHystrixMonitor[data.escapedName] = <HystrixCircuitContainer {...data} hystrixMonitor={this.props.hystrixMonitor}/>;

    this.setState({
      hystrixMonitors: _.extend(this.state.hystrixMonitors, newHystrixMonitor)
    });
  },

  renderHystrixCircuits: function() {
    if (_.isEmpty(this.state.hystrixMonitors)) {
      return <span className="loading">Loading ...</span>;
    } else {
      return _.map(this.state.hystrixMonitors, function(value, key) {
        return value;
      });
    }
  },

  render: function() {
    return (
      <div id={this.props.id} className="row dependencies">
        {this.renderHystrixCircuits()}
      </div>
      );
  }
});

module.exports = HystrixCircuits;