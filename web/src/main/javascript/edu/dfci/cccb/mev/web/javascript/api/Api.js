define(['jquery','angular'], function(jquery, angular){
  
    return angular.module( 'Mev.Api', ['ngResource'])
        .factory ('API', ['$resource', function($resource){
            return {
                user : {
                    datasets : function(){
                        var access = $resource('/dataset',
                        {format:'json'},
                        {get:{method:"GET", isArray:true}});
                        
                        return access;
                    }
                },
                dataset : {
                    
                    selections : function(){
                        var access = $resource('/dataset/:dataset/:dimension/selection',
                                {format:'json'});
                        
                        return access;
                    },
                    data : function(){
                        var access = $resource('/dataset/:dataset/data?format:json');
                        
                        return access;
                    },
                    analysis : function(){
                        var access = $resource('/dataset/:dataset/analysis/:name',
                            {format:'json'},
                            {
                                'create.limma':{
                                    method:'POST', 
                                    url:'/dataset/:dataset/analyze/limma/'
                                        + ":name(dimension=:dimension"
                                        + ",experiment=:experiment"
                                        + ",control=:control)"
                                },
                                'create.hcl':{
                                    method:'Post',
                                    url:'dataset/:dataset/analyze/hcl/'
                                        +  ':name(:dimension,:metric,:algorithm)',
                                }
                            }
                        );
                        
                        return access;
                    }
                }
            };
        }]);
  
  
});