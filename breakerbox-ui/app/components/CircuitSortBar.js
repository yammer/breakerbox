var React = require('react');
var $ = require("jquery");
var HystrixCommandMonitor = require('../lib/HystrixCommandMonitor');
var HystrixThreadPoolMonitor = require('../lib/HystrixThreadPoolMonitor');
var HystrixCircuits = require('./HystrixCircuits');
var HystrixThreadPools = require('./HystrixThreadPools');

var CircuitSortBar = React.createClass({
  getInitialState: function() {
    var proxyStream = '/tenacity/proxy.stream?origin=localhost:8080/turbine.stream?cluster=production&delay=1000'
    var dependenciesId = 'dependencies_0';
    var dependencyThreadPoolsId = 'dependencyThreadPools_0';
    var state = {
      source: new EventSource(proxyStream),
      hystrixMonitor: new HystrixCommandMonitor(0, dependenciesId, {includeDetailIcon:false}),
      hystrixThreadPoolMonitor: new HystrixThreadPoolMonitor(0, dependencyThreadPoolsId)
    };

    return state;
  },

  render: function() {
    return (
      <div id="monitor">
        <div className="container col-lg-12">
          <div className="row"> 
            <div className="menubar"> 
              <div className="title">Circuit</div> 
              <div className="menu_actions"> 
                Sort:  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByErrorThenVolume}>Error then Volume</a> | 
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortAlphabetically}>Alphabetical</a> |  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByVolume}>Volume</a> |  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByError}>Error</a> |  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByLatencyMean}>Mean</a> |  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByLatencyMedian}>Median</a> |  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByLatency90}>90</a> |  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByLatency99}>99</a> |  
                <a href="javascript://" onclick={this.state.hystrixMonitor.sortByLatency995}>99.5</a>  
              </div> 
              <div className="menu_legend"> 
                <span className="success">Success</span> | <span className="shortCircuited">Short-Circuited</span> | <span className="badRequest"> Bad Request</span> | <span className="timeout">Timeout</span> | <span className="rejected">Rejected</span> | <span className="failure">Failure</span> | <span className="errorPercentage">Error %</span> 
              </div> 
            </div> 
          </div> 
          <HystrixCircuits id={this.state.hystrixMonitor.containerId} hystrixMonitor={this.state.hystrixMonitor} source={this.state.source} />
          <div className="spacer"></div> 

          <div className="row"> 
            <div className="menubar"> 
              <div className="title">Thread Pools</div> 
              <div className="menu_actions"> 
                Sort: <a href="javascript://" onclick={this.state.hystrixThreadPoolMonitor.sortAlphabetically}>Alphabetical</a> |  
                <a href="javascript://" onclick={this.state.hystrixThreadPoolMonitor.sortByVolume}>Volume</a> |  
              </div> 
            </div> 
          </div> 
          <HystrixThreadPools id={this.state.hystrixThreadPoolMonitor.containerId} hystrixThreadPoolMonitor={this.state.hystrixThreadPoolMonitor} source={this.state.source} />
          <div className="spacer"></div> 
          <div className="spacer"></div> 
          </div> 
        </div>
    ); 
  }
});

module.exports = CircuitSortBar;