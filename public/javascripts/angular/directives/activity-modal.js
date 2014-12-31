angular.module("activity-modal", [])
    .directive('ltActivityModal', ['$modal', 'ActivityModalService', function ($modal, ActivityModalService) {
        return {
            template:'<a class="click" ng-click="openActivityModal(activityId)">Afficher l&#39;activité (PDF)</a>',
            restrict: 'EA',
            replace: true,
            scope: {
                activityId: '='
            },
            link: function(scope, element, attrs){

                scope.openActivityModal = function(activityId) {
                    ActivityModalService.openActivityModal($modal, activityId);
                };
            }
        }
    }])
    .factory("ActivityModalService", function() {

        return {
            openActivityModal: function($modal, activityId) {
                $modal.open({
                    templateUrl: '/assets/javascripts/angular/templates/activity-modal.html',
                    controller: function ($scope, $modalInstance, activityId) {
                        $scope.activityId = activityId;

                        $scope.dismissActivityModal = function () {
                            $modalInstance.dismiss("cancel");
                        }
                    },
                    resolve: {
                        activityId: function () {
                            return activityId;
                        }
                    }
                });
            }
        };
    });
