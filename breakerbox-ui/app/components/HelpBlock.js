var React = require('react');
var _ = require('lodash');

var HelpBlock = React.createClass({
  render: function() {
    if (_.isUndefined(this.props.strongText)) {
      return <span className="help-block">{this.props.text}</span>;
    } else {
      return <span className="help-block">{this.props.text} <strong>{this.props.strongText}</strong></span>
    }
  }
});

module.exports = HelpBlock;