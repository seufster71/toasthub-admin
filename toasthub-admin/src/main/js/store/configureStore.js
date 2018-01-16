import {createStore, applyMiddleware} from 'redux';
import { composeWithDevTools } from 'redux-devtools-extension';
import rootReducer from '../reducers/index';
import thunk from 'redux-thunk';
import reduxImmutableStateInvariant from 'redux-immutable-state-invariant';

export default function configureStore() {
  const initialState = {
    appPrefs:{lang: localStorage.getItem('lang'), headerName: 'ToastHub', currentPage: 'home'},
    session:{sessionActive: false}
  }
  return createStore(rootReducer, initialState, composeWithDevTools(applyMiddleware(thunk, reduxImmutableStateInvariant())) );
}
