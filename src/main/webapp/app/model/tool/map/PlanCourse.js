/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
Ext.define('Ssp.model.tool.map.PlanCourse', {
    extend: 'Ssp.model.AbstractBase',
    fields: [{name:'termCode',type:'string'},
             {name:'courseCode',type:'string'},
             {name:'formattedCourse', type:'string'},
             {name:'courseTitle',type:'string'},
             {name:'courseDescription',type:'string'},
             {name:'creditHours', type:'float'},
             {name:'orderInTerm',type:'int'},
             {name:'isDev',type:'boolean'},
             {name:'contactNotes',type:'string'},
             {name:'studentNotes',type:'string'},
             {name:'isImportant',type:'boolean'},
             {name:'isTranscript',type:'boolean', defaultValue:false},
             {name:'duplicateOfTranscript',type:'boolean', defaultValue:false},
             {name:'validInTerm',type:'boolean', defaultValue:true, convert: null},
             {name:'hasCorequisites',type:'boolean', defaultValue:true, convert: null},
             {name:'hasPrerequisites',type:'boolean', defaultValue:true, convert: null},
             {name:'invalidReasons',type:'string'},
             {name:'electiveId',type:'string'}],
    isDev: function(){
        		return this.get("isDev");
        	}
});