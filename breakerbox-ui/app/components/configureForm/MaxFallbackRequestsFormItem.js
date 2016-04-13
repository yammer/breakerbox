var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputFormItem = require('./InputFormItem');

var MaxFallbackRequestsFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputFallbackSemaphoreMaxConcurrentRequests',
      name: 'semaphoreFallbackMaxConcurrentRequests', 
      value: this.props.value
    };

    var helpBlock = <HelpBlock text="Default 10." strongText="On-call hint: When experiencing semaphore fallback rejections, tuning this higher could help."/>;
    
    return (
      <InputFormItem
        label='Fallback Max Requests'
        input={input}
        helpBlock={helpBlock} />    
    );
  }
});

module.exports = MaxFallbackRequestsFormItem;