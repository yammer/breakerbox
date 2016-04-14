var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputFormItem = require('./InputFormItem');

var CorePoolSizeFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputThreadPoolCoreSize',
      name: 'threadPoolCoreSize',
      value: this.props.value
    };

    var helpBlock = <HelpBlock text="Default 10." strongText="On-call hint: When experiencing rejections, tuning this higher could help."/>;
    
    return (
      <InputFormItem
        legend='Threadpool'
        label='Core Size'
        input={input}
        helpBlock={helpBlock} />    
    );
  }
});

module.exports = CorePoolSizeFormItem;