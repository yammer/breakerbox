var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputAddon = require('../InputAddon');
var InputFormItem = require('./InputFormItem');

var ExecutionTimeFormItem = React.createClass({
  render: function() {
    var helpBlock = <HelpBlock text="Default: 1000ms. Absolute timeout for the entire operation."
      strongText="On-call hint: When experiencing timeouts, tuning this higher can help."/>;

    var input = {
      id: 'inputExecutionTimeout',
      name: 'executionTimeout',
      value: this.props.value,
      addon: <InputAddon text="ms"/>
    };

    return (
      <InputFormItem
        legend='Absolute Timeout'
        label='Execution Timeout'
        input={input}
        helpBlock={helpBlock} />
    );
  }
});

module.exports = ExecutionTimeFormItem;