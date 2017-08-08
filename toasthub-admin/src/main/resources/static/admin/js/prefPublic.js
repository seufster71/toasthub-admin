/**
 * @author Edward H. Seufert
 * Copyright (C) 2016 The ToastHub Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

PrefPublic.prototype = Object.create(toastHubPreference.prototype);
PrefPublic.prototype.constructor = PrefPublic;

toastHub.registerController("prefPublic",new PrefPublic("perfPublic"));

function PrefPublic(instanceName){
	toastHubPreference.call(this,instanceName,this);
	this.category = "PUBLIC";
	var self = this;
	
	
	this.initCustom = function(params){
		toastHub.logSystem.log("DEBUG","prefPublic::PrefPublic::initCustom");
		params.category = this.category;
		params.appForms = ["APP_FORMFIELD_FORM","APP_LABEL_FORM","APP_TEXT_FORM","APP_OPTION_FORM","APP_PAGE_FORM"];
		params.appTexts = ["GLOBAL_PAGE","ADMIN_PREFERENCE_PAGE"];
		params.appLabels = ["ADMIN_PREFERENCE_PAGE"];
	}; // initCustom
	

};