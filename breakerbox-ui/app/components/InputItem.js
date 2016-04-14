var React = require('react');
var configureFormItemStyles = require('../styles/configureFormItemStyles');

var InputItem = React.createClass({
  getInitialState: function() {
    return {
      value: this.props.value
    };
  },

  handleChange: function(event) {
    this.setState({value: event.target.value});
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({value: nextProps.value});
  },

  render: function() {
    return (
      <div className='input-group col-lg-3' style={configureFormItemStyles}>
        <input type="number" className="form-control" {...this.props} value={this.state.value} onChange={this.handleChange}/>
          {this.props.addon}
      </div>
    );
  }
});

module.exports = InputItem;