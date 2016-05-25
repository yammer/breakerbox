var $ = require('jquery');
var HystrixUtils = require('./HystrixUtils');

var HystrixThreadPoolUtils = {
  preProcessData: function(data) {
    this.validateData(data);
    // escape string used in jQuery & d3 selectors
    data.escapedName = data.name.replace(HystrixUtils.preProcessDataRegex,'\\$1') + '_' + data.index;
    // do math
    this.convertAllAvg(data);
    this.calcRatePerSecond(data);
  },

  validateData: function(data) {  
    this.assertNotNull(data,"type");
    this.assertNotNull(data,"name");
    // this.assertNotNull(data,"currentTime");
    this.assertNotNull(data,"currentActiveCount");
    this.assertNotNull(data,"currentCompletedTaskCount");
    this.assertNotNull(data,"currentCorePoolSize");
    this.assertNotNull(data,"currentLargestPoolSize");
    this.assertNotNull(data,"currentMaximumPoolSize");
    this.assertNotNull(data,"currentPoolSize");
    this.assertNotNull(data,"currentQueueSize");
    this.assertNotNull(data,"currentTaskCount");
    this.assertNotNull(data,"rollingCountThreadsExecuted");
    this.assertNotNull(data,"rollingMaxActiveThreads");
    this.assertNotNull(data,"reportingHosts");

    this.assertNotNull(data,"propertyValue_queueSizeRejectionThreshold");
    this.assertNotNull(data,"propertyValue_metricsRollingStatisticalWindowInMilliseconds");
  },
  
  assertNotNull: function(data, key) {
    if(data[key] == undefined) {
      if (key == "dependencyOwner") {
        data["dependencyOwner"] = data.name;
      } else {
        throw new Error("Key Missing: " + key + " for " + data.name)
      }
    }
  },

  convertAllAvg: function(data) {
    this.convertAvg(data, "propertyValue_queueSizeRejectionThreshold", false);
    
    // the following will break when it becomes a compound string if the property is dynamically changed
    this.convertAvg(data, "propertyValue_metricsRollingStatisticalWindowInMilliseconds", false);
  },
    
  convertAvg: function(data, key, decimal) {
    if (decimal) {
      data[key] = HystrixUtils.roundNumber(data[key]/data["reportingHosts"]);
    } else {
      data[key] = Math.floor(data[key]/data["reportingHosts"]);
    }
  },

  calcRatePerSecond: function(data) {
    var numberSeconds = data["propertyValue_metricsRollingStatisticalWindowInMilliseconds"] / 1000;

    var totalThreadsExecuted = data["rollingCountThreadsExecuted"];
    if (totalThreadsExecuted < 0) {
      totalThreadsExecuted = 0;
    }
    data["ratePerSecond"] =  HystrixUtils.roundNumber(totalThreadsExecuted / numberSeconds);
    data["ratePerSecondPerHost"] =  HystrixUtils.roundNumber(totalThreadsExecuted / numberSeconds / data["reportingHosts"]);
  },

  deleteThreadPool: function(poolName) {
    $('#THREAD_POOL_' + poolName).remove();
  }
};

module.exports = HystrixThreadPoolUtils;