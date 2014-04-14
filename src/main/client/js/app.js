/// <reference path="services/UserAuthenticationService.js" /> ???
var medelModule = angular.module('atomClient', [ 'ngRoute', 'xml', 'ui.bootstrap' ])//, 'ui.bootstrap', 'ngGrid', 'ngGrid.services'])
    .config(
        [ '$routeProvider', '$httpProvider', function ($routeProvider, $httpProvider) {
                
            // define all routes and set the template and controller to show/handle
            // the request
            $routeProvider.when('/:serviceId?/:datasetId?', {
                controller : AtomCtrl
            });
            //.otherwise({
//                
//                redirectTo : '/',
//                controller: AtomCtrl
//            });
            
            // the interceptor produces an error in combination with accordion and template-cache
            //$httpProvider.interceptors.push('xmlHttpInterceptor');

        }
     ]).run(function($http) {
         console.log("RUN");
     });


angular.module("template/accordion/accordion-group.html", []).run(["$templateCache", function($templateCache) {
    $templateCache.put("template/accordion/accordion-group.html",
      "<div class=\"panel panel-default\">\n" +
      "  <div class=\"accordion-toggle\" ng-click=\"isOpen = !isOpen\">\n" +
      "    <div class=\"panel-heading\">\n" +
      "      <h4 class=\"panel-title\" accordion-transclude=\"heading\">{{heading}}\n" +
      "      </h4>\n" +
      "    </div>\n" +
      "  </div>\n" +
      "  <div class=\"panel-collapse\" collapse=\"!isOpen\">\n" +
      "     <div class=\"panel-body\" ng-transclude></div>\n" +
      "  </div>\n" +
      "</div>");
  }]);