(function () {

    define([], function () {

        return function (module) {

            module.directive('deseqAccordion', ['tableResultsFilter', 'alertService', 'projectionService', 'pathService', 'BoxPlotService',
                function (tableFilter, alertService, projection, paths, BoxPlotService) {
                    return {
                        restrict: 'E',
                        templateUrl: paths.module + '/templates/DESeqAccordion.tpl.html',
                        scope: {
                            project: "=project",
                            analysis: "=analysis",
                            heatmapView: "=",
                            isItOpen: "@"
                        },
                        link: function (scope) {


                            scope.headers = [
                                {
                                    'name': 'ID',
                                    'field': "id",
                                    'icon': "search"
                                },
                                {
                                    'name': 'Log-Fold-Change',
                                    'field': "logFoldChange",
                                    'icon': [">=", "<="]
                                },
                                {
                                    'name': 'Mean Expression Control',
                                    'field': "meanExpressionControl",
                                    'icon': "none"
                                },
                                {
                                    'name': 'Mean Expression Experimental',
                                    'field': "meanExpressionExperimental",
                                    'icon': "none"
                                },
                                {
                                    'name': 'P-Value',
                                    'field': "pValue",
                                    'icon': "<=",
                                    'default': 0.05
                                },
                                {
                                    'name': 'q-Value',
                                    'field': "qValue",
                                    'icon': "<="
                                }
                            ];
                            
                            scope.filteredResults = undefined;

//                            scope.applyFilter = function () {
//
//                                scope.filteredResults = tableFilter(scope.analysis.results, scope.filterParams)
//
//                                return scope.filteredResults;
//                            };
                            
                            
                            scope.viewGenes = function(filterParams){
	                       		 scope.filteredResults = tableFilter(scope.analysis.results, filterParams);
	                       		 //and filter the heatmap
	                       		scope.$emit("ui:filteredResults", scope.filteredResults);	                       	
	                       		 scope.applyToHeatmap();
	                       	}

                            scope.selectionParams = {
                                name: undefined,
                                color: '#' + Math.floor(Math.random() * 0xFFFFFF << 0).toString(16)
                            };

                            scope.addSelections = function () {

                                var keys = scope.filteredResults.map(projection.ids);
                                var selectionData = {
                                    name: scope.selectionParams.name,
                                    properties: {
                                        selectionDescription: '',
                                        selectionColor: scope.selectionParams.color
                                    },
                                    keys: keys
                                };

                                scope.project.dataset.selection.post({
                                        datasetName: scope.project.dataset.datasetName,
                                        dimension: "row"

                                    }, selectionData,
                                    function (response) {
                                        scope.project.dataset.resetSelections('row')
                                        var message = "Added " + scope.selectionParams.name + " as new Selection!";
                                        var header = "Heatmap Selection Addition";

                                        alertService.success(message, header);
                                    },
                                    function (data, status, headers, config) {
                                        var message = "Couldn't add new selection. If " + "problem persists, please contact us.";

                                        var header = "Selection Addition Problem (Error Code: " + status + ")";

                                        alertService.error(message, header);
                                    });

                            };

                            scope.applyToHeatmap = function () {

//                              var labels = getKeys(scope.filteredResults);
                          	                                
                          	var labels = scope.filteredResults.map(projection.ids);;

                              scope.heatmapView = scope.project.generateView({
                                  viewType: 'heatmapView',
                                  note: "deseq update " + scope.analysis.name,
                                  labels: {
                                      column: {
                                          keys: scope.project.dataset.column.keys
                                      },
                                      row: {
                                          keys: labels
                                      }
                                  },
                                  expression: {
                                      min: scope.project.dataset.expression.min,
                                      max: scope.project.dataset.expression.max,
                                      avg: scope.project.dataset.expression.avg,
                                  }
                              });

                          };
                        }

                    };
            }])

            return module

        }

    })


})()