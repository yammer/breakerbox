var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputAddon = require('../InputAddon');
var InputFormItem = require('./InputFormItem');

var SleepWindowFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputSleepWindow',
      name: 'sleepWindow',
      value: this.props.value,
      addon: <InputAddon text="ms"/>
    };

    var helpBlock = <HelpBlock text="Default 5000ms. How long to wait when a circuit breaker is open before trying again."/>;
    
    return (
      <InputFormItem 
        input={input}
        label='Sleep Window'
        helpBlock={helpBlock} />
    );
  }
});

module.exports = SleepWindowFormItem;