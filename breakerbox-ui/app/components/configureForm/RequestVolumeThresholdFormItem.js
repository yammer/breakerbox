var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputFormItem = require('./InputFormItem');

var RequestVolumeThresholdFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputRequestVolumeThreshold', 
      name: 'requestVolumeThreshold', 
      value: this.props.value
    };

    var helpBlock = <HelpBlock text="Default: 20. No. of failures within a stat window in order to open a circuit."
      strongText="Tuning this higher will help short-circuits, but tuning exec-timeout/core-size higher is often a better solution."/>;

    return (
      <InputFormItem
        legend='Circuit Breaker'
        label='Request Volume Threshold'
        input={input}
        helpBlock={helpBlock} />
    );
  }
});

module.exports = RequestVolumeThresholdFormItem;