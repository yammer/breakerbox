var React = require('react');

var InputAddon = React.createClass({
  render: function() {
    return (<span className="input-group-addon">{this.props.text}</span>);
  }
});

module.exports = InputAddon;