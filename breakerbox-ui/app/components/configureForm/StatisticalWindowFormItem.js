var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputAddon = require('../InputAddon');
var InputFormItem = require('./InputFormItem');

var StatisticalWindowFormItem = React.createClass({
  render: function() {
    var input = {
      id: this.props.id,
      name: this.props.name,
      value: this.props.value,
      addon: <InputAddon text="ms"/>
    };

    var helpBlock = <HelpBlock text="Default 10000ms."/>;
    
    return (
      <InputFormItem
        label='Statistical Window'
        input={input}
        helpBlock={helpBlock} />
    );
  }
});

module.exports = StatisticalWindowFormItem;