var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputFormItem = require('./InputFormItem');

var MaxQueueSizeFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputMaxQueueSize',
      name: 'maxQueueSize', 
      value: this.props.value
    };

    var helpBlock = <HelpBlock text="Default: -1. -1 is for fail-fast (SynchronousQueue). Non-zero isn't recommended (LinkedBlockingQueue)."/>;
    
    return (
      <InputFormItem
        label='Max Queue Size'
        input={input}
        helpBlock={helpBlock} />    
    );
  }
});

module.exports = MaxQueueSizeFormItem;