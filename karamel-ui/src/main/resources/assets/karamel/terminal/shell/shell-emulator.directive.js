angular.module('main.module')
  .directive('shellEmulator', function($document) {
    return {
      restrict: 'E',
      controller: 'shell-emulator.contoller',
      transclude: true,
      replace: true,
      template: "<section class='terminal' ng-paste='handlePaste($event)'>\n\
                   <div class='terminal-viewport'>\n\
                     <div class='terminal-results'>\n\</div>\n\
                     <span class='terminal-prompt' ng-show='showPrompt'>{{prompt.text}}</span>\n\
                     <span class='terminal-input'>{{commandLine}}</span>\n\
                     <span class='terminal-cursor'>_</span>\n\
                     <input type='text' ng-model='commandLine' class='terminal-target'/>\n\
                  </div>\n\
                  <div ng-transclude></div>\n\
                </section>",
      compile: function compile(tElement, tAttrs, transclude) {
        return {
          pre: function preLink(scope, element, attrs, controller) {

          },
          post: function postLink(scope, element, attrs, controller) {

            var terminal = element;
            var target = angular.element(element[0].querySelector('.terminal-target'));
            var consoleView = angular.element(element[0].querySelector('.terminal-viewport'));
            var results = angular.element(element[0].querySelector('.terminal-results'));
            var prompt = angular.element(element[0].querySelector('.terminal-prompt'));
            var cursor = angular.element(element[0].querySelector('.terminal-cursor'));
            var consoleInput = angular.element(element[0].querySelector('.terminal-input'));

            if (navigator.appVersion.indexOf("Trident") != -1) {
              terminal.addClass('damn-ie');
            }

            var css = attrs['terminalClass'];
            if (css) {
              terminal.addClass(css);
            }

            setInterval(function() {
              var focused = $document[0].activeElement == target[0];
              if (focused) {
                cursor.toggleClass('terminal-cursor-hidden');
              }
              else if (!target.hasClass('terminal-cursor-hidden'))
                cursor.addClass('terminal-cursor-hidden');
            }, 500);

            var mouseover = false;
            element.on('mouseover', function() {
              mouseover = true;
            });
            element.on('mouseleave', function() {
              mouseover = false;
            });

            consoleView.on('click', function() {
              target[0].focus();
              terminal.toggleClass('terminal-focused', true);
            });

            target.on("blur", function(e) {
              if (!mouseover)
                terminal.toggleClass('terminal-focused', false);
            });

            target.on("keypress", function(e) {
              if (scope.showPrompt || scope.allowTypingWriteDisplaying)
                scope.keypress(e.which);
              e.preventDefault();
            });

            target.on("keyup", function(e) {
              //ctrl
              if (e.keyCode === 17) {
                e.preventDefault();
                scope.ctrlDown = false;
              }
            });

            target.on("keydown", function(e) {

              //ctrl
              if (e.keyCode === 17) {
                e.preventDefault();
                scope.ctrlDown = true;
              }
              //ctrl-c
              if (scope.ctrlDown && e.keyCode === 67) {
                e.preventDefault();
                scope.execute("\033[\003");
              }
              //ctrl-d
              if (scope.ctrlDown && e.keyCode === 68) {
                e.preventDefault();
                scope.execute("\033[\004");
              }
              //tab
              if (e.keyCode === 9) {
                if (scope.showPrompt || scope.allowTypingWriteDisplaying)
                  scope.execute(scope.commandLine + "\t\t");
                e.preventDefault();
              }
              //enter
              else if (e.keyCode === 13) {
                if (scope.showPrompt || scope.allowTypingWriteDisplaying)
                  scope.execute(scope.commandLine + "\r");
                e.preventDefault();
              }
              //up arrow
              else if (e.keyCode === 38) {
                if (scope.showPrompt || scope.allowTypingWriteDisplaying)
                  scope.execute("arrup");
                e.preventDefault();
              }
              //down arrow
              else if (e.keyCode === 40) {
                if (scope.showPrompt || scope.allowTypingWriteDisplaying)
                  scope.execute("arrdown")
                e.preventDefault();
              }
              //Client side commands

              //ctrl-u
              if (scope.ctrlDown && e.keyCode === 85) {
                e.preventDefault();
                scope.clearCommandLine();
              }
              //backspace
              if (e.keyCode === 8) {
                if (scope.showPrompt || scope.allowTypingWriteDisplaying)
                  scope.backspace();
                e.preventDefault();
              }

            });

            scope.$watchCollection(function() {
              return scope.machinesLines[scope.machine];
            }, function(newLines, oldLines) {

              if (scope.machineSwitched || !oldLines || (oldLines.length && !newLines.length)) { // removal detected
                var children = results.children();
                for (var i = 0; i < children.length; i++) {
                  children[i].remove();
                }
              }

              scope.showPrompt = false;
              var f = [function() {
                  scope.showPrompt = true;
                  scope.$$phase || scope.$apply();
                  consoleView[0].scrollTop = consoleView[0].scrollHeight;
                }];

              if (newLines) {
                for (var j = 0; j < newLines.length; j++) {

                  var newLine = newLines[j];
                  if (!scope.machineSwitched && newLine.displayed)
                    continue;

                  newLine.displayed = true;

                  var line = document.createElement('pre');
                  line.textContent = '';
                  line.className = 'terminal-line';
                  for (var i = 0; i < newLine.items.length; i++) {
                    var item = newLine.items[i];
                    var span = document.createElement('span');
                    span.innerHTML = item.data;
                    span.setAttribute("style", item.style);
                    line.appendChild(span);
                  }

                  results[0].appendChild(line)
                }
              }
              f[f.length - 1]();
            });

          }
        }
      }
    }
  });