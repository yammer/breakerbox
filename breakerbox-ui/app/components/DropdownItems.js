var React = require('react');
var ReactRouter = require('react-router');

var DropdownItem = React.createClass({
  render: function() {
    var target = '/' + [this.props.type, this.props.text].join('/');
    return (<li><ReactRouter.Link to={target}>{this.props.text}</ReactRouter.Link></li>);
  } 
});

var DropdownItems = React.createClass({
  render: function() {
    var items = this.props.data.map(function (item) {
      return (<DropdownItem key={item} type={this.props.type} text={item}/>);
    }, this);
    return (
      <ul className="dropdown-menu">
      {items}
      </ul>); 
  }
});

module.exports = DropdownItems;