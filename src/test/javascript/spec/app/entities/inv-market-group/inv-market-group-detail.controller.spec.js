'use strict';

describe('Controller Tests', function() {

    describe('InvMarketGroup Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockInvMarketGroup;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockInvMarketGroup = jasmine.createSpy('MockInvMarketGroup');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'InvMarketGroup': MockInvMarketGroup
            };
            createController = function() {
                $injector.get('$controller')("InvMarketGroupDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'eveSmartTraderApp:invMarketGroupUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
