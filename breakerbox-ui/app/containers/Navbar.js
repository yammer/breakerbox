var React = require('react');
var ReactRouter = require('react-router');
var $ = require('jquery');

global.jQuery = $;
require('bootstrap');

var DropdownItems = require('../components/DropdownItems')

var Navbar = React.createClass({
  getInitialState: function() {
    return {
      clusters: [],
      noMetaClusters: []
    };
  },

  componentDidMount: function() {
    $.ajax({
      url: this.props.clustersUri,
      dataType: 'json',
      cache: false,
      context: this
    })
    .done(function(data) {
      this.setState({clusters: data});
    })
    .fail(function(xhr, status, err) {
      console.error(this.props.clustersUri, status, err.toString());
    });

    $.ajax({
      url: this.props.noMetaClustersUri,
      dataType: 'json',
      cache: false,
      context: this
    })
    .done(function(data) {
      this.setState({noMetaClusters: data});
    })
    .fail(function(xhr, status, err) {
      console.error(this.props.noMetaClustersUri, status, err.toString());
    })
  },

  render: function() {
    return (
      <nav className="commentBox navbar navbar-default">
      <div className="container-fluid">
        <div className="navbar-header">
          <ReactRouter.Link className="navbar-brand" to="/dashboard">breakerbox</ReactRouter.Link>
        </div>

        <div className="collapse navbar-collapse">
          <div className="nav navbar-nav">
            <li className="dropdown">
              <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">dashboards<span className="caret"></span></a>
              <DropdownItems data={this.state.clusters} type="dashboard"/>
            </li>
            <li className="dropdown">
              <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">configure<span className="caret"></span></a>
              <DropdownItems data={this.state.noMetaClusters} type="configure"/>
            </li>
          </div>
        </div>
      </div>
      </nav>);
  }
});

module.exports = Navbar;