'use strict';

angular.module('karamel.terminal')
  .factory('ExperimentsService', ['$window', function( $window ){
    return {
      store: function( value ){
        try{
          if( $window.Storage ){
              if (value === null) {
                  $window.localStorage.removeItem('expeiment');                  
              } else {            
                  $window.localStorage.setItem( 'experiment', $window.JSON.stringify( value ) );
              }
            return true;
          } else {
            return false;
          }
        } catch( error ){
          console.error( error, error.message );
        }
        return false;
      },
      recover: function( ){
        try{
          if( $window.Storage ){
            return $window.JSON.parse( $window.localStorage.getItem( 'experiment' ) )
          } else {
            return false;
          }
        } catch( error ){
          console.error( error, error.message );
        }
        return false;
      }
    }
  }])