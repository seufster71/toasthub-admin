import React, {Component} from 'react';
import { render } from 'react-dom';
import configureStore from './store/configureStore';
import {Provider} from 'react-redux';
import {initPublic} from './core/common/appPrefActions';
import {sessionCheck} from './member/session/sessionActions';
import PageContainer from './PageContainer.js';
import Bootstrap from 'bootstrap/dist/css/bootstrap.css';
import Theme from './theme.css';

if (process.env.NODE_ENV !== 'production') {
  console.log('Looks like we are in development mode!');
}

const store = configureStore();
store.dispatch(initPublic());
store.dispatch(sessionCheck());

window.onbeforeunload = () => {
  localStorage.setItem('lang',store.getState().appPrefs.lang);
}

class App extends Component {

  constructor() {
    super();

	}

  render() {
    return (
      <PageContainer />
    );
  }
}


render( <Provider store={store}><App/></Provider>, document.getElementById('app') );
