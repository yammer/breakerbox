var $ = require('jquery');
require('./tinysort');

var HystrixCommandMonitor = function(index, containerId, args) {
    this.args = args || {};
    this.index = index;
    this.containerId = containerId;
    
    // intialize various variables we use for visualization
    this.maxXaxisForCircle="40%";
    this.maxYaxisForCircle="40%";
    this.maxRadiusForCircle="125";
    
    // CIRCUIT_BREAKER circle visualization settings
    this.circuitCircleRadius = d3.scale.pow().exponent(0.5).domain([0, 400]).range(["5", this.maxRadiusForCircle]); // requests per second per host
    this.circuitCircleYaxis = d3.scale.linear().domain([0, 400]).range(["30%", this.maxXaxisForCircle]);
    this.circuitCircleXaxis = d3.scale.linear().domain([0, 400]).range(["30%", this.maxYaxisForCircle]);
    this.circuitColorRange = d3.scale.linear().domain([10, 25, 40, 50]).range(["green", "#FFCC00", "#FF9900", "red"]);
    this.circuitErrorPercentageColorRange = d3.scale.linear().domain([0, 10, 35, 50]).range(["grey", "black", "#FF9900", "red"]);

    this.sortedBy = 'alph_asc';

    /**
     * We want to keep sorting in the background since data values are always changing, so this will re-sort every X milliseconds
     * to maintain whatever sort the user (or default) has chosen.
     * 
     * In other words, sorting only for adds/deletes is not sufficient as all but alphabetical sort are dynamically changing.
     */
    setInterval(function() {
      // sort since we have added a new one
      this.sortSameAsLast();
    }.bind(this), 10000);
};
   
HystrixCommandMonitor.prototype.updateCircle = function(variablePrefix, cssTarget, rate, errorPercentage) {
  var newXaxisForCircle = this[variablePrefix + 'CircleXaxis'](rate);
  if(parseInt(newXaxisForCircle) > parseInt(this.maxXaxisForCircle)) {
    newXaxisForCircle = this.maxXaxisForCircle;
  }
  var newYaxisForCircle = this[variablePrefix + 'CircleYaxis'](rate);
  if(parseInt(newYaxisForCircle) > parseInt(this.maxYaxisForCircle)) {
    newYaxisForCircle = this.maxYaxisForCircle;
  }
  var newRadiusForCircle = this[variablePrefix + 'CircleRadius'](rate);
  if(parseInt(newRadiusForCircle) > parseInt(this.maxRadiusForCircle)) {
    newRadiusForCircle = this.maxRadiusForCircle;
  }
  
  d3.selectAll(cssTarget)
    .transition()
    .duration(400)
    .attr("cy", newYaxisForCircle)
    .attr("cx", newXaxisForCircle)
    .attr("r", newRadiusForCircle)
    .style("fill", this[variablePrefix + 'ColorRange'](errorPercentage));
};
    
HystrixCommandMonitor.prototype.updateSparkline = function(variablePrefix, cssTarget, newDataPoint) {
  var currentTimeMilliseconds = new Date().getTime();
  var data = this[variablePrefix + cssTarget + '_data'];
  if(typeof data == 'undefined') {
    // else it's new
    if(typeof newDataPoint == 'object') {
      // we received an array of values, so initialize with it
      data = newDataPoint;
    } else {
      // v: VALUE, t: TIME_IN_MILLISECONDS
      data = [{"v":parseFloat(newDataPoint),"t":currentTimeMilliseconds}];
    }
    this[variablePrefix + cssTarget + '_data'] = data;
  } else {
    if(typeof newDataPoint == 'object') {
      /* if an array is passed in we'll replace the cached one */         
      data = newDataPoint;
    } else {
      // else we just add to the existing one
      data.push({"v":parseFloat(newDataPoint),"t":currentTimeMilliseconds});
    }
  }
  
  while(data.length > 200) { // 400 should be plenty for the 2 minutes we have the scale set to below even with a very low update latency
    // remove data so we don't keep increasing forever 
    data.shift();
  } 
  
  if(data.length == 1 && data[0].v == 0) {
    //console.log("we have a single 0 so skipping");
    // don't show if we have a single 0
    return;
  }
  
  if(data.length > 1 && data[0].v == 0 && data[1].v != 0) {
    //console.log("we have a leading 0 so removing it");
    // get rid of a leading 0 if the following number is not a 0
    data.shift();
  } 
  
  var xScale = d3.time.scale().domain([new Date(currentTimeMilliseconds-(60*1000*2)), new Date(currentTimeMilliseconds)]).range([0, 140]);
  
  var yMin = d3.min(data, function(d) { return d.v; });
  var yMax = d3.max(data, function(d) { return d.v; });
  var yScale = d3.scale.linear().domain([yMin, yMax]).nice().range([60, 0]); // y goes DOWN, so 60 is the "lowest"
  
  sparkline = d3.svg.line()
  // assign the X function to plot our line as we wish
  .x(function(d,i) { 
    // return the X coordinate where we want to plot this datapoint based on the time
    return xScale(new Date(d.t));
  })
  .y(function(d) {
    return yScale(d.v);
  })
  .interpolate("basis");
  
  d3.selectAll(cssTarget).attr("d", sparkline(data));
};

// public methods for sorting
HystrixCommandMonitor.prototype.sortByVolume = function() {
  var direction = "desc";
  if(this.sortedBy == 'rate_desc') {
    direction = 'asc';
  }
  this.sortByVolumeInDirection(direction);
};

HystrixCommandMonitor.prototype.sortByVolumeInDirection = function(direction) {
  this.sortedBy = 'rate_' + direction;
  $('#' + this.containerId + ' div.monitor').tsort({order: direction, attr: 'rate_value'});
};

HystrixCommandMonitor.prototype.sortAlphabetically = function() {
  var direction = "asc";
  if(this.sortedBy == 'alph_asc') {
    direction = 'desc';
  }
  this.sortAlphabeticalInDirection(direction);
};

HystrixCommandMonitor.prototype.sortAlphabeticalInDirection = function(direction) {
  this.sortedBy = 'alph_' + direction;
  $('#' + this.containerId + ' div.monitor').tsort("p.name", {order: direction});
};


HystrixCommandMonitor.prototype.sortByError = function() {
  var direction = "desc";
  if(this.sortedBy == 'error_desc') {
    direction = 'asc';
  }
  this.sortByErrorInDirection(direction);
};

HystrixCommandMonitor.prototype.sortByErrorInDirection = function(direction) {
  this.sortedBy = 'error_' + direction;
  $('#' + this.containerId + ' div.monitor').tsort(".errorPercentage .value", {order: direction});
};

HystrixCommandMonitor.prototype.sortByErrorThenVolume = function() {
  var direction = "desc";
  if(this.sortedBy == 'error_then_volume_desc') {
    direction = 'asc';
  }
  this.sortByErrorThenVolumeInDirection(direction);
};

HystrixCommandMonitor.prototype.sortByErrorThenVolumeInDirection = function(direction) {
  this.sortedBy = 'error_then_volume_' + direction;
  $('#' + this.containerId + ' div.monitor').tsort({order: direction, attr: 'error_then_volume'});
};

HystrixCommandMonitor.prototype.sortByLatency90 = function() {
  var direction = "desc";
  if(this.sortedBy == 'lat90_desc') {
    direction = 'asc';
  }
  this.sortedBy = 'lat90_' + direction;
  this.sortByMetricInDirection(direction, ".latency90 .value");
};

HystrixCommandMonitor.prototype.sortByLatency99 = function() {
  var direction = "desc";
  if(this.sortedBy == 'lat99_desc') {
    direction = 'asc';
  }
  this.sortedBy = 'lat99_' + direction;
  this.sortByMetricInDirection(direction, ".latency99 .value");
};

HystrixCommandMonitor.prototype.sortByLatency995 = function() {
  var direction = "desc";
  if(this.sortedBy == 'lat995_desc') {
    direction = 'asc';
  }
  this.sortedBy = 'lat995_' + direction;
  this.sortByMetricInDirection(direction, ".latency995 .value");
};

HystrixCommandMonitor.prototype.sortByLatencyMean = function() {
  var direction = "desc";
  if(this.sortedBy == 'latMean_desc') {
    direction = 'asc';
  }
  this.sortedBy = 'latMean_' + direction;
  this.sortByMetricInDirection(direction, ".latencyMean .value");
};

HystrixCommandMonitor.prototype.sortByLatencyMedian = function() {
  var direction = "desc";
  if(this.sortedBy == 'latMedian_desc') {
    direction = 'asc';
  }
  this.sortedBy = 'latMedian_' + direction;
  this.sortByMetricInDirection(direction, ".latencyMedian .value");
};

HystrixCommandMonitor.prototype.sortByMetricInDirection = function(direction, metric) {
  $('#' + this.containerId + ' div.monitor').tsort(metric, {order: direction});
};

// this method is for when new divs are added to cause the elements to be sorted to whatever the user last chose
HystrixCommandMonitor.prototype.sortSameAsLast = function() {
  if(this.sortedBy == 'alph_asc') {
    this.sortAlphabeticalInDirection('asc');
  } else if(this.sortedBy == 'alph_desc') {
    this.sortAlphabeticalInDirection('desc');
  } else if(this.sortedBy == 'rate_asc') {
    this.sortByVolumeInDirection('asc');
  } else if(this.sortedBy == 'rate_desc') {
    this.sortByVolumeInDirection('desc');
  } else if(this.sortedBy == 'error_asc') {
    this.sortByErrorInDirection('asc');
  } else if(this.sortedBy == 'error_desc') {
    this.sortByErrorInDirection('desc');
  } else if(this.sortedBy == 'error_then_volume_asc') {
    this.sortByErrorThenVolumeInDirection('asc');
  } else if(this.sortedBy == 'error_then_volume_desc') {
    this.sortByErrorThenVolumeInDirection('desc');  
  } else if(this.sortedBy == 'lat90_asc') {
    this.sortByMetricInDirection('asc', '.latency90 .value');
  } else if(this.sortedBy == 'lat90_desc') {
    this.sortByMetricInDirection('desc', '.latency90 .value');
  } else if(this.sortedBy == 'lat99_asc') {
    this.sortByMetricInDirection('asc', '.latency99 .value');
  } else if(this.sortedBy == 'lat99_desc') {
    this.sortByMetricInDirection('desc', '.latency99 .value');
  } else if(this.sortedBy == 'lat995_asc') {
    this.sortByMetricInDirection('asc', '.latency995 .value');
  } else if(this.sortedBy == 'lat995_desc') {
    this.sortByMetricInDirection('desc', '.latency995 .value');
  } else if(this.sortedBy == 'latMean_asc') {
    this.sortByMetricInDirection('asc', '.latencyMean .value');
  } else if(this.sortedBy == 'latMean_desc') {
    this.sortByMetricInDirection('desc', '.latencyMean .value');
  } else if(this.sortedBy == 'latMedian_asc') {
    this.sortByMetricInDirection('asc', '.latencyMedian .value');
  } else if(this.sortedBy == 'latMedian_desc') {
    this.sortByMetricInDirection('desc', '.latencyMedian .value');
  }  
};

// a temporary home for the logger until we become more sophisticated
function log(message) {
  console.log(message);
};

module.exports = HystrixCommandMonitor;