var React = require('react');
var HelpBlock = require('../HelpBlock');
var InputFormItem = require('./InputFormItem');

var StatisticalWindowBucketsFormItem = React.createClass({
  render: function() {
    var input = {
      id: this.props.id,
      name: this.props.name,
      value: this.props.value
    };

    var helpBlock = <HelpBlock text="Default 10."/>;
    
    return (
      <InputFormItem
        label='Statistical Window Buckets'
        input={input}
        helpBlock={helpBlock} />
    );
  }
});

module.exports = StatisticalWindowBucketsFormItem;