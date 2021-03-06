'use strict';

describe('Controller Tests', function() {

    describe('MarketOrder Management Detail Controller', function() {
        var $scope, $rootScope;
        var MockEntity, MockMarketOrder, MockInvType, MockSellableInvType;
        var createController;

        beforeEach(inject(function($injector) {
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            MockEntity = jasmine.createSpy('MockEntity');
            MockMarketOrder = jasmine.createSpy('MockMarketOrder');
            MockInvType = jasmine.createSpy('MockInvType');
            MockSellableInvType = jasmine.createSpy('MockSellableInvType');
            

            var locals = {
                '$scope': $scope,
                '$rootScope': $rootScope,
                'entity': MockEntity ,
                'MarketOrder': MockMarketOrder,
                'InvType': MockInvType,
                'SellableInvType': MockSellableInvType
            };
            createController = function() {
                $injector.get('$controller')("MarketOrderDetailController", locals);
            };
        }));


        describe('Root Scope Listening', function() {
            it('Unregisters root scope listener upon scope destruction', function() {
                var eventType = 'eveSmartTraderApp:marketOrderUpdate';

                createController();
                expect($rootScope.$$listenerCount[eventType]).toEqual(1);

                $scope.$destroy();
                expect($rootScope.$$listenerCount[eventType]).toBeUndefined();
            });
        });
    });

});
