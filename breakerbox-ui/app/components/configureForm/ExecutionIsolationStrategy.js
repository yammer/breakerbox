var React = require('react');
var ConfigureFormItem = require('../ConfigureFormItem');
var HelpBlock = require('../HelpBlock');
var RadioItem = require('../RadioItem');

var ExecutionIsolationStrategy = React.createClass({
  getInitialState: function() {
    return {
      strategy: this.props.strategy
    };
  },

  componentWillReceiveProps: function(nextProps) {
    this.setState({strategy: nextProps.strategy});
  },

  _selectStrategy: function(event) {
    this.setState({strategy: event.target.value});
  },

  _isThreadChecked: function() {
    return this.state.strategy === this.props.thread.value;
  },

  _isSemaphoreChecked: function() {
    return this.state.strategy === this.props.semaphore.value;
  },

  render: function() {
    var thread = {
      legend: 'Execution Isolation Strategy',
      label: 'Thread',
      formItem: <RadioItem {...this.props.thread} onChange={this._selectStrategy} checked={this._isThreadChecked()}/>,
      helpBlock: <HelpBlock text="Default when using TenacityCommand." strongText="Interrupts the thread after a timeout"/>
    };
    var semaphore = {
      label: 'Semaphore',
      formItem: <RadioItem {...this.props.semaphore} onChange={this._selectStrategy} checked={this._isSemaphoreChecked()}/>,
      helpBlock: <HelpBlock text="Default when using TenacityObservableCommand." strongText="Unsubscribes after a timeout"/>
    };
    return (
      <div>
        <ConfigureFormItem {...thread}/>
        <ConfigureFormItem {...semaphore}/>
      </div>
    );
  }
});

module.exports = ExecutionIsolationStrategy;