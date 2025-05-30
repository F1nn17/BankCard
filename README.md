# Bank Card System

## 📝 Описание
Bank Card System - это REST API для управления банковскими картами с использованием **Java**, **Spring Boot** и **PostgreSQL**.  
Поддерживает ролевую модель, JWT-аутентификацию.

---

## ⚙️ Стек технологий
- **Язык**: Java 17
- **Фреймворк**: Spring Boot 3.4.1
- **База данных**: PostgreSQL
- **Docker** и **Docker Compose**
- **JWT** для аутентификации
- **Swagger UI** для документирования API

---

## **Требования для локального запуска**
- **Docker и Docker Compose** установлены на вашей системе.
- **Java 17** установлена для разработки (если запускать без Docker).
- **Maven** установлен для сборки (если запускать без Docker).

---

## **Сборка и запуск Docker контейнеров**
Сначала убедитесь, что Docker запущен на вашей машине.
### Выполните команду для сборки и запуска контейнеров:
### **"docker-compose up --build"**
После успешного запуска:
- Приложение будет доступно по адресу: **http://localhost:8080.**
- Swagger-документация API будет доступна по адресу: **http://localhost:8080/swagger-ui/index.html.**

---

## **Полезные команды Docker**
- Запуск контейнеров:
  **docker-compose up -d**
- Остановка контейнеров:
  **docker-compose down**
- Просмотр логов:
 **docker-compose logs -f**
- Пересборка образов и перезапуск:
 **docker-compose up --build**
