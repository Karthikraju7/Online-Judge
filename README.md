# 🖥️ Online Coding Judge Platform

A **full-stack online coding judge** platform supporting **Python, Java, and C++**, designed for secure, scalable, and efficient code evaluation.  
Built with **Spring Boot, MySQL, React, Docker, and JWT authentication**.

**Main aim:** This platform simulates a pure interview environment. You won’t see topic names or any hints, and failed test cases are hidden. You have to think through the problem carefully and dry-run your code, just like in a real coding interview.

---

## 🚀 Features

- **Multi-language Support** – Execute and evaluate submissions in **Python, Java, and C++**.

- **Secure Authentication** – Login & registration flow with:  
  - Email verification  
  - Forgot password email flow  
  - JWT-based authentication

- **Code Evaluation Engine** – Modular execution system with:  
  - Structured test cases  
  - Secure verdicts (✅ Accepted, ❌ Wrong Answer, ⏳ TLE, 💾 MLE)

- **Admin Panel** – Manage problems with:  
  - Add/Edit problem functionality  
  - Hidden test case handling  
  - Custom evaluation logic for each language

- **Scalable Architecture** – Docker-based isolated execution environment

- **Submission Tracking** – Store and track all submissions with detailed verdicts

---

## 🛠️ Tech Stack

**Frontend:** React + Vite  
**Backend:** Spring Boot  
**Database:** MySQL  
**Containerization:** Docker  
**Authentication:** JWT  
**Languages Supported:** Python, Java, C++

---

## 📦 Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-username/online-judge.git
   cd online-judge

2. **Backend Setup**

- Install Java 17+ and Maven
- Update `application.properties` with your DB credentials
- Run:

  ```bash
  mvn spring-boot:run

3. **Frontend Setup**

- Install Node.js and npm
- Navigate to client folder:

  ```bash
  cd client
  npm install
  npm run dev

## 📷 Screenshots

You can view all screenshots in the [OJ_ScreenShots folder](OJ_ScreenShots/) here.




