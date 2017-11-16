var app = angular.module('app', [ 'ngRoute', 'ngResource', 'ui.bootstrap' ]);
app.config(function($routeProvider) {
	$routeProvider.when("/", {
		templateUrl : "views/home.html"
	}).when("/detail/:processId", {
		templateUrl : "views/execute.html"
	});
});
