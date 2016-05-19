var React = require('react');
var _ = require('lodash');
var d3 = require('d3');
var $ = require('jquery');
var HystrixUtils = require('../lib/HystrixUtils');

var HystrixCircuit = React.createClass({
  componentDidMount: function() {
    /* escape with two backslashes */
    var vis = d3.select('#chart_CIRCUIT_' + this.props.escapedName).append("svg:svg").attr("width", "100%").attr("height", "100%");
    /* add a circle -- we don't use the data point, we set it manually, so just passing in [1] */
    var circle = vis.selectAll("circle").data([1]).enter().append("svg:circle");
    /* setup the initial styling and sizing of the circle */
    circle.style("fill", "green").attr("cx", "30%").attr("cy", "30%").attr("r", 5);

    /* add the line graph - it will be populated by javascript, no default to show here */
    /* escape with two backslashes */
    d3.select('#graph_CIRCUIT_' + this.props.escapedName).append("svg:svg").attr("width", "100%").attr("height", "100%"); 
    d3.selectAll('#graph_CIRCUIT_' + this.props.escapedName + ' svg').append("svg:path");
    this.updateD3();
    this.props.hystrixMonitor.sortSameAsLast();
  },

  circuitStatus: function() {
    if (this.props.propertyValue_circuitBreakerForceClosed) {
      return <span className="smaller">[ <font style={{color: "orange"}}>Forced Closed</font> ]</span>;
    }
    if (this.props.propertyValue_circuitBreakerForceOpen) {
      return <font style={{color: "red"}}>Forced Open</font>;
    } else {
      if (this.props.isCircuitBreakerOpen == this.props.reportingHosts) {
        return <font style={{color: "red"}}>Open</font>;
      } else if (this.props.isCircuitBreakerOpen == 0) {
        return <font style={{color: "green"}}>Closed</font>;
      } else { 
        return <font style={{color: "orange"}}>{this.props.isCircuitBreakerOpen.toString().replace("true", "Open").replace("false", "Closed")}</font>;
      }
    }
  },

  rejectedRequestCount: function() {
    if (this.props.propertyValue_executionIsolationStrategy === 'THREAD') {
      return (
        <a href="javascript://" title="Threadpool Rejected Request Count" className="line rejected">
          {HystrixUtils.addCommas(this.props.rollingCountThreadPoolRejected)}
        </a>);
    }
    else if (this.props.propertyValue_executionIsolationStrategy === 'SEMAPHORE') {
      return (
        <a href="javascript://" title="Semaphore Rejected Request Count" className="line rejected">
          {HystrixUtils.addCommas(this.props.rollingCountSemaphoreRejected)}
        </a>);
    }
  },

  componentDidUpdate: function() {
    this.updateD3();
  },

  updateD3: function() {
    var ratePerSecond = this.props.ratePerSecond;
    var ratePerSecondPerHost = this.props.ratePerSecondPerHost;
    var ratePerSecondPerHostDisplay = ratePerSecondPerHost;
    var errorThenVolume = isNaN( ratePerSecond )? -1: (this.props.errorPercentage * 100000000) +  ratePerSecond;
    // set the rates on the div element so it's available for sorting
    $('#CIRCUIT_' + this.props.escapedName).attr('rate_value', ratePerSecond);
    $('#CIRCUIT_' + this.props.escapedName).attr('error_then_volume', errorThenVolume);

    // update errorPercentage color on page
    $('#CIRCUIT_' + this.props.escapedName + ' a.errorPercentage').css('color', this.props.hystrixMonitor.circuitErrorPercentageColorRange(this.props.errorPercentage));

    this.props.hystrixMonitor.updateCircle('circuit', '#CIRCUIT_' + this.props.escapedName + ' circle', ratePerSecondPerHostDisplay, this.props.errorPercentage);

    if (this.props.graphValues) {
      // we have a set of values to initialize with
      this.props.hystrixMonitor.updateSparkline('circuit', '#CIRCUIT_' + this.props.escapedName + ' path', this.props.graphValues);
    } else {
      this.props.hystrixMonitor.updateSparkline('circuit', '#CIRCUIT_' + this.props.escapedName + ' path', ratePerSecond);
    }
  },

  render: function() {
    return (
      <div className="monitor_data">
        <div className="counters"> 
          <div className="cell line">
            <a href="javascript://" title="Error Percentage [Timed-out + Threadpool Rejected + Failure / Total]" className="errorPercentage">
              <span className="value">{this.props.errorPercentage}</span> %
            </a>
          </div>
          
          <div className="cell borderRight">
            <a href="javascript://" title="Timed-out Request Count" className="line timeout">
              {HystrixUtils.addCommas(this.props.rollingCountTimeout)}
            </a>
            {this.rejectedRequestCount()}
            <a href="javascript://" title="Failure Request Count" className="line failure">
              {HystrixUtils.addCommas(this.props.rollingCountFailure)}
            </a> 
          </div>
          <div className="cell borderRight">
            <a href="javascript://" title="Successful Request Count" className="line success">
              {HystrixUtils.addCommas(this.props.rollingCountSuccess)}
            </a>
            <a href="javascript://" title="Short-circuited Request Count" className="line shortCircuited">
              {HystrixUtils.addCommas(this.props.rollingCountShortCircuited)}
            </a>
            <a href="javascript://" title="Bad Request Count" className="line badRequest">
              {HystrixUtils.addCommas(this.props.rollingCountBadRequests)}
            </a>
            <br></br>
          </div>
        </div>

        <div className="rate">
          <a href="javascript://" title="Total Request Rate per Second per Reporting Host" className="rate">
            <span className="smaller">Host: </span>
            <span className="ratePerSecondPerHost">{HystrixUtils.addCommas(HystrixUtils.roundNumber(this.props.ratePerSecondPerHost))}</span>/s
          </a>
        </div>
        <div className="rate">  
          <a href="javascript://" title="Total Request Rate per Second for Cluster" className="rate">
            <span className="smaller">Cluster: </span>
            <span className="ratePerSecond">{HystrixUtils.addCommas(HystrixUtils.roundNumber(this.props.ratePerSecond))}</span>/s
          </a>
        </div>

        <div className="circuitStatus">
          Circuit {this.circuitStatus()}
        </div>
    
        <div className="spacer"></div>

        <div className="tableRow">
          <div className="cell header">Hosts</div>
          <div className="cell data">{this.props.reportingHosts || 'Single'}</div>
          <div className="cell header">90th</div>
          <div className="cell data latency90"><span className="value">{HystrixUtils.getInstanceAverage(this.props.latencyTotal['90'], this.props.reportingHosts, false)}</span>ms</div>
        </div>
        <div className="tableRow">
          <div className="cell header">Median</div>
          <div className="cell data latencyMedian"><span className="value">{HystrixUtils.getInstanceAverage(this.props.latencyTotal['50'], this.props.reportingHosts, false)}</span>ms</div>
          <div className="cell header">99th</div>
          <div className="cell data latency99"><span className="value">{HystrixUtils.getInstanceAverage(this.props.latencyTotal['99'], this.props.reportingHosts, false)}</span>ms</div>
        </div>
        <div className="tableRow">
          <div className="cell header">Mean</div>
          <div className="cell data latencyMean"><span className="value">{this.props.latencyTotal_mean}</span>ms</div>
          <div className="cell header">99.5th</div>
          <div className="cell data latency995"><span className="value">{HystrixUtils.getInstanceAverage(this.props.latencyTotal['99.5'], this.props.reportingHosts, false)}</span>ms</div>
        </div>
      </div>);
  }
});

module.exports = HystrixCircuit;