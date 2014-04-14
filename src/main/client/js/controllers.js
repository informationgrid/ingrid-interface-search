function AtomCtrl($scope, $http) {
    a = $scope;
    $scope.oneAtATime = false;
    $scope.entries = [];
    $scope.feeds = [];
    $scope.message = { loading: " Dienste werden geladen ..." };
    $scope.datasetLoaded = [];
    $scope.subsetsLoaded = true;
    console.log("AtomCtrl");
    
    $http.get( "service-list" ).then(function(response) {
        var feeds = response.xml.find("entry");
        angular.forEach(feeds, function(feed) {
            var element = angular.element(feed);
            var title = element.find("title")[0].innerHTML;
            var summary = element.find("summary")[0].innerHTML;
            var date = element.find("updated")[0].innerHTML;
            var link = feed.querySelector("[rel=alternate]").getAttribute("href");
            $scope.feeds.push( {
                title: title,
                summary: summary,
                date: date,
                link: link
            });
        });
        $scope.feedsLoaded = true;
    });
    
    
    $scope.showDatasetFeeds = function() {
        console.log("changed:", $scope.selectedFeed);
        $scope.entries = [];
        $scope.subsetsLoaded = false;
        $scope.message.loading = " Datensätze werden geladen ...";
        $http.get( $scope.selectedFeed.link ).then(function(response) {
            var feedId = response.xml.find("id")[0].innerHTML;
            var entriesDom = response.xml.find("entry");
            angular.forEach(entriesDom, function(entry) {
                var element = angular.element(entry);
                var title = element.find("title")[0].innerHTML;
                var summary = element.find("summary")[0].innerHTML;
                var date = element.find("updated")[0].innerHTML;
                var link = entry.querySelector("[rel=alternate]").getAttribute("href");
                $scope.entries.push( {
                    title: title,
                    summary: summary,
                    date: date,
                    link: link,
                    useConstraints: "???"
                });
            });
            $scope.subsetsLoaded = true;
            $scope.datasetLoaded = [];
            
        }, function(error) {
            //console.error(error);
        });
    };
    
    $scope.loadDownloadsFeed = function(dsEntry, isopen, index) {
        if (!$scope.datasetLoaded[index]) {
            console.log("loadDownloadsFeed:", dsEntry);
            $http.get( dsEntry.link ).then(function(response) {
                dsEntry.useConstraints = response.xml.find("rights")[0].innerHTML;
                var entriesDom = response.xml.find("entry");
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
    }
    
}