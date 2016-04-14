var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputFormItem = require('./InputFormItem');

var MaxRequestsFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputSemaphoreMaxConcurrentRequests',
      name: 'semaphoreMaxConcurrentRequests', 
      value: this.props.value
    };

    var helpBlock = <HelpBlock text="Default 10." strongText="On-call hint: When experiencing semaphore rejections, tuning this higher could help."/>;
    
    return (
      <InputFormItem
        legend='Semaphore'
        label='Max Requests'
        input={input}
        helpBlock={helpBlock} />    
    );
  }
});

module.exports = MaxRequestsFormItem;