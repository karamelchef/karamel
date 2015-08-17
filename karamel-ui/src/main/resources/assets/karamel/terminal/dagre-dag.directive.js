'use strict';

angular.module('main.module')
  .directive('dagreDag', [function() {
      return {
        restrict: "A",
        link: function(scope, element, attrs) {
          // Set up zoom support
          var svg = d3.select("svg"),
            inner = svg.select("g"),
            zoom = d3.behavior.zoom().on("zoom", function() {
            inner.attr("transform", "translate(" + d3.event.translate + ")" +
              "scale(" + d3.event.scale + ")");
          });
          svg.call(zoom);

          scope.$watch('dagData', function(data) {
            if (data !== undefined) {
              updateDag(data);
            }
          });

          var updateDag = function(data) {
            var render = new dagreD3.render();

            // Left-to-right layout
            var g = new dagreD3.graphlib.Graph();
            g.setGraph({
              nodesep: 70,
              ranksep: 50,
              rankdir: "LR",
              marginx: 20,
              marginy: 20
            });
            var tasks = JSON.parse(data);
            for (var id in tasks) {
              var task = tasks[id];
              var className = '';
              if (task.status === 'WAITING')
                className += 'waiting';
              else if (task.status === "READY")
                className += 'ready ';
              else if (task.status === "ONGOING")
                className += 'ongoing blinking';
              else if (task.status === "DONE")
                className += 'done';
              else if (task.status === "FAILED")
                className += 'failed';

              var html = '<div>';
              html += '<span class="status"></span>';
              html += '<span class="name">' + task.name + '</span>';
              html += '<br>';
              html += '<span class="name">' + task.machine + '</span>';
              html += '</div>';

              g.setNode(task.id, {
                labelType: "html",
                label: html,
                rx: 5,
                ry: 5,
                width: 100,
                height: 25,
                padding: 0,
                class: className
              });

              if (task.preds) {
                for (var e in task.preds) {
                  g.setEdge(task.preds[e], task.id, {
                    label: "",
                    width: 40
                  });
                }
              }
            }
//            var elm = $compile(g)(scope);
            inner.call(render, g);
            var gs = d3.selectAll("svg g g g g g g ");
            for (var i in gs[0]) {
              var gi = gs[0][i];
              gi.setAttribute("transform", "translate(-50,-12.5)");
              var fo = gi.getElementsByTagName("foreignObject");
              fo[0].setAttribute("width", "100");
              fo[0].setAttribute("height", "25");
            }
          };
        }
      };
    }]);

