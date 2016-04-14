var React = require('react');
var ConfigureFormItem = require('../ConfigureFormItem');
var InputItem = require('../InputItem');

var InputFormItem = React.createClass({
  render: function() {
    var formItem = <InputItem {...this.props.input}/>;

    return (
      <ConfigureFormItem 
        legend={this.props.legend}
        label={this.props.label}
        formItem={formItem}
        helpBlock={this.props.helpBlock} />
    );
  }
});

module.exports = InputFormItem;