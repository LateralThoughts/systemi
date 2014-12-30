/* Day block directive */

angular.module("day-block", [])
    .directive('ltDayBlock', function () {
        return {
            templateUrl:'/assets/javascripts/angular/templates/day-block.html',
            restrict: 'EA',
            replace: true,
            scope: {
            	day: '='
            },
            link: function(scope){
            	scope.toggle = function(day) {
            		day.toggleNextState();
            	};
            }
        }
    });