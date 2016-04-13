var React = require('react');
var $ = require('jquery');

var Dashboard = React.createClass({
  contextTypes: {
    router: React.PropTypes.object.isRequired
  },

  getInitialState: function() {
    return {
      dashboard: {
        name: 'production',
        turbine: 'localhost:8080'
      }
    };
  },

  componentDidMount: function() {
    var url = '/dashboard';
    $.ajax({
      url: url,
      dataType: 'json',
      cache: false,
      context: this
    })
    .done(function(data) {
      this.setState({dashboard: data});
      this._redirectToDefaultDashboard(this._dashboard());
    })
    .fail(function(xhr, status, err) {
      console.error(url, status, err.toString());
    });
  },

  componentWillReceiveProps: function(nextProps) {
    if (_.isUndefined(nextProps.params.id)) {
      this._redirectToDefaultDashboard(this.state.dashboard.name);
    }
  },

  _redirectToDefaultDashboard: function(name) {
    this.context.router.push('/dashboard/' + name);
  },

  _dashboard: function() {
    return this.props.params.id || this.state.dashboard.name;
  },

  render: function() {
    var style = {
      height: '100%',
      width:  '100%',
      minHeight: '1200px'
    };
    var src = '/assets/monitor/monitor.html?delay=1000&stream=' +
      encodeURIComponent(this.state.dashboard.turbine) +
      '%2Fturbine.stream%3Fcluster%3D' +
      this.props.params.id;
    return (<iframe id="tenacity-dashboard-iframe"
        seamless
        className="col-lg-12"
        style={style}
        src={src}>
        </iframe>);
  } 
});

module.exports = Dashboard;