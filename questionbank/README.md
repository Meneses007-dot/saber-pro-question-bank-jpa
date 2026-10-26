# Banco de Preguntas Saber Pro — Taller 10 (JPA)

Aplicación Spring Boot que implementa un Banco de Preguntas para simulacros Saber Pro,
manteniendo el modelo de dominio libre de anotaciones JPA y separando la persistencia
en una capa de infraestructura.

## Propósito

El proyecto resuelve la Parte II del taller:

- Modelo de dominio del banco de preguntas (usuarios, competencias, temas, preguntas,
  opciones, metadatos y cuestionarios).
- Las cuatro relaciones JPA obligatorias: `@OneToOne`, `@OneToMany`, `@ManyToOne` y
  `@ManyToMany`.
- Herencia de preguntas con la estrategia `JOINED`.
- Ciclo de vida de la pregunta validado en el dominio.
- Contratos de repositorio independientes de la tecnología, implementados con Spring
  Data JPA en infraestructura.
- Casos de uso expuestos por REST, con pruebas unitarias e integración sobre H2.

## Arquitectura y reglas de dependencia

```text
src/main/java/com/example/questionbank/
├── api/              QuestionController, QuizController, DTOs, GlobalExceptionHandler
├── application/      QuestionService, ReviewService, QuizService, comandos
├── domain/
│   ├── model/        User, Role, Competency, Topic, Question (abstracta),
│   │                 MultipleChoiceQuestion, TrueFalseQuestion, AnswerOption,
│   │                 QuestionMetadata, Quiz, QuestionStatus, Difficulty, QuestionType
│   ├── exception/    DomainException, InvalidStateTransitionException, ResourceNotFoundException
│   └── repository/   QuestionRepository, UserRepository, CompetencyRepository,
│                     TopicRepository, QuizRepository
└── infrastructure/
    ├── jpa/record/       *Record (aquí y solo aquí vive jakarta.persistence)
    ├── jpa/repository/   Spring*DataRepository (Spring Data JPA)
    ├── jpa/mapper/       UserMapper, TopicMapper, CompetencyMapper, QuestionMapper, QuizMapper
    ├── jpa/              *RepositoryJpa (implementan los contratos del dominio)
    └── seed/             DataSeeder (catálogo de ejemplo, se puede desactivar)
```

Reglas que se cumplen y se verifican con `grep`:

1. `domain/` no importa `jakarta.persistence` ni `org.springframework` y no tiene
   anotaciones JPA.
2. `application/` y `api/` no importan `JpaRepository`, `Spring*DataRepository` ni
   clases de `infrastructure`.
3. Los mapeadores son el único punto de traducción entre el dominio y los records JPA.
4. La arquitectura es **en capas** con inversión de dependencias en el repositorio; no
   se usan puertos ni adaptadores hexagonales.

## Cómo ejecutar

Requisitos: JDK 17+ y Maven 3.8+.

```bash
# Compilar y ejecutar todas las pruebas (55 pruebas)
mvn clean test

# Empaquetar y ejecutar la aplicación
mvn spring-boot:run
```

La API queda en `http://localhost:8080`.

### Consola H2

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:questionbankdb`
- Usuario: `sa`
- Contraseña: vacía

La base es en memoria y se recrea en cada arranque (`ddl-auto=create-drop`).
`open-in-view=false`, por lo que los mapeos de colecciones se hacen dentro de las
transacciones de los servicios.

### Datos de ejemplo

Al arrancar, `DataSeeder` carga (si la tabla de usuarios está vacía):

| id | Usuario | Rol | Email |
|---|---|---|---|
| 1 | Ana Administrator | ADMIN | admin@questionbank.edu |
| 2 | Arturo Author | AUTHOR | author@questionbank.edu |
| 3 | Rita Reviewer | REVIEWER | reviewer@questionbank.edu |
| 4 | Tomas Teacher | TEACHER | teacher@questionbank.edu |
| 5 | Sara Student | STUDENT | student@questionbank.edu |

| id | Tema | | id | Competencia |
|---|---|---|---|---|
| 1 | Software Architecture | | 1 | Solution Design |
| 2 | Databases | | 2 | Problem Analysis |
| | | | 3 | Persistence |

Se puede desactivar con `questionbank.seed.enabled=false`.

## Casos de uso con `curl`

Las instrucciones usan los ids del catálogo de ejemplo. El orden reproduce el flujo
completo: crear, consultar, clasificar, enviar a revisión, publicar y crear el
cuestionario.

### 1. Crear una pregunta (selección múltiple)

```bash
curl -s -X POST http://localhost:8080/api/questions \
  -H "Content-Type: application/json" \
  -d '{
    "statement": "Which JPA inheritance strategy stores common columns in a base table?",
    "authorId": 2, "topicId": 1, "competencyIds": [1, 2],
    "type": "MULTIPLE_CHOICE", "difficulty": "MEDIUM",
    "options": [
      {"text": "JOINED", "correct": true},
      {"text": "SINGLE_TABLE", "correct": false},
      {"text": "TABLE_PER_CLASS", "correct": false},
      {"text": "NONE", "correct": false},
      {"text": "ALL", "correct": false}
    ],
    "metadata": {
      "lastReviewedAt": "2026-10-01", "cognitiveLevel": "ANALYZE",
      "estimatedMinutes": 3, "editorialNotes": "Draft for the quiz"
    }
  }'
```

Respuesta `201 Created` con la pregunta en estado `DRAFT` (id 1).

### 2. Consultar preguntas

```bash
# Por identificador
curl -s http://localhost:8080/api/questions/1

# Por autor
curl -s "http://localhost:8080/api/questions?authorId=2"

# Por estado
curl -s "http://localhost:8080/api/questions?status=DRAFT"

# Publicadas que evalúan una competencia (solo devuelve PUBLISHED)
curl -s "http://localhost:8080/api/questions?competencyId=2"
```

### 3. Clasificar una pregunta

```bash
curl -s -X PUT http://localhost:8080/api/questions/1/classification \
  -H "Content-Type: application/json" \
  -d '{"topicId": 2, "competencyIds": [2, 3]}'
```

### 4. Enviar a revisión (y llevarla hasta `PUBLISHED`)

```bash
curl -s -X POST http://localhost:8080/api/questions/1/submit
curl -s -X POST http://localhost:8080/api/questions/1/review/start
curl -s -X POST http://localhost:8080/api/questions/1/review/approve
curl -s -X POST http://localhost:8080/api/questions/1/publish
```

`submit` valida enunciado, dificultad, autor, tema, al menos una competencia y las
reglas del tipo (5 opciones con una sola correcta, o valor de verdad definido). Si la
pregunta no cumple, responde `422`; si el cambio de estado es ilegal, `409`.

### 5. Crear un cuestionario

```bash
curl -s -X POST http://localhost:8080/api/quizzes \
  -H "Content-Type: application/json" \
  -d '{"title": "Saber Pro simulation 1", "createdById": 4, "questionIds": [1]}'
```

Respuesta `201 Created`. El servicio carga todas las preguntas y valida que estén
`PUBLISHED` **antes** de guardar: si una no lo está, responde `422` y no persiste nada.

Ejemplo de rechazo (con una pregunta en `DRAFT`):

```bash
curl -s -X POST http://localhost:8080/api/quizzes \
  -H "Content-Type: application/json" \
  -d '{"title": "Invalid simulation", "createdById": 4, "questionIds": [2]}'
# 422 Unprocessable Entity:
# "Question 2 is in status DRAFT; only PUBLISHED questions can be added to a quiz"
```

## Endpoints

| Método | Ruta | Caso de uso |
|---|---|---|
| `POST` | `/api/questions` | Crear pregunta |
| `GET` | `/api/questions/{id}` | Consultar por id |
| `GET` | `/api/questions?authorId=&status=&competencyId=` | Listar con criterio |
| `PUT` | `/api/questions/{id}/classification` | Clasificar |
| `POST` | `/api/questions/{id}/submit` | Enviar a revisión |
| `POST` | `/api/questions/{id}/review/start` | Iniciar revisión |
| `POST` | `/api/questions/{id}/review/approve` | Aprobar |
| `POST` | `/api/questions/{id}/review/reject` | Rechazar (vuelve a `DRAFT`) |
| `POST` | `/api/questions/{id}/publish` | Publicar |
| `POST` | `/api/questions/{id}/archive` | Archivar |
| `POST` | `/api/quizzes` | Crear cuestionario |
| `GET` | `/api/quizzes/{id}` | Consultar cuestionario |
| `GET` | `/api/quizzes` | Listar cuestionarios |

Errores: `404` recurso inexistente, `409` transición de estado ilegal, `422` regla de
negocio violada, `400` entrada inválida. El cuerpo de error es uniforme
(`timestamp`, `status`, `error`, `message`, `path`).

## Pruebas

```bash
mvn clean test
```

Cubren:

- Ciclo de vida y reglas de contenido del dominio (pruebas puras).
- Persistencia y recuperación con `@DataJpaTest` sobre H2.
- Relaciones (opciones, competencias, tema, autor, metadatos) y herencia `JOINED`.
- Restricciones: FK de autor/tema, UNIQUE de metadatos, borrado en cascada de opciones.
- El mismo contrato de `QuestionRepository` contra el fake en memoria y contra JPA.
- Cuestionarios solo con preguntas `PUBLISHED`, con fakes y de extremo a extremo.
- Endpoints y códigos HTTP con `MockMvc`.

## Decisiones de alcance

- Se implementan `User`, `Competency`, `Topic`, `Question`, `MultipleChoiceQuestion`,
  `TrueFalseQuestion`, `AnswerOption`, `QuestionMetadata` y `Quiz`.
- Quedan fuera `UserProfile`, `QuestionTag`, `Review`/`ReviewCriterion`/`ReviewResult`,
  `QuestionHistory`, `QuizQuestion`, `Attempt`, `StudentAnswer` y `Result`. El flujo de
  revisión se modela como transiciones de estado y no como una entidad `Review`.
- El cuestionario solo admite preguntas en `PUBLISHED` (una pregunta publicada ya fue
  aprobada).
- La relación `Question`–`Competency` es `@ManyToMany` directa porque no necesita
  atributos adicionales. `QuestionCompetency` (con porcentaje) se evaluó y se descartó
  por alcance.
