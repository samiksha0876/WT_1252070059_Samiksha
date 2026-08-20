# VIT Semester Result Management System
Spring Boot + MySQL + responsive HTML/CSS/JS frontend.

Computes each subject's total as **MSE (30%) + ESE (70%)** for 4 subjects, then the
overall percentage, grade and PASS/FAIL status.

---

## 1. Install prerequisites

### 1.1 Java Development Kit (JDK 17)
1. Go to https://adoptium.net/ (Eclipse Temurin) or https://www.oracle.com/in/java/technologies/downloads/
2. Download **JDK 17 (LTS)** for your OS (Windows/macOS/Linux).
3. Run the installer, accept defaults.
4. Verify in a terminal / command prompt:
   ```
   java -version
   ```
   It should print version 17.x.

### 1.2 Spring Tool Suite (STS)
1. Go to https://spring.io/tools
2. Click **Download STS4** and pick your OS (Windows/Mac/Linux).
3. Windows: unzip the downloaded archive to a folder, e.g. `C:\sts-4`. There's no
   installer — you just extract it.
   macOS: open the `.dmg` and drag STS to Applications.
4. Launch `SpringToolSuite4.exe` (Windows) or the app (macOS). Pick a workspace
   folder when prompted (e.g. `C:\Users\<you>\sts-workspace`).

### 1.3 MySQL Server + MySQL Workbench
1. Go to https://dev.mysql.com/downloads/installer/ (Windows) or use Homebrew on
   macOS: `brew install mysql`.
2. Run the **MySQL Installer**, choose "Server only" or "Full", and install
   **MySQL Server 8.x** plus **MySQL Workbench** (GUI tool).
3. During setup you'll be asked to set a **root password** — remember it, you'll
   need it in `application.properties`.
4. Start the MySQL service (the installer usually starts it automatically; on
   Windows check via Services.msc that "MySQL80" is running).
5. Open **MySQL Workbench**, connect using `root` and your password, to confirm
   the server is reachable. You do not need to manually create the database —
   the app creates it automatically (see `createDatabaseIfNotExist=true` in
   `application.properties`).

### 1.4 Maven
STS ships with an embedded Maven, so a separate install is optional. If you
want a standalone one: https://maven.apache.org/download.cgi and add its `bin`
folder to your PATH, then check `mvn -v`.

---

## 2. Get the project into STS

1. Unzip the project you downloaded (`result-management.zip`) to a folder,
   e.g. `C:\projects\result-management`.
2. Open STS → **File → Import…**
3. Choose **Maven → Existing Maven Projects → Next**.
4. Click **Browse**, select the unzipped `result-management` folder, tick the
   `pom.xml` that appears, click **Finish**.
5. STS will download the dependencies listed in `pom.xml` (needs internet
   access) — watch the progress bar bottom-right until it finishes.

---

## 3. Configure the database connection

Open `src/main/resources/application.properties` in STS and edit:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vit_result_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

Replace `YOUR_MYSQL_PASSWORD` with the root password you set during MySQL
installation. `vit_result_db` will be auto-created on first run;
`spring.jpa.hibernate.ddl-auto=update` auto-creates the `students`, `subjects`,
and `marks` tables from the JPA entities, and `data.sql` seeds 4 subjects.

---

## 4. Run the application

**In STS:**
1. Right-click the project in the *Package/Project Explorer* → **Run As → Spring Boot App**.
2. Watch the *Console* tab — it should end with `Tomcat started on port 8080` and
   no red stack traces.

**Or from a terminal** (inside the project folder):
```
./mvnw spring-boot:run        # macOS/Linux
mvnw.cmd spring-boot:run      # Windows
```
(If you don't have the wrapper jar, use `mvn spring-boot:run` with a standalone Maven install.)

If it fails to connect to MySQL, double-check the server is running and the
username/password in `application.properties` are correct.

---

## 5. Use the app

1. Open a browser at **http://localhost:8080**
2. **Tab 1 – Register Student**: enter name, PRN, branch, semester → Save.
3. **Tab 2 – Enter Marks**: pick the student and each of the 4 seeded subjects
   (Data Structures, DBMS, Operating Systems, Computer Networks) one at a
   time, enter MSE (out of 30) and ESE (out of 70) → Save, repeat for all 4.
4. **Tab 3 – View Result**: pick the student → Generate. You'll see a
   subject-wise mark sheet plus total marks, percentage, grade and PASS/FAIL.

The page is responsive — resize the browser or open it on a phone to see the
layout adapt (stacked form fields, scrollable tables).

---

## 6. Verify the API directly (optional)

You can test endpoints with `curl`, Postman, or STS's own **Boot Dashboard**:

```
GET  http://localhost:8080/api/students
GET  http://localhost:8080/api/subjects
POST http://localhost:8080/api/students     { "name":"...", "prn":"...", "branch":"...", "semester":3 }
POST http://localhost:8080/api/marks        { "studentId":1, "subjectId":1, "mseMarks":25, "eseMarks":60 }
GET  http://localhost:8080/api/result/1
```

---

## 7. Grading logic used

| Percentage | Grade |
|---|---|
| ≥ 75 | A |
| ≥ 65 | B |
| ≥ 55 | C |
| ≥ 50 | D |
| ≥ 40 | E |
| < 40 | F |

A student is marked **FAIL** overall if any single subject total falls below
40/100, even if the aggregate percentage clears 40%.

---

## 8. Project structure

```
result-management/
├── pom.xml
├── src/main/java/com/vit/result/
│   ├── ResultManagementApplication.java
│   ├── entity/        Student.java, Subject.java, Marks.java
│   ├── repository/    StudentRepository.java, SubjectRepository.java, MarksRepository.java
│   ├── dto/           MarksRequest.java, SubjectResultDTO.java, ResultResponse.java
│   ├── service/        ResultService.java   (MSE 30% + ESE 70% calculation)
│   └── controller/    StudentController.java, ResultController.java
└── src/main/resources/
    ├── application.properties
    ├── data.sql        (seeds 4 subjects)
    └── static/         index.html, css/style.css, js/app.js
```
