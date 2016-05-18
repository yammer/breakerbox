var React = require('react');
var $ = require('jquery');
var _ = require('lodash');
var HystrixCircuit = require('./HystrixCircuit');

function preProcessData(data) {
  // set defaults for values that may be missing from older streams
  setIfMissing(data, "rollingCountBadRequests", 0);
  // assert all the values we need
  validateData(data);
  // escape string used in jQuery & d3 selectors
  data.escapedName = data.name.replace(/([ !"#$%&'()*+,./:;<=>?@[\]^`{|}~])/g,'\\$1') + '_' + data.index;
  // do math
  convertAllAvg(data);
  calcRatePerSecond(data);
};

function setIfMissing(data, key, defaultValue) {
  if(data[key] == undefined) {
    data[key] = defaultValue;
  }
};

/**
 * Since the stream of data can be aggregated from multiple hosts in a tiered manner
 * the aggregation just sums everything together and provides us the denominator (reportingHosts)
 * so we must divide by it to get an average per instance value. 
 * 
 * We want to do this on any numerical values where we want per instance rather than cluster-wide sum.
 */
function convertAllAvg(data) {
  convertAvg(data, "errorPercentage", true);
  convertAvg(data, "latencyExecute_mean", false);
  convertAvg(data, "latencyTotal_mean", false);
  
  // the following will break when it becomes a compound string if the property is dynamically changed
  convertAvg(data, "propertyValue_metricsRollingStatisticalWindowInMilliseconds", false);
};

function convertAvg(data, key, decimal) {
  if (decimal) {
    data[key] = getInstanceAverage(data[key], data["reportingHosts"], decimal);
  } else {
    data[key] = getInstanceAverage(data[key], data["reportingHosts"], decimal);
  }
};

function getInstanceAverage(value, reportingHosts, decimal) {
  if (decimal) {
    return roundNumber(value/reportingHosts);
  } else {
    return Math.floor(value/reportingHosts);
  }
};

function calcRatePerSecond(data) {
  var numberSeconds = data["propertyValue_metricsRollingStatisticalWindowInMilliseconds"] / 1000;

  var totalRequests = data["requestCount"];
  if (totalRequests < 0) {
    totalRequests = 0;
  }
  data["ratePerSecond"] =  roundNumber(totalRequests / numberSeconds);
  data["ratePerSecondPerHost"] =  roundNumber(totalRequests / numberSeconds / data["reportingHosts"]) ;
};

function validateData(data) {
  assertNotNull(data,"reportingHosts");
  assertNotNull(data,"type");
  assertNotNull(data,"name");
  assertNotNull(data,"group");
  // assertNotNull(data,"currentTime");
  assertNotNull(data,"isCircuitBreakerOpen");
  assertNotNull(data,"errorPercentage");
  assertNotNull(data,"errorCount");
  assertNotNull(data,"requestCount");
  assertNotNull(data,"rollingCountCollapsedRequests");
  assertNotNull(data,"rollingCountExceptionsThrown");
  assertNotNull(data,"rollingCountFailure");
  assertNotNull(data,"rollingCountFallbackFailure");
  assertNotNull(data,"rollingCountFallbackRejection");
  assertNotNull(data,"rollingCountFallbackSuccess");
  assertNotNull(data,"rollingCountResponsesFromCache");
  assertNotNull(data,"rollingCountSemaphoreRejected");
  assertNotNull(data,"rollingCountShortCircuited");
  assertNotNull(data,"rollingCountSuccess");
  assertNotNull(data,"rollingCountThreadPoolRejected");
  assertNotNull(data,"rollingCountTimeout");
  assertNotNull(data,"rollingCountBadRequests");
  assertNotNull(data,"currentConcurrentExecutionCount");
  assertNotNull(data,"latencyExecute_mean");
  assertNotNull(data,"latencyExecute");
  assertNotNull(data,"latencyTotal_mean");
  assertNotNull(data,"latencyTotal");
  assertNotNull(data,"propertyValue_circuitBreakerRequestVolumeThreshold");
  assertNotNull(data,"propertyValue_circuitBreakerSleepWindowInMilliseconds");
  assertNotNull(data,"propertyValue_circuitBreakerErrorThresholdPercentage");
  assertNotNull(data,"propertyValue_circuitBreakerForceOpen");
  assertNotNull(data,"propertyValue_executionIsolationStrategy");
  assertNotNull(data,"propertyValue_executionIsolationThreadTimeoutInMilliseconds");
  assertNotNull(data,"propertyValue_executionIsolationThreadInterruptOnTimeout");
  // assertNotNull(data,"propertyValue_executionIsolationThreadPoolKeyOverride");
  assertNotNull(data,"propertyValue_executionIsolationSemaphoreMaxConcurrentRequests");
  assertNotNull(data,"propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests");
  assertNotNull(data,"propertyValue_requestCacheEnabled");
  assertNotNull(data,"propertyValue_requestLogEnabled");
  assertNotNull(data,"propertyValue_metricsRollingStatisticalWindowInMilliseconds");
}
    
function assertNotNull(data, key) {
  if(data[key] == undefined) {
    throw new Error("Key Missing: " + key + " for " + data.name);
  }
}

/* round a number to X digits: num => the number to round, dec => the number of decimals */
function roundNumber(num) {
  var dec=1;
  var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
  var resultAsString = result.toString();
  if(resultAsString.indexOf('.') == -1) {
    resultAsString = resultAsString + '.0';
  }
  return resultAsString;
};


function addCommas(nStr){
  nStr += '';
  if(nStr.length <=3) {
    return nStr; //shortcut if we don't need commas
  }
  x = nStr.split('.');
  x1 = x[0];
  x2 = x.length > 1 ? '.' + x[1] : '';
  var rgx = /(\d+)(\d{3})/;
  while (rgx.test(x1)) {
    x1 = x1.replace(rgx, '$1' + ',' + '$2');
  }
  return x1 + x2;
};


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
          deleteCircuit(data.escapedName);
        } else {
          this.displayCircuit(data);
        }
      }
    }
  },

  displayCircuit: function(data) { 
    try {
      preProcessData(data);
    } catch (err) {
      console.log("Failed preProcessData: " + err.message);
      return;
    }
    
    // add the 'addCommas' function to the 'data' object so the HTML templates can use it
    data.addCommas = addCommas;
    // add the 'roundNumber' function to the 'data' object so the HTML templates can use it
    data.roundNumber = roundNumber;
    // add the 'getInstanceAverage' function to the 'data' object so the HTML templates can use it
    data.getInstanceAverage = getInstanceAverage;
    
    var addNew = false;
    // check if we need to create the container
    if(!$('#CIRCUIT_' + data.escapedName).length) {
      // args for display
      if(this.props.hystrixMonitor.args.includeDetailIcon != undefined && this.props.hystrixMonitor.includeDetailIcon) {
        data.includeDetailIcon = true;
      }else {
        data.includeDetailIcon = false;
      }

      var newHystrixMonitor = {};
      newHystrixMonitor[data.escapedName] = <HystrixCircuit {...data}/>;

      this.setState({
        hystrixMonitors: _.extend(this.state.hystrixMonitors, newHystrixMonitor)
      });
      
      // it doesn't exist so add it
      //var html = tmpl(this.hystrixTemplateCircuitContainer, data);
      // remove the loading thing first
      //$('#' + this.containerId + ' span.loading').remove();
      // now create the new data and add it
      //$('#' + this.containerId + '').append(html);
      
      // add the default sparkline graph
      //d3.selectAll('#graph_CIRCUIT_' + data.escapedName + ' svg').append("svg:path");
      
      // remember this is new so we can trigger a sort after setting data
      addNew = true;
    }
    
    /*
    // now update/insert the data
    $('#CIRCUIT_' + data.escapedName + ' div.monitor_data').html(tmpl(this.hystrixTemplateCircuit, data));
    
    var ratePerSecond = data.ratePerSecond;
    var ratePerSecondPerHost = data.ratePerSecondPerHost;
    var ratePerSecondPerHostDisplay = ratePerSecondPerHost;
    var errorThenVolume = isNaN( ratePerSecond )? -1: (data.errorPercentage * 100000000) +  ratePerSecond;
    // set the rates on the div element so it's available for sorting
    $('#CIRCUIT_' + data.escapedName).attr('rate_value', ratePerSecond);
    $('#CIRCUIT_' + data.escapedName).attr('error_then_volume', errorThenVolume);
    
    // update errorPercentage color on page
    $('#CIRCUIT_' + data.escapedName + ' a.errorPercentage').css('color', this.circuitErrorPercentageColorRange(data.errorPercentage));
    
    this.updateCircle('circuit', '#CIRCUIT_' + data.escapedName + ' circle', ratePerSecondPerHostDisplay, data.errorPercentage);
    
    if(data.graphValues) {
      // we have a set of values to initialize with
      this.updateSparkline('circuit', '#CIRCUIT_' + data.escapedName + ' path', data.graphValues);
    } else {
      this.updateSparkline('circuit', '#CIRCUIT_' + data.escapedName + ' path', ratePerSecond);
    }

    if(addNew) {
      // sort since we added a new circuit
      this.sortSameAsLast();
    }
    */
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