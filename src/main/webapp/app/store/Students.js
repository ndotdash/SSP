Ext.define('Ssp.store.Students', {
    extend: 'Ext.data.Store',
    model: 'Ssp.model.Person',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject: {
        apiProperties: 'apiProperties'
    },
	constructor: function(){
		Ext.apply(this, {
							proxy: this.apiProperties.getProxy(this.apiProperties.getItemUrl('person')),
							autoLoad: false
						});
		return this.callParent(arguments);
	}
});