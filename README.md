# Практическая работа №6

Вариант 3. Маршрутизация служебных заявок.

## Сборка и запуск

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr03.SelfCheck
```

Windows (PowerShell):

```powershell
mkdir out -Force
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
java -cp out ru.edu.pr03.SelfCheck
```