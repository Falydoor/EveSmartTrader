'use strict';

describe('Controller Tests', function() {

    describe('SellableInvType Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockSellableInvType, MockMarketOrder, MockInvType;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockSellableInvType = jasmine.createSpy('MockSellableInvType');
            MockMarketOrder = jasmine.createSpy('MockMarketOrder');
            MockInvType = jasmine.createSpy('MockInvType');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'SellableInvType': MockSellableInvType,
                'MarketOrder': MockMarketOrder,
                'InvType': MockInvType
            };
            createController = function() {
                $injector.get('$controller')("SellableInvTypeDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'eveSmartTraderApp:sellableInvTypeUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
