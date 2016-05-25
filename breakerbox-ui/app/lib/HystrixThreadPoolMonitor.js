var $ = require('jquery');
var d3 = require('d3');
require('./tinysort');

var HystrixThreadPoolMonitor = function(index, containerId) {
    this.containerId = containerId;
    
    var maxXaxisForCircle="40%";
    var maxYaxisForCircle="40%";
    var maxRadiusForCircle="125";
    var maxDomain = 2000;
    
    this.circleRadius = d3.scale.pow().exponent(0.5).domain([0, maxDomain]).range(["5", maxRadiusForCircle]); // requests per second per host
    this.circleYaxis = d3.scale.linear().domain([0, maxDomain]).range(["30%", maxXaxisForCircle]);
    this.circleXaxis = d3.scale.linear().domain([0, maxDomain]).range(["30%", maxYaxisForCircle]);
    this.colorRange = d3.scale.linear().domain([10, 25, 40, 50]).range(["green", "#FFCC00", "#FF9900", "red"]);
    this.errorPercentageColorRange = d3.scale.linear().domain([0, 10, 35, 50]).range(["grey", "black", "#FF9900", "red"]);

    this.sortedBy = 'alph_asc';

    /**
     * We want to keep sorting in the background since data values are always changing, so this will re-sort every X milliseconds
     * to maintain whatever sort the user (or default) has chosen.
     * 
     * In other words, sorting only for adds/deletes is not sufficient as all but alphabetical sort are dynamically changing.
     */
    this.intervalId = setInterval(function() {
      // sort since we have added a new one
      this.sortSameAsLast();
    }.bind(this), 1000)
};

HystrixThreadPoolMonitor.prototype.updateCircle = function(cssTarget, rate, errorPercentage) {
  var newXaxisForCircle = this.circleXaxis(rate);
  if(parseInt(newXaxisForCircle) > parseInt(this.maxXaxisForCircle)) {
    newXaxisForCircle = this.maxXaxisForCircle;
  }
  var newYaxisForCircle = this.circleYaxis(rate);
  if(parseInt(newYaxisForCircle) > parseInt(this.maxYaxisForCircle)) {
    newYaxisForCircle = this.maxYaxisForCircle;
  }
  var newRadiusForCircle = this.circleRadius(rate);
  if(parseInt(newRadiusForCircle) > parseInt(this.maxRadiusForCircle)) {
    newRadiusForCircle = this.maxRadiusForCircle;
  }
  
  d3.selectAll(cssTarget)
    .transition()
    .duration(400)
    .attr("cy", newYaxisForCircle)
    .attr("cx", newXaxisForCircle)
    .attr("r", newRadiusForCircle)
    .style("fill", this.colorRange(errorPercentage));
}

HystrixThreadPoolMonitor.prototype.sortSameAsLast = function() {
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
  } else if(this.sortedBy == 'lat90_asc') {
    this.sortByMetricInDirection('asc', 'p90');
  } else if(this.sortedBy == 'lat90_desc') {
    this.sortByMetricInDirection('desc', 'p90');
  } else if(this.sortedBy == 'lat99_asc') {
    this.sortByMetricInDirection('asc', 'p99');
  } else if(this.sortedBy == 'lat99_desc') {
    this.sortByMetricInDirection('desc', 'p99');
  } else if(this.sortedBy == 'lat995_asc') {
    this.sortByMetricInDirection('asc', 'p995');
  } else if(this.sortedBy == 'lat995_desc') {
    this.sortByMetricInDirection('desc', 'p995');
  } else if(this.sortedBy == 'latMean_asc') {
    this.sortByMetricInDirection('asc', 'pMean');
  } else if(this.sortedBy == 'latMean_desc') {
    this.sortByMetricInDirection('desc', 'pMean');
  } else if(this.sortedBy == 'latMedian_asc') {
    this.sortByMetricInDirection('asc', 'pMedian');
  } else if(this.sortedBy == 'latMedian_desc') {
    this.sortByMetricInDirection('desc', 'pMedian');
  }  
};

HystrixThreadPoolMonitor.prototype.sortByVolume = function() {
  var direction = "desc";
  if(this.sortedBy == 'rate_desc') {
    direction = 'asc';
  }
  this.sortByVolumeInDirection(direction);
};

HystrixThreadPoolMonitor.prototype.sortByVolumeInDirection = function(direction) {
  this.sortedBy = 'rate_' + direction;
  $('#' + this.containerId + ' div.monitor').tsort({order: direction, attr: 'rate_value'});
};

HystrixThreadPoolMonitor.prototype.sortAlphabetically = function() {
  var direction = "asc";
  if(this.sortedBy == 'alph_asc') {
    direction = 'desc';
  }
  this.sortAlphabeticalInDirection(direction);
};

HystrixThreadPoolMonitor.prototype.sortAlphabeticalInDirection = function(direction) {
  this.sortedBy = 'alph_' + direction;
  $('#' + this.containerId + ' div.monitor').tsort("p.name", {order: direction});
};

HystrixThreadPoolMonitor.prototype.sortByMetricInDirection = function(direction, metric) {
  $('#' + this.containerId + ' div.monitor').tsort(metric, {order: direction});
};

module.exports = HystrixThreadPoolMonitor;