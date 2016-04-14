var React = require('react');
var Navbar = require('../containers/Navbar');

var App = React.createClass({
  render: function() {
    return (
      <div>
        <Navbar clustersUri='/clusters' noMetaClustersUri='/clusters?no-meta=true'/>
        {this.props.children}
      </div>);
  }
});

module.exports = App;