/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
function PartnerCtrl($scope, $http, $routeParams, $route, $timeout, $location, xmlFilter) {
    if ($location.search().partner) {
        let partnerLoc = {"hh":"Hamburg",
            "bb":"Brandenburg",
            "be":"Berlin",
            "bw":"Baden-Württemberg",
            "by":"Bayern",
            "hb": "Bremen",
            "he": "Hessen",
            "mv": "Mecklenburg-Vorpommern",
            "ni": "Niedersachsen",
            "nw": "Nordrhein-Westfalen",
            "rp": "Rheinland-Pfalz",
            "sh": "Schleswig-Holstein",
            "sl": "Saarland",
            "sn": "Sachsen",
            "st": "Sachsen-Anhalt",
            "th": "Thüringen"}
        $scope.partner=$location.search().partner;
        $scope.partnerLoc=partnerLoc;
    }
}

function AtomCtrl($scope, $http, $routeParams, $route, $timeout, $location, xmlFilter) {
    a = $scope;
    $scope.oneAtATime = false;
    $scope.entries = [];
    $scope.feeds = [];
    $scope.message = { loading: " Dienste werden geladen ..." };
    $scope.datasetLoaded = [];
    $scope.subsetsLoaded = true;
    // console.log("AtomCtrl");

    var filter = "";
    if ($location.search().q) {
        filter = "?q=" + $location.search().q;
    }
    if ($location.search().partner) {
    	if (filter.length === 0) {
            filter = "?q=partner:" + $location.search().partner;
    	} else {
            filter = filter + "+partner:" + $location.search().partner;
    	}
    	$scope.partner=$location.search().partner;
    }
    if ($location.search().serviceOnly) {
        $scope.serviceOnly = true;
    	if (filter.length === 0) {
            filter = "?q=" + $location.search().serviceId;
    	} else {
            filter = filter + "+" + $location.search().serviceId;
    	}
    }

    $http.get( "service-list" + filter ).success(function(response) {
        // console.log("services loaded");
        var xml = xmlFilter(response);
        var feeds = xml.find("entry");
        angular.forEach(feeds, function(feed) {
            var element = angular.element(feed);
            var title = element.find("title").text();
            var summary = element.find("summary").text();
            var date = element.find("updated").text();
            var link = element.find("link[rel=alternate]").attr("href");
            var feedObj = {
                title: title,
                summary: summary,
                date: date,
                link: link
            };
            
            if (!$scope.serviceOnly) {
                $scope.feeds.push( feedObj );
            }
            
            if (link.indexOf($routeParams.serviceId) !== -1) {
                $scope.selectedFeed = feedObj;
                $scope.showDatasetFeeds($routeParams.datasetId);
            }
        });
        $scope.feedsLoaded = true;
    }).error(function(data, status) {
        $scope.feedsLoaded = true;
        $scope.message.error = "Keine Dienste gefunden!";
    });
    
    $scope.showDatasetFeeds = function(datasetId) {
        // console.log("changed:", $scope.selectedFeed);
        if ($scope.selectedFeed === null) return;

        $scope.currentFeed = $scope.selectedFeed;
        $scope.selectedFeed = null;

        $scope.entries = [];
        $scope.subsetsLoaded = false;
        $scope.message.loading = " Datensätze werden geladen ...";
        var link = $scope.currentFeed.link;
        $scope.selectedServiceId = link.substring(link.lastIndexOf("/") + 1);
        var searchObj;
        searchObj =  {
            serviceId: $scope.selectedServiceId
        };
        if ($scope.serviceOnly) {
            searchObj.serviceOnly = $scope.serviceOnly;
        }
        if ($scope.partner) {
            searchObj.partner = $scope.partner;
        }
        if (datasetId) searchObj.datasetId = datasetId;
        $location.search(searchObj);

        $http.get( link ).then(function(response) {
            var xml = xmlFilter(response.data);
            var entriesDom = xml.find("entry");
            var index = 0;
            angular.forEach(entriesDom, function(entry) {
                var element = angular.element(entry);
                var title = element.find("title").text();
                var summary = element.find("summary").text();
                var date = element.find("updated").text();
                var link = element.find("link[rel=alternate]").attr("href");
                var entryObj = {
                    title: title,
                    summary: summary,
                    date: date,
                    link: link,
                    index: index
                };
                $scope.entries.push( entryObj );
                
                if (link.indexOf(datasetId) !== -1) {
                    $scope.loadDownloadsFeed(entryObj, index);
                    var i = index;
                    $timeout(function() {
                        var panel = angular.element("accordion .panel_" + i);
                        var pos = panel[0].offsetTop;
                        window.scrollTo(0, pos);
                        panel.scope().isopen = true;
                        panel.scope().$apply();
                    }, 100);
                }
                index++;
            });
            $scope.subsetsLoaded = true;
            $scope.datasetLoaded = [];
        });
    };
    
    $scope.loadDownloadsFeed = function(dsEntry, index) {
        if (!$scope.datasetLoaded[index]) {
            // console.log("loadDownloadsFeed:", dsEntry);
            var link = dsEntry.link;
            var searchObj;
            searchObj =  {
                serviceId: $scope.selectedServiceId,
                datasetId: link.substring(link.lastIndexOf("/")+1)
            };
            if ($scope.serviceOnly) {
                searchObj.serviceOnly = $scope.serviceOnly;
            }
            if ($scope.partner) {
                searchObj.partner = $scope.partner;
            }
            $location.search(searchObj);

            $http.get( dsEntry.link + "?detail=true" ).then(function(response) {
                var xml = xmlFilter(response.data);
                dsEntry.useConstraints = xml.find("rights").text();
                var detailLinkElem = xml.find("feed link[rel=detail]").attr("href");
                dsEntry.detailLink = detailLinkElem;// ? detailLinkElem.getAttribute("href") : null;
                var entriesDom = xml.find("entry");
                dsEntry.downloads = [];
                angular.forEach(entriesDom, function(entry) {
                    var linkElem = angular.element(entry).find("link[rel=alternate]");
                    var link = linkElem.attr("href");
                    var fileType = linkElem.attr("type");
                    // console.log("filtetype: " + fileType);
                    var crs = entriesDom.find("category").attr("label");
                    
                    dsEntry.downloads.push({
                        link: link,
                        filename: link.substr(link.lastIndexOf("/") + 1),
                        fileType: fileType,
                        crs: crs
                    });
                });
                
                $scope.datasetLoaded[index] = true;
                
                // IE8/9 Hack:
                // Icon only appears after a hover/focus event!
                setTimeout(function() {
                    angular.element("accordion .panel_" + index + " .glyphicon").parent().focus();
                }, 100);
            });
        }
    };
    
}
