var React = require('react');

var RadioItem = React.createClass({
  render: function() {
    return (
      <div className="col-lg-1">
        <div className="col-lg-1 radio">
          <input type="radio" {...this.props}/>
        </div>
      </div>
    );
  }
});

module.exports = RadioItem;