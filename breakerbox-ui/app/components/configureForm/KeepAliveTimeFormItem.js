var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputAddon = require('../InputAddon');
var InputFormItem = require('./InputFormItem');

var KeepAliveTimeFormItem = React.createClass({
  render: function() {
    var input = {
      id: 'inputKeepAliveMinutes',
      name: 'keepAliveMinutes', 
      value: this.props.value,
      addon: <InputAddon text="mins"/>
    };

    var helpBlock = <HelpBlock text="Default 1 minute. Unusued at the moment. Probably will be removed."/>;
    
    return (
      <InputFormItem
        label='Keep Alive Time'
        input={input}
        helpBlock={helpBlock} />    
    );
  }
});

module.exports = KeepAliveTimeFormItem;