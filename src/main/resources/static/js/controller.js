function hashSize(obj) {
	var size = 0, key;
	for (key in obj) {
		if (obj.hasOwnProperty(key))
			size++;
	}
	return size;
};

function hash2nameValueArray(obj) {
	var arr = [];

	var key;
	for (key in obj) {
		if (obj.hasOwnProperty(key)) {
			arr.push({
				"name" : key,
				"type" : getType(obj[key]),
				"value" : getValue(obj[key])		
			});
		}
	}

	return arr;
}

function getType(obj) {
	var splitPos = obj.indexOf(':');
	var type = obj.substring(0,splitPos);
	return type;
}

function getValue(obj) {
	var splitPos = obj.indexOf(':') + 1;
	var value = obj.substring(splitPos);
	return value;
}

app.controller('ProcessCtrl', function($scope, $uibModal, $log, Process, Task) {
	$scope.size = {
		"type" : "select",
		"value" : "5"
	};
	$scope.states = {
		"value" : ""
	};
	$scope.currentPage = 1;
	var listProcesses = function(vpage, vsize, vstate) {
		Process.query({
			page : vpage,
			size : vsize,
			state : vstate
		}, function(data) {
			$scope.content = data.content;
			$scope.total_elements = data.totalElements;
			$scope.total_pages = data.totalPages;
			$scope.items_per_page = data.size;
		});
	}
	var loadTasks = function() {
		Task.get(function(data) {
			$scope.states = data;
		});
	}
	var addProcess = function(process) {
		$log.log('addProcess: ' + process);
		process.state = 'NOT_STARTED';
		process.$save(function(data) {
			$scope.total_elements = data.totalElements;
			reminder = data.totalElements % $scope.size.value;
			if (reminder > 0)
				reminder = 1;
			else
				reminder = 0;
			$scope.total_pages = parseInt(data.totalElements
					/ $scope.size.value)
					+ reminder;
			$scope.items_per_page = $scope.size.value;
			listProcesses($scope.total_pages - 1, $scope.size.value,
					$scope.states.value);
			$scope.currentPage = $scope.total_pages;
		});
	}
	
	loadTasks();
	listProcesses(0, $scope.size.value, $scope.states.value);

	$scope.sizeChange = function() {
		$scope.currentPage = 0;
		listProcesses(0, $scope.size.value, $scope.states.value);
	}
	$scope.filterChange = function() {
		$scope.currentPage = 0;
		listProcesses(0, $scope.size.value, $scope.states.value);
	}
	$scope.pageChange = function() {
		listProcesses($scope.currentPage - 1, $scope.size.value,
				$scope.states.value);
	}
	$scope.removeProcess = function(processId) {
		$log.log($scope.currentPage);
		var page = $scope.currentPage;
		Process.delete({ id: processId }, function() {
			if(page>0)
				page = $scope.currentPage-1;
			listProcesses(page, $scope.size.value, $scope.states.value);
		});
	}	
	
	// **********
	// OPEN MODAL
	$scope.openModal = function() {
		var modalInstance = $uibModal.open({
			ariaLabelledBy : 'modal-title',
			ariaDescribedBy : 'modal-body',
			templateUrl : 'views/newprocess.html',
			controller : 'ModalInstanceCtrl',
		});
		// ******
		// RESULT
		modalInstance.result.then(function(process) {
			addProcess(process);
		}, function() {
			$log.info('Modal dismissed at: ' + new Date());
		});
	};
});

app.controller('ModalInstanceCtrl',
		function($scope, $uibModalInstance, Process) {
			$scope.process = new Process();
			$scope.okModal = function() {
				$uibModalInstance.close($scope.process);
			};
			$scope.cancelModal = function() {
				$uibModalInstance.dismiss('cancel');
			};
		});

app.factory('Task', function($resource) {
	return $resource("/task", {}, {
		'get' : {
			method : 'GET',
			isArray : true
		}
	});
});

app.factory('Process', function($resource) {
	return $resource("/process/:id", {}, {
		'query' : {
			method : 'GET'
		}
	});
});

app.factory('Events', function($resource) {
	return $resource("/events/:fromtask", {}, {
		'get' : {
			method : 'GET',
			isArray : true
		}
	});
});

app
		.controller(
				'ExecuteCtrl',
				function($scope, $log, $route, $routeParams, Process, Events) {
					$scope.processId = $routeParams.processId;

					$scope.globals = [];
					$scope.imediates = [];
					
					var loadEvents = function() {
						Events.get({
							fromtask : $scope.process.state
						}, function(data) {
							$scope.events = data;
						});
					}
					
					Process.get({
						id : $routeParams.processId
					}, function(data) {
						$scope.process = new Process();
						$scope.process.id = data.id;
						$scope.process.name = data.name;
						$scope.process.state = data.state;
						$scope.globals = $scope.globals
								.concat(hash2nameValueArray(data.globals));
						$scope.imediates = $scope.imediates
								.concat(hash2nameValueArray(data.input));
						loadEvents(data.state);
					});

					$scope.addGlobal = function() {
						$scope.globals.push({
							"name" : "",
							"type" : "string",
							"value" : ""
						});
					};
					$scope.addImediate = function() {
						$scope.imediates.push({
							"name" : "",
							"type" : "string",
							"value" : ""
						});
					};

					$scope.removeGlobal = function(index) {
						$scope.globals.splice(index, 1);
					};
					$scope.removeImediate = function(index) {
						$scope.imediates.splice(index, 1);
					};

					$scope.setSubmitButton = function(name) {
						$scope.submitButton = name;
					};
					
					$scope.submit = function() {
						var globals = {};
						var global_count = $scope.globals.length;
						for (var i = 0; i < global_count; i++) {
							if ($scope.globals[i].name != '') {
								globals[$scope.globals[i].name] = $scope.globals[i].type + ':'+ $scope.globals[i].value;
							}
						}
						var imediates = {};
						var imediate_count = $scope.imediates.length;
						for (var j = 0; j < imediate_count; j++) {
							if ($scope.imediates[j].name != '') {
								imediates[$scope.imediates[j].name] = $scope.imediates[j].type + ':' + $scope.imediates[j].value;
							}
						}

						$scope.process.globals = globals;
						$scope.process.input = imediates;
						$scope.process.event = $scope.submitButton;
						$scope.process.$save({
							id : $scope.process.id
						}, function() {
							$route.reload();
						});
					};
				});