function AtomCtrl($scope, $http, $routeParams, $route, $timeout, $location, xmlFilter) {
    a = $scope;
    $scope.oneAtATime = false;
    $scope.entries = [];
    $scope.feeds = [];
    $scope.message = { loading: " Dienste werden geladen ..." };
    $scope.datasetLoaded = [];
    $scope.subsetsLoaded = true;
    console.log("AtomCtrl");

    var filter = "";
    if ($location.search().q) {
        filter = "?q=" + $location.search().q;
    }
    
    $http.get( "service-list" + filter ).success(function(response) {
        var xml = xmlFilter(response);
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
                $scope.showDatasetFeeds($routeParams.datasetId);
            }
        });
        $scope.feedsLoaded = true;
    }).error(function(data, status) {
        $scope.feedsLoaded = true;
        $scope.message.error = "Keine Dienste gefunden!";
    });
    
    
    $scope.showDatasetFeeds = function(datasetId) {
        console.log("changed:", $scope.selectedFeed);
        if ($scope.selectedFeed === null) return;

        $scope.currentFeed = $scope.selectedFeed;
        $scope.selectedFeed = null;

        $scope.entries = [];
        $scope.subsetsLoaded = false;
        $scope.message.loading = " Datensätze werden geladen ...";
        var link = $scope.currentFeed.link;
        $scope.selectedServiceId = link.substring(link.lastIndexOf("/") + 1);
        var searchObj = {
            serviceId: $scope.selectedServiceId            
        };
        if (datasetId) searchObj.datasetId = datasetId;
        $location.search(searchObj);

        $http.get( link ).then(function(response) {
            var xml = xmlFilter(response.data);
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
                    index: index
                };
                $scope.entries.push( entryObj );
                
                if (link.indexOf(datasetId) !== -1) {
                    $scope.loadDownloadsFeed(entryObj, index);
                    var i = index;
                    $timeout(function() {
                        var panel = angular.element(document.querySelector("accordion .panel_" + i));
                        var pos = panel[0].offsetTop;// + panel.parent().scrollTop();
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
            console.log("loadDownloadsFeed:", dsEntry);
            var link = dsEntry.link;
            $location.search({
                serviceId: $scope.selectedServiceId,
                datasetId: link.substring(link.lastIndexOf("/")+1)
            });
            $http.get( dsEntry.link + "?detail=true" ).then(function(response) {
                var xml = xmlFilter(response.data);
                dsEntry.useConstraints = xml.find("rights")[0].textContent;
                var detailLinkElem = xml.find("feed")[0].querySelector("[rel=detail]");
                dsEntry.detailLink = detailLinkElem ? detailLinkElem.getAttribute("href") : null;
                var entriesDom = xml.find("entry");
                dsEntry.downloads = [];
                angular.forEach(entriesDom, function(entry) {
                    var linkElem = entry.querySelector("[rel=alternate]");
                    var link = linkElem.getAttribute("href");
                    var fileType = linkElem.getAttribute("type");
                    var crs = entry.querySelector("category").getAttribute("label");
                    
                    dsEntry.downloads.push({
                        link: link,
                        filename: link.substr(link.lastIndexOf("/") + 1),
                        fileType: fileType,
                        crs: crs
                    });
                });
                
                $scope.datasetLoaded[index] = true;
            });
        }
    };
    
}