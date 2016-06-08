(function () {
    'use strict';

    angular
        .module('eveSmartTraderApp')
        .factory('Register', Register);

    Register.$inject = ['$resource'];

    function Register ($resource) {
        return $resource('api/register', {}, {});
    }
})();
