/* Day block directive */

facstemi
    .directive('ltDayBlock', function () {
        return {
            templateUrl:'/assets/javascripts/angular/templates/day-block.html',
            restrict: 'EA',
            replace: true,
            scope: {
            	day: '='
            },
            link: function(scope, element, attrs){
            	scope.toggle = function(day) {
            		day.state = !day.state;
            	}
            }
        }
    });