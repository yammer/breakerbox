var React = require('react');
var ReactRouter = require('react-router');

var PropertyKey = React.createClass({
  symbol: function() {
    var style = {float: 'right'};
    switch (this.props.data.syncStatus) {
      case 'SYNCHRONIZED':   return <span className="glyphicon glyphicon-ok-sign" style={style}></span>;
      case 'UNSYNCHRONIZED': return <span className="glyphicon glyphicon-exclamation-sign" style={style}></span>;
      default:               return <span className="glyphicon glyphicon-question-sign" style={style}></span>; 
    }
  },

  render: function() {
    var style = {fontSize: '0.7em'};
    var url = ['/configure', this.props.service, this.props.data.propertyKey].join('/');
    var linkClasses = this.props.active ? 'list-group-item active' : 'list-group-item';
    return (
      <ReactRouter.Link to={url} className={linkClasses} id={this.props.data.propertyKey}>
        <p className="list-group-item-text" style={style}>{this.props.data.propertyKey}
          {this.symbol()}
        </p>
      </ReactRouter.Link>
      );
  }
});

var PropertyKeys = React.createClass({
  render: function() {
    var items = this.props.data.map(function (item) {
      var properties = {
        key: item.propertyKey,
        data: item,
        service: this.props.service,
        active: item.propertyKey === this.props.selected
      };
      return <PropertyKey {...properties}/>;
    }, this);
    return <div>{items}</div>;
  }
});

module.exports = PropertyKeys;