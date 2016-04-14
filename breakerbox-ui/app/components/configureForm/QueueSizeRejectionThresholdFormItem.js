var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputFormItem = require('./InputFormItem');

var QueueSizeRejectionThresholdFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputQueueSizeRejectionThreshold',
      name: 'queueSizeRejectionThreshold', 
      value: this.props.value
    };

    var helpBlock = <HelpBlock text="Default 5. When Max Queue Size is non-zero, this allows for a limit on the unbounded queue (LinkedBlockingQueue)."/>;
    
    return (
      <InputFormItem
        label='Queue Size Rejection Threshold'
        input={input}
        helpBlock={helpBlock} />    
    );
  }
});

module.exports = QueueSizeRejectionThresholdFormItem;