## `Статус сборки` [![Build status](https://ci.appveyor.com/api/projects/status/9ypd2a315qyhsrjq?svg=true)](https://ci.appveyor.com/project/Lognestix/aqa-exercise-2-4-1)
# Репортинг (AQA_Exercise_2.4-1)
## Домашнее задание по курсу "Автоматизированное тестирование"
## Тема: «2.4. BDD», задание №1: «Page Object's»
- Добавление доменных методов через Page Object's
### Предварительные требования
- На компьютере пользователя должна быть установлена:
	- Intellij IDEA
### Установка и запуск
1. Склонировать проект на свой компьютер
	- открыть терминал
	- ввести команду 
		```
		git clone https://github.com/Lognestix/AQA_Exercise_2.4-1
		```
1. Открыть склонированный проект в Intellij IDEA
1. В Intellij IDEA перейти во вкладку Terminal (Alt+F12) и запустить SUT командой
	```
	java -jar artifacts/app-ibank-build-for-testers.jar
	```
1. Запустить авто-тесты В Intellij IDEA во вкладке Terminal открыв еще одну сессию, ввести команду
	```
	./gradlew clean test
	```