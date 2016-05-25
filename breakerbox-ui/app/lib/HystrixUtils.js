var $ = require('jquery');

var preProcessDataRegex = /([ !"#$%&'()*+,.\/:;<=>?@[\]^`{|}~])/g;

var HystrixUtils = {
  preProcessDataRegex: preProcessDataRegex,
  
  preProcessData: function(data) {
    // set defaults for values that may be missing from older streams
    this.setIfMissing(data, "rollingCountBadRequests", 0);
    // assert all the values we need
    this.validateData(data);
    // escape string used in jQuery & d3 selectors
    data.escapedName = data.name.replace(preProcessDataRegex,'\\$1') + '_' + data.index;
    // do math
    this.convertAllAvg(data);
    this.calcRatePerSecond(data);
  },

  setIfMissing: function(data, key, defaultValue) {
    if(data[key] == undefined) {
      data[key] = defaultValue;
    }
  },

  /**
   * Since the stream of data can be aggregated from multiple hosts in a tiered manner
   * the aggregation just sums everything together and provides us the denominator (reportingHosts)
   * so we must divide by it to get an average per instance value. 
   * 
   * We want to do this on any numerical values where we want per instance rather than cluster-wide sum.
   */
  convertAllAvg: function(data) {
    this.convertAvg(data, "errorPercentage", true);
    this.convertAvg(data, "latencyExecute_mean", false);
    this.convertAvg(data, "latencyTotal_mean", false);
    
    // the following will break when it becomes a compound string if the property is dynamically changed
    this.convertAvg(data, "propertyValue_metricsRollingStatisticalWindowInMilliseconds", false);
  },

  convertAvg: function(data, key, decimal) {
    if (decimal) {
      data[key] = this.getInstanceAverage(data[key], data["reportingHosts"], decimal);
    } else {
      data[key] = this.getInstanceAverage(data[key], data["reportingHosts"], decimal);
    }
  },

  calcRatePerSecond: function(data) {
    var numberSeconds = data["propertyValue_metricsRollingStatisticalWindowInMilliseconds"] / 1000;

    var totalRequests = data["requestCount"];
    if (totalRequests < 0) {
      totalRequests = 0;
    }
    data["ratePerSecond"] =  this.roundNumber(totalRequests / numberSeconds);
    data["ratePerSecondPerHost"] =  this.roundNumber(totalRequests / numberSeconds / data["reportingHosts"]) ;
  },

  validateData: function(data) {
    this.assertNotNull(data,"reportingHosts");
    this.assertNotNull(data,"type");
    this.assertNotNull(data,"name");
    this.assertNotNull(data,"group");
    // this.assertNotNull(data,"currentTime");
    this.assertNotNull(data,"isCircuitBreakerOpen");
    this.assertNotNull(data,"errorPercentage");
    this.assertNotNull(data,"errorCount");
    this.assertNotNull(data,"requestCount");
    this.assertNotNull(data,"rollingCountCollapsedRequests");
    this.assertNotNull(data,"rollingCountExceptionsThrown");
    this.assertNotNull(data,"rollingCountFailure");
    this.assertNotNull(data,"rollingCountFallbackFailure");
    this.assertNotNull(data,"rollingCountFallbackRejection");
    this.assertNotNull(data,"rollingCountFallbackSuccess");
    this.assertNotNull(data,"rollingCountResponsesFromCache");
    this.assertNotNull(data,"rollingCountSemaphoreRejected");
    this.assertNotNull(data,"rollingCountShortCircuited");
    this.assertNotNull(data,"rollingCountSuccess");
    this.assertNotNull(data,"rollingCountThreadPoolRejected");
    this.assertNotNull(data,"rollingCountTimeout");
    this.assertNotNull(data,"rollingCountBadRequests");
    this.assertNotNull(data,"currentConcurrentExecutionCount");
    this.assertNotNull(data,"latencyExecute_mean");
    this.assertNotNull(data,"latencyExecute");
    this.assertNotNull(data,"latencyTotal_mean");
    this.assertNotNull(data,"latencyTotal");
    this.assertNotNull(data,"propertyValue_circuitBreakerRequestVolumeThreshold");
    this.assertNotNull(data,"propertyValue_circuitBreakerSleepWindowInMilliseconds");
    this.assertNotNull(data,"propertyValue_circuitBreakerErrorThresholdPercentage");
    this.assertNotNull(data,"propertyValue_circuitBreakerForceOpen");
    this.assertNotNull(data,"propertyValue_executionIsolationStrategy");
    this.assertNotNull(data,"propertyValue_executionIsolationThreadTimeoutInMilliseconds");
    this.assertNotNull(data,"propertyValue_executionIsolationThreadInterruptOnTimeout");
    // this.assertNotNull(data,"propertyValue_executionIsolationThreadPoolKeyOverride");
    this.assertNotNull(data,"propertyValue_executionIsolationSemaphoreMaxConcurrentRequests");
    this.assertNotNull(data,"propertyValue_fallbackIsolationSemaphoreMaxConcurrentRequests");
    this.assertNotNull(data,"propertyValue_requestCacheEnabled");
    this.assertNotNull(data,"propertyValue_requestLogEnabled");
    this.assertNotNull(data,"propertyValue_metricsRollingStatisticalWindowInMilliseconds");
  },
      
  assertNotNull: function(data, key) {
    if(data[key] == undefined) {
      throw new Error("Key Missing: " + key + " for " + data.name);
    }
  },

  /* round a number to X digits: num => the number to round, dec => the number of decimals */
  roundNumber: function(num) {
    var dec=1;
    var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
    var resultAsString = result.toString();
    if(resultAsString.indexOf('.') == -1) {
      resultAsString = resultAsString + '.0';
    }
    return resultAsString;
  },

  getInstanceAverage: function(value, reportingHosts, decimal) {
    if (decimal) {
      return this.roundNumber(value / reportingHosts);
    } else {
      return Math.floor(value / reportingHosts);
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

  deleteCircuit: function(circuitName) {
    $('#CIRCUIT_' + circuitName).remove();
  }
};

module.exports = HystrixUtils;