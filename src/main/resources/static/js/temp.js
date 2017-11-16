app
		.controller(
				'ExecuteCtrl',
				function($scope, $log, $routeParams, Process, Task) {
					$scope.processId = $routeParams.processId;

					$scope.globals = [];
					$scope.imediates = [];

					Process.get({
						id : $routeParams.processId
					}, function(data) {
						$scope.process = new Process();
						$scope.process.id = data.id;
						$scope.process.name = data.name;
						$scope.process.state = data.state;
						$scope.globals = $scope.globals
								.concat(hash2nameValueArray(data.globals));
					});

					$scope.addGlobal = function() {
						$scope.globals.push({
							"name" : "",
							"value" : ""
						});
					};
					$scope.addImediate = function() {
						$scope.imediates.push({
							"name" : "",
							"value" : ""
						});
					};

					$scope.removeGlobal = function(index) {
						$scope.globals.splice(index, 1);
					};
					$scope.removeImediate = function(index) {
						$scope.imediates.splice(index, 1);
					};

					$scope.submit = function() {
						var globals = {};
						var imediates = {};
						var global_count = $scope.globals.length;
						for (var i = 0; i < global_count; i++) {
							if ($scope.globals[i].name != '') {
								globals[$scope.globals[i].name] = $scope.globals[i].value;
							}
						}
						var imediate_count = $scope.imediates.length;
						for (var j = 0; j < imediate_count; j++) {
							if ($scope.imediates[j].name != '') {
								imediates[$scope.imediates[j].name] = $scope.imediates[j].value;
							}
						}

						// // var imediate_count = $scope.imediates.length;
						// // //$log.log(imediate_count);
						// // for (var i = 0; i < imediate_count; i++) {
						// //
						// // // if ($scope.imediates[i].name != '')
						// // // imediates[$scope.imediates[i].name] =
						// $scope.imediates[i].value;
						// // }
						// //
						// $scope.process.globals = globals;
						// $scope.process.input = imediates;
						// $scope.process.$save({
						// id : $scope.process.id
						// });
					};
				});