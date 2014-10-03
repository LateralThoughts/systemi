/* Day block directive */

// Please note that $modalInstance represents a modal window (instance) dependency.
// It is not the same as the $modal service used above.

var ModalInstanceCtrl = function ($scope, $modalInstance, day) {
  $scope.day = day;

  $scope.halfUp = function() {
  	day.halfDown = false;
  	day.halfUp = true;
  	$modalInstance.close();
  }

  $scope.halfDown = function() {
  	day.halfDown = true;
  	day.halfUp = false;
  	$modalInstance.close();
  }

  $scope.dismiss = function () {
  	day.halfDown = false;
  	day.halfUp = false;
    $modalInstance.close();
  };

  $scope.cancel = function () {
  	day.halfDown = true;
  	day.halfUp = true;
    $modalInstance.dismiss('cancel');
  };
};

angular.module("day-block", [])
    .directive('ltDayBlock', function ($modal, $log) {
        return {
            templateUrl:'/assets/javascripts/angular/templates/day-block.html',
            restrict: 'EA',
            replace: true,
            scope: {
            	day: '='
            },
            link: function(scope, element, attrs){
            	scope.toggle = function(day) {
            		day.toggleNextState();
            	}
            	/* deprecated : open modal */
	           	scope.open = function (day) {
				    var modalInstance = $modal.open({
				      templateUrl: '/assets/javascripts/angular/templates/day-block-modal.html',
				      controller: ModalInstanceCtrl,
				      resolve: {
				        day: function () {
				          return scope.day;
				        }
				      }
				    });
				};
            }
        }
    });