var React = require('react');
var HystrixTheadPool = require('./HystrixThreadPool');

var HystrixThreadPoolContainer = React.createClass({
  divId: function() {
    return this.props.name + '_' + this.props.index;
  },

  displayNameElement: function() {
    var displayName = this.props.name;
    if(displayName.length > 32) {
      displayName = displayName.substring(0,4) + "..." + displayName.substring(displayName.length-20, displayName.length);
    }

    return (<p className="name" title={this.props.name}>{displayName}</p>);
  },

  render: function() {
    return (
      <div className="monitor last" id={'CIRCUIT_' + this.divId()} style={{position:'relative'}}>
        <div id={'chart_CIRCUIT_' + this.divId()} className="chart" style={{position:'absolute', top:'0px', left:0, float:'left', width:'100%', height:'100%'}}></div>
        <div style={{position:'absolute', top:0, width:'100%', height:'15px', opacity:0.8, background:'white'}}>
          {this.displayNameElement()}
        </div>
        <div style={{position:'absolute', top:'15px', opacity:0.8, background:'white', width:'100%', height:'95%'}}>
          <HystrixTheadPool {...this.props} />
        </div>
        <div id={'graph_CIRCUIT_' + this.divId()} className="graph" style={{position:'absolute', top:'25px', left:0, float:'left', width:'140px', height:'62px'}}></div>
      </div>
        );
  }
});

module.exports = HystrixThreadPoolContainer;