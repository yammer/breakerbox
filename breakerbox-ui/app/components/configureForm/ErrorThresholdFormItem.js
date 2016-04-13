var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputAddon = require('../InputAddon');
var InputFormItem = require('./InputFormItem');

var ErrorThresholdFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputErrorThresholdPercentage', 
      name: 'errorThresholdPercentage', 
      value: this.props.value,
      addon: <InputAddon text="%"/>
    };
    
    var helpBlock = <HelpBlock text="Default: 50%. The percentage of requests needed to fail within the statistical window to open a circuit. This also requires that the request volume threshold is exceeded."/>;
    
    return (
      <InputFormItem
        input={input} 
        label='Error Threshold'
        helpBlock={helpBlock} />
    );
  }
});

module.exports = ErrorThresholdFormItem;