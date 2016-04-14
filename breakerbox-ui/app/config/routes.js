var React = require('react');
var ReactRouter = require('react-router');
var Router = ReactRouter.Router;
var Route = ReactRouter.Route;
var IndexRoute = ReactRouter.IndexRoute;

var App = require('../components/App');
var Dashboard = require('../containers/Dashboard');
var Configure = require('../containers/Configure');

var routes = (
  <Router history={ReactRouter.hashHistory}>
    <Route path="/" component={App}>
      <IndexRoute component={Dashboard} />
      <Route path="dashboard(/:id)" component={Dashboard} />
      <Route path="configure/:id(/:propertyKey)" component={Configure} />
    </Route>
  </Router>
);

module.exports = routes;