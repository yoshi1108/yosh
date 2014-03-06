cd %~d0%~p0
java -Dgroovy.source.encoding=UTF-8 -Dfilse.encoding=UTF-8 -cp "./;./grails-app/services/;./src/;./lib/*;" groovy.ui.GroovyMain grails-app/services/uma/view/MainWindow.groovy