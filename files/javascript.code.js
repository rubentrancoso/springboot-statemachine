app.directive('nodup', function($log) {
	return {
		require : 'ngModel',
		link : function(scope, element, attr, mCtrl) {
			function nodup(value) {
				$log.log('nodup');
				$log.log('value: ' + value);
				$log.log('id: ' + attr.id);
				$log.log('element: ' + element);
				$log.log('attr: ' + attr);
				$log.log('mCtrl: ' + mCtrl);
				
				var current_id = attr.id;
				if(current_id.startsWith("global_name_")) {
					if(isDup(value, /^global_name_new-/)) {
						$log.log('dup global');
					}
				} else if (current_id.startsWith("imediate_name_")) {
					if(isDup(value, /^imediate_name_new-/)) {
						
					}
				}
//					var inputs = document.querySelectorAll("input");
//					var elements = [];
//					for (var i=0; i<inputs.length; i++)
//					    if ( /^global_name_new-/.test(inputs[i].id)) {
//					    	if(inputs[i].id != current_id) {
//						    	$log.log(inputs[i].value);
//					    	}
//					    }
//				}
//		
//				if (value.indexOf("e") > -1) {
//					mCtrl.$setValidity('charE', true);
//				} else {
//					mCtrl.$setValidity('charE', false);
//				}
				return value;
			}
			mCtrl.$parsers.push(nodup);
		}
	};
});

function isDup(value, regex) {

	var inputs = document.querySelectorAll("input");
	var elements = [];
	for (var i=0; i<inputs.length; i++)
	    if ( regex.test(inputs[i].id)) {
	    	if(inputs[i].id != current_id) {
		    	$log.log(inputs[i].value);
	    	}
	    }
}
	
	return true;
}