/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
Ext.define('Ssp.util.ColumnRendererUtils',{
	extend: 'Ext.Component',
	mixins: ['Deft.mixin.Injectable'],
    inject: {
    	colorsAllUnpagedStore: 'colorsAllUnpagedStore',
		confidentialityLevelsAllUnpagedStore: 'confidentialityLevelsAllUnpagedStore',
		formUtils: 'formRendererUtils'
    },
    
	initComponent: function() {
		return this.callParent( arguments );
    },

	renderFriendlyBoolean: function(val, metaData, record) {
		var result = "";
		if (val !== null )
        {
           if (val !== "")
           {
        	   result = ((val===true || val === 'true')?'Yes':'No');
           }
        }
        
        return result;
	},    
    
	renderTaskName: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.get('name').toUpperCase() + ': ';
		strHtml += record.get('description') + '</p>';
		strHtml += '</div>';
	    return strHtml;
	},

	renderTaskDueDate: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + Ext.util.Format.date( record.get('dueDate') ,'m/d/Y') + '</p>';
		strHtml += '</div>';
	    return strHtml;
	},	
	
	renderTaskStatus: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + ((record.get('completedDate') != null) ? 'Complete' : 'Incomplete' ) + '</p>';
		strHtml += '</div>';
	    return strHtml;
	},
	
	renderTaskAuthor: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.getCreatedByPersonName() + '</p>';
		strHtml += '</div>';
	    return strHtml;
	},
	
	renderTaskCFLevel: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.get('confidentialityLevel').name + '</p>';
		strHtml += '</div>';
	    return strHtml;
	},
	
	renderGoalName: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.get('name').toUpperCase() + '</p>';
		strHtml += '<p>' + record.get('description') + '</p>';
		strHtml += '</div>';
	    return strHtml;	
	},	
	renderTags: function(val, metaData, record) {
		var tags = record.get("tags");
		if(tags && tags.indexOf(",") > 0)
		{
			var numTags = tags.split(",").length;
			tags = numTags+' tags';
		};
	    return tags;	
	},	
	renderConfidentialityLevelName: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.get('confidentialityLevel').name.toUpperCase() + '</p>';
		strHtml += '</div>';
	    return strHtml;		
	},
	
	renderCompletedDate: function(val, metaData, record) {
	    return Ext.util.Format.date( record.get('completedDate'),'m/d/Y');		
	},

	renderCreatedByDate: function(val, metaData, record) {
	    return Ext.util.Format.date( record.get('createdDate'),'m/d/Y');		
	},

	renderCreatedByDateWithTime: function(val, metaData, record) {
	    return Ext.util.Format.date( record.get('createdDate'),'m/d/Y g:i A');		
	},	

	renderCreatedBy: function(val, metaData, record) {
	    return record.get('createdBy').firstName.toUpperCase() + ' ' + record.get('createdBy').lastName.toUpperCase();		
	},	
	
	renderJournalCreatedBy:function(val, metaData, record) {
		return val.toUpperCase();
	    
	},	
	
	renderCreatedByDateAndName: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.get('createdBy').firstName.toUpperCase() + ' ' + record.get('createdBy').lastName.toUpperCase() + '</p>';
        strHtml += '<p>' + Ext.util.Format.date( record.get('createdDate'),'m/d/Y') + '</p>';
        strHtml += '</div>';
	    return strHtml;		
	},
	
	renderModifiedByDate: function(val, metaData, record) {
	    return Ext.util.Format.date( record.get('modifiedDate'),'m/d/Y');		
	},
	
	renderModifiedBy: function(val, metaData, record) {
	    return record.get('modifiedBy').firstName.toUpperCase() + ' ' + record.get('modifiedBy').lastName.toUpperCase();		
	},	
	
	renderJournalModifiedBy: function(val, metaData, record) {
		return val.toUpperCase();
	   
	},
 
	renderJournalSourceName: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + val + '</p>';
         strHtml += '</div>';
	    return strHtml;		
	},	
	
	renderPhotoIcon: function(val) {
	    return '<img src="' + val + '">';
	},

	renderCoachName: function(val, metaData, record) {
		var strHtml = '<div>';
		strHtml += '<p>' + record.getCoachFullName() + '</p>';
        strHtml += '</div>';
	    return strHtml;
	},
	
	renderSearchStudentName: function(val, metaData, record) {
		var strHtml = '<div>';
		strHtml += '<p>' + record.getFullName() + '</p>';
        strHtml += '</div>';
	    return strHtml;
	},

	renderPersonFullName: function(val, metaData, record) {
		var strHtml = '<div>';
		strHtml += '<p>' + record.getPersonFullName() + '</p>';
        strHtml += '</div>';
	    return strHtml;
	},	

	renderStudentDetails: function(val, metaData, record) {
		var strHtml = '<div>';
		strHtml += '<p>' + record.getFullName() + '</p>';
		strHtml += '<p>COACH: ' + record.getCoachFullName() + '</p>';
        strHtml += '<p>ID: ' + record.get('schoolId') + '</p>';
        strHtml += '<p>STATUS: ' + record.get('currentProgramStatusName') + '</p>';
        strHtml += '</div>';
	    return strHtml;
	},
	
	renderStudentType: function(val, metaData, record) {
		var strHtml = '<div>';
        strHtml += '<p>' + record.getStudentTypeName() + '</p>';
        strHtml += '</div>';
	    return strHtml;
	},
	
	renderAddToolIcon: function(value,meta,record,rowIx,ColIx, store) {
	    return (record.get("active")==false)?
	                'addToolIcon':
	                'hideAddToolIcon';
	},

	renderErrorMessage: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.get('errorMessage') + '</p>';
		strHtml += '</div>';
	    return strHtml;
	},
	
	renderEarlyAlertStatus: function(val, metaData, record) {
		var status = ((record.get('closedDate') != null)? 'Closed' : 'Open');
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + ((record.get('nodeType').toLowerCase() == 'early alert')? status : "N/A") + '</p>';
		strHtml += '</div>';
	    return strHtml;
	},
	
	renderEarlyAlertReason: function(val, metaData, record) {
		var strHtml = '<div style="white-space:normal !important;">';
        strHtml += '<p>' + record.get('earlyAlertReason').name.toLowerCase() + '</p>';
		strHtml += '</div>';
	    return strHtml;		
	},
	
	
	renderObjectStatus: function(val, metadata, record) {
		var active;
		if(record === null)
		{
			active = "Yes"
		}
		else
		{
			active = record.get('objectStatus') == 'ACTIVE' ? "Yes" : "No";
		}
		var strHtml = '<div style="white-space:normal !important;">';
		strHtml += '<p>' + active + '</p>'
		strHtml += '</div>';
		return strHtml;
	},
	
	renderHex: function(val, metadata, record) {
		var hexCode = record.get('hexCode');
		var strHtml = '<div>';
		strHtml += '<div style="float:left;width:49%">';
		strHtml += hexCode;
		strHtml += '</div>'
		strHtml += '<div style="background-color:#' + hexCode + ';width:49%;float:right;">';
		strHtml += '<p>&nbsp;</p>'
		strHtml += '</div>'
		strHtml += '</div>';
		return strHtml;
	},
	
	renderElectiveColor: function(val, metadata, record) {
		var me = this; 
		var colorsStore = me.colorsAllUnpagedStore;
		colorsStore.clearFilter(true);
		
		var color = colorsStore.findRecord('id', val, 0, false, false, true);
		
		if(color == null || color.data == null) {
			return '';
		}
		
		var hexCode = color.data.hexCode;
		var colorName = color.data.name;
		
		var strHtml = '<div>';
		strHtml += '<div style="float:left;width:49%">';
		strHtml += colorName;
		strHtml += '</div>';
		strHtml += '<div style="background-color:#' + hexCode + ';width:49%;float:right;">';
		strHtml += '<p>&nbsp;</p>';
		strHtml += '</div>';
		strHtml += '</div>';
		return strHtml;		
	},
	
	renderConfidentialityLevel: function(val, metadata, record) {
		var me = this; 
		var confidentialityLevelsStore = me.confidentialityLevelsAllUnpagedStore;
		confidentialityLevelsStore.clearFilter(true);
		
		if(val)
			var confidentialityLevel = confidentialityLevelsStore.findRecord('id', val.id, 0, false, false, true);
		else
			var confidentialityLevel = null;
		
		if(confidentialityLevel == null) {
			return '';
		}
		
		
		var confidentialityLevelName = confidentialityLevel.data.name;
		var strHtml = '<div>';
		strHtml += '<div style="float:left;width:90%">';
		strHtml += confidentialityLevelName;
		strHtml += '</div>';
		strHtml += '</div>';
		return strHtml;		
	},
	/**
	 * This method is used to return an object with id values
	 * an array format expected by the ExtJS multiSelect or itemSelect
	 * components.
	 * 
	 * Translates: 
	 * [{"id":"1"},{"id":"2"},{"id":"3"}]
	 * 
	 * Into:
	 * ["1","2","3"]
	 */
	getSelectedIdsForMultiSelect: function( arr ){
		var selectedIds = [];
		Ext.each(arr,function(item,index){
			selectedIds.push(item["id"]);
		});
		return selectedIds;
	},
	
	renderBackgroundColorActive: function(value, metadata, record) {
	    if(record.get('objectStatus') == 'ACTIVE'){
			metadata.style="background-color:#C5D7F1;";
		}
		return value;
	},
	
	renderDateBackgroundColorActive: function(value, metadata, record) {
	    if(record.get('objectStatus') == 'ACTIVE'){
			metadata.style="background-color:#C5D7F1;";
		}
		return Ext.util.Format.date( record.get('modifiedDate'),'Y-m-d g:i A');
	},
	
	renderImportant: function(value, metadata, record) {
		if (record.get('objectStatus') == 'ACTIVE') {
	       metadata.style = "background-color:#C5D7F1";
	    };
		var isImportant = (record.get('isImportant') || record.get('isFinancialAid') || record.get('isF1Visa'));
	    if(isImportant){
			metadata.style += ";color:#BF1C10;"
		}
		return isImportant == true ? "Yes":"";
	},
	
	renderTemplatePrivate: function(value, metadata, record) {
		return record.get('isPrivate') == true ? "Private":"Public";
	},
	
	renderTemplateVisibility: function(value, metadata, record) {
		if(record.get('visibility') == 'PRIVATE')
			return  "Private";
		if(record.get('visibility') == 'AUTHENTICATED')
			return  "Auth";
		if(record.get('visibility') == 'ANONYMOUS')
			return  "Anon";
	}
});
