var React = require('react');
var _ = require('lodash');
var d3 = require('d3');

var HystrixCircuit = React.createClass({
  reportingHosts: function() {
    if (_.isUndefined(this.props.reportingHosts)) {
      return    
      <div>    
        <div className="cell header">Host</div>
        <div className="cell data">Single</div>
      </div>;
    } else {
      return 
      <div>
        <div className="cell header">Hosts</div>
        <div className="cell data">{this.props.reportingHosts}</div>
      </div>;
    }
  },

  getInstanceAverage: function(value, reportingHosts, decimal) {
    if (decimal) {
      return this.roundNumber(value / reportingHosts);
    } else {
      return Math.floor(value / reportingHosts);
    }
  },

  roundNumber: function(num) {
    var dec = 1;
    var result = Math.round(num * Math.pow(10,dec)) / Math.pow(10, dec);
    var resultAsString = result.toString();
    if(resultAsString.indexOf('.') === -1) {
      resultAsString = resultAsString + '.0';
    }
    return resultAsString;
  },

  componentDidMount: function() {
    d3.selectAll('#graph_CIRCUIT_' + this.props.escapedName + ' svg').append("svg:path");
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

  addCommas: function(nStr){
    nStr += '';
    if (nStr.length <= 3) {
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
  },

  rejectedRequestCount: function() {
    if (this.props.propertyValue_executionIsolationStrategy === 'THREAD') {
      return 
        <a href="javascript://" title="Threadpool Rejected Request Count" className="line rejected">
          {this.addCommas(this.props.rollingCountThreadPoolRejected)}
        </a>;
    }
    else if (this.props.propertyValue_executionIsolationStrategy === 'SEMAPHORE') {
      return
        <a href="javascript://" title="Semaphore Rejected Request Count" className="line rejected">
          {this.addCommas(this.props.rollingCountSemaphoreRejected)}
        </a>;
    }
  },

  render: function() {
    return (
      <div>
        <div className="counters"> 
          <div className="cell line">
            <a href="javascript://" title="Error Percentage [Timed-out + Threadpool Rejected + Failure / Total]" className="errorPercentage">
              <span className="value">{this.props.errorPercentage}</span> %
            </a>
          </div>
          
          <div className="cell borderRight">
            <a href="javascript://" title="Timed-out Request Count" className="line timeout">
              {this.addCommas(this.props.rollingCountTimeout)}
            </a>
            {this.rejectedRequestCount()}
            <a href="javascript://" title="Failure Request Count" className="line failure">
              {this.addCommas(this.props.rollingCountFailure)}
            </a> 
          </div>
          <div className="cell borderRight">
            <a href="javascript://" title="Successful Request Count" className="line success">
              {this.addCommas(this.props.rollingCountSuccess)}
            </a>
            <a href="javascript://" title="Short-circuited Request Count" className="line shortCircuited">
              {this.addCommas(this.props.rollingCountShortCircuited)}
            </a>
            <a href="javascript://" title="Bad Request Count" className="line badRequest">
              {this.addCommas(this.props.rollingCountBadRequests)}
            </a>
            <br></br>
          </div>
        </div>

        <div className="rate">
          <a href="javascript://" title="Total Request Rate per Second per Reporting Host" className="rate">
            <span className="smaller">Host: </span>
            <span className="ratePerSecondPerHost">{this.addCommas(this.roundNumber(this.props.ratePerSecondPerHost))}</span>/s
          </a>
        </div>
        <div className="rate">  
          <a href="javascript://" title="Total Request Rate per Second for Cluster" className="rate">
            <span className="smaller">Cluster: </span>
            <span className="ratePerSecond">{this.addCommas(this.roundNumber(this.props.ratePerSecond))}</span>/s
          </a>
        </div>

        <div className="circuitStatus">
          Circuit {this.circuitStatus()}
        </div>
    
        <div className="spacer"></div>

        <div className="tableRow">
          {this.reportingHosts}
          <div className="cell header">90th</div>
          <div className="cell data latency90"><span className="value">{this.getInstanceAverage(this.props.latencyTotal['90'], this.props.reportingHosts, false)}</span>ms</div>
        </div>
        <div className="tableRow">
          <div className="cell header">Median</div>
          <div className="cell data latencyMedian"><span className="value">{this.getInstanceAverage(this.props.latencyTotal['50'], this.props.reportingHosts, false)}</span>ms</div>
          <div className="cell header">99th</div>
          <div className="cell data latency99"><span className="value">{this.getInstanceAverage(this.props.latencyTotal['99'], this.props.reportingHosts, false)}</span>ms</div>
        </div>
        <div className="tableRow">
          <div className="cell header">Mean</div>
          <div className="cell data latencyMean"><span className="value">{this.props.latencyTotal_mean}</span>ms</div>
          <div className="cell header">99.5th</div>
          <div className="cell data latency995"><span className="value">{this.getInstanceAverage(this.props.latencyTotal['99.5'], this.props.reportingHosts, false)}</span>ms</div>
        </div>
      </div>);
  }
});

module.exports = HystrixCircuit;