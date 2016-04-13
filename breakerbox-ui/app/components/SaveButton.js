var React = require('react');

var SaveButton = React.createClass({
  render: function() {
    return (
      <div className="form-group">
        <div className="col-lg-offset-2 col-lg-4">
          <button type="submit" className="btn btn-default">
            {this.props.text}
          </button>
        </div>
      </div>
    );
  }
});

module.exports = SaveButton;