import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';
import NavigationContainer from './core/navigation/NavigationContainer.js';
import LoginContainer from './core/usermanagement/LoginContainer.js';
import MemberContainer from './member/MemberContainer';
import {bindActionCreators} from 'redux';
import * as appPrefActions from './core/common/appPrefActions';


class PageContainer extends Component {
	constructor(props) {
		super(props);
    this.navigationChange = this.navigationChange.bind(this);
	}

  navigationChange(event) {
    if (event.target.id == 'LOGIN') {
      this.props.actions.navChange({currentPage:'login'});
    }
  }

  render() {
    if (this.props.appPrefs.currentPage == 'login') {
      return (<div><NavigationContainer navClick={this.navigationChange} menuName="PUBLIC_MENU_RIGHT"/><LoginContainer/></div>);
    } else if (this.props.appPrefs.currentPage == 'member') {
      return (<div><NavigationContainer navClick={this.navigationChange} menuName="MEMBER_MENU_RIGHT"/><MemberContainer/></div>);
    } else {
      return (<div><NavigationContainer navClick={this.navigationChange} menuName="PUBLIC_MENU_RIGHT"/>Main Page</div>);
    }

  }
}

PageContainer.propTypes = {
	appPrefs: PropTypes.object.isRequired,
	menus: PropTypes.object,
	lang: PropTypes.string,
	actions: PropTypes.object
};

function mapStateToProps(state, ownProps) {
  return {menus:state.appMenus.menus, lang:state.lang, appPrefs:state.appPrefs};
}

function mapDispatchToProps(dispatch) {
  return { actions:bindActionCreators(appPrefActions,dispatch) };
}

export default connect(mapStateToProps,mapDispatchToProps)(PageContainer);
