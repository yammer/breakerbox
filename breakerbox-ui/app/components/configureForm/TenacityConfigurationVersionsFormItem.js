var React = require('react');
var ConfigureFormItem = require('../ConfigureFormItem');
var HelpBlock = require('../HelpBlock');
var TenacityConfigurationVersions = require('../TenacityConfigurationVersions');

var TenacityConfigurationVersionsFormItem = React.createClass({
  render: function() {
    var tenacityConfigurationVersions = {
      id: 'selectConfiguration', 
      configurations: this.props.configurations, 
      onChange: this.props.onChange,
      selectedValue: this.props.selectedValue
    };

    var formItem = <TenacityConfigurationVersions {...tenacityConfigurationVersions}/>;
    var helpBlock = <HelpBlock text="Default means there are no saved configurations yet."/>;
   
    return (
      <ConfigureFormItem
        legend='Configuration Versions'
        label='Configuration Version'
        formItem={formItem}
        helpBlock={helpBlock} />
    );
  }
});

module.exports = TenacityConfigurationVersionsFormItem;