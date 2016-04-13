var React = require('react');
var _ = require('lodash');

var ConfigureFormItem = React.createClass({
  render: function() {
    var legendElement = _.isUndefined(this.props.legend) ? undefined : <legend>{this.props.legend}</legend>;
    return (
      <div>
        {legendElement}
        <div className="form-group">
          <label htmlFor={this.props.formItem.props.id} className="col-lg-3 control-label">{this.props.label}</label>
          {this.props.formItem}
          {this.props.helpBlock}
        </div>
      </div>
      );
  }
});

module.exports = ConfigureFormItem;