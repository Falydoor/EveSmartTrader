'use strict';

describe('Controller Tests', function() {

    describe('InvType Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockInvType, MockInvMarketGroup;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockInvType = jasmine.createSpy('MockInvType');
            MockInvMarketGroup = jasmine.createSpy('MockInvMarketGroup');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'InvType': MockInvType,
                'InvMarketGroup': MockInvMarketGroup
            };
            createController = function() {
                $injector.get('$controller')("InvTypeDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'eveSmartTraderApp:invTypeUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
