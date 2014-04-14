function AtomCtrl($scope, $http, $routeParams, $route, $timeout, $location, xmlFilter) {
    a = $scope;
    $scope.oneAtATime = false;
    $scope.entries = [];
    $scope.feeds = [];
    $scope.message = { loading: " Dienste werden geladen ..." };
    $scope.datasetLoaded = [];
    $scope.subsetsLoaded = true;
    console.log("AtomCtrl");
    
    $http.get( "service-list" ).then(function(response) {
        var xml = xmlFilter(response.data);
        var feeds = xml.find("entry");
        angular.forEach(feeds, function(feed) {
            var element = angular.element(feed);
            var title = element.find("title")[0].textContent;
            var summary = element.find("summary")[0].textContent;
            var date = element.find("updated")[0].textContent;
            var link = feed.querySelector("[rel=alternate]").getAttribute("href");
            var feedObj = {
                title: title,
                summary: summary,
                date: date,
                link: link
            };
            
            $scope.feeds.push( feedObj );
            
            if (link.indexOf($routeParams.serviceId) !== -1) {
                $scope.selectedFeed = feedObj;
                $scope.showDatasetFeeds();
            }
        });
        $scope.feedsLoaded = true;
    });
    
    
    $scope.showDatasetFeeds = function() {
        console.log("changed:", $scope.selectedFeed);
        $scope.entries = [];
        $scope.subsetsLoaded = false;
        $scope.message.loading = " Datensätze werden geladen ...";
        var link = $scope.selectedFeed.link;
        $scope.selectedServiceId = link.substring(link.lastIndexOf("/") + 1);
        $location.search({
            serviceId: $scope.selectedServiceId,
            datasetId: $routeParams.datasetId
        });
        $http.get( link ).then(function(response) {
            var xml = xmlFilter(response.data);
            var feedId = xml.find("id")[0].textContent;
            var entriesDom = xml.find("entry");
            var index = 0;
            angular.forEach(entriesDom, function(entry) {
                var element = angular.element(entry);
                var title = element.find("title")[0].textContent;
                var summary = element.find("summary")[0].textContent;
                var date = element.find("updated")[0].textContent;
                var link = entry.querySelector("[rel=alternate]").getAttribute("href");
                var entryObj = {
                    title: title,
                    summary: summary,
                    date: date,
                    link: link,
                    useConstraints: "???"
                };
                $scope.entries.push( entryObj );
                
                if (link.indexOf($routeParams.datasetId) !== -1) {
                    $scope.loadDownloadsFeed(entryObj, index);
                    var i = index;
                    $timeout(function() {
                        var panel = angular.element(document.querySelector("accordion .panel_" + i));
                        panel.scope().isopen = true;
                        panel.scope().$apply();
                    }, 100);
                }
                index++;
            });
            $scope.subsetsLoaded = true;
            $scope.datasetLoaded = [];
            
        }, function(error) {
            //console.error(error);
        });
    };
    
    $scope.loadDownloadsFeed = function(dsEntry, index) {
        if (!$scope.datasetLoaded[index]) {
            console.log("loadDownloadsFeed:", dsEntry);
            var link = dsEntry.link;
            $location.search({
                serviceId: $scope.selectedServiceId,
                datasetId: link.substring(link.lastIndexOf("/")+1)
            });
            $http.get( dsEntry.link ).then(function(response) {
                var xml = xmlFilter(response.data);
                dsEntry.useConstraints = xml.find("rights")[0].textContent;
                var entriesDom = xml.find("entry");
                dsEntry.downloads = [];
                angular.forEach(entriesDom, function(entry) {
                    var linkElem = entry.querySelector("[rel=alternate]");
                    var link = linkElem.getAttribute("href");
                    var fileType = linkElem.getAttribute("type");
                    var crs = entry.querySelector("category").getAttribute("label");
                    
                    dsEntry.downloads.push({
                        link: link,
                        fileType: fileType,
                        crs: crs
                    });
                });
                
                $scope.datasetLoaded[index] = true;
            });
        }
    };
    
}