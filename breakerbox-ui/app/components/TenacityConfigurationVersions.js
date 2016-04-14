var React = require('react');
var configureFormItemStyles = require('../styles/configureFormItemStyles');

var TenacityConfigurationVersions = React.createClass({
  render: function() {
    var items = this.props.configurations.map(function (item) {
      if (item.isDefault()) {
        return <option key="default" value="0">default</option>;
      } else {
        return (<option key={item.dateTime} value={item.dateTime}>{item.toString()}</option>);
      }
    }, this);

    return (
      <div className="input-group col-lg-4" style={configureFormItemStyles}>
        <select id="selectConfiguration" className="form-control" name="configurationVersion"
          onChange={this.props.onChange} value={this.props.selectedValue}>
          {items}
        </select>
      </div>
      );
  }
});

module.exports = TenacityConfigurationVersions;