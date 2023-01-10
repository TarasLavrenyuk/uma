# UMA - user management app

Simple rest service to store user data.

## Technologies

- Kotlin - programming language
- Spring - web framework
- Postgres - db
- Spring JPA - ORM tool
- Junit - testing framework
- mockk.io - mocking framework
- assertk - assertion framework
- gradle - building tool
- Docker - well, docker ¯\_(ツ)_/¯

## Documentation

Please go to /swagger-ui/index.html to check API documentation.

## How to run

### Pre-requirements

1. Make sure you have gradle 7.6 installed
2. Make sure you have java 17 installed
3. Make sure you have docker installed

### Steps

1. Pull project locally
2. Go to the project root directory and build it with `./gradlew build`. You should have __out__ folder now.
3. Now you can start the app with `docker compose up --build`
4. Enjoy :smile:

### Troubleshooting
If it doesn't work, try to run postgres db separately on port 5432, and then run the app from Intellij :upside_down_face: