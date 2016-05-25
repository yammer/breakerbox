var React = require('react');
var $ = require('jquery');
var d3 = require('d3');
var HystrixUtils = require('../lib/HystrixUtils');

var HystrixThreadPool = React.createClass({
  componentWillMount: function() {
    $('#' + this.props.hystrixThreadPoolMonitor.containerId + ' div.last').removeClass('last');
  },

  componentDidMount: function() {
    var vis = d3.select('#chart_THREAD_POOL_' + this.props.escapedName).append("svg:svg").attr("width", "100%").attr("height", "100%");
    /* add a circle -- we don't use the data point, we set it manually, so just passing in [1] */
    var circle = vis.selectAll("circle").data([1]).enter().append("svg:circle");
    /* setup the initial styling and sizing of the circle */
    circle.style("fill", "green").attr("cx", "30%").attr("cy", "20%").attr("r", 5);

    d3.selectAll('#graph_THREAD_POOL_' + this.props.escapedName + ' svg').append("svg:path");


    this.updateD3();
    this.props.hystrixThreadPoolMonitor.sortSameAsLast();
  },

  componentDidUpdate: function() {
    this.updateD3();
  },

  updateD3: function() {
    $('#THREAD_POOL_' + this.props.escapedName).attr('rate_value', this.props.ratePerSecondPerHost);
    
    // set variables for circle visualization
    var rate = this.props.ratePerSecondPerHost;
    // we will treat each item in queue as 1% of an error visualization
    // ie. 5 threads in queue per instance == 5% error percentage
    var errorPercentage = this.props.currentQueueSize / this.props.reportingHosts; 
    
    this.props.hystrixThreadPoolMonitor.updateCircle('#THREAD_POOL_' + this.props.escapedName + ' circle', this.props.rate, this.props.errorPercentage); 
  },

  render: function() {
    return (    
      <div className="monitor_data">
        <div className="spacer"></div>
        <div className="rate">
          <a href="javascript://" title="Total Execution Rate per Second per Reporting Host" className="rate">
            <span className="smaller">Host: </span>
            <span className="ratePerSecondPerHost">
              {HystrixUtils.addCommas(this.props.ratePerSecondPerHost)}
            </span>/s
          </a>
        </div>
        <div className="rate">  
          <a href="javascript://" title="Total Execution Rate per Second for Cluster" className="rate">
            <span className="smaller">Cluster: </span>
            <span className="ratePerSecond">
              {HystrixUtils.addCommas(this.props.ratePerSecond)}
            </span>/s
          </a>
        </div>
        
        <div className="spacer"></div>     
        <div className="tableRow">
          <div className="cell header left">Active</div>
          <div className="cell data left">{this.props.currentActiveCount}</div>          
          <div className="cell header right">Max Active</div>
          <div className="cell data right">{HystrixUtils.addCommas(this.props.rollingMaxActiveThreads)}</div>
        </div>
        <div className="tableRow">
          <div className="cell header left">Queued</div>
          <div className="cell data left">{this.props.currentQueueSize}</div>
          <div className="cell header right">Executions</div>
          <div className="cell data right">{HystrixUtils.addCommas(this.props.rollingCountThreadsExecuted)}</div>
        </div>
        <div className="tableRow">
          <div className="cell header left">Pool Size</div>
          <div className="cell data left">{this.props.currentPoolSize}</div>
          <div className="cell header right">Queue Size</div>
          <div className="cell data right">{this.props.propertyValue_queueSizeRejectionThreshold}</div>
        </div>
      </div>
      );
  }
});

module.exports = HystrixThreadPool;